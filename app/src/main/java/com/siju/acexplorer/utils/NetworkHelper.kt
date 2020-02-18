package com.siju.acexplorer.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.siju.acexplorer.main.model.helper.SdkHelper

private const val TAG = "NetworkHelper"
class NetworkHelper(val networkChangeCallback: NetworkChangeCallback) {

    companion object {
        fun isConnectedToInternet(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            val connectivityManager = context.getSystemService(
                    Context.CONNECTIVITY_SERVICE) as ConnectivityManager? ?: return false
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(
                    activeNetwork)
            return networkCapabilities != null && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network?) {
            Log.e(TAG, "networkcallback called from onLost:$network")
            //record wi-fi disconnect event
            networkChangeCallback.onNetworkUnavailable()
        }
        override fun onUnavailable() {
            Log.e(TAG, "networkcallback called from onUnvailable")
            networkChangeCallback.onNetworkUnavailable()
        }
        override fun onLosing(network: Network?, maxMsToLive: Int) {
            Log.e(TAG,"networkcallback called from onLosing")
        }
        override fun onAvailable(network: Network?) {
            Log.e(TAG,"NetworkCallback network called from onAvailable ")
            networkChangeCallback.onNetworkAvailable()
        }
    }

    fun registerNetworkRequest(connectivityManager: ConnectivityManager?) {
        if (connectivityManager == null) {
            return
        }
        if (SdkHelper.isAtleastNougat) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
        else {
            val request: NetworkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    fun unregisterNetworkRequest(connectivityManager: ConnectivityManager?) {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    fun getConnectivityManager(context: Context?) : ConnectivityManager? {
        if (context == null) {
            return null
        }
        return context.getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    }

    interface NetworkChangeCallback {
        fun onNetworkAvailable()
        fun onNetworkUnavailable()
    }
}