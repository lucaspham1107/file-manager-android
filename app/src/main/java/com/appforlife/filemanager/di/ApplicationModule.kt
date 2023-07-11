package com.appforlife.filemanager.di

import android.app.Application
import com.appforlife.filemanager.BuildConfig
import com.appforlife.filemanager.R
import com.appforlife.filemanager.data.base.QuotaLimitReached
import com.appforlife.filemanager.data.base.RemoteConfigValues
import com.appforlife.filemanager.data.base.StoreConfigItem
import com.appforlife.filemanager.database.AppSharePreference
import com.appforlife.filemanager.management.*
import com.appforlife.filemanager.utils.convert
import com.appforlife.filemanager.utils.showLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Singleton
    @Provides
    fun provideAdsManager(app: Application) =
        AdsManager(
            app,
            mutableMapOf(),
            BuildConfig.SHOW_ADS,
            BuildConfig.TESTING_ADS,
            interstitialUnitId = app.getString(R.string.inter_ads_id),
            bannerId = app.getString(R.string.banner_ads_id),
            nativeAdsId = app.getString(R.string.native_ads_id),
            openAppAdsId = RemoteConfigValues.ADS_APP_OPEN_ID.second.convert()
        )

    @Provides
    @Singleton
    fun provideRatingManager(app: Application) = ClientRatingManager(app)

    @Provides
    @Singleton
    fun provideBillingClient(app: Application) = BillingClientManager(
        app,
        StoreConfigItem.getStoreConfigList(RemoteConfigValues.STORE_CONFIG.second.toString())
            .flatMap { it.items }.distinct()
    )

    @Provides
    @Singleton
    fun provideQuotaManager(app: Application) =
        QuotaLimitManager(
            app, BuildConfig.QUOTA_LIMIT,
            listOf()
        ) { type ->
            "OnQuotaReach: $type".showLog()
            logEventTracking(QuotaLimitReached(type))
        }.apply {
            isQuotaLimit = BuildConfig.QUOTA_LIMIT
        }

    @Provides
    @Singleton
    fun providePreference(app: Application) = AppSharePreference(app)


}