package com.appforlife.filemanager.management

data class BannerExtraTrackingInfo(
    val impressionExtraTrackingInfo: Map<String, String> = emptyMap(),
    val clickExtraTrackingInfo: Map<String, String> = emptyMap()
)