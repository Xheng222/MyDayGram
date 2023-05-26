package com.xheng.mydaygram.application

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Typeface
import org.litepal.LitePal
import org.litepal.LitePalApplication.getContext

class MyLitePalApplication: Application() {
    // 定义月份名称数组
    private val months = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )
    // 定义星期名称数组
    private val weeks = arrayOf(
        "--",
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
    )
    // 定义 App 是否需要应用锁
    private var unlock = true
    // 定义 App 是否锁定
    var isLocked = false

    // 定义字体
    private lateinit var robotoThin: Typeface
    private lateinit var georgia: Typeface
    private lateinit var arvilSans: Typeface
    private lateinit var avenirRoman: Typeface
    private lateinit var georgiaBold: Typeface
    private lateinit var robotoBold: Typeface
    private lateinit var smileySans: Typeface

    // 获取 Application 实例
    companion object{
        // 定义 Application 实例
        @JvmStatic
        private lateinit var app: MyLitePalApplication

        @JvmStatic
        fun getInstance(): MyLitePalApplication {
            return app
        }
    }

    override fun onCreate() {
        super.onCreate()

        app = this
        load()
        // 初始化数据库
        LitePal.initialize(this)
    }
    // 加载一些设置
    @SuppressLint("CommitPrefEdits")
    private fun load() {
        val settings = getSharedPreferences("settings", 0)
        val edit = settings.edit()
        val AllSettings = settings.all

        if (!AllSettings.containsKey("font.size")) {
            edit.putInt("font.size", 2)
        }
        if (!AllSettings.containsKey("preview.type")) {
            edit.putInt("preview.type", 0)
        }
        if (!AllSettings.containsKey("password")) {
            edit.putString("password", "null")
        }
        if (!AllSettings.containsKey("passwordOn")) {
            edit.putBoolean("passwordOn", false)
        }
        if (!AllSettings.containsKey("system.font.enabled")) {
            edit.putBoolean("system.font.enabled", false)
        }
        if (!AllSettings.containsKey("use.sidebar")) {
            edit.putBoolean("use.sidebar", true)
        }
        edit.apply()

        /*
            unlock属性决定 app 是否开启了密码锁功能，false 是开启， true 是关闭

            isLocked属性决定当前 app 有没有加锁，true则已经加锁，不要再添加输入密码的的 fragment 了

        */

        unlock =  ! settings.getBoolean("passwordOn", false)
        isLocked = false
        // 初始化各字体
        arvilSans = Typeface.createFromAsset(assets, "fonts/Arvil_Sans.ttf")
        avenirRoman = Typeface.createFromAsset(assets, "fonts/Avenir-Roman.otf")
        georgia = Typeface.createFromAsset(assets, "fonts/Georgia.otf")
        georgiaBold = Typeface.createFromAsset(assets, "fonts/Georgia-Bold.otf")
        robotoBold = Typeface.createFromAsset(assets, "fonts/Roboto-Bold.ttf")
        robotoThin = Typeface.createFromAsset(assets, "fonts/Roboto-Thin.ttf")
        smileySans = Typeface.createFromAsset(assets, "fonts/SmileySans-Oblique.ttf")

    }

    //  返回星期
    fun getWeek(index: Int): String = weeks[index]

    //  返回月份
    fun getMonth(index: Int): String = months[index]

    // 锁定状态
    fun isUnlock(): Boolean {
        if(unlock)
            return true
        return false
    }

    fun setUnlock(unlock: Boolean) {
        this.unlock = unlock
    }

    //将 dp 转换成 px
    fun dp_to_px(dp: Float): Int{
        val p: Float = getContext().resources.displayMetrics.density
        return  (dp * p + 0.5f).toInt()
    }

    //获取字体
    fun getAttrs(fontName: String): Typeface {
        return when (fontName) {
            "Arvil_Sans" -> arvilSans
            "Avenir-Roman" -> avenirRoman
            "Georgia" -> georgia
            "Georgia-Bold" -> georgiaBold
            "Roboto-Bold" -> robotoBold
            "Roboto-Thin" -> robotoThin
            "smileySans" -> smileySans
            else -> Typeface.DEFAULT
        }
    }

    fun getTextSize(fontSize: Int): Float {
        when(fontSize){
            0 -> return 11.3f
            1 -> return 13.3f
            2 -> return 15.3f
            3 -> return 17.3f
            4 -> return 19.3f
        }
        return 15.3f
    }
}


