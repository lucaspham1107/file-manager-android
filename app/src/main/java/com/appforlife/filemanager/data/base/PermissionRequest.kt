package com.appforlife.filemanager.data.base

import androidx.annotation.Keep

@Keep
data class PermissionRequest(
    val permissionList: List<String>, val callback: (result: Boolean) -> Unit
)
