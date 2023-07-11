package com.appforlife.filemanager.utils

import android.content.Context
import android.net.ConnectivityManager

@Suppress("DEPRECATION")
object NetworkUtil {
    var TYPE_WIFI = 1
    var TYPE_MOBILE = 2
    var TYPE_NOT_CONNECTED = 0
    private fun getConnectivityStatus(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        cm?.activeNetworkInfo?.apply {
            if (type == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI
            if (type == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }

    fun getConnectionStatus(context: Context?): String {
        var status = ""
        context?.let {
            when (getConnectivityStatus(it)) {
                TYPE_WIFI -> {
                    status = "Wifi"
                }

                TYPE_MOBILE -> {
                    status = "Mobile network"
                }

                TYPE_NOT_CONNECTED -> {
                    status = "Not connected"
                }
            }
        }
        return status
    }
}