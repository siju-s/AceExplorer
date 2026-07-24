/*
 * Copyright (C) 2026 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.smb

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ImpersonationLevel
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.PipeShare
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.EnumSet

/** Lists disk shares through the SRVSVC RPC interface on the authenticated SMB session. */
class SmbShareEnumerator {

    fun listDiskShares(session: Session): List<String> {
        val ipcShare = session.connectShare(IPC_SHARE) as? PipeShare
            ?: throw IOException("This server does not provide an IPC share")
        ipcShare.use { share ->
            share.open(
                SRVSVC_PIPE,
                SMB2ImpersonationLevel.Impersonation,
                EnumSet.of(AccessMask.GENERIC_READ, AccessMask.GENERIC_WRITE),
                EnumSet.noneOf(FileAttributes::class.java),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                EnumSet.noneOf(SMB2CreateOptions::class.java)
            ).use { pipe ->
                verifyBindAck(pipe.transact(bindRequest()))
                return parseShareEnumResponse(pipe.transact(shareEnumRequest()))
            }
        }
    }

    private fun bindRequest(): ByteArray {
        val body = LittleEndianWriter().apply {
            writeShort(MAX_FRAGMENT_SIZE)
            writeShort(MAX_FRAGMENT_SIZE)
            writeInt(0)
            writeByte(1)
            writeBytes(ByteArray(3))
            writeShort(0)
            writeByte(1)
            writeByte(0)
            writeUuid(SRVSVC_INTERFACE_UUID)
            writeShort(3)
            writeShort(0)
            writeUuid(NDR32_UUID)
            writeShort(2)
            writeShort(0)
        }.toByteArray()
        return rpcPdu(PDU_BIND, CALL_ID_BIND, body)
    }

    private fun shareEnumRequest(): ByteArray {
        val stub = LittleEndianWriter().apply {
            writeInt(0) // ServerName: null means this SMB server.
            writeInt(SHARE_INFO_LEVEL)
            writeInt(SHARE_INFO_LEVEL) // Encapsulated-union discriminator.
            writeInt(SHARE_ENUM_CONTAINER_REFERENT)
            // Deferred SHARE_INFO_0_CONTAINER input value.
            writeInt(0)
            writeInt(0)
            writeInt(MAX_PREFERRED_LENGTH)
            writeInt(0) // ResumeHandle: null; max length asks for all entries.
        }.toByteArray()
        val body = LittleEndianWriter().apply {
            writeInt(stub.size)
            writeShort(0)
            writeShort(NETR_SHARE_ENUM_OPNUM)
            writeBytes(stub)
        }.toByteArray()
        return rpcPdu(PDU_REQUEST, CALL_ID_ENUM, body)
    }

    private fun rpcPdu(type: Int, callId: Int, body: ByteArray): ByteArray = LittleEndianWriter().apply {
        writeByte(5)
        writeByte(0)
        writeByte(type)
        writeByte(PFC_FIRST_AND_LAST)
        writeBytes(byteArrayOf(0x10, 0, 0, 0))
        writeShort(RPC_HEADER_SIZE + body.size)
        writeShort(0)
        writeInt(callId)
        writeBytes(body)
    }.toByteArray()

    private fun verifyBindAck(response: ByteArray) {
        val reader = rpcReader(response)
        if (reader.readUnsignedByte() != 5 || reader.readUnsignedByte() != 0) {
            throw IOException("Invalid response while preparing LAN share discovery")
        }
        when (reader.readUnsignedByte()) {
            PDU_BIND_ACK -> return
            PDU_FAULT -> throw IOException("This server refused LAN share discovery")
            else -> throw IOException("This server does not support LAN share discovery")
        }
    }

    private fun parseShareEnumResponse(response: ByteArray): List<String> {
        val reader = rpcReader(response)
        if (reader.readUnsignedByte() != 5 || reader.readUnsignedByte() != 0) {
            throw IOException("Invalid response while listing shared folders")
        }
        val type = reader.readUnsignedByte()
        if (type == PDU_FAULT) throw IOException("This server refused listing shared folders")
        if (type != PDU_RESPONSE) throw IOException("Unexpected response while listing shared folders")
        reader.skip(1 + 4 + 2 + 2 + 4) // Flags, data representation, fragment/auth lengths, call ID.
        reader.skip(4 + 2 + 1 + 1) // Allocation hint, context ID, cancel count and reserved byte.

        val level = reader.readInt()
        val unionLevel = reader.readInt()
        val containerReferent = reader.readInt()
        if (level != SHARE_INFO_LEVEL || unionLevel != SHARE_INFO_LEVEL) {
            throw IOException("This server returned unsupported shared-folder data")
        }

        val entriesRead: Int
        val entriesReferent: Int
        if (containerReferent != 0) {
            entriesRead = reader.readInt()
            entriesReferent = reader.readInt()
        } else {
            entriesRead = 0
            entriesReferent = 0
        }
        if (entriesReferent == 0 || entriesRead == 0) {
            verifyNetApiStatus(reader)
            return emptyList()
        }

        val maxCount = reader.readInt()
        if (entriesRead < 0 || entriesRead > maxCount || entriesRead > MAX_SHARES) {
            throw IOException("Invalid shared-folder list from server")
        }
        val nameReferents = List(entriesRead) { reader.readInt() }
        val names = nameReferents.mapNotNull { referent ->
            if (referent == 0) null else reader.readUnicodeString()
        }
        verifyNetApiStatus(reader)
        return names
            .asSequence()
            .filter { it.isNotBlank() && !it.endsWith('$') && !it.equals(IPC_SHARE, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
            .toList()
    }

    private fun verifyNetApiStatus(reader: LittleEndianReader) {
        reader.readInt() // TotalEntries.
        reader.readInt() // ResumeHandle.
        val status = reader.readInt()
        if (status != 0 && status != ERROR_MORE_DATA) {
            throw IOException("Server could not list shared folders (error $status)")
        }
    }

    private fun rpcReader(response: ByteArray): LittleEndianReader {
        if (response.size < RPC_HEADER_SIZE) throw IOException("Incomplete response from server")
        return LittleEndianReader(response)
    }

    private companion object {
        const val IPC_SHARE = "IPC$"
        const val SRVSVC_PIPE = "srvsvc"
        const val MAX_FRAGMENT_SIZE = 4280
        const val RPC_HEADER_SIZE = 16
        const val PFC_FIRST_AND_LAST = 0x03
        const val PDU_REQUEST = 0
        const val PDU_RESPONSE = 2
        const val PDU_FAULT = 3
        const val PDU_BIND = 11
        const val PDU_BIND_ACK = 12
        const val CALL_ID_BIND = 1
        const val CALL_ID_ENUM = 2
        const val NETR_SHARE_ENUM_OPNUM = 15
        const val SHARE_INFO_LEVEL = 0
        const val MAX_PREFERRED_LENGTH = -1
        const val ERROR_MORE_DATA = 234
        const val MAX_SHARES = 1024
        const val SHARE_ENUM_CONTAINER_REFERENT = 0x00020004
        const val SRVSVC_INTERFACE_UUID = "4b324fc8-1670-01d3-1278-5a47bf6ee188"
        const val NDR32_UUID = "8a885d04-1ceb-11c9-9fe8-08002b104860"
    }
}

private class LittleEndianWriter {
    private val output = ByteArrayOutputStream()

    fun writeByte(value: Int) {
        output.write(value)
    }

    fun writeShort(value: Int) {
        writeByte(value)
        writeByte(value ushr 8)
    }

    fun writeInt(value: Int) {
        writeByte(value)
        writeByte(value ushr 8)
        writeByte(value ushr 16)
        writeByte(value ushr 24)
    }

    fun writeBytes(value: ByteArray) {
        output.write(value)
    }

    fun writeUuid(uuid: String) {
        val parts = uuid.split('-')
        writeInt(parts[0].toLong(16).toInt())
        writeShort(parts[1].toInt(16))
        writeShort(parts[2].toInt(16))
        writeBytes(parts[3].chunked(2).map { it.toInt(16).toByte() }.toByteArray())
        writeBytes(parts[4].chunked(2).map { it.toInt(16).toByte() }.toByteArray())
    }

    fun toByteArray(): ByteArray = output.toByteArray()
}

private class LittleEndianReader(private val data: ByteArray) {
    private var position = 0

    fun readUnsignedByte(): Int = require(1).let { data[position++].toInt() and 0xff }

    fun readInt(): Int =
        readUnsignedByte() or
            (readUnsignedByte() shl 8) or
            (readUnsignedByte() shl 16) or
            (readUnsignedByte() shl 24)

    fun readUnicodeString(): String {
        readInt() // Maximum count.
        readInt() // Offset.
        val count = readInt()
        if (count !in 0..MAX_STRING_CHARS) throw IOException("Invalid shared-folder name from server")
        val bytes = ByteArray(count * 2) { readUnsignedByte().toByte() }
        alignToFourBytes()
        return String(bytes, StandardCharsets.UTF_16LE).trimEnd('\u0000')
    }

    fun skip(length: Int) {
        require(length)
        position += length
    }

    private fun alignToFourBytes() {
        val padding = (4 - position % 4) % 4
        skip(padding)
    }

    private fun require(length: Int) {
        if (length < 0 || position + length > data.size) {
            throw IOException("Incomplete shared-folder data from server")
        }
    }

    private companion object {
        const val MAX_STRING_CHARS = 4096
    }
}
