package com.appforlife.filemanager.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.*


object IMEIUtils {
    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    @SuppressLint("HardwareIds")
    fun getDeviceIMEI(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            .uppercase(Locale.getDefault())
    }
}