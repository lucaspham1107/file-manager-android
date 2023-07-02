package com.appforlife.filemanager.database

import android.app.Application
import com.moniqtap.source.base.BaseSharePreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharePreference @Inject constructor(app: Application) : BaseSharePreference(app) {

    private val STOP_SHOW_ONBOARD = "STOP_SHOW_ONBOARD"
    var stopShowOnboard: Boolean
        get() = getData(STOP_SHOW_ONBOARD, false)
        set(value) {
            storeData(STOP_SHOW_ONBOARD, value)
        }
    private val RECENT_DEVICE_ID = "RECENT_DEVICE_ID"
    var recentDeviceID: String
        get() = getData(RECENT_DEVICE_ID, "")
        set(value) {
            storeData(RECENT_DEVICE_ID, value)
        }
    private val HAPTIC_ENABLE = "HAPTIC_ENABLE"
    var isHapticOn: Boolean
        get() = getData(HAPTIC_ENABLE, false)
        set(value) {
            storeData(HAPTIC_ENABLE, value)
        }

    private val SHOW_DS_FIRST_LOCALLY = "SHOW_DS_FIRST_LOCALLY"
    var showDsFirstLocally: Boolean
        get() = getData(SHOW_DS_FIRST_LOCALLY, true)
        set(value) {
            storeData(SHOW_DS_FIRST_LOCALLY, value)
        }

    private val COUNT_CONTROL = "COUNT_CONTROL"
    var countControl: Int
        get() = getData(COUNT_CONTROL, 0)
        set(value) {
            storeData(COUNT_CONTROL, value)
        }
    private val ALREADY_SHOW_REVIEW = "ALREADY_SHOW_REVIEW"
    var isAlreadyShowReview: Boolean
        get() = getData(ALREADY_SHOW_REVIEW, false)
        set(value) {
            storeData(ALREADY_SHOW_REVIEW, value)
        }

}