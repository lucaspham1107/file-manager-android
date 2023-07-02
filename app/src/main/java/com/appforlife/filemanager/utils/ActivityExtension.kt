package com.appforlife.filemanager.utils

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.findNavController


fun Activity.findNavControllerSafe(@IdRes viewId: Int): NavController? {
    return try {
        findNavController(viewId)
    } catch (e: Exception) {
        null
    }
}

fun NavController.safeNavigate(direction: NavDirections, navOptions: NavOptions?) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction, navOptions) }
}

fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}

fun Activity.makeStatusBarTransparent() {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        statusBarColor = Color.TRANSPARENT
    }
}