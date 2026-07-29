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

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SmbSavedServer(
    val host: String,
    val shareName: String = "",
    val username: String = "",
    val encryptedPassword: String = "",
    val connectionType: SmbConnectionType = SmbConnectionType.MANUAL_SMB
)

enum class SmbConnectionType {
    MANUAL_SMB,
    LAN
}

class SmbServerStore(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val credentialCipher = SmbCredentialCipher()

    fun load(): List<SmbSavedServer> = runCatching {
        val json = preferences.getString(SAVED_SERVERS_KEY, null) ?: return emptyList()
        Gson().fromJson<List<SmbSavedServer>>(json, SERVER_LIST_TYPE)
            .orEmpty()
            .map { server ->
                @Suppress("SENSELESS_COMPARISON")
                if (server.connectionType == null) {
                    server.copy(connectionType = SmbConnectionType.MANUAL_SMB)
                } else {
                    server
                }
            }
    }.getOrDefault(emptyList())

    fun save(server: SmbSavedServer, password: String, rememberPassword: Boolean) {
        val savedServer = server.copy(
            encryptedPassword = if (rememberPassword && password.isNotEmpty()) {
                credentialCipher.encrypt(password).orEmpty()
            } else {
                ""
            }
        )
        val servers = load()
            .filterNot {
                it.host.equals(savedServer.host, ignoreCase = true) &&
                    it.connectionType == savedServer.connectionType &&
                    (it.connectionType == SmbConnectionType.LAN || it.shareName == savedServer.shareName)
            }
            .toMutableList()
        servers.add(0, savedServer)
        preferences.edit().putString(SAVED_SERVERS_KEY, Gson().toJson(servers)).apply()
    }

    fun passwordFor(server: SmbSavedServer): String? = server.encryptedPassword
        .takeIf { it.isNotEmpty() }
        ?.let(credentialCipher::decrypt)

    fun clearCredentials(server: SmbSavedServer) {
        val servers = load().map {
            if (
                it.host.equals(server.host, ignoreCase = true) &&
                it.connectionType == server.connectionType &&
                (it.connectionType == SmbConnectionType.LAN || it.shareName == server.shareName)
            ) {
                it.copy(username = "", encryptedPassword = "")
            } else {
                it
            }
        }
        preferences.edit().putString(SAVED_SERVERS_KEY, Gson().toJson(servers)).apply()
    }

    private companion object {
        const val SAVED_SERVERS_KEY = "smb_saved_servers"
        // Avoid an anonymous TypeToken subclass: R8 strips its generic signature in release builds.
        val SERVER_LIST_TYPE = TypeToken.getParameterized(
            List::class.java,
            SmbSavedServer::class.java
        ).type
    }
}
