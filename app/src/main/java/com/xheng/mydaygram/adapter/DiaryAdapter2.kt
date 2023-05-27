package com.xheng.mydaygram.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication
import com.xheng.mydaygram.model.Diary
import com.xheng.mydaygram.ui.Myspan

class DiaryAdapter2(
    private var context: Context,               // 定义 ListView 上下文
    private var diaries: MutableList<Diary>         // 定义子日记集合
): BaseAdapter() {

    // 获取 Application 实例
    private val app =MyLitePalApplication.getInstance()

    private class ViewHolder(view: View) {
        val diaryItem: TextView
        init {
            diaryItem = view.findViewById(R.id.item2_text)
        }
    }

    override fun getCount() = diaries.size


    override fun getItem(p0: Int) = diaries[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        // 获取子项所对应的日记
        val diary = diaries[p0]

        // 当子项缓存为空时
        if (p1 == null) {
            // 创建子项
            view = LayoutInflater.from(context).inflate(R.layout.diary_2, p2, false)
            // 创建子项持有者
            viewHolder = ViewHolder(view)
            // 绑定子项持有者
            view.tag = viewHolder
        } else {
            // 当子项缓存不为空时，复用子项缓存
            view = p1
            // 取出绑定的子项持有者
            viewHolder = view.tag as ViewHolder
        }

        // 将日记日期字符串化
        val day = diary.getDay().toString() + " "
        // 获取日记的星期
        val week = app.getWeek(diary.getWeek())
        // 整合需要预览的内容
        val preview = day + week + " / " + diary.getDiary()
        // 获取设置
        val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        // 设置字体大小
        val fontSize = settings.getInt("font.size", 2)
        viewHolder.diaryItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getTextSize(fontSize))
        // 设置字体颜色
        val color = Color.parseColor("#1F1F1F")
        viewHolder.diaryItem.setTextColor(Color.argb(237, Color.red(color), Color.green(color), Color.blue(color)))

        val isUseDefaultFont = settings.getBoolean("system.font.enabled", false)
        val fontName =  if(isUseDefaultFont) "Default" else "smileySans"
        // 创建 SpannableString 变量 style
        val style = SpannableString(preview)
        // 设置日期、星期的字体大小
        style.setSpan(RelativeSizeSpan(1.0f), 0, day.length + week.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        // 设置日期、星期的字体样式
        style.setSpan(Myspan(app.getAttrs("Georgia-Bold")), 0, day.length + week.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        // 设置日记内容的字体样式
        style.setSpan(Myspan(app.getAttrs(fontName)), day.length + week.length, preview.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        // 当日记为星期天的日记时，将星期的字体颜色设置为红色
        if (diary.getWeek() == 1) {
            style.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)), day.length, day.length + week.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        } else {
            style.setSpan(ForegroundColorSpan(color), day.length, day.length + week.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        // 应用字体样式
        viewHolder.diaryItem.text = style

        return view
    }
}