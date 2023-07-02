package com.appforlife.filemanager.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

const val META_TERM_OF_USE = "https://metaverselabs.ai/privacy-policy/"
const val META_PRIVACY_AND_POLICY = "https://metaverselabs.ai/terms-of-use/"
const val METAVERSE_LABS_PLAY_STORE_URL =
    "https://play.google.com/store/apps/dev?id=8675396445566418170"
const val FAQs = "https://metaverselabs.ai/smartcar-faq/"
const val SMART_CAR_TAG = "Smart Car Android"
const val CHAT_TOPIC_TAG = "animation_ai_android"
fun Context.checkAppPermission(list: List<String>): Boolean {
    return list.map { checkCallingOrSelfPermission(it) }
        .all { it == PackageManager.PERMISSION_GRANTED }
}

fun getRandomUUID(): String {
    return UUID.randomUUID().toString()
}

private fun getUniquePsuedoID(): String {
    // If all else fails, if the user does have lower than API 9 (lower
    // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
    // returns 'null', then simply the ID returned will be solely based
    // off their Android device information. This is where the collisions
    // can happen.
    // Thanks http://www.pocketmagic.net/?p=1662!
    // Try not to use DISPLAY, HOST or ID - these items could change.
    // If there are collisions, there will be overlapping data
    val m_szDevIDShort =
        "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10

    // Thanks to @Roman SL!
    // https://stackoverflow.com/a/4789483/950427
    // Only devices with API >= 9 have android.os.Build.SERIAL
    // http://developer.android.com/reference/android/os/Build.html#SERIAL
    // If a user upgrades software or roots their device, there will be a duplicate entry
    var serial: String? = null
    try {
        serial = Build::class.java.getField("SERIAL")[null]?.toString()
        // Go ahead and return the serial for api => 9
        return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
    } catch (exception: Exception) {
        // String needs to be initialized
        serial = "serial" // some value
    }

    // Thanks @Joe!
    // https://stackoverflow.com/a/2853253/950427
    // Finally, combine the values we have found by using the UUID class to create a unique identifier
    return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
}

fun Long.getNextPayment(period: String): Long {
    if (period.length != 3) return 0
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val periodDuration = period.takeLast(1)
    val valueDuration = period[1].toString().toIntOrNull() ?: 0
    if (periodDuration == "Y") {
        calendar.add(Calendar.YEAR, valueDuration)
    }
    if (periodDuration == "M") {
        calendar.add(Calendar.MONTH, valueDuration)
    }
    if (periodDuration == "D") {
        calendar.add(Calendar.DAY_OF_MONTH, valueDuration)
    }
    return calendar.timeInMillis
}

fun Long.toddMMMyyyy(): String {
    return try {
        val date = Date(this)
        val format = SimpleDateFormat("dd MMM yyyy", Locale.US)
        format.format(date)
    } catch (e: Exception) {
        ""
    }
}


