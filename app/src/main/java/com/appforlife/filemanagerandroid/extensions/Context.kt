package com.appforlife.filemanagerandroid.extensions

import android.content.Context
import com.appforlife.filemanagerandroid.helpers.Config
import com.simplemobiletools.commons.extensions.isPathOnOTG
import com.simplemobiletools.commons.extensions.isPathOnSD

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.isPathOnRoot(path: String) = !(path.startsWith(config.internalStoragePath) || isPathOnOTG(path) || (isPathOnSD(path)))
