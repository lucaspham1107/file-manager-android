package com.appforlife.filemanager.data.base

import androidx.annotation.Keep
import com.android.billingclient.api.BillingClient
import com.appforlife.filemanager.utils.*

@Keep
class SkuInfo(
    val sku: AugmentedSkuDetails,
    val iapItem: IAPItem?
) {
    var displayName: String = ""
    var trialPeriod: String = ""
    var icon: Int = 0
    val formattedPrice =
        sku.skuDetails.getFormattedPrice()
    var isTrial = sku.skuDetails.isTrial()
    var moreDescription: String? = null
    var isConsumable = false
    var isPromoted = false

    init {
        if (iapItem == null) {
            handleException(RuntimeException("Missing IAP_ITEM_CONFIG: ${sku.skuDetails.productId}"))
        }
        trialPeriod = sku.skuDetails.getFreeTrialPeriod()
        displayName = iapItem?.title.orEmpty()
        isConsumable = sku.skuDetails.productType == BillingClient.ProductType.INAPP
        moreDescription = iapItem?.parseFormat(sku.skuDetails.getFormattedPrice())
        isPromoted = iapItem?.isPromoted ?: false
    }

    fun getSubscriptionPeriod(): String {
        return sku.skuDetails.getSubscriptionPeriod()
    }
}