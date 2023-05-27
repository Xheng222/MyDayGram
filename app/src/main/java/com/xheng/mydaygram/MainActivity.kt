package com.xheng.mydaygram

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.xheng.mydaygram.application.MyLitePalApplication
import com.xheng.mydaygram.fragments.*


class MainActivity : FragmentActivity() {

    // app 实例
    private val app = MyLitePalApplication.getInstance()

    private val DiaryFragment = DiaryFragment()
    private val MainFragment = MainFragment()
    private val SearchFragment = SearchFragment()
    private val SettingFragment = SettingFragment()
    private val PasswordFragment = PasswordFragment()


    @SuppressLint("CommitTransaction")
    fun switchFragment (tag: String, message: Bundle?) {
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        if (fragment != null) {
            transaction.remove(fragment)
        }

        fragment = when (tag) {
                "DiaryFragment" -> DiaryFragment
                "SearchFragment" -> SearchFragment
                "SettingFragment" -> SettingFragment
                "PasswordFragment" -> PasswordFragment
                "MainFragment" -> MainFragment
                else -> {null}
        }
        message.apply { fragment?.arguments = message }
        if (fragment != null) {
            if (tag == "PasswordFragment")
                supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragmentContainerView, fragment, tag)
                    .commit()
            else
                supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragmentContainerView, fragment, tag)
                    .commit()
        }
    }

    fun pop() {
        supportFragmentManager.popBackStack()
    }

    private fun locked() {
        val password = PasswordFragment()
        val bundle = Bundle()
        bundle.putInt("mode", 1)
        bundle.apply { password.arguments = bundle }
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainerView, password, "PasswordFragment1")
            .commit()
    }

    // 在onResume阶段检查 app 是否应该使用应用锁
    override fun onResume() {
        super.onResume()
        // 获取解锁状态
        val unlock = app.isUnlock()
        // 需要解锁
        if (!unlock) {
            // 检查app有没有加锁,不要重复上锁
            if (!app.isLocked) {
                locked()
                app.isLocked = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app.isLocked = false

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainerView, MainFragment, "MainFragment")
            .commit()

    }

    /*
        处理按键的按下的事件
        当触发事件的按键是返回键时
        获取当前回退栈中的Fragment个数，然后弹出顶部的fragment
    */
    @SuppressLint("SuspiciousIndentation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && supportFragmentManager.backStackEntryCount >= 1) {
            // app 上锁情况下，直接返回 true （即禁用了返回键功能）
            if (app.isLocked)
                    return true

            if (supportFragmentManager.backStackEntryCount == 1) {
                //退出程序
                moveTaskToBack(true)
                return true
            }
            supportFragmentManager.popBackStackImmediate()
            return true
        }
        else
            return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        // 阻止activity保存 fragment 的状态
        //super.onSaveInstanceState(outState)

    }

//    fun freshMainFragment() {
//        MainFragment.fresh()
//    }
//
//    fun freshSearch() {
//        SearchFragment.fresh()
//    }
//
//    fun freshSet() {
//        SettingFragment.loadSettings()
//    }

//    override fun onPause() {
//
//
//        super.onPause()
//        Log.e("e", "a")
//    }
}