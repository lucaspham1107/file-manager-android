package com.appforlife.filemanager.data.base


import com.google.gson.annotations.SerializedName

data class InterAdsConfig(
    @SerializedName("ad_id")
    val adId: String = "",
    @SerializedName("event_type")
    val eventType: String = "",
    @SerializedName("interval_threshold")
    val intervalThreshold: List<Int> = listOf()
)