package com.appforlife.filemanager.data

import com.moniqtap.source.data.SkuInfo
import com.moniqtap.source.utils.*

import java.util.*

object RemoteConfigValues {
    val USER_SEGMENT_NAME =
        CustomPair<String, Any>("user_segment_name", "")

    val ADS_BANNER_ID =
        CustomPair<String, Any>("ads_banner_id", "ca-app-pub-6569277087635149/2905837423")

    val ADS_APP_OPEN_ID =
        CustomPair<String, Any>("ads_app_open_id", "ca-app-pub-6569277087635149/8301634332")

    val ADS_INTERSTITIAL_ID =
        CustomPair<String, Any>("ads_interstitial_id", "ca-app-pub-6569277087635149/7966592412")

    val ADS_NATIVE_ID =
        CustomPair<String, Any>("ads_native_id", "ca-app-pub-6569277087635149/1045960848")


    val STORE_CONFIG = CustomPair<String, Any>(
        "store_configs",
        "[\n" +
                "  {\n" +
                "    \"name\": \"DirectStore1\",\n" +
                "    \"trackingName\": \"DirectStoreVC\",\n" +
                "    \"items\": [\n" +
                "      \"weekly\",\n" +
                "      \"monthly\",\n" +
                "      \"lifetime\"\n" +
                "    ],\n" +
                "    \"type\": \"direct\",\n" +
                "    \"enabled\": true\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"Store\",\n" +
                "    \"trackingName\": \"StoreVC\",\n" +
                "    \"items\": [\n" +
                "      \"weekly\",\n" +
                "      \"monthly\",\n" +
                "      \"lifetime\"\n" +
                "    ],\n" +
                "    \"type\": \"store\",\n" +
                "    \"enabled\": true\n" +
                "  }\n" +
                "]"
    )
    val IAP_ITEM_CONFIG = CustomPair<String, Any>(
        "iap_item_configs",
        "[\n" +
                "  {\n" +
                "    \"item\": \"weekly\",\n" +
                "    \"title\": \"Weekly\",\n" +
                "    \"format\": \"3 Days Free Trial then %@\",\n" +
                "    \"type\": \"trial\",\n" +
                "    \"isPromoted\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"item\": \"monthly\",\n" +
                "    \"title\": \"Monthly\",\n" +
                "    \"format\": \"Auto-renewing\",\n" +
                "    \"type\": \"normal\",\n" +
                "    \"isPromoted\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"item\": \"lifetime\",\n" +
                "    \"title\": \"Lifetime\",\n" +
                "    \"format\": \"One-time Payment\",\n" +
                "    \"type\": \"normal\",\n" +
                "    \"isPromoted\": true\n" +
                "  },\n" +
                "  {\n" +
                "    \"item\": \"weekly2\",\n" +
                "    \"title\": \"Weekly\",\n" +
                "    \"format\": \"3 Days Free Trial then %@\",\n" +
                "    \"type\": \"trial\",\n" +
                "    \"isPromoted\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"item\": \"monthly2\",\n" +
                "    \"title\": \"Monthly\",\n" +
                "    \"format\": \"Auto-renewing\",\n" +
                "    \"type\": \"normal\",\n" +
                "    \"isPromoted\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"item\": \"lifetime2\",\n" +
                "    \"title\": \"LIFETIME\",\n" +
                "    \"format\": \"One-time Payment\",\n" +
                "    \"type\": \"normal\",\n" +
                "    \"isPromoted\": true\n" +
                "  }\n" +
                "]"
    )


    val IS_SHOW_DS_AFTER_CONNECT_DEVICE = CustomPair<String, Any>(
        "is_show_ds_after_connect_device",
        true
    )

    val ENABLE_DS_FIRST = CustomPair<String, Any>(
        "enable_ds_first",
        true
    )

    val INTERSTITIAL_THRESHOLD = CustomPair<String, Any>(
        "interstitial_threshold",
        "{\n" +
                "  \"screen_switch\": {\n" +
                "    \"first\": 7,\n" +
                "    \"after\": 8\n" +
                "  },\n" +
                "  \"remote_control\": {\n" +
                "    \"first\": 19,\n" +
                "    \"after\": 20\n" +
                "  },\n" +
                "  \"app\": {\n" +
                "    \"first\": 1,\n" +
                "    \"after\": 2\n" +
                "  },\n" +
                "  \"mira_cast\": {\n" +
                "    \"first\": 1,\n" +
                "    \"after\": 1\n" +
                "  }\n" +
                "}"
    )

    fun getInterstitialAdsThresholdWithFirstInitUsage(): Map<String, Pair<Int, Int>> {
        return try {
            INTERSTITIAL_THRESHOLD.second
                .convert<String>()
                .jsonToMap()
                .map { threshold ->
                    threshold.key to threshold.value.asJsonObject
                        .let { Pair(it.get("first")?.asInt ?: 0, it.get("after")?.asInt ?: 0) }
                }.toMap()
        } catch (e: Exception) {
            handleException(e)
            mutableMapOf()
        }
    }

    val ONBOARD_STORE_CONFIG = CustomPair<String, Any>(
        "onboard_storeConfigs",
        "[\n" +
                "  {\n" +
                "    \"name\": \"DirectStoreOnboard1\",\n" +
                "    \"trackingName\": \"DirectStoreVC\",\n" +
                "    \"items\": [\n" +
                "      \"lifetime\",\n" +
                "      \"monthly\",\n" +
                "      \"weekly\"\n" +
                "    ],\n" +
                "    \"type\": \"direct\",\n" +
                "    \"enabled\": true\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"Store\",\n" +
                "    \"trackingName\": \"StoreVC\",\n" +
                "    \"items\": [\n" +
                "      \"weekly\",\n" +
                "      \"monthly\",\n" +
                "      \"lifetime\"\n" +
                "    ],\n" +
                "    \"type\": \"store\",\n" +
                "    \"enabled\": true\n" +
                "  }\n" +
                "]"
    )

    val SUBSCRIPTION_TERMS =
        CustomPair<String, Any>(
            "subscription_terms",
            "[\"-  Subscribed users have unlimited use of the Remote and access to all of its Premium features, without any ads.\",\"-  Non-subscribed users can continuously use the app with advertisements, and have a limited daily quota for use of Premium features.\",\"-  Users can subscribe to %{subCount} different plans: %{subList} auto-renewing subscriptions.\",\"-  Alternatively, users can purchase the full app (%{lifetimeName}) for a one-time payment of (%{lifetimePrice}). All updates and new features are received.\",\"-  Payment will be charged to your Google Account at confirmation of purchase.\",\"-  Subscriptions automatically renew unless auto-renew is disabled at least 24 hours before the end of the current period.\",\"-  Account will be charged for renewal within 24-hour prior to the end of the current period, and identify the cost of renewal.\",\"-  Any unused portion of a free trial period, if offered, will be forfeited when the user purchases a subscription to that publication, where applicable.\",\"-  Subscriptions may be managed by the user and auto-renewal may be turned off by going to the user’s Account Settings after purchase. Note that uninstalling the app will not cancel your subscription.\",\"    1. On your Android phone or tablet, open the Google Play Store .\",\"    2. Check if you’re signed in to the correct Google Account.\",\"    3. Tap Menu Subscriptions.\",\"    4. Select the subscription you want to cancel.\",\"    5. Tap Cancel subscription.\",\"    6. Follow the instructions.\"]"
        )

    val DIRECT_STORE_BENEFIT_LIST = CustomPair<String, Any>(
        "direct_store_benefit_list",
        "[\n" +
                "  \"Screen Mirroring Mastery\",\n" +
                "  \"Ad-Free &  Auto Updates\",\n" +
                "  \"Endless Media Casting\",\n" +
                "  \"Instant App Access\"\n" +
                "]"
    )


    val IS_SHOW_ONBOARD = CustomPair<String, Any>(
        "is_show_onboard",
        true
    )

    val IS_SHOW_CLOSE_BTN_ONBOARD = CustomPair<String, Any>(
        "is_show_close_btn_onboard",
        false
    )

    val DELAY_SHOW_CLOSE_DS_OB = CustomPair<String, Any>(
        "delay_time_show_close_btn_ds",
        3000L
    )

    val DAILY_QUOTA_LIMIT = CustomPair<String, Any>(
        "daily_quota_limit",
        "{\n" +
                "  \"quota_mira_cast\": 3,\n" +
                "  \"quota_remote_control_tap\": 200,\n" +
                "  \"quota_remote_control_touch\": 2000\n" +
                "  \"quota_cast_media\": 3\n" +
                "}"
    )


    val COUNT_CONTROL_UNTIL_REVIEW = CustomPair<String, Any>(
        "count_control_until_popup_review",
        30
    )

    val DELAY_TIME_TO_SHOW_LIST_DEVICES = CustomPair<String, Any>(
        "delay_time_to_show_list_devices",
        5000
    )

    fun getDSBenefitList(): List<String> {
        return try {
            DIRECT_STORE_BENEFIT_LIST.second.convert<String>().toJsonArray()
                .toList { it.asString }
        } catch (e: Exception) {
            handleException(e)
            listOf()
        }
    }

    fun getTermText(subList: List<SkuInfo>, lifetime: SkuInfo?): String {
        return try {
            SUBSCRIPTION_TERMS.second.convert<String>()
                .toJsonArray()
                .toList {
                    it.asString
                }
                .filter {
                    !it.uppercase(Locale.ROOT)
                        .contains("%{subCount}".uppercase(Locale.ROOT)) || subList.isNotEmpty()
                }
                .filter {
                    !it.uppercase(Locale.ROOT)
                        .contains("%{lifetimePrice}".uppercase(Locale.ROOT)) || lifetime != null
                }
                .joinToString("\n")
                .replace("%{subCount}", subList.size.toString())
                .replace(
                    "%{subList}",
                    subList.joinToString(", ") { "${it.displayName} (${it.sku.skuDetails.getPriceCurrencyCode()} ${it.sku.skuDetails.getFormattedPrice()})" }
                )
                .replace("%{lifetimeName}", lifetime?.displayName.orEmpty())
                .replace(
                    "%{lifetimePrice}",
                    "${
                        lifetime?.sku?.skuDetails?.getPriceCurrencyCode().orEmpty()
                    } ${lifetime?.sku?.skuDetails?.getFormattedPrice().orEmpty()}"
                )
        } catch (e: Exception) {
            handleException(e)
            ""
        }
    }
}