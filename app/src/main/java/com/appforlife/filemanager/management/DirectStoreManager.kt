package com.appforlife.filemanager.management

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.appforlife.filemanager.base.BaseActivity
import com.appforlife.filemanager.base.BaseApplication
import com.appforlife.filemanager.base.BaseDirectStoreAdapter
import com.appforlife.filemanager.data.base.IAPItem
import com.appforlife.filemanager.data.base.SkuInfo
import com.appforlife.filemanager.data.base.StoreConfigItem
import com.appforlife.filemanager.utils.NetworkUtil
import com.appforlife.filemanager.utils.showLog

abstract class DirectStoreManager {
    constructor(
        activity: BaseActivity<*>,
        billingClientManager: BillingClientManager,
        directStoreAdapter: BaseDirectStoreAdapter<*>,
        isTestingDirectStore: Boolean,
        isDirectStoreOrStore: Boolean,
        recyclerView: RecyclerView?,
        pageName: String,
        dsName: String,
        isAutoShow: Boolean,
        dsCondition: String,
        trackingExtraInfo: Map<String, String> = mapOf(),
    ) {
        lifecycleOwner = activity
        this.activity = activity
        this.billingClientManager = billingClientManager
        this.directStoreAdapter = directStoreAdapter
        this.isTestingDirectStore = isTestingDirectStore
        this.isDirectStoreOrStore = isDirectStoreOrStore
        this.directStoreJsonConfig = BaseApplication.instance.getDirectStoreJsonConfig()
        this.iapItemJsonConfig = BaseApplication.instance.getIapItemJsonConfig()
        this.recyclerView = recyclerView
        this.pageName = pageName
        this.dsName = dsName
        this.isAutoShow = isAutoShow
        this.dsCondition = dsCondition
        this.connectionStatus = NetworkUtil.getConnectionStatus(activity)
        this.trackingExtraInfo = trackingExtraInfo
        this.iapList = IAPItem.getConfigList(iapItemJsonConfig)
    }


    constructor(
        activity: BaseActivity<*>,
        billingClientManager: BillingClientManager,
        directStoreAdapter: BaseDirectStoreAdapter<*>,
        isTestingDirectStore: Boolean,
        dsType: String,
        recyclerView: RecyclerView?,
        pageName: String,
        dsName: String,
        isAutoShow: Boolean,
        dsCondition: String,
        trackingExtraInfo: Map<String, String> = mapOf(),
    ) {
        lifecycleOwner = activity
        this.activity = activity
        this.billingClientManager = billingClientManager
        this.directStoreAdapter = directStoreAdapter
        this.isTestingDirectStore = isTestingDirectStore
        this.directStoreJsonConfig = BaseApplication.instance.getDirectStoreJsonConfig()
        this.iapItemJsonConfig = BaseApplication.instance.getIapItemJsonConfig()
        this.recyclerView = recyclerView
        this.pageName = pageName
        this.dsName = dsName
        this.isAutoShow = isAutoShow
        this.dsCondition = dsCondition
        this.connectionStatus = NetworkUtil.getConnectionStatus(activity)
        this.trackingExtraInfo = trackingExtraInfo
        this.iapList = IAPItem.getConfigList(iapItemJsonConfig)
        this.isDirectStoreOrStore = dsType == StoreConfigItem.TYPE_DIRECT_STORE
        this.dsType = dsType
    }

    private val activity: BaseActivity<*>
    private val billingClientManager: BillingClientManager
    private val directStoreAdapter: BaseDirectStoreAdapter<*>
    private val isTestingDirectStore: Boolean
    private val lifecycleOwner: LifecycleOwner
    private val isDirectStoreOrStore: Boolean
    private val directStoreJsonConfig: String
    private val iapItemJsonConfig: String
    private val recyclerView: RecyclerView?
    private val pageName: String
    private val dsName: String
    private val isAutoShow: Boolean
    private val dsCondition: String
    private val connectionStatus: String
    private val trackingExtraInfo: Map<String, String>
    private val iapList: List<IAPItem>
    private var dsType: String = StoreConfigItem.TYPE_DIRECT_STORE

    fun start(): DirectStoreManager {
        billingClientManager.purchases.observe(lifecycleOwner) {
            directStoreAdapter.apply {
                isDisabled =
                    it.find { purchase -> iapList.find { iap -> purchase.products.find { sku -> iap.item == sku } != null && iap.isLifetime() } != null } != null
                notifyDataSetChanged()
            }
            onPurchaseUpdated(it)
        }
        billingClientManager.skusWithSkuDetails.observe(lifecycleOwner) {
            val directStoreConfig =
                if (dsType == StoreConfigItem.TYPE_DIRECT_STORE) {
                    if (isDirectStoreOrStore) {
                        StoreConfigItem.getDirectStoreConfig(directStoreJsonConfig)
                    } else {
                        StoreConfigItem.getStoreConfig(directStoreJsonConfig)
                    }
                } else {
                    if (dsType == StoreConfigItem.TYPE_STORE) {
                        StoreConfigItem.getStoreConfig(directStoreJsonConfig)
                    } else {
                        StoreConfigItem.getOtherStoreConfig(directStoreJsonConfig, dsType)
                    }
                }

            val a = iapList

            val fullSkuList = it.map { sku ->
                SkuInfo(
                    sku,
                    iapList.find { config -> config.item == sku.skuDetails.productId })
            }

            val showingSkuDetails = directStoreConfig?.items.orEmpty()
                .plus(billingClientManager.purchases.value.orEmpty()
                    .map { item -> if (item.products.isNotEmpty()) item.products[0] else null })
                .distinct()
                .mapNotNull { sku -> fullSkuList.find { item -> item.sku.skuDetails.productId == sku } }
                .mapNotNull { item ->
                    item.iapItem?.promotedStoreConfig =
                        item.sku.skuDetails.productId.contains(directStoreConfig?.promoted ?: "")
                    item
                }
                .toMutableList()

            showingSkuDetails.count { skuInfo -> skuInfo.sku.skuDetails.productType == BillingClient.ProductType.INAPP }
                .let { countInAppItem ->
                    if (countInAppItem > 1) {
                        val item = showingSkuDetails.find { skuInfo ->
                            skuInfo.sku.skuDetails.productType == BillingClient.ProductType.INAPP && skuInfo.sku.purchases.isNullOrEmpty()
                        }
                        item?.let { showingSkuDetails.remove(item) }
                    }
                }
            directStoreAdapter.setList(showingSkuDetails)
            onSkuListUpdate(fullSkuList, showingSkuDetails)
        }
        directStoreAdapter.apply {
            onItemClick = { _: Int, item: SkuInfo ->
                billingClientManager.setupTracking(
                    pageName,
                    dsName,
                    isAutoShow,
                    dsCondition,
                    connectionStatus,
                    trackingExtraInfo
                )
                onSkuInfoItemClick(item)
            }
            if (isTestingDirectStore) {
                onConsumeClick = { _: Int, item: SkuInfo ->
                    onConsumeClick(item)
                }
            }
        }
        recyclerView?.apply {
            val newLayoutManager =
                layoutManager ?: LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            layoutManager = newLayoutManager
            adapter = directStoreAdapter.apply { isTestingMode = isTestingDirectStore }
        }
        return this
    }

    private fun onConsumeClick(item: SkuInfo) {
        item.sku.purchases?.getOrNull(0)?.let { purchase ->
            billingClientManager.consume(purchase).observe(lifecycleOwner) {
                "consume result: $it".showLog()
                if (it) {
                    item.sku.purchases =
                        item.sku.purchases?.filter { it.purchaseToken != purchase.purchaseToken }
                            ?.toMutableList()
                    billingClientManager.reloadSkuList()
                }
            }
        }
    }

    private fun onSkuInfoItemClick(item: SkuInfo) {
        if (item.sku.purchases?.filter { it.isAutoRenewing }.isNullOrEmpty()) {
            //continue buying this item
            billingClientManager.purchases.value.orEmpty().apply {
                if (find { purchase -> iapList.find { purchase.products.find { sku -> it.item == sku } != null && it.isLifetime() } != null } == null) {
                    val skus = find { it.isAutoRenewing }?.products
                    val sku = if (skus?.isNotEmpty() == true) skus[skus.lastIndex] else null
                    billingClientManager.buy(
                        activity,
                        item.sku,
                        sku
                    )
                }
            }
        }
    }

    abstract fun onPurchaseUpdated(purchaseList: List<Purchase>)
    abstract fun onSkuListUpdate(
        fullSkuDetails: List<SkuInfo>,
        showingSkuDetails: List<SkuInfo>
    )
}