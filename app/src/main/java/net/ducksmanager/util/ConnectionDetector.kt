package net.ducksmanager.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.connectivityManager

open class ConnectionDetector(lifecycleScope: LifecycleCoroutineScope, callback: () -> Unit) {
    private var networkCallback: ConnectivityManager.NetworkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (WhatTheDuck.isOfflineMode) {
                WhatTheDuck.isOfflineMode = false
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }

        override fun onLost(network: Network?) {
            if (!WhatTheDuck.isOfflineMode) {
                WhatTheDuck.isOfflineMode = true
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
    }

    fun unregister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: RuntimeException) { }
        }
    }
}