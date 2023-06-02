package com.xheng.mydaygram.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Scroller
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication


class MyListView: ListView, AbsListView.OnScrollListener{
    // 创建 Handler 变量
    private var handler: Handler? = Handler(Looper.getMainLooper())

    // 定义滚动器（用于滚动的辅助计算）
    private var scroller: Scroller? = null

    // 定义手指按下时的屏幕 Y 坐标
    private var putY = 0f

    // 定义手指滑动时的初始 Y 坐标
    private var startY = 0f

    // 创建最大过载滑动距离
    private val maxOverScrollY: Int = MyLitePalApplication.getInstance().dp_to_px(100.0f)

    // 标记 ListView 是否滑动到顶部
    private var isTop = false

    // 标记 ListView 是否滑动到底部
    private var isBottom = false

    // 标记 ListView 是否从顶部下拉
    private var is_goto_Top = false

    // 标记 ListView 是否从底部上拉
    private var is_goto_Bottom = false

    // 标记是否已记录手指滑动时的初始 Y 坐标
    private var isRecord = false

    // 标记 ListView 能否滑动
    private var isBlock = false

    // 定义 ListView 的头布局
    private lateinit var header: View

    // 创建头布局的高度
    private val headerHeight: Int = MyLitePalApplication.getInstance().dp_to_px(80.0f)

    // 定义滑动监听器
    private var listener: OnSwipeListener? = null

    // 设置滑动监听器
    fun setOnSwipeListener(listener: OnSwipeListener){
        this.listener = listener
    }

    // 初始化ListView
    private fun initListView(context: Context){
        // 滚动监听
        setOnScrollListener(this)

        // 创建带加速插值器的滚动器（滚动动画为加速滚动效果）
        scroller = Scroller(context, AccelerateInterpolator())

        // 实例化头布局
        header = LayoutInflater.from(context).inflate(R.layout.date_hander, this, false)

        // 将头布局添加到 ListView
        addHeaderView(header, null, false)

        // 将头布局的 paddingTop 设置为头布局高度的负值，让其处于隐藏状态
        setHeaderPaddingTop(-headerHeight)


    }

    // 构造函数
    constructor(context: Context): super(context) {
        initListView(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        initListView(context)

    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle) {
        initListView(context)
    }

    // 设置头布局的 paddingTop 并重绘
    private fun setHeaderPaddingTop(paddingTop: Int) {
        // 设置头布局各个方向的 padding 值
        header.setPadding(header.paddingLeft, paddingTop, header.paddingRight, header.paddingBottom)

        // 重新绘制头布局
        header.invalidate()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            // 记录按下的位置
            MotionEvent.ACTION_DOWN ->
                putY = ev.y
            // 判断滑动的相关事件
            MotionEvent.ACTION_MOVE -> {
                // 标记 ListView 是否顶部下拉
                is_goto_Top = isTop && ev.y - putY > 0
                // 标记 ListView 是底部否上拉
                is_goto_Bottom = isBottom && ev.y - putY < 0
                // 当 ListView 不可滑动时
                if (isBlock) {
                    // 不处理滑
                    return false
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    // 处理屏幕触摸事件

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            // 当手指按到屏幕上时，停止滚动
            MotionEvent.ACTION_DOWN ->
                scroller?.forceFinished(true)
            // 当手指在屏幕上滑动时
            MotionEvent.ACTION_MOVE -> {
                if(is_goto_Top || is_goto_Bottom ) {
                    if(!isRecord) {
                        startY = ev.y
                        isRecord = true
                    }
                }
                onPull(ev)
            }
            // 当手指离开屏幕时
            MotionEvent.ACTION_UP -> {
                listener?.onRelease(isBlock)
                springBack()
                // 取消 ListView 顶部下拉标记
                is_goto_Top = false
                // 取消 ListView 底部上拉标记
                is_goto_Bottom = false
                // 取消已记录滑动初始 Y 坐标的标记
                isRecord = false
                // 恢复 ListView 为可滑动状态
                isBlock = false
            }
        }
        return super.onTouchEvent(ev)
    }

    //实现顶部下拉和底部上拉的onPull拖拽效果
    private fun onPull(ev: MotionEvent?) {
        // 清空当前所有消息队列，防止正在回弹时，用户又滑动产生冲突
        handler?.removeCallbacksAndMessages(null)

        // 计算手指的滑动距离（除以一定的系数是为了产生粘滞效果）
        var deltaY: Int = ((ev!!.y - startY) / 2).toInt()

        if(is_goto_Top) {
            // 当 ListView 顶部出现并且下拉距离小于头布局的高度时
            if (deltaY < headerHeight) {
                val last: Int = deltaY - headerHeight
                setHeaderPaddingTop(last)
            }
        }

        if(is_goto_Bottom) {
            // 当滑动距离超过最大过载滑动距离时
            if (-deltaY > maxOverScrollY) {
                // 将滑动距离设置为最大过载滑动距离，阻止滑动
                deltaY = -maxOverScrollY
                isBlock = true
            }
            // 滑动至指定位置
            smoothScrollTo(-deltaY)
            // 执行滑动过程中的一些操作
            listener?.onSwipe(-deltaY, maxOverScrollY)
        }
    }

    //实现springBack越界回弹效果
    private fun springBack() {
        // 获取头布局当前的 paddingTop值
        var nowPaddingTop: Int? = header.paddingTop

        // 当 ListView 处于顶部下拉状态时
        if (nowPaddingTop != null) {
            if (nowPaddingTop > -headerHeight) {
                // 选中头布局之下的第一个子项，防止回弹时该子项被拉出屏幕之外
                // setSelection(0)

                // 定义 Handler 发送消息的延迟时间，并随着循环增加， 好让前面的消息有足够的处理时间
                var delay = 0

                //回弹
                while (nowPaddingTop > -headerHeight) {
                    nowPaddingTop -= 10
                    delay += 10
                    val pt: Int = nowPaddingTop

                    header.postDelayed({
                        if (pt < -headerHeight) {
                            // 如果回弹的距离超过头布局的高度时，则恢复初始状态，防止越界
                            setHeaderPaddingTop(-headerHeight)
                        } else {
                            // 即时设置头布局的 paddingTop 值
                            setHeaderPaddingTop(pt)
                        }
                    }, delay.toLong())
                }

            }
        }

        // 恢复 ListView 底部默认位置
        smoothScrollTo(0)
    }

    //实现smoothScrollTo滚动效果
    private fun smoothScrollTo(endY: Int) {
        scroller?.startScroll(0, (scroller?.finalY ?: 0), 0, endY - (scroller?.finalY ?: 0), 300)
        // 重新绘制 ListView
        invalidate()
    }

    //重写控制滚动
    override fun computeScroll() {
        if (scroller!!.computeScrollOffset()) {
            // 调用 ListView 的 scrollTo 方法完成滚动
            scrollTo(0, scroller!!.currY)

            // 重新绘制 ListView
            invalidate()
        }
        super.computeScroll()
    }



    /**
     * 监听当前滚动的 Item（包括头布局和脚部局）
     * AbsListView 是 ListView 的父类，是用于实现条目的虚拟列表的基类，这里的列表没有空间的定义
     * p1 代表屏幕当前可见的第一个子项的索引
     * p2 代表屏幕当前可见的子项总数，包括没有完整显示的子项
     * p3 代表 ListView 的子项总数
     */

    override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {
        // 当 ListView 第一个可见子项的索引为 0 时
        if (p1 == 0) {
            val firstItem: View? = getChildAt(0)
            // 当 ListView 第一个可见子项不为空且子项顶部的坐标为 0 时，标记 ListView 到达顶部
            isTop = (firstItem != null) && (firstItem.top == 0)
        }

        // 当 ListView 可见子项的总数等于 ListView 子项的总数时（数据不满屏）
        if (p2 == p3) {
            isBottom = true
        }
        else if(p1 + p2 == p3) {
            // 当 ListView 第一个可见子项与最后一个可见子项的索引之和等于 ListView 子项总数时，获取 ListView 最后一个可见子项（数据满屏）
            val lastVisibleItemView = getChildAt(childCount - 1)

            // 当 ListView 最后一个可见子项不为空并且子项的底部坐标为 ListView 的高度时，标记 ListView 到达底部
            isBottom = lastVisibleItemView != null && lastVisibleItemView.bottom == height

        }
    }

    //控制 ListView 的越界效果
    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        return super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            0,
            isTouchEvent
        )
    }

    override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {}

    interface OnSwipeListener {
        // 手指滑动时的操作
        fun onSwipe(Y: Int, scrollY: Int)

        // 手指松开时的操作
        fun onRelease(isBlock: Boolean)
    }


}