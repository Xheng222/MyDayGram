package com.xheng.mydaygram.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication

class ChooseYearAdapter(
    private var context: Context,       // 定义 RecyclerView 上下文
    private var currentYear: Int,       // 定义当前的年份
    private var selectedYear: Int,       // 定义用户所选择的年份
    private val years: MutableList<Int> = mutableListOf()  // 创建年份集合
) : RecyclerView.Adapter<ChooseYearAdapter.ViewHolder>() {

    init {
        for (i in 2000..currentYear) {
            years.add(i)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var selectYear: Button

        init {
            selectYear = view.findViewById(R.id.selectYear)
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
    override fun getItemCount() = years.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 创建子项
        val view = LayoutInflater.from(context).inflate(R.layout.year_items, parent, false)

        // 创建子项持有者
        val viewHolder = ViewHolder(view)

        // 设置年份按钮的字体
        viewHolder.selectYear.typeface = app.getAttrs("Arvil_Sans")
        return viewHolder
    }

    // 绑定子项持有者
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 获取子项所对应的年份
        val year = years[position]

        // 设置年份按钮的点击事件
        holder.selectYear.setOnClickListener(View.OnClickListener {
            onItemClickListener.onItemClick(year)
        })

        // 获取年份按钮的布局参数，控件的布局参数必须与父布局一样，父布局为 LinearLayout，故转换成 LinearLayout.LayoutParams
        val layoutParams  = holder.selectYear.layoutParams as LinearLayout.LayoutParams

        // 当年份位于 2001年至当前年份之间时

        when (year) {
            2000 -> {
                // 当2000年时
                layoutParams.setMargins(app.dp_to_px(-20.0F), 0, app.dp_to_px(-20.0F), 0)
            }

            currentYear -> {
                layoutParams.setMargins(app.dp_to_px(-20.0F), 0, app.dp_to_px(-20.0F), 0)
            }

            else -> {
                layoutParams.setMargins(app.dp_to_px(-20.0F), 0, app.dp_to_px(-20.0F), 0)
            }
        }

        // 将布局参数应用到按钮
        holder.selectYear.layoutParams = layoutParams

        // 设置年份按钮的文本
        holder.selectYear.text = year.toString()

        // 当按钮对应的年份不是用户当前选中的年份时
        if (year != selectedYear) {
            holder.selectYear.setTextColor(Color.parseColor("#81817F"))
        } else {
            holder.selectYear.setTextColor(Color.parseColor("#252525"))
        }
    }



    interface OnItemClickListener {
        // 执行点击时的操作
        fun onItemClick(selectedYear: Int)
    }
}

