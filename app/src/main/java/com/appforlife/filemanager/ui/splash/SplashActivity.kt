package com.appforlife.filemanager.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.appforlife.filemanager.ui.onboarding.OnBoardingActivity
import com.appforlife.filemanager.MainActivity
import com.appforlife.filemanager.R
import com.appforlife.filemanager.base.BaseActivity
import com.appforlife.filemanager.database.AppSharePreference
import com.appforlife.filemanager.databinding.ActivitySplashBinding
import com.appforlife.filemanager.management.BillingClientManager
import com.appforlife.filemanager.utils.setFullScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    @Inject
    lateinit var appSharePreference: AppSharePreference

    @Inject
    lateinit var billingClientManager: BillingClientManager

    companion object {
        const val DELAY_TIME = 2500L
    }

    override fun getLayoutId(): Int = R.layout.activity_splash

    private fun getDestinationIntent(): Intent =
        if (appSharePreference.stopShowOnboard || billingClientManager.isAppPurchased) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnBoardingActivity::class.java)
        }


    override fun setUpViews() {
        super.setUpViews()
        window.setFullScreen()
        continueAction()
    }

    private fun continueAction() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(getDestinationIntent())
            finishAffinity()
        }, DELAY_TIME)
    }
}