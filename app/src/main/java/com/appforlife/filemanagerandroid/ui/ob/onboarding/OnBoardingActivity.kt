package com.appforlife.filemanagerandroid.ui.ob.onboarding

import android.content.Intent
import com.appforlife.filemanagerandroid.R
import com.appforlife.filemanagerandroid.base.BaseActivity
import com.appforlife.filemanagerandroid.databinding.ActivityOnBoardingBinding
import com.appforlife.filemanagerandroid.extensions.onDebounceClickListener
import com.appforlife.filemanagerandroid.extensions.setFullScreen
import com.appforlife.filemanagerandroid.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {
    override fun setUpViews() {
        super.setUpViews()
        window.setFullScreen()
        binding.vp.apply {
            adapter = OnboardingPagerAdapter(fragmentManager = supportFragmentManager, lifecycle)
            binding.circleIndicator.setViewPager2(this)
            offscreenPageLimit = 3
        }
        binding.txtContinue.onDebounceClickListener {
            val currentPage = binding.vp.currentItem
            if (currentPage < 2) binding.vp.setCurrentItem(currentPage + 1, true)
            else startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_on_boarding
}