package com.appforlife.filemanager.base

import com.appforlife.filemanager.data.base.SkuInfo

abstract class BaseDirectStoreAdapter<R : BaseRecycleViewHolder>(
    items: MutableList<SkuInfo> = mutableListOf(),
    var isTestingMode: Boolean = false
) :
    BaseRecycleAdapter<SkuInfo, R>(items) {

    var onConsumeClick: ((position: Int, item: SkuInfo) -> Unit)? = null

    override fun onBindView(holder: R, position: Int, item: SkuInfo) {

        onBindView(
            holder, position, item,
            item.formattedPrice,
            item.getSubscriptionPeriod(),
            item.displayName,
            item.moreDescription.orEmpty(),
            item.isPromoted,
            if (item.isTrial && item.trialPeriod.isNotEmpty()) item.trialPeriod else null,
            !item.sku.purchases.isNullOrEmpty(),
            !item.isConsumable,
            isDisabled,
            isTestingMode
        )
    }

    abstract fun onBindView(
        holder: R,
        position: Int,
        item: SkuInfo,
        price: String,
        subscriptionPeriod: String,
        displayName: String,
        description: String,
        isPromoted: Boolean,
        trialDuration: String?,
        isPurchased: Boolean,
        isSubscriptionSkuType: Boolean,
        isDisabled: Boolean,
        isTestingMode: Boolean
    )
}