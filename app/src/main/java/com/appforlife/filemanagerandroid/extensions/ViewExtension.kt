package com.appforlife.filemanagerandroid.extensions

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
import androidx.core.text.toSpannable
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar


fun View.marginPx(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = this }
        top?.run { topMargin = this }
        right?.run { rightMargin = this }
        bottom?.run { bottomMargin = this }
    }
}


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
    this?.setOnClickListener(object : DebouncedClickListener(300L) {
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
        //handleException(e)
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


