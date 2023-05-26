package com.xheng.mydaygram.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.contentValuesOf
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication
import com.xheng.mydaygram.model.Diary
import com.xheng.mydaygram.ui.MyTextView
import com.xheng.mydaygram.ui.Myspan
import java.util.*
import java.util.regex.Pattern


class SearchAdapter(
    result: MutableList<Diary>,
    val context: Context
) : BaseAdapter() {
    //自定义的 ViewHolder
    class ViewHolder() {
        lateinit var searchResult: MyTextView
        lateinit var diaryDate: MyTextView
        lateinit var count: MyTextView

        constructor(view: View) : this() {
            searchResult = view.findViewById(R.id.search_result)
            diaryDate = view.findViewById(R.id.diary_date)
            count = view.findViewById(R.id.count)
        }
    }

    val app = MyLitePalApplication.getInstance()

    // 定义搜索关键字
    private var keyWords: String? = ""


    // 定义日搜索结果集合
    private var searchResult = result
    // 定义过滤空白字符的正则表达式，Pattern.CASE_INSENSITIVE 表示忽略大小写
    private val patten = Pattern.compile("\\s+|\\n", Pattern.CASE_INSENSITIVE)

    fun setkeyWords(string: String?) {
        keyWords = string
    }

    // 统计搜索结果内容数量
    private fun searchCount(diary: String?, keywords: String?): Array<Int>? {
        // 当日记的内容为空或者关键字为空时
        if (diary == null || keywords == null) {
            return null
        }
        // 将日记内容和关键词中的所有大写字母转换成小写
        val deal_diary: String = diary.lowercase(Locale.getDefault())
        val deal_keywords: String = keywords.lowercase(Locale.getDefault())

        // 定义搜索的起始索引
        var searchIndex = 0
        // 定义相匹配的内容数量
        var searchCount = 0
        // 定义最后一个相匹配的内容的起始索引
        var lastMatchIndex = 0

        while (true){
            // 获取相匹配的内容的起始坐标
            val matchIndex: Int = deal_diary.indexOf(deal_keywords, searchIndex)

            // 当没有内容相匹配时，返回搜索结果
            if(matchIndex == -1){
                return arrayOf(lastMatchIndex, searchCount)
            }

            // 检索下一个内容
            searchIndex = deal_keywords.length + matchIndex
            searchCount++
            // 储存最后一个相匹配的内容的起始坐标
            lastMatchIndex = matchIndex
        }
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return searchResult.size
    }

    override fun getItem(p0: Int): Any {
        return searchResult[p0]
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        // 获取子项所对应的日记
        val diary = searchResult[p0]
        // 定义View
        val view: View
        // 定义ViewHolder
        val viewHolder: ViewHolder

        // 当缓存为空时,创建
        if (p1 == null) {
            // 创建子项
            view = LayoutInflater.from(p2?.context).inflate(R.layout.search_result, p2, false)
            // 创建子项持有者
            viewHolder = ViewHolder(view)
            // 绑定子项持有者
            view.tag = viewHolder
        }
        else {
            // 当子项缓存不为空时，复用子项缓存
            view = p1
            // 取出绑定的子项持有者
            viewHolder = p1.tag as ViewHolder
        }

        // 将日记内容中的所有空白类字符替换成 " "
        val deal_diary = diary.getDiary()?.let { patten.matcher(it).replaceAll(" ") }
        // 统计关键字出现的次数，当搜索结果为空时
        val result = searchCount(deal_diary, keyWords) ?: throw IllegalStateException("return null")

        // 获取最后一个相匹配的内容的起始坐标
        val lastMatchIndex: Int = result[0]
        // 定义搜索结果的起始坐标
        var searchResultStart = 0
        // 从 lastMatchIndex 的前 16 个字符开始向前搜索 " "
        val tempStart = lastMatchIndex - 16
        if (tempStart >= 0) {
            searchResultStart = tempStart
            while (searchResultStart > 0){
                if (deal_diary?.get(searchResultStart) == ' ') {
                    // 获取 " " 后面一个字符的坐标
                    searchResultStart++
                    break
                }
                searchResultStart--
            }
        }
        // 定义搜索结果的终止坐标
        var searchResultEnd = searchResultStart + 180
        if (deal_diary != null) {
            if (searchResultEnd > deal_diary.length)
                searchResultEnd = deal_diary.length
        }

        // 从日记内容中截取搜索结果
        val cut = deal_diary?.substring(searchResultStart, searchResultEnd)

        // 创建 SpannableString 变量 highlight，用于高亮显示关键词
        val highlight = SpannableString(cut)
        // 获取关键字在搜索结果中的起始坐标
        val keyWordsIndex = keyWords?.let { cut?.lowercase(Locale.getDefault())?.indexOf(it.lowercase(Locale.getDefault())) }
        // 设置关键字的颜色
        highlight.setSpan(ForegroundColorSpan(Color.parseColor("#8C8C8C")), keyWordsIndex!!, keyWords!!.length + keyWordsIndex, Spannable.SPAN_POINT_MARK )


        // 获取设置
        val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        // 设置字体大小
        val fontSize = settings.getInt("font.size", 2)
        viewHolder.searchResult.textSize = app.getTextSize(fontSize)
        val isUseDefaultFont = settings.getBoolean("system.font.enabled", false)
        val fontName =  if(isUseDefaultFont) "Default" else "smileySans"
        cut?.let { highlight.setSpan(Myspan(app.getAttrs(fontName)), 0, it.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE) }

        // 显示日记的搜索结果
        viewHolder.searchResult.setText(highlight, TextView.BufferType.SPANNABLE)
        if (p2 != null) {
            // 显示日记的日期
            viewHolder.diaryDate.text = String.format(p2.context.resources.getString(R.string.diary_date),
                app.getMonth(diary.getMonth()),
                diary.getDay(),
                diary.getYear(),
                app.getWeek(diary.getWeek())
            )
            // 显示相匹配的内容数目
            viewHolder.count.text = String.format(p2.context.resources.getString(R.string.result_count), result[1])
        }

        // 返回创建好的子项
        return view
    }
}