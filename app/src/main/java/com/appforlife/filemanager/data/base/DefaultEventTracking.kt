package com.appforlife.filemanager.data.base

import com.appforlife.filemanager.management.EventInfo
import com.google.android.gms.ads.AdValue

class FirstOpenTracking : EventInfo("moniq_tap_first_open", mapOf("FA_conversion" to "1"))

class QuotaLimitReached(type: String) : EventInfo("quota_limit_reach", mapOf("type" to type))


const val EVENT_ERROR = "error_event"

//Keys
private const val PAGE_NAME = "page_name"
private const val DS_NAME = "ds_name"
private const val CONNECTION_STATUS = "connection_status"
private const val ITEM_NAME = "item_name"
private const val ITEM_ID = "item_id"
private const val DS_CONDITION = "ds_condition"
private const val ADS_CONDITION = "ads_condition"
private const val ITEM_PURCHASE = "item_purchase"

data class DsItemClickEvent(
    val itemName: String,
    val dsCondition: String,
    val extraInfo: Map<String, String>
) :
    EventInfo(
        "ds_item_click",
        mapOf(
            ITEM_NAME to itemName,
            DS_CONDITION to dsCondition
        ) + extraInfo
    )

data class RatingRequestEvent(
    val pageName: String,
    val flow: String,
    val connectionStatus: String,
    val extraInfo: Map<String, String>
) : EventInfo(
    "rating_request",
    mapOf(
        PAGE_NAME to pageName,
        "FLOW" to flow,
        CONNECTION_STATUS to connectionStatus
    ) + extraInfo
)


class RewardedAdsEarnedMoney(adValue: AdValue) : EventInfo(
    "rewardedAds_Earned_Money", mapOf(
        "currencyCode" to adValue.currencyCode,
        "precisionType" to adValue.precisionType.toString(),
        "valueMicros" to adValue.valueMicros.toString()
    )
)

data class AdBannerImpression(
    val pageName: String,
    val extraInfo: Map<String, String>
) : EventInfo("ad_banner_impression", mapOf(PAGE_NAME to pageName) + extraInfo)

data class AdBannerClick(
    val pageName: String,
    val extraInfo: Map<String, String>
) : EventInfo("ad_banner_click", mapOf(PAGE_NAME to pageName) + extraInfo)


data class AdInterstitialImpression(
    val adsCondition: String,
    val extraInfo: Map<String, String>
) : EventInfo("ad_inter_impression", mapOf(ADS_CONDITION to adsCondition) + extraInfo)

data class AdNativeClick(
    val pageName: String,
    val extraInfo: Map<String, String>
) : EventInfo("ad_native_click", mapOf(PAGE_NAME to pageName) + extraInfo)

data class AdNativeImpression(
    val pageName: String,
    val extraInfo: Map<String, String>
) : EventInfo("ad_native_impression", mapOf(PAGE_NAME to pageName) + extraInfo)

data class InAppPurchase(
    val pageName: String,
    val dsName: String,
    val itemName: String,
    val itemId: String,
    val dsCondition: String,
    val extraInfo: Map<String, String>,
    val itemPurchase: String,
) : EventInfo(
    "in_app_purchase", mapOf(
        PAGE_NAME to pageName,
        DS_NAME to dsName,
        ITEM_NAME to itemName,
        ITEM_ID to itemId,
        DS_CONDITION to dsCondition,
        ITEM_PURCHASE to itemPurchase,
    ) + extraInfo
)

data class PurchaseTracking(
    val productId: String,
    var orderIdPurchase: String? = null,
    var signaturePurchase: String? = null,
    var purchaseTokenPurchase: String? = null,
    var pricePurchase: Double? = null,
    var currencyPurchase: String? = null,
    var skuPurchase: String? = null,
    var purchaseTimePurchase: Long? = null,
) :
    EventInfo(
        "purchase_$productId",
        mapOf("buy" to "buy"),
        orderIdPurchase,
        signaturePurchase,
        purchaseTokenPurchase,
        pricePurchase,
        currencyPurchase,
        skuPurchase,
        purchaseTimePurchase
    )

class RemoteConfigLoaded(isRemoteConfigLoaded: String) : EventInfo(
    "remote_config_load", mapOf("is_remote_config_loaded" to isRemoteConfigLoaded)
)

val DEFAULT_FORCE_UPDATE =
    "{\"isShowPopup\":false,\"isForceUpdate\":false,\"version\":1,\"url\":\"https://play.google.com/\"}"

class EventReturnFailResultPurchase : EventInfo(
    "return_fail_result_purchase", mapOf()
)