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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.Socket

data class SmbNearbyServer(val host: String)

object SmbServerDiscovery {

    suspend fun findNearbyServers(context: Context): List<SmbNearbyServer> = withContext(Dispatchers.IO) {
        val localAddress = findWifiIpv4Address(context) ?: return@withContext emptyList()
        val octets = localAddress.address.map { it.toInt() and 0xff }
        val subnet = "${octets[0]}.${octets[1]}.${octets[2]}"
        coroutineScope {
            (1..254)
                .filter { it != octets[3] }
                .chunked(MAX_CONCURRENT_PROBES)
                .flatMap { hosts ->
                    hosts.map { host -> async { "$subnet.$host".takeIf(::hasSmbPort) } }.awaitAll()
                }
                .filterNotNull()
                .map(::SmbNearbyServer)
        }
    }

    private fun findWifiIpv4Address(context: Context): Inet4Address? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiNetwork = connectivityManager.allNetworks.firstOrNull { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } ?: return null
        return connectivityManager.getLinkProperties(wifiNetwork)
            ?.linkAddresses
            ?.mapNotNull { it.address as? Inet4Address }
            ?.firstOrNull { it.isSiteLocalAddress }
    }

    private fun hasSmbPort(host: String): Boolean = runCatching {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, SMB_PORT), CONNECT_TIMEOUT_MS)
        }
        true
    }.getOrDefault(false)

    private const val SMB_PORT = 445
    private const val CONNECT_TIMEOUT_MS = 250
    private const val MAX_CONCURRENT_PROBES = 32
}
