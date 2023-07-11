package com.appforlife.filemanager.management

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.size
import com.appforlife.filemanager.data.*
import com.appforlife.filemanager.data.base.AdBannerClick
import com.appforlife.filemanager.data.base.AdBannerImpression
import com.appforlife.filemanager.data.base.AdInterstitialImpression
import com.appforlife.filemanager.data.base.AdNativeClick
import com.appforlife.filemanager.data.base.AdNativeImpression
import com.appforlife.filemanager.data.base.InterAdsConfig
import com.appforlife.filemanager.data.base.RewardedAdsEarnedMoney
import com.appforlife.filemanager.utils.handleException
import com.appforlife.filemanager.utils.showLog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


open class AdsManager constructor(
    val context: Context,
    var adsInterstitialAdThreshold: MutableMap<String, Int> = mutableMapOf(),
    var isShowAds: Boolean,
    val isTesting: Boolean,
    var interstitialUnitId: String,
    var bannerId: String,
    var rewardAdsId: String? = null,
    var openAppAdsId: String? = null,
    var isSingleInterAdMode: Boolean = true,
    var nativeAdsId: String? = null
) {

    companion object {
        private const val TEST_REWARD_ADS_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val TEST_BANNER_ADS_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ADS_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_OPEN_ADS_ID = "ca-app-pub-3940256099942544/3419835294"
        private const val TEST_NATIVE_ADS_ID = "ca-app-pub-3940256099942544/2247696110"
    }

    private var maxRetry = 2
    private val interstitialAdsEventCount = mutableMapOf<String, Int>()
    private val listOfInterAdsConfig = mutableListOf<InterAdsConfig>()
    private val listOfInterAds = mutableListOf<InterstitialAd?>()
    private val listOfInterAdsCurrentThresholdIndex = mutableMapOf<String, Int>()

    protected var mInterstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var appOpenAd: AppOpenAd? = null

    protected var interstitialAdCallback: InterstitialAdCallback? = null
    private lateinit var loadCallback: AppOpenAd.AppOpenAdLoadCallback

    private var isShowingAd: Boolean = false
    private var loadTime: Long = 0


    fun updateInterAdsThreshold(eventType: String, threshold: Int) {
        adsInterstitialAdThreshold[eventType] = threshold
        interstitialAdsEventCount[eventType] = 0
    }

    fun updateInterstitialAdsThresholdWithFirstInitUsage(map: Map<String, Pair<Int, Int>>) {
        adsInterstitialAdThreshold = map.map { it.key to it.value.second }.toMap().toMutableMap()
        interstitialAdsEventCount.clear()
        adsInterstitialAdThreshold.keys.forEach {
            interstitialAdsEventCount[it] =
                adsInterstitialAdThreshold[it]!! - (map[it]?.first ?: 0)
            "Interstitial: $it - ${interstitialAdsEventCount[it]}/${adsInterstitialAdThreshold[it]}".showLog()
        }
    }

    fun updateMultipleInterAdsConfig(config: List<InterAdsConfig>) {
        listOfInterAdsConfig.clear()
        listOfInterAds.clear()
        listOfInterAdsConfig.addAll(config)
        if (isTesting) {
            initInterstitialAdsById(TEST_INTERSTITIAL_ADS_ID)
        } else {
            listOfInterAdsConfig.map { it.adId }.forEach { adsId ->
                initInterstitialAdsById(adsId)
            }
        }
        listOfInterAdsConfig.forEach { adsConfig ->
            if (adsConfig.intervalThreshold.isNotEmpty()) {
                adsInterstitialAdThreshold[adsConfig.eventType] =
                    adsConfig.intervalThreshold.first()
                interstitialAdsEventCount[adsConfig.eventType] = 0
                listOfInterAdsCurrentThresholdIndex[adsConfig.eventType] = 0
            }
        }
    }

    fun init() {
        if (isShowAds) {
            maxRetry = 3
            MobileAds.initialize(context)
            initAdsRewards()
            initInterstitialAds()
            initOpenAppAds()
        } else {
            mInterstitialAd = null
        }
    }

    @SuppressLint("CheckResult")
    private fun initInterstitialAdsDelay(retry: Int = maxRetry) {
        Flowable.just(retry)
            .delay(if (retry < maxRetry) 10 else 500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                initInterstitialAds(retry)
            }, {
                it.fillInStackTrace()
            })
    }

    private fun initInterstitialAds(retry: Int = maxRetry) {
        if (mInterstitialAd != null) {
            return
        }

        "Init interstitial ads, retry=$retry".showLog()
        if (retry <= 0) {
            return
        }
        InterstitialAd.load(
            context,
            if (isTesting) {
                TEST_INTERSTITIAL_ADS_ID
            } else {
                interstitialUnitId
            },
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    "On InterstitialAds Failed to load".showLog()
                    handleException(
                        RuntimeException(
                            "InterstitialAds failed to load, just ignore!, mess = ${p0.message} -" +
                                    "domain = ${p0.domain} - code = ${p0.code} - responseInfo = ${p0.responseInfo?.toString()} " +
                                    "- cause = ${p0.cause?.toString()}" +
                                    "- error = $p0"
                        )
                    )
                    mInterstitialAd = null
                    initInterstitialAdsDelay(retry - 1)
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    "On InterstitialAds Loaded".showLog()
                    mInterstitialAd = p0

                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                "Failed to show interstitial ads".showLog()
                                mInterstitialAd = null
                                interstitialAdCallback = null
                                initInterstitialAdsDelay()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                "On InterstitialAds showed".showLog()
                                interstitialAdCallback?.onImpression()
                                mInterstitialAd = null
                                initInterstitialAdsDelay()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                "On InterstitialAds closed".showLog()
                                interstitialAdCallback?.onClosed()
                                interstitialAdCallback = null
                            }
                        }
                }
            }
        )
    }

    private fun initInterstitialAdsById(adsId: String, retry: Int = maxRetry) {
        "Init initInterstitialAdsById, retry=$retry, adsId=$adsId".showLog()
        if (retry <= 0) {
            return
        }
        InterstitialAd.load(
            context,
            adsId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    "On InterstitialAds Failed to load".showLog()
                    handleException(
                        RuntimeException(
                            "InterstitialAds failed to load, just ignore!, mess = ${p0.message} -" +
                                    "domain = ${p0.domain} - code = ${p0.code} - responseInfo = ${p0.responseInfo?.toString()} " +
                                    "- cause = ${p0.cause?.toString()}" +
                                    "- error = $p0 - adsId = $adsId"
                        )
                    )
                    initInterstitialAdsById(adsId, retry - 1)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    "On InterstitialAds Loaded".showLog()

                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                "Failed to show interstitial ads".showLog()
                                interstitialAdCallback = null
                                initInterstitialAdsById(adsId)
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                "On InterstitialAds showed".showLog()
                                interstitialAdCallback?.onImpression()
                                initInterstitialAdsById(adsId)
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                "On InterstitialAds closed".showLog()
                                interstitialAdCallback?.onClosed()
                                interstitialAdCallback = null
                            }
                        }
                    val index = listOfInterAds.indexOfFirst { it?.adUnitId == adsId }
                    if (index != -1) {
                        listOfInterAds[index] = interstitialAd
                    } else {
                        listOfInterAds.add(interstitialAd)
                    }
                }
            }
        )
    }

    @SuppressLint("CheckResult")
    private fun initAdsRewardsDelay(
        retry: Int = maxRetry,
        onAdsLoaded: ((Boolean) -> Unit)? = null
    ) {
        Flowable.just(retry)
            .delay(if (retry < maxRetry) 10 else 500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                initAdsRewards(retry, onAdsLoaded)
            }, {
                it.fillInStackTrace()
            })
    }

    private fun initAdsRewards(retry: Int = maxRetry, onAdsLoaded: ((Boolean) -> Unit)? = null) {
        if (retry <= 0) {
            onAdsLoaded?.invoke(false)
            return
        }

        if (rewardAdsId == null) {
            onAdsLoaded?.invoke(false)
            return
        }

        if (isTesting) {
            rewardAdsId = TEST_REWARD_ADS_ID
        }

        if (rewardedAd != null) {
            onAdsLoaded?.invoke(true)
            return
        }

        RewardedAd.load(
            context,
            rewardAdsId!!,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    rewardedAd = null
                    "On Rewarded Ads Failed to load".showLog()
                    initAdsRewardsDelay(retry - 1, onAdsLoaded)
                }

                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    "On Rewarded Ads Loaded".showLog()
                    rewardedAd = p0
                    onAdsLoaded?.invoke(true)
                    rewardedAd?.setOnPaidEventListener {
                        logEventTracking(RewardedAdsEarnedMoney(it))
                    }
                }
            })
    }

    /**
     * Method to show interstitial ad
     * @param activity the host activity
     * @param eventType type of event to check threshold
     * @param showNow if true then show it immediately, ignore threshold
     * @param interstitialAdCallback callback for ad's event
     * @param impressionExtraTrackingInfo extra tracking info
     * @return true if the ad is shown, false otherwise
     */
    fun showInterstitialAd(
        activity: Activity,
        eventType: String,
        showNow: Boolean = false,
        interstitialAdCallback: InterstitialAdCallback? = null,
        impressionExtraTrackingInfo: Map<String, String> = emptyMap(),
    ): Boolean =
        if (isSingleInterAdMode) showSingleInterstitialAd(
            activity = activity,
            eventType = eventType,
            showNow = showNow,
            interstitialAdCallback = interstitialAdCallback,
            impressionExtraTrackingInfo = impressionExtraTrackingInfo
        ) else showMultipleInterstitialAd(
            activity = activity,
            eventType = eventType,
            showNow = showNow,
            interstitialAdCallback = interstitialAdCallback,
            impressionExtraTrackingInfo = impressionExtraTrackingInfo
        )

    /**
     * Method to show interstitial ad
     * @param activity the host activity
     * @param eventType type of event to check threshold
     * @param showNow if true then show it immediately, ignore threshold
     * @param interstitialAdCallback callback for ad's event
     * @param impressionExtraTrackingInfo extra tracking info
     *
     * @return true if the ad is shown, false otherwise
     */
    private fun showSingleInterstitialAd(
        activity: Activity,
        eventType: String,
        showNow: Boolean = false,
        interstitialAdCallback: InterstitialAdCallback? = null,
        impressionExtraTrackingInfo: Map<String, String> = emptyMap()
    ): Boolean {
        if (mInterstitialAd == null || !isShowAds) {
            this.interstitialAdCallback = null
            return false
        }

        val currentEventCount = interstitialAdsEventCount[eventType]
        val threshold = adsInterstitialAdThreshold[eventType]?.takeIf { it > 0 }
        val exceedThreshold = if (currentEventCount != null && threshold != null) {
            val newEventCount = currentEventCount + 1
            interstitialAdsEventCount[eventType] = newEventCount % threshold
            newEventCount == adsInterstitialAdThreshold[eventType]
        } else {
            false
        }

        "Interstitial Ads $eventType: ${interstitialAdsEventCount[eventType]}/${adsInterstitialAdThreshold[eventType]}".showLog()

        return if (exceedThreshold || showNow) {
            // Decorator to track impression
            this.interstitialAdCallback = object : InterstitialAdCallback {
                override fun onClosed() {
                    interstitialAdCallback?.onClosed()
                }

                override fun onImpression() {
                    logEventTracking(
                        AdInterstitialImpression(
                            adsCondition = eventType,
                            extraInfo = impressionExtraTrackingInfo
                        )
                    )
                    interstitialAdCallback?.onImpression()
                }
            }
            mInterstitialAd?.show(activity)
            true
        } else {
            false
        }
    }

    /**
     * Method to show interstitial by multi ad id
     * @param activity the host activity
     * @param eventType type of event to check threshold
     * @param showNow if true then show it immediately, ignore threshold
     * @param interstitialAdCallback callback for ad's event
     * @param impressionExtraTrackingInfo extra tracking info
     *
     * @return true if the ad is shown, false otherwise
     */
    private fun showMultipleInterstitialAd(
        activity: Activity,
        eventType: String,
        showNow: Boolean = false,
        interstitialAdCallback: InterstitialAdCallback? = null,
        impressionExtraTrackingInfo: Map<String, String> = emptyMap()
    ): Boolean {

        val adsConfig = listOfInterAdsConfig.find { it.eventType == eventType }
        val interstitialAd =
            listOfInterAds.find { it?.adUnitId == adsConfig?.adId || it?.adUnitId == TEST_INTERSTITIAL_ADS_ID }
        if (interstitialAd == null || !isShowAds) {
            this.interstitialAdCallback = null
            return false
        }

        val currentEventCount = interstitialAdsEventCount[eventType]
        val threshold = adsInterstitialAdThreshold[eventType]?.takeIf { it > 0 }
        val exceedThreshold = if (currentEventCount != null && threshold != null) {
            interstitialAdsEventCount[eventType] = currentEventCount + 1
            currentEventCount == adsInterstitialAdThreshold[eventType]
        } else {
            false
        }

        "Interstitial Ads $eventType: ${currentEventCount}/${adsInterstitialAdThreshold[eventType]}".showLog()

        return if (exceedThreshold || showNow) {
            val intervalThresholdSize = adsConfig?.intervalThreshold?.size ?: 0
            val currentIntervalIndex = listOfInterAdsCurrentThresholdIndex[eventType]
            val newIntervalIndex = if (currentIntervalIndex != null) currentIntervalIndex + 1 else 0
            if (newIntervalIndex < intervalThresholdSize) {
                val newThreshold = adsConfig?.intervalThreshold?.get(newIntervalIndex) ?: 0
                updateInterAdsThreshold(eventType = eventType, threshold = newThreshold)
                listOfInterAdsCurrentThresholdIndex[eventType] = newIntervalIndex
            } else {
                interstitialAdsEventCount[eventType] = 0
            }

            // Decorator to track impression
            this.interstitialAdCallback = object : InterstitialAdCallback {
                override fun onClosed() {
                    interstitialAdCallback?.onClosed()
                }

                override fun onImpression() {
                    logEventTracking(
                        AdInterstitialImpression(
                            adsCondition = eventType,
                            extraInfo = impressionExtraTrackingInfo
                        )
                    )
                    interstitialAdCallback?.onImpression()
                }
            }
            interstitialAd.show(activity)
            true
        } else {
            false
        }
    }

    fun setupBannerAds(
        pageName: String,
        adContainer: ViewGroup,
        bannerAdCallback: BannerAdCallback? = null,
        bannerExtraTrackingInfo: BannerExtraTrackingInfo = BannerExtraTrackingInfo()
    ) {
        /* please setup ads unit id in xml */
        if (isShowAds) {
            var adView: AdView? = null
            if (adContainer.size > 0) {
                try {
                    adView = adContainer[0] as AdView
                } catch (e: Exception) {
                    handleException(e)
                }

            } else {
                adView = AdView(context)
                adView.setAdSize(calculateAdSize(adContainer))
                if (isTesting) {
                    adView.adUnitId = TEST_BANNER_ADS_ID
                } else {
                    adView.adUnitId = bannerId
                }
                adContainer.addView(adView)
            }

            val adRequest = AdRequest.Builder().build()
            adView?.loadAd(adRequest)
            bannerAdCallback?.onRequestBannerAd()

            adView?.adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (adContainer.size > 0) {
                        adContainer.removeViewAt(0)
                    }
                    bannerAdCallback?.onBannerAdLoaded(false)
                }

                override fun onAdLoaded() {
                    bannerAdCallback?.onBannerAdLoaded(true)
                }

                override fun onAdImpression() {
                    logEventTracking(
                        AdBannerImpression(
                            pageName,
                            bannerExtraTrackingInfo.impressionExtraTrackingInfo
                        )
                    )
                    bannerAdCallback?.onBannerAdImpression()
                }

                override fun onAdOpened() {
                    logEventTracking(
                        AdBannerClick(
                            pageName,
                            bannerExtraTrackingInfo.clickExtraTrackingInfo
                        )
                    )
                    bannerAdCallback?.onBannerAdOpen()
                }
            }
        } else if (adContainer.size > 0) {
            adContainer.removeViewAt(0)
        }
    }


    private fun calculateAdSize(adContainer: ViewGroup): AdSize {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val density = metrics.density
        var adWidthPixels = adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = metrics.widthPixels.toFloat()
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    fun showOpenAppAds(
        activity: Activity,
        onAdsShowing: ((Boolean) -> Unit)? = null,
        onAdsDismiss: (() -> Unit)? = null
    ): Boolean {
        if (!isShowingAd && isAdAvailable()) {
            ("Open app ads: Will show ad.").showLog()
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    this@AdsManager.appOpenAd = null
                    isShowingAd = false
                    initOpenAppAds()
                    onAdsDismiss?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onAdsShowing?.invoke(false)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    onAdsShowing?.invoke(true)
                }
            }
            appOpenAd?.show(activity)
            return true
        } else {
            ("Open app ads: Can not show ad.").showLog()
            initOpenAppAds()
            return false
        }
    }

    private fun initOpenAppAds() {
        if (isAdAvailable() || openAppAdsId == null) return

        if (isTesting) openAppAdsId = TEST_OPEN_ADS_ID

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {

            override fun onAdLoaded(ad: AppOpenAd) {
                this@AdsManager.appOpenAd = ad
                this@AdsManager.loadTime = (Date()).time
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                ("Open app ads: loadAdError = ${loadAdError.responseInfo}").showLog()
            }

        }
        val request: AdRequest = getOpenAppAdRequest()
        val adsOrientation = when (context.resources.configuration.orientation) {
            ORIENTATION_PORTRAIT -> AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT
            ORIENTATION_LANDSCAPE -> AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
            else -> AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT
        }
        try {
            AppOpenAd.load(context, openAppAdsId!!, request, adsOrientation, loadCallback)
        } catch (e: Exception) {
            handleException(e)
        }
    }


    private fun getOpenAppAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanFourHoursAgo()
    }

    /** Utility method to check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanFourHoursAgo(): Boolean {
        val dateDifference = (Date()).time - this.loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return (dateDifference < (numMilliSecondsPerHour * 4L))
    }

    fun loadNativeAd(
        pageName: String,
        adBehaviorCallback: NativeAdBehaviorCallback? = null,
        extraTrackingInfo: NativeExtraTrackingInfo = NativeExtraTrackingInfo(),
        nativeAdOptions: NativeAdOptions = NativeAdOptions.Builder().build(),
        nativeAdLoadedCallback: NativeAdLoadedCallback
    ) {
        if (!isShowAds) {
            return
        }
        val targetAdId =
            if (isTesting) {
                TEST_NATIVE_ADS_ID
            } else {
                nativeAdsId
            }

        if (targetAdId == null) {
            nativeAdLoadedCallback.onError(IllegalStateException("Native ad id is null!"))
            return
        }
        AdLoader.Builder(context, targetAdId).withAdListener(
            object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    nativeAdLoadedCallback.onError(IllegalStateException("Failed to load, errorCode=${loadAdError.code}"))
                }

                override fun onAdImpression() {
                    logEventTracking(
                        AdNativeImpression(
                            pageName = pageName,
                            extraInfo = extraTrackingInfo.impressionExtraTrackingInfo
                        )
                    )
                    adBehaviorCallback?.onImpression()
                }

                override fun onAdClicked() {
                    logEventTracking(
                        AdNativeClick(
                            pageName = pageName,
                            extraInfo = extraTrackingInfo.clickExtraTrackingInfo
                        )
                    )
                    adBehaviorCallback?.onClicked()
                }
            }
        ).withNativeAdOptions(nativeAdOptions)
            .forNativeAd {
                nativeAdLoadedCallback.onSuccess(it)
            }
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    data class NativeExtraTrackingInfo(
        val impressionExtraTrackingInfo: Map<String, String> = emptyMap(),
        val clickExtraTrackingInfo: Map<String, String> = emptyMap()
    )

    interface NativeAdBehaviorCallback {

        fun onImpression() {

        }

        fun onClicked() {

        }
    }

    interface NativeAdLoadedCallback {
        fun onSuccess(nativeAd: NativeAd)

        fun onError(exception: Exception)
    }


}

interface InterstitialAdCallback {
    fun onClosed() {

    }

    fun onImpression() {

    }
}