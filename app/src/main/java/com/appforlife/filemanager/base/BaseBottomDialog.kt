package com.appforlife.filemanager.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.appforlife.filemanager.R

abstract class BaseBottomDialog : BottomSheetDialogFragment() {

    protected var isFirstCreated = true

    protected abstract fun getLayoutResourceId(): Int

    protected abstract fun setDataBindingView(binding: ViewDataBinding?)

    protected fun showLoading() {
    }

    protected fun hideLoading() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutResourceId(), container, false)
        setDataBindingView(DataBindingUtil.bind(view))
        return view
    }

    override fun onResume() {
        super.onResume()
        if (isFirstCreated) {
            if (view != null) {
                view?.post { isFirstCreated = false }
            }
        }
    }

}