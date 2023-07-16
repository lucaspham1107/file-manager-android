package com.appforlife.filemanagerandroid.base

import android.annotation.SuppressLint
import android.os.Environment
import com.appforlife.filemanagerandroid.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.hasPermission
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.helpers.isRPlus

open class SimpleActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher,
    )

    override fun getAppLauncherName() = getString(R.string.app_name)

    @SuppressLint("NewApi")
    fun hasStoragePermission(): Boolean {
        return if (isRPlus()) {
            Environment.isExternalStorageManager()
        } else {
            hasPermission(PERMISSION_WRITE_STORAGE)
        }
    }
}
