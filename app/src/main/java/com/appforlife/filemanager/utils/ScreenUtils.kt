package com.appforlife.filemanager.utils

import android.util.DisplayMetrics

object ScreenUtils {

    var screenWidth = 0F
    var screenHeight = 0F
    var rateDifference = 0F

    fun calculatePercentScreen(displayMetrics: DisplayMetrics) {
        val designWidth = 412F
        val designHeight = 870F
        val designRatio = designWidth / designHeight
        val currentDeviceRatio = displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()
        rateDifference = currentDeviceRatio / designRatio
    }
}