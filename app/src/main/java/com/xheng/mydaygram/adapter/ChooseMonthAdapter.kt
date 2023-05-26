package com.xheng.mydaygram.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication

class ChooseMonthAdapter(
    private var context: Context,       // 定义 RecyclerView 上下文
    private var currentYear: Int,       // 定义当前的年份
    private var selectedYear: Int,       // 定义用户所选择的年份
    private var currentMonth: Int,        //定义当前的月份
    private var selectedMonth: Int,       //定义用户选择的月份
    private val months: MutableList<Int> = mutableListOf()  // 创建年份集合
) : RecyclerView.Adapter<ChooseMonthAdapter.ViewHolder>() {

    init {
        for (i in 0..11) {
            months.add(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var selectMonth: ImageButton

        init {
            selectMonth = view.findViewById(R.id.selectMonth)
        }
    }

    // 获取 Application 实例
    private val app = MyLitePalApplication.getInstance()

    // 定义 RecyclerView 子项点击事件的接口
    private lateinit var onItemClickListener: OnItemClickListener

    // 设置 RecyclerView 子项点击事件的监听器
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        if (listener != null) {
            onItemClickListener = listener
        }
    }
    override fun getItemCount() = months.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 创建子项
        val view = LayoutInflater.from(context).inflate(R.layout.month_items, parent, false)

        // 创建子项持有者
        return ViewHolder(view)
    }

    // 绑定子项持有者
    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 获取子项所对应的月份
        val month = months[position]

        // 设置月份按钮的点击事件
        holder.selectMonth.setOnClickListener(View.OnClickListener {
            onItemClickListener.onItemClick(month)
        })

        // 获取月份按钮的布局参数，控件的布局参数必须与父布局一样，父布局为 LinearLayout，故转换成 LinearLayout.LayoutParams
        val layoutParams  = holder.selectMonth.layoutParams as LinearLayout.LayoutParams

        when (month) {
            0 -> {
                // 当 1 月时
                layoutParams.setMargins(app.dp_to_px(9.0f), 0, 0, 0)
            }

            11 -> {
                layoutParams.setMargins(0, 0, app.dp_to_px(9.0f), 0)
            }

            else -> {
                layoutParams.setMargins(0, 0, 0, 0)
            }
        }

        // 将布局参数应用到按钮
        holder.selectMonth.layoutParams = layoutParams

        // 获取并设置对应月份的图片的 id
        val monthButtonId = context.resources.getIdentifier("month_" + position + "_button", "drawable", context.packageName)
        holder.selectMonth.setImageResource(monthButtonId)

        // 当用户所选择的年份是今年，并且月份大于当前的月份时
        if (selectedYear == currentYear && position > currentMonth) {
            // 将月份图片的透明度设置为 100， 表明不可选
            holder.selectMonth.imageAlpha = 100
            // 设置不可选
            holder.selectMonth.isEnabled = false
        } else {
            // 将月份图片的透明度设置为 225 ，并可选
            holder.selectMonth.setImageAlpha(255)
            holder.selectMonth.setEnabled(true)
        }

        // 当月份不是用户所选中的月份时
        holder.selectMonth.isSelected = position == selectedMonth
    }



    interface OnItemClickListener {
        // 执行点击时的操作
        fun onItemClick(selectedMonth: Int)
    }
}
