package com.appforlife.filemanagerandroid.base.textview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.appforlife.filemanagerandroid.R

open class ManropeW400TextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {


    init {
        this.typeface = ResourcesCompat.getFont(context, R.font.manrope_regular)
        this.includeFontPadding = false
    }

}