package com.appforlife.filemanager.customviews.textview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.appforlife.filemanager.R

open class ManropeW300TextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {


    init {
        this.typeface = ResourcesCompat.getFont(context, R.font.manrope_light)
        this.includeFontPadding = false
    }

}