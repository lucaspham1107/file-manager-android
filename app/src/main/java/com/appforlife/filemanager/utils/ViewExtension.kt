package com.appforlife.filemanager.utils

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.appforlife.filemanager.R
import java.util.*

private const val SECOND = 1
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val MONTH = 30 * DAY
private const val YEAR = 12 * MONTH

private fun currentDate(): Long {
    val calendar = Calendar.getInstance()
    return calendar.timeInMillis
}

fun Long.toTimeAgo(): String {
    val time = this
    val now = currentDate()
    val diff = (now - time) / 1000
    return when {
        diff < MINUTE -> "just now"
        diff < 2 * MINUTE -> "a minute ago"
        diff < 60 * MINUTE -> "${diff / MINUTE} minutes ago"
        diff < 2 * HOUR -> "an hour ago"
        diff < 24 * HOUR -> "${diff / HOUR} hours ago"
        diff < 2 * DAY -> "yesterday"
        diff < 30 * DAY -> "${diff / DAY} days ago"
        diff < 2 * MONTH -> "a month ago"
        diff < 12 * MONTH -> "${diff / MONTH} months ago"
        diff < 2 * YEAR -> "a year ago"
        else -> "${diff / YEAR} years ago"
    }
}

fun View.setOnSingleClickListener(action: (View) -> Unit) {
    setOnClickListener { view ->
        view.isClickable = false
        action(view)
        view.postDelayed({
            view.isClickable = true
        }, 300L)
    }
}

fun View.showKeyBoard(delay: Long? = null) {
    delay?.let {
        Handler().postDelayed({
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }, it)
    } ?: run {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}

fun View.hideKeyBoard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun TextView.setTextClickable(data: String, from: Int, to: Int, method: () -> Unit) {
    val spanned = SpannableString(data)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            method.invoke()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }
    spanned.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
        from,
        to,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spanned.setSpan(
        clickableSpan,
        from,
        to,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
    text = spanned
}

@BindingAdapter("goneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun Context.copyToClipboard(content: String, message: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(content, content)
    clipboard.setPrimaryClip(clip)
    toast(message)
}

fun Context.toast(message: Int) {
    Toast.makeText(this, this.getString(message), Toast.LENGTH_SHORT).show()
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showDialog(
    title: String,
    message: String,
    cancelable: Boolean? = true,
    listener: DialogInterface.OnClickListener? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(cancelable ?: true)
        .setPositiveButton(android.R.string.ok, listener)
        .show()
}

fun Context.showYesNoDialog(
    message: Int,
    positive: Int,
    negative: Int,
    cancelable: Boolean? = false,
    yesListener: DialogInterface.OnClickListener? = null,
    noListener: DialogInterface.OnClickListener? = null
) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setCancelable(cancelable ?: false)
        .setPositiveButton(positive, yesListener)
        .setNegativeButton(negative, noListener)
        .show()
}

inline fun getValueAnimator(
    forward: Boolean = true,
    duration: Long,
    interpolator: TimeInterpolator,
    crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
    val a =
        if (forward) ValueAnimator.ofFloat(0f, 1f)
        else ValueAnimator.ofFloat(1f, 0f)
    a.addUpdateListener { updateListener(it.animatedValue as Float) }
    a.duration = duration
    a.interpolator = interpolator
    return a
}

