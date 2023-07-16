package com.appforlife.filemanagerandroid.ui.ob.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_TABS = 3

class OnboardingPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingOneFragment.newInstance()
            1 -> OnboardingTwoFragment.newInstance()
            2 -> OnboardingThreeFragment.newInstance()
            else -> Fragment()
        }
    }
}