package com.appforlife.filemanager.management

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.appforlife.filemanager.utils.showLog

open class EventInfo(
    var eventType: String,
    var eventParam: Map<String, String>,
    var orderId: String? = null,
    var signature: String? = null,
    var purchaseToken: String? = null,
    var price: Double? = null,
    var currency: String? = null,
    var sku: String? = null,
    var purchaseTime: Long? = null
)

open class BaseEventTrackingManager constructor(
    private val app: Application,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val allowLogEvent: Boolean
) {
    fun logEvent(event: EventInfo) {
        if (allowLogEvent) {
            firebaseAnalytics.logEvent(event.eventType) {
                event.eventParam.forEach {
                    "firebase ${event.eventType} - ${it.key} - ${it.value}".showLog()
                    param(it.key, it.value)
                }
            }
        }
    }

    fun setCurrentAppVersion(currentAppVersion: String) {
        firebaseAnalytics.setUserProperty(
            "current_app_version",
            currentAppVersion.apply { "current_app_version: $this".showLog() })
    }

    fun setUserSegmentName(userSegmentName: String) {
        firebaseAnalytics.setUserProperty(
            "user_segment_name",
            userSegmentName.apply { "user_segment_name: $this".showLog() })
    }

    fun setUserProperties(currentAppVersion: String, userSegmentName: String) {
        firebaseAnalytics.setUserProperty(
            "current_app_version",
            currentAppVersion.apply { "current_app_version: $this".showLog() })
        firebaseAnalytics.setUserProperty(
            "user_segment_name",
            userSegmentName.apply { "user_segment_name: $this".showLog() })
    }

    companion object {
        var instance: BaseEventTrackingManager? = null
        fun init(
            app: Application,
            firebaseAnalytics: FirebaseAnalytics,
            allowLogEvent: Boolean
        ) {
            instance =
                BaseEventTrackingManager(app, firebaseAnalytics, allowLogEvent)
        }
    }
}

fun logEventTracking(event: EventInfo) {
    BaseEventTrackingManager.instance?.logEvent(event)
}

fun setCurrentAppVersion(version: String) {
    BaseEventTrackingManager.instance!!.setCurrentAppVersion(version)
}

fun setUserSegmentName(userSegmentName: String) {
    BaseEventTrackingManager.instance!!.setUserSegmentName(userSegmentName)
}

fun setUserProperties(currentAppVersion: String, userSegmentName: String) {
    BaseEventTrackingManager.instance!!.setUserProperties(currentAppVersion, userSegmentName)
}