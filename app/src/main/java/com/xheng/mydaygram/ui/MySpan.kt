package com.xheng.mydaygram.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

// 解决 SpannableString、SpannableStringBuilder 无法使用自定义字体的问题
class Myspan(
    private val typeface: Typeface
): MetricAffectingSpan() {

    private fun switch(paint: Paint) {
        val oldTypeface = paint.typeface
        val oldStyle = oldTypeface?.style ?: 0
        val fakeStyle = oldStyle and typeface.style.inv()
        if ((fakeStyle and Typeface.BOLD) != 0) {
            paint.isFakeBoldText = true
        }
        if ((fakeStyle and Typeface.ITALIC) != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = typeface
    }

    override fun updateDrawState(p0: TextPaint?) {
        p0?.let { switch(it) }
    }

    override fun updateMeasureState(p0: TextPaint) {
        switch(p0)
    }
}