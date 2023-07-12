package com.appforlife.filemanager.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appforlife.filemanager.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnboardingTwoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboarding_two, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = OnboardingTwoFragment()
    }
}