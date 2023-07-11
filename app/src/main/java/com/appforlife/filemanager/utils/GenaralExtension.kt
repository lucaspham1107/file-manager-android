package com.appforlife.filemanager.utils

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.viewpager.widget.ViewPager
import com.appforlife.filemanager.BuildConfig
import com.appforlife.filemanager.R
import com.appforlife.filemanager.base.BaseApplication
import com.appforlife.filemanager.management.AdsManager
import com.appforlife.filemanager.management.InterstitialAdCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Period
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

fun Context.getSharePref(): SharedPreferences {
    return getSharedPreferences(packageName, Context.MODE_PRIVATE)
}

@Suppress("UNCHECKED_CAST")
fun <T> Any.convert() = this as T

@Keep
data class CustomPair<T, R>(var first: T, var second: R) {
    @Suppress("UNCHECKED_CAST")
    fun <X, Y> toPair() = Pair(first as X, second as Y)
}


fun Any.toJson() =
    try {
        Gson().toJson(this)
    } catch (e: Exception) {
        handleException(e)
        ""
    }

fun <T> String.jsonToListObject(): List<T> {
    return Gson().fromJson(this, object : TypeToken<List<T>>() {}.type)
}

inline fun <reified T> String.jsonToObj() = Gson()
    .fromJson(this, T::class.java)

fun Int.pxToDp(): Int {
    return (this / Resources.getSystem().displayMetrics.density).toInt()
}

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun String.showLog(tag: String = "CUSTOM_LOG") {
    if (BuildConfig.DEBUG) {
        Log.d(tag, this)
    }
}

fun String.simplyName(): String {
    if (this.contains("ly")) {
        return dropLast(2).lowercase()
    }
    return this.lowercase()
}

fun Context.openDirectStore(
    isAtLaunch: Boolean = false,
    isAutoShow: Boolean = false,
    dsCondition: String
) {

    //startActivityView<DirectStoreOneActivity> {}
}

fun View.showKeyboard() {
    post {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(
            this,
            InputMethodManager.SHOW_IMPLICIT
        )
    }
}

fun View.hideKeyboard() {
    post {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun String.showToast(context: Context, isLong: Boolean = false): String {
    val length = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(context, this, length).show()
    return this
}

fun String.showToast(isLong: Boolean) {
    val length = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(BaseApplication.instance, this, length).show()
}

fun Context.showToast(msg: String): String {
    Toast.makeText(
        this,
        msg,
        Toast.LENGTH_SHORT
    ).show()
    return msg
}


fun AppCompatImageView.changeColor(context: Context, colorId: Int, parse: Boolean = true) {
    if (parse) {
        setColorFilter(
            context.getColorRessource(colorId), PorterDuff.Mode.SRC_ATOP
        )
    } else {
        setColorFilter(
            colorId, PorterDuff.Mode.SRC_ATOP
        )
    }
}

fun Context.openUrlBrowser(url: String, urlTitle: String) {
    startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)),
            urlTitle
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun parsePeriod(period: String): String {
    return if (period == "") {
        ""
    } else {
        try {
            Period.parse(period).let { periodValue ->
                periodValue.days.let { day ->
                    if (day == 0) "" else if (day == 1) "Day" else "$day Days "
                } +
                        periodValue.weeks.let { week ->
                            if (week == 0) "" else if (week == 1) "Week" else "$week Weeks "
                        } +
                        periodValue.months.let { month ->
                            if (month == 0) "" else if (month == 1) "Month" else "$month Months "
                        } +
                        periodValue.years.let { year ->
                            if (year == 0) "" else if (year == 1) "Year" else "$year Years "
                        }
            }.trim()
        } catch (e: Exception) {
            handleException(e)
            return ""
        }
    }
}

fun View.isHide(boolean: Boolean) {
    if (boolean) {
        visibility = View.INVISIBLE
        isEnabled = false
    } else {
        visibility = View.VISIBLE
        isEnabled = true
    }
}

fun File.getMimeType() =
    URLConnection.guessContentTypeFromName(name.split(".").last().let { ".$it" })

fun String.getMimeType() =
    try {
        if (split(".").last() == "mp3") {
            "audio/mp3"
        } else {
            URLConnection.guessContentTypeFromName(this)
        }
    } catch (e: Exception) {
        handleException(e)
        "image"
    }

fun File.toFileInputStream() = FileInputStream(this)

fun Context.getIpAddress(): String {
    return (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?)?.let {
        val ipInt = it.connectionInfo.ipAddress
        InetAddress.getByAddress(
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
        )
            .hostAddress
    }.orEmpty()
}

fun Context.getPackageInfo(): PackageInfo? {
    try {
        return packageManager.getPackageInfo(packageName, 0)
    } catch (e: Exception) {
        handleException(e)
        return null
    }
}

fun Context.checkAppPermission(list: List<String>): Boolean {
    return list.map { checkCallingOrSelfPermission(it) }
        .all { it == PackageManager.PERMISSION_GRANTED }
}

fun Int.format(numberOfDigit: Int = 2) = String.format("%0${numberOfDigit}d", this)

fun Date.format(format: String = "dd-MM-yyyy"): String {
    return SimpleDateFormat(format, Locale.ROOT).format(this)
}


fun ViewPager.addPageChangedListener(
    listener: (position: Int) -> Unit,
    onPageScrolled: ((position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit)? = null
) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) {
            onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            listener.invoke(position)
        }

    })
}

fun String.toJsonObject(): JsonObject {
    return try {
        return JsonParser.parseString(this).asJsonObject
    } catch (e: Exception) {
        e.printStackTrace()
        JsonObject()
    }
}

fun String.toJsonArray(): JsonArray = JsonParser.parseString(this).asJsonArray

fun <T> JsonArray.mapToList(map: (JsonObject) -> T): MutableList<T> {
    val length = this.size()
    val result = mutableListOf<T>()
    for (i in 0..length - 1) {
        result.add(map.invoke(this.get(i).asJsonObject))
    }
    return result
}


fun <T> JsonArray.toList(map: (JsonElement) -> T): MutableList<T> {
    val length = this.size()
    val result = mutableListOf<T>()
    for (i in 0 until length) {
        result.add(map.invoke(this.get(i)))
    }
    return result
}

fun String.jsonToMap() = toJsonObject().let {
    it.keySet().associateWith { key ->
        it[key]
    }
}

fun pingToIpAddress(ip: String, timeout: Int = 500): Boolean {
    val address = InetAddress.getByName(ip)
    return address.isReachable(timeout)
}

fun String.fromHTML() =
    HtmlCompat.fromHtml(
        this,
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )

fun Context.getColorRessource(colorRes: Int) =
    ContextCompat.getColor(this, colorRes)

fun Context.getDrawableResource(resId: Int) = ContextCompat.getDrawable(this, resId)

fun handleException(e: Exception) {
    e.printStackTrace()
    FirebaseCrashlytics.getInstance().recordException(e)
}

fun handleException(e: Throwable) {
    e.printStackTrace()
    FirebaseCrashlytics.getInstance().recordException(e)
}

inline fun <reified T> Context.startActivityView(block: (Intent.() -> Unit)) {
    try {
        startActivity(Intent(this, T::class.java).apply { block.invoke(this) })
    } catch (e: Exception) {
        handleException(e)
    }
}


fun <T> Application.queryContentProvider(
    queryColumn: Array<String>, uri: Uri, queryCur: (Cursor) -> T
): List<T> {
    val cur = contentResolver.query(uri, queryColumn, null, null, null)
    if (cur == null || cur.count <= 0 || !cur.moveToFirst()) {
        return emptyList()
    }
    val resultList = mutableListOf<T>()
    do {
        try {
            resultList.add(queryCur(cur))
        } catch (e: Exception) {
            handleException(e)
        }
    } while (cur.moveToNext())
    return resultList
}

fun Context.showAppInPlayStore() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    } catch (anfe: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }
}

fun Context.showOtherApps() {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/dev?id=9175334228948628001")
        ).setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )
        startActivity(intent)
    } catch (e: Exception) {
        handleException(e)
    }
}

fun Context.getApplicationName(): String {
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(stringId)
}

private fun Context.buildDefaultEmailFooter(): String {
    val packageInfo = packageManager.getPackageInfo(
        packageName, 0
    )
    val versionName = packageInfo.versionName
    val androidVersion = Build.VERSION.RELEASE
    return getString(
        R.string.default_send_mail_footer_format,
        getApplicationName(),
        versionName,
        packageName,
        androidVersion,
        IMEIUtils.getDeviceIMEI(this)
    )
}

private fun Context.buildDefaultEmailSubject(): String =
    getString(R.string.default_send_mail_subject_format, getApplicationName())


/**
 * Method to start mail support
 * @param supportMail the support mail, default to "support@moniqtap.com"
 * @param title the title of the intent, default to "Send mail..."
 * @param subject the subject of the email, default to "{App name} Support"
 * @param footer the footer of the email, default to "{App name} {version name} {package name}\nAndroid {android version}"
 * @return true if start mail successfully
 */
fun Activity.startMailSupport(
    supportMail: String = getString(R.string.default_support_email),
    title: String = getString(R.string.default_send_mail_title),
    subject: String = buildDefaultEmailSubject(),
    footer: String = buildDefaultEmailFooter()
): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportMail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n$footer")
        }

        startActivity(
            Intent.createChooser(
                intent,
                title
            )
        )
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

fun String.toLongNumber(): Long? {
    return try {
        toLong()
    } catch (e: Exception) {
        null
    }
}

fun Activity?.getVibrator(): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            this?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        this?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}

fun AdsManager.showInterAdsWithScope(
    activity: Activity, event: String, showNow: Boolean = false,
    interstitialAdCallback: InterstitialAdCallback? = null,
    impressionExtraTrackingInfo: Map<String, String> = emptyMap(), scope: CoroutineScope
) {
    scope.launch(Dispatchers.Main) {
        this@showInterAdsWithScope.showInterstitialAd(
            activity,
            event,
            showNow,
            interstitialAdCallback, impressionExtraTrackingInfo
        )
    }
}

fun Long.durationFormat(): String =
    String.format("%02d:%02d:%02d", this / 3600 % 24, this / 60, this % 60)

fun bitmapToFile(
    context: Context?,
    bitmap: Bitmap,
    fileNameToSave: String
): File? { // File name like "image.png"
    //create a file to write bitmap data
    var file: File? = null
    return try {
        val outputDir = File(context?.getExternalFilesDir(null), "outputs")
        outputDir.mkdir()
        file = File.createTempFile(fileNameToSave, ".png", outputDir)

//Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
        val bitmapdata: ByteArray = bos.toByteArray()

//write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        file
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        file // it will return null
    }
}

fun <T> Application.queryContentProvider(
    queryColumn: Array<String>, uri: Uri, columnAscOrder: String, queryCur: (Cursor) -> T
): List<T> {
    val resultList = LinkedList<T>()
    val cur = contentResolver.query(uri, queryColumn, null, null, "$columnAscOrder DESC")
    if (cur != null && cur.count > 0) {
        if (cur.moveToFirst()) {
            do {
                resultList.add(queryCur(cur))
            } while (cur.moveToNext())
        }
    }
    return resultList
}

fun Activity.openActivityAndClearStack(intent: Intent) {
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent).apply {
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)
        finish()
    }
}

@Suppress("DEPRECATION")
fun Application.isNetworkConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE).convert<ConnectivityManager>()
    if (Build.VERSION.SDK_INT < 23) {
        return cm.activeNetworkInfo
            ?.let { ni ->
                ni.isConnected && (ni.type == ConnectivityManager.TYPE_WIFI || ni.type == ConnectivityManager.TYPE_MOBILE)
            } ?: false
    } else {
        return cm.activeNetwork
            ?.let { cm.getNetworkCapabilities(it) }
            ?.let { nc ->
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
            ?: false
    }
}


