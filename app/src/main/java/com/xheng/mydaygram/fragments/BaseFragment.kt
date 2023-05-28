package com.xheng.mydaygram.fragments

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication

open class BaseFragment: Fragment() {

    // 获取 Application 实例
    protected var app: MyLitePalApplication = MyLitePalApplication.getInstance()

    // 这个类为所有 fragment 设置进入与退出动画
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter)
            AnimationUtils.loadAnimation(activity, R.anim.activity_in)
        else
            AnimationUtils.loadAnimation(activity, R.anim.activity_out)
    }

    override fun onPause() {
//        MainScope().cancel()
        super.onPause()
    }

}