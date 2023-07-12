package com.appforlife.filemanager.customviews.textview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.appforlife.filemanager.R

open class ManropeW700EditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {


    init {
        this.typeface = ResourcesCompat.getFont(context, R.font.manrope_bold)
        this.includeFontPadding = false
    }

}