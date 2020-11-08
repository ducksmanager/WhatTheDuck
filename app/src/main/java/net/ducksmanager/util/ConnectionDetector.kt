package net.ducksmanager.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.connectivityManager
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode

open class ConnectionDetector(lifecycleScope: LifecycleCoroutineScope, callback: () -> Unit) {
    private var networkCallback: ConnectivityManager.NetworkCallback = @RequiresApi(VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (isOfflineMode) {
                isOfflineMode = false
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }

        override fun onLost(network: Network?) {
            if (!isOfflineMode) {
                isOfflineMode = true
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }
    }

    init {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
    }

    fun unregister() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: RuntimeException) { }
        }
    }
}