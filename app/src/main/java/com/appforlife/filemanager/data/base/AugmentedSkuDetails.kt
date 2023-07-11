package com.appforlife.filemanager.data.base

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

enum class PurchaseState {
    Purchased, Subscribed, Cancelled
}

data class AugmentedSkuDetails(
    val skuDetails: ProductDetails,
    var purchases: MutableList<Purchase>?
) {
    fun getPurchaseState(): PurchaseState? {
        return purchases?.getOrNull(0)?.let {
            if (skuDetails.productType == BillingClient.ProductType.INAPP) {
                PurchaseState.Purchased
            } else if (it.isAutoRenewing) {
                PurchaseState.Subscribed
            } else {
                PurchaseState.Cancelled
            }
        }
    }
}