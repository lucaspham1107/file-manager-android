package com.appforlife.filemanager.ui.onboarding

import android.content.Intent
import com.appforlife.filemanager.MainActivity
import com.appforlife.filemanager.R
import com.appforlife.filemanager.base.BaseActivity
import com.appforlife.filemanager.databinding.ActivityOnBoardingBinding
import com.appforlife.filemanager.utils.onDebounceClickListener
import com.appforlife.filemanager.utils.setFullScreen
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
            else {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_on_boarding
}