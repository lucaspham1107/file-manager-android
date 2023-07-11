package com.appforlife.filemanager.base

import android.support.multidex.MultiDexApplication
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.appforlife.filemanager.data.*
import com.appforlife.filemanager.data.base.DefaultRemoteConfigValues
import com.appforlife.filemanager.data.base.FirstOpenTracking
import com.appforlife.filemanager.data.base.IAPItem
import com.appforlife.filemanager.data.base.RemoteConfigLoaded
import com.appforlife.filemanager.data.base.StoreConfigItem
import com.appforlife.filemanager.management.*
import com.appforlife.filemanager.utils.CustomPair
import com.appforlife.filemanager.utils.IMEIUtils.getDeviceIMEI
import com.appforlife.filemanager.utils.convert
import com.appforlife.filemanager.utils.getPackageInfo

abstract class BaseApplication : MultiDexApplication() {
    abstract fun allowLoggingEventTracking(): Boolean
    abstract fun getBillingManager(): BillingClientManager
    abstract fun getRemoteConfigList(): List<CustomPair<String, Any>>
    abstract fun applyTestVersion(): Boolean
    abstract fun getIapItemJsonConfig(): String
    abstract fun getDirectStoreJsonConfig(): String

    val liveDataRemoteConfig = MutableLiveData<Boolean>()
    val appPreference by lazy { BaseSharePreference(this) }

    companion object {
        lateinit var instance: BaseApplication
    }

    override fun onCreate() {
        instance = this
        getBillingManager().start()
        BaseEventTrackingManager.init(this, Firebase.analytics, allowLoggingEventTracking())
        setCurrentAppVersion(if (applyTestVersion()) "0" else getPackageInfo()?.versionName.orEmpty())
        reloadRemoteConfig()
        super.onCreate()
    }

    private fun reloadRemoteConfig() {
        RemoteConfigManager.reloadConfig(
            Firebase.remoteConfig,
            getRemoteConfigList()
                .plus(DefaultRemoteConfigValues.USER_SEGMENT_NAME)
                .plus(DefaultRemoteConfigValues.FORCE_UPDATE)
        ) { loadFromPreviousVersion, configUpdated, fetchSuccess ->
            logEventTracking(RemoteConfigLoaded(fetchSuccess.toString() + " - ${RemoteConfigManager.valueFetch}"))
            liveDataRemoteConfig.postValue(fetchSuccess)
            if (fetchSuccess) {
                appPreference.isRemoteConfigLoaded = true
                setUserSegmentName(DefaultRemoteConfigValues.USER_SEGMENT_NAME.second.convert())
                if (!appPreference.getData("LOG_FIRST_OPEN", false)) {
                    logEventTracking(FirstOpenTracking())
                    appPreference.storeData("LOG_FIRST_OPEN", true)
                }
            }
            getBillingManager().updateSkuList(
                StoreConfigItem.getStoreConfigList(getDirectStoreJsonConfig())
                    .flatMap { it.items }
                    .plus(
                        IAPItem.getConfigList(getIapItemJsonConfig())
                            .map { it.item }
                    )
                    .distinct()
            )
            remoteConfigFetched(loadFromPreviousVersion, configUpdated, fetchSuccess)
            getDeviceIMEI(this).let {
                FirebaseCrashlytics.getInstance().setUserId(it)
            }
        }
    }

    abstract fun remoteConfigFetched(
        loadFromPreviousVersion: Boolean,
        configUpdated: Boolean,
        fetchSuccess: Boolean
    )
}