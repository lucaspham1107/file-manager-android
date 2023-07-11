package com.appforlife.filemanager.management

import android.app.Activity
import android.content.Context
import com.appforlife.filemanager.data.base.RatingRequestEvent
import com.appforlife.filemanager.utils.NetworkUtil
import com.appforlife.filemanager.utils.showLog
import com.google.android.play.core.review.ReviewManagerFactory

data class RatingEventInfo(val type: String, var threshold: Int)

data class RatingInfoWrapper(val ratingEventInfo: RatingEventInfo, var currentCount: Int = 0)

class ClientRatingManager(
    context: Context,
    list: List<RatingEventInfo> = listOf()
) {
    private val reviewManager = ReviewManagerFactory.create(context)
    var ratingList = list.map { it.type to RatingInfoWrapper(it) }.toMap()

    fun updateRatingList(map: Map<String, Int>) {
        ratingList =
            map.map { it.key to RatingInfoWrapper(RatingEventInfo(it.key, it.value)) }.toMap()
    }

    fun onEvent(
        type: String,
        activity: Activity,
        pageName: String,
        flow: String,
        extraInfo: Map<String, String> = mapOf()
    ) {
        ratingList[type]?.let {
            if (it.currentCount > it.ratingEventInfo.threshold) {
                return@let
            }
            it.currentCount++
            if (it.currentCount == it.ratingEventInfo.threshold) {
                startReview(activity)
                logRatingRequest(
                    pageName,
                    flow,
                    NetworkUtil.getConnectionStatus(activity),
                    extraInfo
                )
            }
        }
    }

    fun startReview(activity: Activity) {
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { reviewInfo ->
            if (reviewInfo.isSuccessful) {
                val reviewInfoResult = reviewInfo.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfoResult)
                flow.addOnCompleteListener {
                    "Review done".showLog()
                }
            } else {
                reviewInfo.exception?.printStackTrace()
            }
        }
    }

    private fun logRatingRequest(
        pageName: String,
        flow: String,
        connectionStatus: String,
        extraInfo: Map<String, String>
    ) {
        val event = RatingRequestEvent(
            pageName = pageName,
            flow = flow,
            connectionStatus = connectionStatus,
            extraInfo = extraInfo
        ).apply { this.toString().showLog() }
        logEventTracking(event)
    }
}