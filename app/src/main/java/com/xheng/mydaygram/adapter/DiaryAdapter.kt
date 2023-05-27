package com.xheng.mydaygram.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication
import com.xheng.mydaygram.model.Diary
import com.xheng.mydaygram.ui.MyListView
import com.xheng.mydaygram.ui.MyTextView

class DiaryAdapter(
    private var context: Context,
    private var diaries: MutableList<Diary>
) : BaseAdapter() {

    // 获取 Application 实例
    private val app = MyLitePalApplication.getInstance()

    // 内部 ViewHolder 类
    class ViewHolder(
        itemView: View
    ){
        val dot: ImageView = itemView.findViewById(R.id.dot)
        val diary1: LinearLayout = itemView.findViewById(R.id.diary1_layout)
        val showWeek: MyTextView = itemView.findViewById(R.id.diary_week)
        val showDay: MyTextView = itemView.findViewById(R.id.diary_day)
        val showContent: MyTextView = itemView.findViewById(R.id.diary_content)
        val horizontalLine: View = itemView.findViewById(R.id.horizontal_line)
        val verticalLine: View = itemView.findViewById(R.id.vertical_line)
    }

    // 更新数据
    private fun update(listView: MyListView, position: Int, diary: Diary){
        // 将最新的日记覆盖到 ListView 的日记集合
        diaries[position - 1] = diary
        // 获取第一个可见的子项位置
        val first = listView.firstVisiblePosition
        // 创建需要更新的子项
        val view = listView.getChildAt(position - first)
        // 调用 getView 更新子项
        getView(position - 1, view, listView)
    }

    // 删除日记
    fun delete(listView: MyListView, position: Int){
        // 获取需要重置的日记
        val diary = diaries[position - 1]
        // 将其内容设置空
        diary.setDiary(null)
        // 更新 ListView 单条数据
        update(listView, position, diary)
    }

    // 获取 ListView 子项所对应的位置
    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    // 获取 ListView 子项所对应的数据
    override fun getItem(p0: Int): Any {
        return diaries[p0]
    }

    // 获取 ListView 的子项数目
    override fun getCount(): Int {
        return diaries.size
    }

    // 创建 ListView 视图
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val diary = diaries[p0]
        val view: View
        val viewHolder: ViewHolder

        if(p1 == null){
            view = LayoutInflater.from(context).inflate(R.layout.diary_1, p2, false)
            viewHolder = ViewHolder(view)
            // 绑定子项持有者
            view.tag = viewHolder
        } else{
            view = p1
            // 取出绑定的子项持有者
            viewHolder = view.tag as ViewHolder
        }

        // 获取设置
        val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        // 设置字体大小
        val fontSize = settings.getInt("font.size", 2)
        viewHolder.showContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getTextSize(fontSize))

        // 设置默认字体
        val isUseDefaultFont = settings.getBoolean("system.font.enabled", false)
        viewHolder.showContent.typeface = if (isUseDefaultFont) Typeface.DEFAULT else app.getAttrs("smileySans")

        // 设置预览类型
        val preType = settings.getInt("preview.type", 0) + 1
        viewHolder.showContent.maxLines = preType

        //星期日使用红点，其他时候使用黑点，日期同理
        if (diary.getWeek() == 1 ){
            viewHolder.dot.setImageResource(R.drawable.red_dot_00)
            viewHolder.showDay.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            viewHolder.dot.setImageResource(R.drawable.dot_00)
            viewHolder.showDay.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // 当日记内容不为空时
        if (diary.getDiary() != ""){
            // 隐藏圆点图片
//             viewHolder.dot.visibility = View.GONE
            viewHolder.dot.isVisible = false

            // 显示日记预览
//            viewHolder.diary1.visibility = View.VISIBLE
            viewHolder.diary1.isVisible = true

            // 设置显示的 TextView 文本
            viewHolder.showDay.text = diary.getDay().toString()
            viewHolder.showWeek.text = app.getWeek(diary.getWeek()).subSequence(0, 3)
            viewHolder.showContent.text = diary.getDiary().toString()
        } else {
            // 日记为空时，设置圆点，隐藏日记
            // viewHolder.dot.visibility = View.GONE
            viewHolder.dot.isVisible = true

            // viewHolder.diary1.visibility = View.VISIBLE
            viewHolder.diary1.isVisible = false
        }

        viewHolder.showDay.setBackgroundColor(Color.TRANSPARENT)
        viewHolder.showWeek.setBackgroundResource(R.drawable.week_background)
        viewHolder.showContent.setBackgroundColor(Color.TRANSPARENT)
        viewHolder.horizontalLine.setBackgroundColor(Color.parseColor("#494949"))
        viewHolder.verticalLine.setBackgroundColor(Color.parseColor("#494949"))
        viewHolder.diary1.setBackgroundResource(R.drawable.diary1_background)

        return view
    }
}
