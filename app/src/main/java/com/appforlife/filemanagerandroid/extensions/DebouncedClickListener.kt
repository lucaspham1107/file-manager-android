package com.appforlife.filemanagerandroid.extensions

import android.os.SystemClock
import android.view.View
import java.util.WeakHashMap
import kotlin.math.abs


/**
 * A Debounced OnClickListener
 * Rejects clicks that are too close together in time.
 * This class is safe to use as an OnClickListener for multiple views, and will debounce each one separately.
 */
@Suppress("KDocUnresolvedReference")
abstract class DebouncedClickListener(private val minimumIntervalMillis: Long) :
    View.OnClickListener {

    private val lastClickMap: MutableMap<View, Long>
    abstract fun onDebouncedClick(v: View?)

    override fun onClick(clickedView: View) {
        val previousClickTimestamp = lastClickMap[clickedView]
        val currentTimestamp: Long = SystemClock.uptimeMillis()
        if (previousClickTimestamp == null || abs(currentTimestamp - previousClickTimestamp) > minimumIntervalMillis) {
            lastClickMap[clickedView] = currentTimestamp
            onDebouncedClick(clickedView)
        }
    }

    init {
        lastClickMap = WeakHashMap()
    }
}