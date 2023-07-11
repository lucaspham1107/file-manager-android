package com.appforlife.filemanager.utils

import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.appforlife.filemanager.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/*
fun NativeAdView.setUpWith(nativeAd: NativeAd) {
    // Set the media view.

    // Set other ad assets.
    headlineView = findViewById(R.id.nativeAdsTitle)
    bodyView = findViewById(R.id.nativeAdsContent)
    callToActionView = findViewById(R.id.nativeAdsAction)
    iconView = findViewById(R.id.nativeAdsIcon)

    // The headline and media content are guaranteed to be in every UnifiedNativeAd.
    (headlineView as TextView).text = nativeAd.headline
    nativeAd.mediaContent?.let { mediaView?.setMediaContent(it) }

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.
    bodyView?.isVisible = nativeAd.body != null
    callToActionView?.isVisible = nativeAd.callToAction != null
    iconView?.isVisible = nativeAd.icon != null
    priceView?.isVisible = nativeAd.price != null
    storeView?.isVisible = nativeAd.store != null
    advertiserView?.isVisible = nativeAd.advertiser != null
    (bodyView as TextView).text = nativeAd.body
    (callToActionView as Button).text = nativeAd.callToAction
    (iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
    setNativeAd(nativeAd)
}*/
