package com.xheng.mydaygram.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication

class MyTextView : AppCompatTextView {


    private fun attrsType(context: Context, attrs: AttributeSet){
        // 获取 xml 中自定义属性的集合
        val attrsType: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTextView)

        val font: String? = attrsType.getString(0)

        if(font != null){
            typeface = MyLitePalApplication.getInstance().getAttrs(font)
        }
        attrsType.recycle()
    }

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        attrsType(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, size: Int): super(context, attrs, size){
        attrsType(context, attrs)
    }
}