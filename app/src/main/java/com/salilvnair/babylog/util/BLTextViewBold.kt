package com.salilvnair.babylog.util

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class BLTextViewBold(context: Context, attrSet: AttributeSet): AppCompatTextView(context, attrSet) {
    init {
        applyFont()
    }

    private fun applyFont() {
        val typeface: Typeface = Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
        setTypeface(typeface)
    }
}