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

import android.app.Application
import android.net.Uri
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import androidx.documentfile.provider.DocumentFile
import java.security.MessageDigest
import java.util.EnumSet
import javax.inject.Inject

data class SmbEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedAt: Long,
    val thumbnailPath: String? = null
)

data class SmbOpenedFile(
    val file: File,
    val imageFiles: List<File> = emptyList(),
    val imageIndex: Int = 0
)

enum class SmbTransferDirection { UPLOAD, DOWNLOAD }

data class SmbTransferState(
    val direction: SmbTransferDirection,
    val name: String,
    val copiedBytes: Long,
    val totalBytes: Long
) {
    val percent: Int get() = if (totalBytes > 0) ((copiedBytes * 100) / totalBytes).toInt() else 0
}

data class SmbUiState(
    val loading: Boolean = false,
    val connected: Boolean = false,
    val connectionType: SmbConnectionType? = null,
    val host: String = "",
    val username: String = "",
    val shareName: String = "",
    val path: String = "",
    val entries: List<SmbEntry> = emptyList(),
    val savedServers: List<SmbSavedServer> = emptyList(),
    val nearbyServers: List<SmbNearbyServer> = emptyList(),
    val discoveringServers: Boolean = false,
    val hasScannedServers: Boolean = false,
    val lanShares: List<String> = emptyList(),
    val transfer: SmbTransferState? = null,
    val error: String? = null
)

@HiltViewModel
class SmbBrowserViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val serverStore = SmbServerStore(application)
    private val _state = MutableStateFlow(SmbUiState(savedServers = serverStore.load()))
    val state: StateFlow<SmbUiState> = _state.asStateFlow()

    private val _openedFile = MutableLiveData<SmbOpenedFile?>()
    val openedFile: LiveData<SmbOpenedFile?> = _openedFile

    private var client: SMBClient? = null
    private var connection: Connection? = null
    private var session: Session? = null
    private var diskShare: DiskShare? = null
    private var thumbnailJob: Job? = null
    private var openFileJob: Job? = null
    private var disconnectJob: Job? = null
    private var transferJob: Job? = null
    private val shareEnumerator = SmbShareEnumerator()

    fun connect(
        host: String,
        shareName: String,
        username: String,
        password: String,
        rememberPassword: Boolean,
        savedServer: SmbSavedServer?
    ) {
        cancelScheduledDisconnect()
        openFileJob?.cancel()
        _state.update {
            it.copy(
                loading = true,
                connected = false,
                error = null,
                entries = emptyList(),
                lanShares = emptyList()
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            closeConnection()
            val attemptedSavedPassword = password.isEmpty() &&
                savedServer?.encryptedPassword?.isNotEmpty() == true
            try {
                val resolvedPassword = password.ifEmpty {
                    savedServer?.let(serverStore::passwordFor).orEmpty()
                }
                val newClient = SMBClient()
                val newConnection = newClient.connect(host.trim())
                val newSession = newConnection.authenticate(
                    AuthenticationContext(username.trim(), resolvedPassword.toCharArray(), "")
                )
                val newShare = newSession.connectShare(shareName.trim()) as? DiskShare
                    ?: throw IOException("The selected share is not a file share")

                client = newClient
                connection = newConnection
                session = newSession
                diskShare = newShare
                serverStore.save(
                    SmbSavedServer(
                        host = host.trim(),
                        shareName = shareName.trim(),
                        username = username.trim(),
                        connectionType = SmbConnectionType.MANUAL_SMB
                    ),
                    password = resolvedPassword,
                    rememberPassword = rememberPassword
                )
                _state.update {
                    it.copy(
                        connected = true,
                        connectionType = SmbConnectionType.MANUAL_SMB,
                        host = host.trim(),
                        username = username.trim(),
                        shareName = shareName.trim(),
                        path = "",
                        savedServers = serverStore.load(),
                        error = null
                    )
                }
                loadDirectory("")
            } catch (error: Exception) {
                closeConnection()
                _state.update {
                    it.copy(
                        loading = false,
                        connected = false,
                        host = "",
                        shareName = "",
                        path = "",
                        entries = emptyList(),
                        error = if (attemptedSavedPassword && error.isAuthenticationFailure()) {
                            "Saved password may be outdated. Enter a new password or clear credentials."
                        } else {
                            error.userMessage()
                        }
                    )
                }
            }
        }
    }

    fun connectLan(
        host: String,
        username: String,
        password: String,
        rememberPassword: Boolean,
        savedServer: SmbSavedServer?
    ) {
        cancelScheduledDisconnect()
        openFileJob?.cancel()
        _state.update {
            it.copy(
                loading = true,
                connected = false,
                error = null,
                entries = emptyList(),
                lanShares = emptyList()
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            closeConnection()
            val attemptedSavedPassword = password.isEmpty() &&
                savedServer?.encryptedPassword?.isNotEmpty() == true
            try {
                val resolvedPassword = password.ifEmpty {
                    savedServer?.let(serverStore::passwordFor).orEmpty()
                }
                val newClient = SMBClient()
                val newConnection = newClient.connect(host.trim())
                val newSession = newConnection.authenticate(
                    AuthenticationContext(username.trim(), resolvedPassword.toCharArray(), "")
                )
                client = newClient
                connection = newConnection
                session = newSession
                val lanServer = SmbSavedServer(
                    host = host.trim(),
                    username = username.trim(),
                    connectionType = SmbConnectionType.LAN
                )
                serverStore.save(lanServer, resolvedPassword, rememberPassword)
                val shares = shareEnumerator.listDiskShares(newSession)
                if (shares.isEmpty()) {
                    throw IOException("No folders are shared by this Mac account. Add a folder in macOS File Sharing.")
                }
                _state.update {
                    it.copy(
                        loading = false,
                        connected = true,
                        connectionType = SmbConnectionType.LAN,
                        host = host.trim(),
                        username = username.trim(),
                        shareName = "",
                        path = "",
                        entries = shares.map { it.toLanShareEntry() },
                        savedServers = serverStore.load(),
                        lanShares = shares,
                        error = null
                    )
                }
            } catch (error: Exception) {
                closeConnection()
                _state.update {
                    it.copy(
                        loading = false,
                        savedServers = serverStore.load(),
                        error = if (attemptedSavedPassword && error.isAuthenticationFailure()) {
                            "Saved password may be outdated. Enter a new password or clear credentials."
                        } else {
                            error.userMessage()
                        }
                    )
                }
            }
        }
    }

    fun openLanShare(shareName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true, error = null, entries = emptyList()) }
            try {
                runCatching { diskShare?.close() }
                diskShare = null
                val newShare = requireSession().connectShare(shareName) as? DiskShare
                    ?: throw IOException("The selected shared folder is not a file share")
                diskShare = newShare
                _state.update {
                    it.copy(
                        connected = true,
                        shareName = shareName,
                        path = "",
                        error = null
                    )
                }
                loadDirectory("")
            } catch (error: Exception) {
                _state.update { it.copy(loading = false, error = error.userMessage()) }
            }
        }
    }

    fun clearCredentials(server: SmbSavedServer) {
        serverStore.clearCredentials(server)
        _state.update { it.copy(savedServers = serverStore.load()) }
    }

    fun hasUsableSavedPassword(server: SmbSavedServer): Boolean =
        !serverStore.passwordFor(server).isNullOrEmpty()

    fun connectSaved(server: SmbSavedServer) {
        if (server.connectionType == SmbConnectionType.LAN) {
            connectLan(server.host, server.username, "", true, server)
        } else {
            connect(server.host, server.shareName, server.username, "", true, server)
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            if (diskShare == null) return@launch
            _state.update { it.copy(loading = true, error = null) }
            loadDirectory(_state.value.path)
        }
    }

    fun disconnect() {
        cancelScheduledDisconnect()
        openFileJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            thumbnailJob?.cancel()
            closeConnection()
            _state.value = SmbUiState(savedServers = serverStore.load())
        }
    }

    fun showConnectionPicker() {
        cancelScheduledDisconnect()
        thumbnailJob?.cancel()
        openFileJob?.cancel()
        _openedFile.value = null
        val currentState = _state.value
        _state.value = SmbUiState(
            savedServers = serverStore.load(),
            nearbyServers = currentState.nearbyServers,
            discoveringServers = currentState.discoveringServers,
            hasScannedServers = currentState.hasScannedServers
        )
        viewModelScope.launch(Dispatchers.IO) { closeConnection() }
    }

    fun cancelScheduledDisconnect() {
        disconnectJob?.cancel()
        disconnectJob = null
    }

    fun scheduleDisconnect() {
        if (!_state.value.connected || _state.value.transfer != null || disconnectJob?.isActive == true) return
        disconnectJob = viewModelScope.launch {
            delay(DISCONNECT_TIMEOUT_MS)
            disconnect()
        }
    }

    fun discoverServers() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_state.value.discoveringServers) return@launch
            _state.update { it.copy(discoveringServers = true, hasScannedServers = true, error = null) }
            runCatching { SmbServerDiscovery.findNearbyServers(application) }
                .onSuccess { servers ->
                    _state.update {
                        it.copy(
                            discoveringServers = false,
                            nearbyServers = servers
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(discoveringServers = false, error = error.userMessage())
                    }
                }
        }
    }

    fun openDirectory(entry: SmbEntry) {
        if (_state.value.connectionType == SmbConnectionType.LAN && _state.value.shareName.isBlank()) {
            openLanShare(entry.name)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true, error = null) }
            loadDirectory(entry.path)
        }
    }

    fun navigateUp(): Boolean {
        val currentPath = _state.value.path
        if (currentPath.isBlank()) {
            if (
                _state.value.connectionType == SmbConnectionType.LAN &&
                _state.value.shareName.isNotBlank()
            ) {
                openLanRoot()
                return true
            }
            return false
        }
        val parentPath = currentPath.substringBeforeLast('\\', "")
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true, error = null) }
            loadDirectory(parentPath)
        }
        return true
    }

    fun openFile(entry: SmbEntry) {
        openFileJob?.cancel()
        openFileJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true, error = null) }
            try {
                if (entry.isImage()) {
                    val imageEntries = _state.value.entries
                        .filter { !it.isDirectory && it.isImage() }
                        .take(MAX_VIEWER_IMAGE_COUNT)
                    val imageFiles = imageEntries.map { imageEntry ->
                        val target = viewerCacheFile(imageEntry)
                        if (!target.exists() || target.length() != imageEntry.size) {
                            downloadRemoteFile(imageEntry, target)
                        }
                        target
                    }
                    val imageIndex = imageEntries.indexOfFirst { it.path == entry.path }
                    check(imageIndex >= 0) { "Selected image is no longer in this folder" }
                    _state.update { it.copy(loading = false) }
                    _openedFile.postValue(SmbOpenedFile(imageFiles[imageIndex], imageFiles, imageIndex))
                } else {
                    val target = File(remoteCacheDirectory(), cacheFileName(entry.name))
                    downloadRemoteFile(entry, target)
                    _state.update { it.copy(loading = false) }
                    _openedFile.postValue(SmbOpenedFile(target))
                }
            } catch (error: Exception) {
                _state.update { it.copy(loading = false, error = error.userMessage()) }
            }
        }
    }

    fun upload(uri: Uri, displayName: String, size: Long) {
        if (_state.value.shareName.isBlank()) return
        transferJob?.cancel()
        transferJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                openTransferShare().use { transferShare ->
                    val remotePath = remotePath(
                        _state.value.path,
                        uniqueRemoteName(transferShare, displayName)
                    )
                    application.contentResolver.openInputStream(uri)?.use { input ->
                        transferShare.openFile(
                            remotePath,
                            EnumSet.of(AccessMask.GENERIC_WRITE),
                            null,
                            SMB2ShareAccess.ALL,
                            SMB2CreateDisposition.FILE_CREATE,
                            null
                        ).use { remoteFile ->
                            remoteFile.outputStream.use { output ->
                                copyWithProgress(
                                    input,
                                    output,
                                    SmbTransferDirection.UPLOAD,
                                    displayName,
                                    size
                                )
                            }
                        }
                    } ?: throw IOException("Could not read selected file")
                }
                reopenCurrentShare()
                loadDirectory(_state.value.path)
                _state.update { it.copy(transfer = null) }
            } catch (error: Exception) {
                _state.update { it.copy(transfer = null, error = error.userMessage()) }
            }
        }
    }

    fun download(entry: SmbEntry, destinationTree: Uri) {
        if (entry.isDirectory) return
        transferJob?.cancel()
        transferJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val directory = DocumentFile.fromTreeUri(application, destinationTree)
                    ?: throw IOException("Selected folder is unavailable")
                val destination = directory.createFile(
                    application.contentResolver.getType(destinationTree) ?: "application/octet-stream",
                    uniqueDocumentName(directory, entry.name)
                ) ?: throw IOException("Could not create destination file")
                openTransferShare().use { transferShare ->
                    transferShare.openFile(
                        entry.path,
                        EnumSet.of(AccessMask.GENERIC_READ),
                        null,
                        SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_OPEN,
                        null
                    ).use { remoteFile ->
                        remoteFile.inputStream.use { input ->
                            application.contentResolver.openOutputStream(destination.uri)?.use { output ->
                                copyWithProgress(
                                    input,
                                    output,
                                    SmbTransferDirection.DOWNLOAD,
                                    entry.name,
                                    entry.size
                                )
                            } ?: throw IOException("Could not write destination file")
                        }
                    }
                }
                reopenCurrentShare()
                _state.update { it.copy(transfer = null) }
            } catch (error: Exception) {
                _state.update { it.copy(transfer = null, error = error.userMessage()) }
            }
        }
    }

    private fun copyWithProgress(
        input: java.io.InputStream,
        output: java.io.OutputStream,
        direction: SmbTransferDirection,
        name: String,
        totalBytes: Long
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var copied = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            output.write(buffer, 0, read)
            copied += read
            _state.update { it.copy(transfer = SmbTransferState(direction, name, copied, totalBytes)) }
        }
    }

    private fun uniqueRemoteName(share: DiskShare, name: String): String {
        val currentNames = share.list(_state.value.path).map { it.fileName }.toSet()
        return uniqueName(name) { it in currentNames }
    }

    private fun uniqueDocumentName(directory: DocumentFile, name: String): String =
        uniqueName(name) { candidate -> directory.findFile(candidate) != null }

    private fun uniqueName(name: String, exists: (String) -> Boolean): String {
        if (!exists(name)) return name
        val base = name.substringBeforeLast('.', name)
        val extension = name.substringAfterLast('.', "").takeIf { it != name }.orEmpty()
        var index = 1
        while (exists("$base ($index)${if (extension.isEmpty()) "" else ".$extension"}")) index++
        return "$base ($index)${if (extension.isEmpty()) "" else ".$extension"}"
    }

    private fun remotePath(parent: String, name: String): String =
        listOf(parent, name).filter { it.isNotBlank() }.joinToString("\\")

    fun consumeOpenedFile() {
        _openedFile.value = null
    }

    private fun loadDirectory(path: String) {
        thumbnailJob?.cancel()
        try {
            val entries = requireShare().list(path)
                .asSequence()
                .filter { it.fileName != "." && it.fileName != ".." }
                .map { it.toSmbEntry(path) }
                .sortedWith(
                    compareByDescending<SmbEntry> { it.isDirectory }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                )
                .toList()
            _state.update {
                it.copy(
                    loading = false,
                    connected = true,
                    path = path,
                    entries = entries,
                    error = null
                )
            }
            loadPreviews(path, entries)
        } catch (error: Exception) {
            _state.update { it.copy(loading = false, error = error.userMessage()) }
        }
    }

    private fun FileIdBothDirectoryInformation.toSmbEntry(parentPath: String): SmbEntry {
        val isDirectory = EnumWithValue.EnumUtils.isSet(
            fileAttributes,
            FileAttributes.FILE_ATTRIBUTE_DIRECTORY
        )
        return SmbEntry(
            name = fileName,
            path = listOf(parentPath, fileName).filter { it.isNotBlank() }.joinToString("\\"),
            isDirectory = isDirectory,
            size = endOfFile,
            modifiedAt = lastWriteTime.toDate().time
        )
    }

    private fun String.toLanShareEntry(): SmbEntry = SmbEntry(
        name = this,
        path = this,
        isDirectory = true,
        size = 0,
        modifiedAt = 0
    )

    private fun openLanRoot() {
        viewModelScope.launch(Dispatchers.IO) {
            thumbnailJob?.cancel()
            runCatching { diskShare?.close() }
            diskShare = null
            _state.update {
                it.copy(
                    loading = false,
                    shareName = "",
                    path = "",
                    entries = it.lanShares.map { share -> share.toLanShareEntry() },
                    error = null
                )
            }
        }
    }

    private fun loadPreviews(parentPath: String, entries: List<SmbEntry>) {
        thumbnailJob = viewModelScope.launch(Dispatchers.IO) {
            entries.filter { it.supportsPreview() && it.size in 1..MAX_PREVIEW_SOURCE_BYTES }.forEach { entry ->
                val thumbnail = thumbnailCacheFile(entry)
                if (!thumbnail.exists()) {
                    runCatching {
                        if (entry.isImage()) downloadRemoteFile(entry, thumbnail) else createVideoThumbnail(entry, thumbnail)
                    }
                        .onFailure { thumbnail.delete() }
                }
                if (thumbnail.exists() && _state.value.path == parentPath) {
                    _state.update { state ->
                        state.copy(entries = state.entries.map {
                            if (it.path == entry.path) it.copy(thumbnailPath = thumbnail.absolutePath) else it
                        })
                    }
                }
            }
        }
    }

    private fun downloadRemoteFile(entry: SmbEntry, target: File) {
        requireShare().openFile(
            entry.path,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
        ).use { remoteFile ->
            remoteFile.inputStream.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun createVideoThumbnail(entry: SmbEntry, target: File) {
        requireShare().openFile(
            entry.path,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
        ).use { remoteFile ->
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(object : MediaDataSource() {
                    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int =
                        runCatching { remoteFile.read(buffer, position, offset, size) }
                            .getOrDefault(-1)
                            .takeIf { it > 0 }
                            ?: -1

                    override fun getSize(): Long = entry.size

                    override fun close() = Unit
                })
                val frame = retriever.getFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: throw IOException("Could not read a video frame")
                try {
                    target.outputStream().use { output ->
                        check(frame.compress(android.graphics.Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output))
                    }
                } finally {
                    frame.recycle()
                }
            } finally {
                retriever.release()
            }
        }
    }

    private fun thumbnailCacheFile(entry: SmbEntry): File {
        val key = "${_state.value.host}|${_state.value.shareName}|${entry.path}"
            .toByteArray()
            .let { MessageDigest.getInstance("SHA-256").digest(it) }
            .joinToString("") { "%02x".format(it) }
        return File(remoteCacheDirectory(), "thumbnail-$key.jpg")
    }

    private fun viewerCacheFile(entry: SmbEntry): File {
        val key = "${_state.value.host}|${_state.value.shareName}|${entry.path}|${entry.modifiedAt}"
            .toByteArray()
            .let { MessageDigest.getInstance("SHA-256").digest(it) }
            .joinToString("") { "%02x".format(it) }
        val extension = entry.name.substringAfterLast('.', "jpg")
        return File(remoteCacheDirectory(), "viewer-$key.$extension")
    }

    private fun SmbEntry.isImage(): Boolean = name.substringAfterLast('.', "")
        .lowercase() in IMAGE_EXTENSIONS

    private fun SmbEntry.supportsPreview(): Boolean = isImage() || isVideo()

    private fun SmbEntry.isVideo(): Boolean = name.substringAfterLast('.', "")
        .lowercase() in VIDEO_EXTENSIONS

    private fun requireShare(): DiskShare = diskShare ?: throw IOException("Not connected to a shared folder")

    private fun openTransferShare(): DiskShare =
        requireSession().connectShare(_state.value.shareName) as? DiskShare
            ?: throw IOException("Selected share is not a file share")

    private fun reopenCurrentShare() {
        thumbnailJob?.cancel()
        runCatching { diskShare?.close() }
        diskShare = openTransferShare()
    }

    private fun requireSession(): Session = session ?: throw IOException("Connection to server was closed")

    private fun remoteCacheDirectory(): File = File(application.cacheDir, "smb").apply { mkdirs() }

    private fun cacheFileName(fileName: String): String {
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return "${System.currentTimeMillis()}-$safeName"
    }

    private fun closeConnection() {
        runCatching { diskShare?.close() }
        diskShare = null
        runCatching { session?.close() }
        session = null
        runCatching { connection?.close() }
        connection = null
        runCatching { client?.close() }
        client = null
    }

    override fun onCleared() {
        thumbnailJob?.cancel()
        openFileJob?.cancel()
        disconnectJob?.cancel()
        closeConnection()
        super.onCleared()
    }

    private fun Throwable.userMessage(): String {
        val detail = message.orEmpty()
        return when {
            detail.contains("connection refused", ignoreCase = true) ||
                detail.contains("failed to connect", ignoreCase = true) ->
                "SMB file sharing is unavailable. Turn on File Sharing on the server, then try again."
            detail.isNotBlank() -> detail
            else -> "Could not connect to the SMB server"
        }
    }

    private fun Throwable.isAuthenticationFailure(): Boolean =
        message.orEmpty().contains("logon", ignoreCase = true) ||
            message.orEmpty().contains("authentication", ignoreCase = true) ||
            message.orEmpty().contains("STATUS_LOGON_FAILURE", ignoreCase = true)

    private companion object {
        const val MAX_PREVIEW_SOURCE_BYTES = 100L * 1024L * 1024L
        const val JPEG_QUALITY = 85
        const val DISCONNECT_TIMEOUT_MS = 5 * 60 * 1000L
        const val MAX_VIEWER_IMAGE_COUNT = 100
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")
        val VIDEO_EXTENSIONS = setOf("3gp", "avi", "mkv", "mov", "mp4", "mpeg", "webm")
    }
}
