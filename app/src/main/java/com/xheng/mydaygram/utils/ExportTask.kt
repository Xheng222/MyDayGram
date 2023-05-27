package com.xheng.mydaygram.utils

import android.content.Intent
import android.net.Uri
import com.xheng.mydaygram.application.MyLitePalApplication
import com.xheng.mydaygram.model.Diary
import org.litepal.LitePal
import org.litepal.extension.find

class ExportTask {
    private val app = MyLitePalApplication.getInstance()

    // 定义导出的内容
    private lateinit var diary: String

    //导出日记
    private fun export(format: String) {
        val all = LitePal.order("year, month, day").find<Diary>()

        val builder = StringBuilder()

        if (all.isNotEmpty()) {
            for (diary in all) {
                val date = String.format(format, app.getMonth(diary.getMonth()), diary.getDay(), app.getWeek(diary.getWeek()), diary.getYear()) + "\n\n"
                builder.append(date + diary.getDiary() + "\n\n")
            }

            diary = builder.toString()
        } else {
            diary = ""
        }
    }

    fun exportTo(pattern: Int): Intent? {
        when (pattern) {
            0 -> {
                export("%s %d %s %d")
                // 创建跳转到邮箱 App 的 Intent
                val intent = Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", "", null))
                // 添加邮件主题
                intent.putExtra("android.intent.extra.SUBJECT", "My DayGrams")
                // 添加邮件内容
                intent.putExtra("android.intent.extra.TEXT", diary)

                return intent
            }

            1 -> {
                export("* %s %d %s %d *")
                // 创建跳转到邮箱 App 的 Intent
                val intent = Intent("android.intent.action.SEND")
                // 设置分享类型为文本
                intent.type = "text/plain"
                // 添加文本主题
                intent.putExtra("android.intent.extra.SUBJECT", "My DayGrams")
                // 添加文本内容
                intent.putExtra("android.intent.extra.TEXT", diary)

                return intent
            }

            else ->
                return null
        }
    }
}