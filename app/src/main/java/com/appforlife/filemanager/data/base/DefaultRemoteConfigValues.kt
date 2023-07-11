package com.appforlife.filemanager.data.base

import com.appforlife.filemanager.data.base.DEFAULT_FORCE_UPDATE
import com.appforlife.filemanager.utils.CustomPair

object DefaultRemoteConfigValues {
    val USER_SEGMENT_NAME =
        CustomPair<String, Any>("user_segment_name", "moniqtap")
    val FORCE_UPDATE =
        CustomPair<String, Any>(
            "force_update", DEFAULT_FORCE_UPDATE
        )

}