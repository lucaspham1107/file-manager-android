package com.appforlife.filemanagerandroid.ui.splash

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.appforlife.filemanagerandroid.ui.ob.onboarding.OnBoardingActivity
import com.appforlife.filemanagerandroid.R
import com.appforlife.filemanagerandroid.base.BaseActivity
import com.appforlife.filemanagerandroid.databinding.ActivitySplashBinding
import com.appforlife.filemanagerandroid.extensions.setFullScreen
import com.appforlife.filemanagerandroid.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    @Inject
    lateinit var appSharePreference: com.appforlife.filemanagerandroid.helpers.Config


    companion object {
        const val DELAY_TIME = 2500L
    }

    override fun getLayoutId(): Int = R.layout.activity_splash

    private fun getDestinationIntent(): Intent =
        if (appSharePreference.stopShowOnboard) {
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