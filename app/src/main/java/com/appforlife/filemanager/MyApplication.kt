package com.appforlife.filemanager

import com.appforlife.filemanager.base.BaseApplication
import com.appforlife.filemanager.data.base.RemoteConfigValues
import com.appforlife.filemanager.data.base.StoreConfigItem
import com.appforlife.filemanager.management.AdsManager
import com.appforlife.filemanager.management.BillingClientManager
import com.appforlife.filemanager.management.QuotaLimitManager
import com.appforlife.filemanager.utils.CustomPair
import com.appforlife.filemanager.utils.convert
import com.appforlife.filemanager.utils.jsonToMap
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseAppLifecycleListener
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : BaseApplication(), FirebaseAppLifecycleListener {

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var billingClientManager: BillingClientManager

    @Inject
    lateinit var quotaLimitManager: QuotaLimitManager

    override fun allowLoggingEventTracking(): Boolean = BuildConfig.LOG_EVENT_TRACKING

    override fun getBillingManager(): BillingClientManager = billingClientManager

    override fun getRemoteConfigList(): List<CustomPair<String, Any>> {
        return listOf(
            RemoteConfigValues.STORE_CONFIG,
            RemoteConfigValues.IAP_ITEM_CONFIG,
            RemoteConfigValues.ONBOARD_STORE_CONFIG,
            RemoteConfigValues.USER_SEGMENT_NAME,
            RemoteConfigValues.ADS_BANNER_ID,
            RemoteConfigValues.ADS_APP_OPEN_ID,
            RemoteConfigValues.ADS_INTERSTITIAL_ID,
            RemoteConfigValues.ADS_NATIVE_ID,
            RemoteConfigValues.INTERSTITIAL_THRESHOLD,
            RemoteConfigValues.IS_SHOW_ONBOARD,
            RemoteConfigValues.IS_SHOW_CLOSE_BTN_ONBOARD,
            RemoteConfigValues.DELAY_SHOW_CLOSE_DS_OB,
            RemoteConfigValues.DIRECT_STORE_BENEFIT_LIST,
            RemoteConfigValues.SUBSCRIPTION_TERMS,
            RemoteConfigValues.IS_SHOW_DS_AFTER_CONNECT_DEVICE,
            RemoteConfigValues.ENABLE_DS_FIRST,
            RemoteConfigValues.DAILY_QUOTA_LIMIT,
            RemoteConfigValues.COUNT_CONTROL_UNTIL_REVIEW,
            RemoteConfigValues.DELAY_TIME_TO_SHOW_LIST_DEVICES,
        )
    }

    override fun applyTestVersion(): Boolean = BuildConfig.APPLY_TEST_VERSION

    override fun getIapItemJsonConfig(): String =
        RemoteConfigValues.IAP_ITEM_CONFIG.second.convert()

    override fun getDirectStoreJsonConfig(): String =
        RemoteConfigValues.STORE_CONFIG.second.convert()


    override fun remoteConfigFetched(
        loadFromPreviousVersion: Boolean,
        configUpdated: Boolean,
        fetchSuccess: Boolean
    ) {
        if (fetchSuccess || loadFromPreviousVersion) {
            adsManager.interstitialUnitId = RemoteConfigValues.ADS_INTERSTITIAL_ID.second.convert()
            adsManager.bannerId = RemoteConfigValues.ADS_BANNER_ID.second.convert()
            adsManager.nativeAdsId = RemoteConfigValues.ADS_NATIVE_ID.second.convert()
            adsManager.openAppAdsId = RemoteConfigValues.ADS_APP_OPEN_ID.second.convert()
        }

        adsManager.updateInterstitialAdsThresholdWithFirstInitUsage(RemoteConfigValues.getInterstitialAdsThresholdWithFirstInitUsage())
        adsManager.init()

        RemoteConfigValues.DAILY_QUOTA_LIMIT.second
            .convert<String>()
            .jsonToMap()
            .apply {
                map { it.key to it.value.asInt }.toMap().entries.forEach {
                    quotaLimitManager.updateLimit(
                        it.key,
                        it.value
                    )
                }
            }

        billingClientManager.updateSkuList(
            StoreConfigItem.getStoreConfigList(RemoteConfigValues.STORE_CONFIG.second.toString())
                .flatMap { it.items }.distinct()
        )
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.getInstance().addLifecycleEventListener(this)
    }

    override fun onDeleted(firebaseAppName: String?, options: FirebaseOptions?) {

    }

}