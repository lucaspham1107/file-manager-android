package com.appforlife.filemanager.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.appforlife.filemanager.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.snackbar.Snackbar


fun View.marginPx(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = this }
        top?.run { topMargin = this }
        right?.run { rightMargin = this }
        bottom?.run { bottomMargin = this }
    }
}

/*fun NativeAdView.populateUnifiedNativeAdView(nativeAd: NativeAd) {
    mediaView = findViewById(R.id.media_view)
    bodyView = findViewById(R.id.body)

    headlineView = findViewById(R.id.primary)
    callToActionView = findViewById(R.id.cta)
    iconView = findViewById(R.id.icon)

    (headlineView as? TextView)?.text = nativeAd.headline
    nativeAd.mediaContent?.let { mediaView?.setMediaContent(it) }

    bodyView?.isVisible = nativeAd.body != null
    callToActionView?.isVisible = nativeAd.callToAction != null
    iconView?.isVisible = nativeAd.icon != null
    priceView?.isVisible = nativeAd.price != null
    storeView?.isVisible = nativeAd.store != null
    advertiserView?.isVisible = nativeAd.advertiser != null
    (bodyView as? TextView)?.text = nativeAd.body
    (callToActionView as? TextView)?.text = nativeAd.callToAction
    (iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
    setNativeAd(nativeAd)
}*/

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun Window.setFullScreen() {
    this.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun View.hideDown(parent: ViewGroup, duration: Long = 600) {
    val tagAnim = "hideDown"
    if (this.tag != tagAnim) {
        this.tag = tagAnim
        val transition: Transition = Fade()
        transition.duration = duration
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(parent, transition)
        this.visibility = View.GONE
    }

}

fun View.showUp(parent: ViewGroup, duration: Long = 600) {
    val tagAnim = "showUp"
    if (this.tag != tagAnim) {
        this.tag = tagAnim
        val transition: Transition = Fade()
        transition.duration = duration
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(parent, transition)
        this.visibility = View.VISIBLE
    }

}

fun Dialog.applyWidthHeight(context: Context, height: Float? = null) {
    val widthPercent = 0.87f
    val heightPercent = height ?: 0.34f
    val screenSize = ScreenMetricsCompat.getScreenSize(context)
    window?.setLayout(
        (screenSize.width * widthPercent).toInt(),
        (screenSize.height * heightPercent).toInt()
    )
    window?.setGravity(Gravity.CENTER)
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

fun Dialog.applyKeyInputWidthHeight(context: Context) {
    val widthPercent = 0.87f
    val heightPercent = 0.15f
    val screenSize = ScreenMetricsCompat.getScreenSize(context)
    window?.setLayout(
        (screenSize.width * widthPercent).toInt(),
        (screenSize.height * heightPercent).toInt()
    )
    window?.setGravity(Gravity.CENTER)
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

fun Spannable.setColorForPath(paths: Array<String>, color: Int): Spannable {
    val str = this.toString()
    val spannableStringBuilder = SpannableStringBuilder().append(str)
    for (i in paths.indices) {
        spannableStringBuilder.setColorMatchTexts(color, paths[i])
    }
    return spannableStringBuilder.toSpannable()
}


fun SpannableStringBuilder.setColorMatchTexts(color: Int, keyText: String): SpannableStringBuilder {
    var starIndex = this.toString().indexOf(keyText)
    while (starIndex >= 0) {
        val start = starIndex
        val end = start + keyText.length
        setColor(color, start, end)
        starIndex = this.toString().indexOf(keyText, starIndex + 1)
    }
    return this
}

fun SpannableStringBuilder.setColor(color: Int, start: Int, end: Int): SpannableStringBuilder {
    if (start >= 0 && end <= length && start <= end) {
        setSpan(
            ForegroundColorSpan(color),
            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return this
}

fun View?.onDebounceClickListener(action: () -> Unit) {
    this?.setOnClickListener(object : DebouncedClickListener(DEFAULT_DEBOUNCING_TIME) {
        override fun onDebouncedClick(v: View?) {
            action.invoke()
        }
    })
}

fun getDuration(context: Context?, uri: Uri?): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
    } catch (e: Exception) {
        handleException(e)
        0L
    } finally {
        retriever.release()
    }
}

fun View.showSnackBar(
    message: String,
    retryActionName: String? = null,
    action: (() -> Unit)? = null
) {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)

    action?.let {
        snackBar.setAction(retryActionName) {
            it()
        }
    }

    snackBar.show()
}


