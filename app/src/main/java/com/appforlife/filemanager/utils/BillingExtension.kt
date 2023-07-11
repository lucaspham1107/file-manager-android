package com.appforlife.filemanager.utils

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails

fun ProductDetails.isTrial(): Boolean {
    val isLifeTime = productType == BillingClient.ProductType.INAPP
    return if (isLifeTime) this.oneTimePurchaseOfferDetails?.priceAmountMicros == 0L else {
        this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros == 0L
    }
}

fun ProductDetails.getFormattedPrice(): String {
    val isLifeTime = productType == BillingClient.ProductType.INAPP
    return if (isLifeTime) this.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty() else {
        this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice.orEmpty()
    }
}

fun ProductDetails.getPriceCurrencyCode(): String {
    val isLifeTime = productType == BillingClient.ProductType.INAPP
    return if (isLifeTime) this.oneTimePurchaseOfferDetails?.priceCurrencyCode.orEmpty() else {
        this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.priceCurrencyCode.orEmpty()
    }
}

fun ProductDetails.getFreeTrialPeriod(): String {
    return this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull { it.priceAmountMicros == 0L }?.billingPeriod.orEmpty()
}

fun ProductDetails.getSubscriptionPeriod(): String {
    return this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull { it.priceAmountMicros != 0L }?.billingPeriod.orEmpty()
}

fun ProductDetails.getPriceAmountMicros(): Long {
    val isLifeTime = productType == BillingClient.ProductType.INAPP
    return if (isLifeTime) this.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L else {
        this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.priceAmountMicros
            ?: 0L
    }
}