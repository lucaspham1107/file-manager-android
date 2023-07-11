/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appforlife.filemanager.management

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.querySkuDetails
import com.appforlife.filemanager.base.BaseSharePreference
import com.appforlife.filemanager.data.base.AugmentedSkuDetails
import com.appforlife.filemanager.data.base.DsItemClickEvent
import com.appforlife.filemanager.data.base.EVENT_ERROR
import com.appforlife.filemanager.data.base.EventReturnFailResultPurchase
import com.appforlife.filemanager.data.base.InAppPurchase
import com.appforlife.filemanager.data.base.PurchaseTracking
import com.appforlife.filemanager.utils.showLog
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow


open class BillingClientManager constructor(
    private val context: Context, var skusList: List<String> = listOf(),
    private var pageName: String? = null,
    private var dsName: String? = null,
    private var isAutoShow: Boolean = false,
    private var dsCondition: String? = null,
    private var connectionStatus: String? = null,
    private var extraInfo: Map<String, String> = mapOf()
) : PurchasesUpdatedListener, BillingClientStateListener, PurchasesResponseListener,
    ProductDetailsResponseListener {

    private val TAG = this::class.java.simpleName

    private var purchasedList = mutableListOf<Purchase>()
    private var skuDetailList = mutableListOf<AugmentedSkuDetails>()
    private var userClickedSku: AugmentedSkuDetails? = null

    var sharePreference = BaseSharePreference(context)
    var isAppPurchased = sharePreference.isAppPurchased
    var isAppPurchasedLiveData = MutableLiveData(isAppPurchased)
    private var isScheduler = AtomicBoolean(false)
    private var jobProcessCheckPurchase: Job? = null
    private var jobCheckResult: Job? = null

    /**
     * Service connection state event. Observe to check connection to Google Play service
     */
    val serviceConnectEvent = MutableLiveData<Boolean>()

    /**
     * Optional check before calling any purchasing tasks, if false, try to call billingClient.startConnection manually
     */
    var isServiceConnected = false
        private set
    private var retryCounter = AtomicInteger(1)
    private val baseDelayMillis = 500
    private val taskDelay = 2000L
    private val maxRetry = 3

    /**
     * Purchases are observable. This list will be updated when the Billing Library
     * detects new or existing purchases. All observers will be notified.
     */
    val purchases = MutableLiveData<List<Purchase>>()

    /**
     * SkuDetails for all known SKUs.
     */
    val skusWithSkuDetails = MutableLiveData<List<AugmentedSkuDetails>>()

    /**
     * Instantiate a new BillingClient instance.
     */
    private lateinit var billingClient: BillingClient

    private fun resetConnectionRetryPolicyCounter() {
        retryCounter.set(1)
    }

    private fun connectionRetryPolicy(block: () -> Unit) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            val counter = retryCounter.getAndIncrement()
            if (counter < maxRetry) {
                val waitTime: Long = (2f.pow(counter) * baseDelayMillis).toLong()
                delay(waitTime)
                block()
            }
        }
    }

    /**
     * All this is doing is check that billingClient is connected and if it's
     * not, request connection, wait x number of seconds and then proceed with
     * the actual task.
     */
    private fun taskExecutionRetryPolicy(
        billingClient: BillingClient,
        listener: BillingClientStateListener,
        task: () -> Unit
    ) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            if (!billingClient.isReady) {
                billingClient.startConnection(listener)
                delay(taskDelay)
            }
            task()
        }
    }

    private fun connectToPlayBillingService(): Boolean {
        Log.d(TAG, "connectToPlayBillingService")
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
            return true
        }
        return false
    }

    fun start() {
        Log.d(TAG, "ON_CREATE")
        // Create a new BillingClient in onCreate().
        // Since the BillingClient can only be used once, we need to create a new instance
        // after ending the previous connection to the Google Play Store in onDestroy().
        if (!this::billingClient.isInitialized || !billingClient.isReady) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases() // Not used for subscriptions.
                .build()
            connectToPlayBillingService()
        }
    }

    open fun stop() {
        Log.d(TAG, "ON_DESTROY")
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient can only be used once -- closing connection")
            // BillingClient can only be used once.
            // After calling endConnection(), we must create a new BillingClient.
            billingClient.endConnection()
        }
        purchasedList = mutableListOf()
        skuDetailList = mutableListOf()
        purchases.postValue(purchasedList)
        skusWithSkuDetails.postValue(skuDetailList)
        "Billing status: ${billingClient.isReady}".showLog(TAG)
    }

    fun reloadSkuList() {
        purchasedList = mutableListOf()
        skuDetailList = mutableListOf()
        purchases.postValue(purchasedList)
        skusWithSkuDetails.postValue(skuDetailList)
        queryAll()
    }

    fun queryAll() {
        querySkuDetails(BillingClient.ProductType.SUBS)
        querySkuDetails(BillingClient.ProductType.INAPP)
        queryPurchases(BillingClient.ProductType.INAPP)
        queryPurchases(BillingClient.ProductType.SUBS)
        checkPurchasedStatusUpdated()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            resetConnectionRetryPolicyCounter() //for retry policy
            // The billing client is ready. You can query purchases here.
            queryAll()
            isServiceConnected = true
            serviceConnectEvent.postValue(isServiceConnected)
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected")
        connectionRetryPolicy { connectToPlayBillingService() }
    }

    fun checkPurchasedStatusUpdated() {
        val isPurchased = this.purchasedList.isNotEmpty()
        if (isAppPurchased != isPurchased) {
            "OnPurchaseStatusUpdated: $isPurchased".showLog(TAG)
            isAppPurchased = isPurchased
            sharePreference.isAppPurchased = isAppPurchased
            isAppPurchasedLiveData.postValue(isAppPurchased)
        }
    }

    fun updateSkuList(list: List<String>) {
        if (this.skusList.size != list.size || this.skusList.find { sku -> list.find { it == sku } != null } == null) {
            this.skusList = list
            reloadSkuList()
        }
        skusList.forEach { "--sku: $it".showLog(TAG) }
    }

    /**
     * In order to make purchases, you need the [SkuDetails] for the item or subscription.
     * This is an asynchronous call that will receive a result in onSkuDetailsResponse.
     */
    fun querySkuDetails(type: String) {
        if (skusList.isEmpty())
            return
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        skusList.forEach { item ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(item)
                    .setProductType(type)
                    .build()
            )
        }
        Log.d(TAG, "querySkuDetails: $type - ${skusList.joinToString(", ")}")
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        taskExecutionRetryPolicy(billingClient, this) {
            billingClient.queryProductDetailsAsync(params, this)
        }
    }

    fun findPurchase(sku: ProductDetails): List<Purchase> {
        return findPurchase(sku.productId)
    }

    fun findPurchase(sku: String): List<Purchase> {
        return purchasedList.filter { it.products.contains(sku) }
    }

    /**
     * Receives the result from [querySkuDetails].
     *
     * Store the SkuDetails and post them in the [skusWithSkuDetails]. This allows other parts
     * of the app to use the [SkuDetails] to show SKU information and make purchases.
     */
    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        products: MutableList<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                skuDetailList =
                    products
                        .map { AugmentedSkuDetails(it, findPurchase(it).toMutableList()) }
                        .plus(skuDetailList).toMutableList()

                skusWithSkuDetails.postValue(skuDetailList.also { postedValue ->
                    Log.i(
                        TAG,
                        "onSkuDetailsResponse: code=$responseCode, msg=$debugMessage, count= ${postedValue.size}, new=${products.size}"
                    )
                })
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> {
                Log.e(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            }

            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                // These response codes are not expected.
                Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    fun queryPurchases(type: String) {
        Log.d(TAG, "queryPurchases: SUBS")
        taskExecutionRetryPolicy(billingClient, this) {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(type).build(), this
            )
        }
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        isScheduler = AtomicBoolean(false)
        jobProcessCheckPurchase?.cancel()
        jobCheckResult?.cancel()
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(
            TAG,
            "onPurchasesUpdated: purchases=${purchases?.size ?: 0}, code=$responseCode, debugMessage=$debugMessage"
        )
        purchases?.forEach { pur ->
            pur.products.forEach {
                val pricingPhase =
                    userClickedSku?.skuDetails?.subscriptionOfferDetails?.lastOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()
                val price =
                    (pricingPhase?.priceAmountMicros
                        ?: 0) / 1000000.0
                val priceCurrencyCode = pricingPhase?.priceCurrencyCode
                if (it.contains(".")) {
                    val array = it.split(".")
                    if (array.isNotEmpty()) {
                        logEventTracking(
                            PurchaseTracking(
                                array.last(),
                                pur.orderId,
                                pur.signature,
                                pur.purchaseToken,
                                price,
                                priceCurrencyCode,
                                it,
                                pur.purchaseTime
                            )
                        )
                    }
                } else {
                    logEventTracking(
                        PurchaseTracking(
                            it,
                            pur.orderId,
                            pur.signature,
                            pur.purchaseToken,
                            price,
                            priceCurrencyCode,
                            it,
                            pur.purchaseTime
                        )
                    )
                }
            }
        }
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null && purchases.isNotEmpty()) {
                    purchases.forEach { pur ->
                        pur.products.forEach {
                            val itemPurchase = if (it.contains(".")) {
                                "android.${it.split(".").last()}"
                            } else {
                                ""
                            }
                            logEventTracking(
                                InAppPurchase(
                                    pageName = pageName.orEmpty(),
                                    dsName = dsName.orEmpty(),
                                    itemName = it,
                                    itemId = it,
                                    dsCondition = dsCondition.orEmpty(),
                                    extraInfo = extraInfo,
                                    itemPurchase = itemPurchase,
                                )
                            )
                        }
                    }
                } else {
                    Log.d(TAG, "onPurchasesUpdated: null purchase list")
                    processPurchases(null)
                }
                reloadSkuList()
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.e(
                    TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                            "does not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The SKU product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
            }
        }
    }

    /**
     * Send purchase SingleLiveEvent and update purchases LiveData.
     *
     * The SingleLiveEvent will trigger network call to verify the subscriptions on the sever.
     * The LiveData will allow Google Play settings UI to update based on the latest purchase data.
     */
    private fun processPurchases(purchasesList: List<Purchase>?) {
        Log.d(TAG, "processPurchases: ${purchasesList?.size} purchase(s)")
        if (isUnchangedPurchaseList(purchasesList)) {
            Log.d(TAG, "processPurchases: Purchase list has not changed")
            return
        }
        this.purchasedList = this.purchasedList.plus(purchasesList.orEmpty()).toMutableList()
        purchases.postValue(purchasedList)
        skuDetailList.forEach { it.purchases = findPurchase(it.skuDetails).toMutableList() }
        skusWithSkuDetails.postValue(skuDetailList)
        purchasesList?.let { pur ->
            logAcknowledgementStatus(purchasesList)
            pur.filter { !it.isAcknowledged }
                .forEach {
                    acknowledgePurchase(it.purchaseToken)
                }
        }
        checkPurchasedStatusUpdated()
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */
    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>?): Boolean {
        // TODO: Optimize to avoid updates with identical data.
        return false
    }

    /**
     * Log the number of purchases that are acknowledge and not acknowledged.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * When the purchase is first received, it will not be acknowledge.
     * This application sends the purchase token to the server for registration. After the
     * purchase token is registered to an account, the Android app acknowledges the purchase token.
     * The next time the purchase list is updated, it will contain acknowledged purchases.
     */
    private fun logAcknowledgementStatus(purchasesList: List<Purchase>) {
        var ackYes = 0
        var ackNo = 0
        for (purchase in purchasesList) {
            if (purchase.isAcknowledged) {
                ackYes++
            } else {
                ackNo++
            }
        }
        Log.d(TAG, "logAcknowledgementStatus: acknowledged=$ackYes unacknowledged=$ackNo")
    }

    /**
     * Launching the billing flow.
     *
     * Launching the UI to make a purchase requires a reference to the Activity.
     */
    private fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        if (!billingClient.isReady) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }

    /**
     * Acknowledge a purchase.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * Apps should acknowledge the purchase after confirming that the purchase token
     * has been associated with a user. This app only acknowledges purchases after
     * successfully receiving the subscription data back from the server.
     *
     * Developers can choose to acknowledge purchases from a server using the
     * Google Play Developer API. The server has direct access to the user database,
     * so using the Google Play Developer API for acknowledgement might be more reliable.
     * TODO(134506821): Acknowledge purchases on the server.
     *
     * If the purchase token is not acknowledged within 3 days,
     * then Google Play will automatically refund and revoke the purchase.
     * This behavior helps ensure that users are not charged for subscriptions unless the
     * user has successfully received access to the content.
     * This eliminates a category of issues where users complain to developers
     * that they paid for something that the app is not giving to them.
     */
    fun acknowledgePurchase(purchaseToken: String) {
        Log.d(TAG, "acknowledgePurchase")
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            Log.d(TAG, "acknowledgePurchase: $responseCode $debugMessage")
        }
    }

    fun buy(app: Activity, sku: AugmentedSkuDetails, oldSku: String? = null): Boolean {
        var updateParams: BillingFlowParams.SubscriptionUpdateParams? = null
        // Only set the old SKU parameter if the old SKU is already owned.
        if (oldSku != null && oldSku != sku.skuDetails.productId && sku.skuDetails.productType == BillingClient.ProductType.SUBS) {
            findPurchase(oldSku).firstOrNull()?.let {
                updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setOldPurchaseToken(it.purchaseToken)
                    .setReplaceProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE)
                    .build()
            }
        }
        val offerToken = leastPricedOfferToken(sku.skuDetails.subscriptionOfferDetails)
        val productDetailsParamsList = ImmutableList.of(
            ProductDetailsParams.newBuilder().setProductDetails(sku.skuDetails)
                .setOfferToken(offerToken).build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingParams = if (updateParams != null) BillingFlowParams.newBuilder()
            .setSubscriptionUpdateParams(updateParams!!)
            .setProductDetailsParamsList(productDetailsParamsList)
            .build() else billingFlowParams
        launchBillingFlow(app, billingParams)
        logDsItemClickEvent(sku)
        userClickedSku = sku
        jobProcessCheckPurchase?.cancel()
        jobProcessCheckPurchase = startScheduler(sku.skuDetails.productType)
        return true
    }

    fun consume(purchase: Purchase): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            result.postValue((billingResult.responseCode == BillingClient.BillingResponseCode.OK).apply {
                if (this) {
                    purchasedList =
                        purchasedList.filter { it.purchaseToken != purchase.purchaseToken }
                            .toMutableList()
                    purchases.postValue(purchasedList)
                }
            })
        }
        return result
    }

    fun getOpenPlayStoreIntent(sku: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions?sku=$sku&package=${context.packageName}")
        )
    }

    fun setupTracking(
        pageName: String,
        dsName: String,
        isAutoShow: Boolean,
        dsCondition: String,
        connectionStatus: String,
        trackingExtraInfo: Map<String, String> = mapOf()
    ) {
        this.pageName = pageName
        this.dsName = dsName
        this.isAutoShow = isAutoShow
        this.dsCondition = dsCondition
        this.connectionStatus = connectionStatus
        this.extraInfo = trackingExtraInfo
    }

    /**
     * Events tracking
     */
    private fun logDsItemClickEvent(sku: AugmentedSkuDetails?) {
        val event = DsItemClickEvent(
            itemName = sku?.skuDetails?.title ?: EVENT_ERROR,
            dsCondition = dsCondition ?: EVENT_ERROR,
            extraInfo = extraInfo
        ).apply { this.toString().showLog(TAG) }
        logEventTracking(event)
    }

    override fun onQueryPurchasesResponse(result: BillingResult, purchases: MutableList<Purchase>) {
        result.apply {
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (isScheduler.get()) {
                        Log.d(TAG, "scheduler: schedulerProcess")
                        jobCheckResult = schedulerProcess(result, purchases)
                    } else {
                        if (purchases.isNotEmpty()) {
                            processPurchases(purchases)
                        } else
                            processPurchases(null)
                    }

                }

                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                BillingClient.BillingResponseCode.ERROR -> {
                    Log.e(TAG, "onQueryPurchasesResponse: $responseCode $debugMessage")
                }

                BillingClient.BillingResponseCode.USER_CANCELED,
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                    // These response codes are not expected.
                    Log.wtf(TAG, "onQueryPurchasesResponse: $responseCode $debugMessage")
                }
            }
        }
    }

    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>?
    ): String {
        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        if (!offerDetails.isNullOrEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros < lowestPrice) {
                        lowestPrice = price.priceAmountMicros.toInt()
                        leastPricedOffer = offer
                        offerToken = leastPricedOffer.offerToken
                    }
                }
            }
        }
        return offerToken
    }

    private fun startScheduler(type: String?): Job {
        isScheduler = AtomicBoolean(true)
        return CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(5000)
                if (isScheduler.get()) {
                    type?.let { queryPurchases(it) }
                }
            }
        }
    }

    private fun schedulerProcess(
        result: BillingResult,
        purchases: MutableList<Purchase>
    ): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            delay(5000)
            if (isActive && purchases.isNotEmpty() && isScheduler.get()) {
                Log.d(TAG, "scheduler: onPurchasesUpdated")
                logEventTracking(EventReturnFailResultPurchase())
                onPurchasesUpdated(result, purchases)
            }
        }
    }
}
