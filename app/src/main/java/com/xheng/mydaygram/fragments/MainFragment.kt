package com.xheng.mydaygram.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xheng.mydaygram.MainActivity
import com.xheng.mydaygram.R
import com.xheng.mydaygram.adapter.ChooseMonthAdapter
import com.xheng.mydaygram.adapter.ChooseYearAdapter
import com.xheng.mydaygram.adapter.DiaryAdapter
import com.xheng.mydaygram.adapter.DiaryAdapter2
import com.xheng.mydaygram.model.Diary
import com.xheng.mydaygram.ui.MyListView
import com.xheng.mydaygram.ui.MyTextView
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import java.text.SimpleDateFormat
import java.util.*


class MainFragment: BaseFragment(), Runnable, View.OnClickListener, AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener, MyListView.OnSwipeListener{

    // 获取当前日历
    private val calendar = Calendar.getInstance()

    // 获取当前年份
    private val currentYear = calendar[Calendar.YEAR]

    // 获取当前月份
    private val currentMonth = calendar[Calendar.MONTH]

    // 获取当前日期
    private val currentDay = calendar[Calendar.DAY_OF_MONTH]

    // 获取当前星期
    private val currentWeek = calendar[Calendar.DAY_OF_WEEK]

    // 定义用户所选择的年份，默认等于当前年份
    private var selectedYear = currentYear

    // 定义用户所选择的月份，默认等于当前月份
    private var selectedMonth = currentMonth

    // 定义用户选择的日期，默认等于当前日
    private var selectDay = currentDay

    // 定义日记集合
    private var diaries = mutableListOf<Diary>()

    // 定义被点击的子项索引
    private var position = 0

    // 定义日记的 ListView
    private lateinit var myListView: MyListView

    // 定义类型 1 的日记适配器
    private lateinit var diaryAdapter1: DiaryAdapter

    // 定义类型 2 的日记适配器
    private lateinit var diaryAdapter2: DiaryAdapter2

    // 定义no diary显示文本框
    private lateinit var noDiary: TextView

    // 定义头布局显示当前星期的 TextView
    private var showWeek: MyTextView? = null

    // 定义头布局显示当前日期的 TextView
    private var showDay: MyTextView? = null

    // 定义头布局显示当前时间的 TextView
    private var showTime: MyTextView? = null

    // 定义头布局日期与时间之间的竖线
    private var verticalLine: View? = null

    // 定义切换月份的按钮
    private var selectMonth: Button? = null

    // 定义切换年份的按钮
    private var selectYear: Button? = null

    // 定义添加今天日记的按钮
    private var addToday: ImageButton? = null

    // 用于标记活动是否处于暂停状态
    private var isPause = false

    // 用于标记是否使用类型 2 的子项布局
    private var isItemType2 = false

    // 使用 Handler 在主线程里对 TextView 显示的时间进行更新
    private var handler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // 更新 TextView 显示的时间
            showTime!!.text =
                SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(System.currentTimeMillis())
        }
    }

    //刷新界面
    private fun refresh() {
        if(!isItemType2){
            diaryAdapter1.notifyDataSetChanged()
            // 将 ListView 滚动到合适的项
            handler?.postDelayed({
                myListView.smoothScrollToPosition(selectDay)
            }, 135)

        } else {
            diaryAdapter2.notifyDataSetChanged()
        }
    }

    //创建一个采用指定样式和动画的对话框
    private fun createChooser(): AlertDialog {
        //创建对话框
        val chooser = AlertDialog.Builder(activity, R.style.ChooserDialog).show()

        // 获取对话框的窗体
        val window = chooser.window
        // 设置对话框在屏幕底部弹出
        window?.setGravity(Gravity.BOTTOM)
        //设置动画
        window?.setWindowAnimations(R.style.ChooserAnimation)

        // 创建变量用于存放屏幕信息
        val resources: Resources = resources
        val dm: DisplayMetrics = resources.displayMetrics

        // 获取并修改对话框的布局信息
        val lp = window?.attributes
        lp?.width = dm.widthPixels
        window?.attributes = lp

        return chooser
    }

    //加载日记
    private fun loadDiary(): Boolean{
        val results =
            LitePal.where("year = ? and month = ?", selectedYear.toString(), selectedMonth.toString())
                .order("day")
                .find<Diary>()

        // 清空日记集合
        diaries.clear()
        noDiary.isVisible = false
        if(isItemType2) {
            if (results.isEmpty()) {
                noDiary.isVisible = true
            } else {
                for (diary in results) {
                    if (diary.getDiary() != "") {
                        diaries.add(diary)
                    }
                }

            }

        } else{
            // 定义用户所选中的月份已经过去的天数 (包括已经写了日记的最新一天)
            val lostDay: Int =
                if (selectedMonth == currentMonth && selectedYear == currentYear) {
                    // 用户所选中的年份、月份与当前时间一致
                    currentDay
                } else {
                    // 用户所选中的年份、月份早已过去时，将日历翻到该年该月1号
                    calendar[selectedYear, selectedMonth] = 1
                    // 获取该月的最大天数
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                }

            // 根据用户所选中的月份已经过去的天数来创建日记（内容默认为空）
            for (i in 1..lostDay ){
                // 将日历设定到该年该月该日
                calendar.set(selectedYear, selectedMonth, i)
                // 将日记添加到日记集合中
                diaries.add(Diary(selectedYear, selectedMonth, i, calendar.get(Calendar.DAY_OF_WEEK), ""))
            }

            for (diary in results) {
                if (diary.getDay() <= lostDay) {
                    diaries[diary.getDay() - 1] = diary
                }
            }
        }
        if (results.isEmpty())
            return false
        return true
    }

    private fun loadSettings(){
        // 刷新界面
        refresh()

        // 延迟设置头布局各控件对应的字体颜色，提高响应速度
        myListView.postDelayed({
            showDay?.setTextColor(Color.parseColor("#52524E"))
            showWeek?.setTextColor(Color.parseColor("#4B4B48"))
            verticalLine?.setBackgroundColor(Color.parseColor("#494949"))
            showTime?.setTextColor(Color.parseColor("#797872"))
        }, 500)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("MyDayGram", "开始创建 Main Fragment")
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // 获取 noDiary 实例，设为不可见
        noDiary = view.findViewById(R.id.noData)
        noDiary.isVisible = false
        noDiary.typeface = app.getAttrs("Georgia")
        //加载日记
        loadDiary()
        // 获取 ListView 的实例
        myListView = view.findViewById(R.id.my_list_view)
//        myListView.visibility = View.INVISIBLE

        // 创建类型 1 的日记适配器
        diaryAdapter1 = DiaryAdapter(requireContext(), diaries)
        // 创建类型 2 的日记适配器
        diaryAdapter2 = DiaryAdapter2(requireContext(), diaries)
        // 设置 ListView 的适配器
        myListView.adapter = if (isItemType2) diaryAdapter2 else diaryAdapter1

        // 监听 ListView 的点击事件
        myListView.onItemClickListener = this

        // 监听 ListView 的长按事件
        myListView.onItemLongClickListener = this

        // 监听 ListView 的滑动事件
        myListView.setOnSwipeListener(this)


        // 获取头布局中显示当前星期的 TextView 实例
        showWeek = view.findViewById(R.id.show_week)
        showWeek?.text = String.format(resources.getString(R.string.today_week), app.getWeek(currentWeek))

        showDay = view.findViewById(R.id.show_day)
        showDay?.text = String.format(resources.getString(R.string.today_day), app.getMonth(currentMonth), currentDay)

        showTime = view.findViewById(R.id.show_time)
        // 获取时间分隔符的实例
        verticalLine = view.findViewById(R.id.vertical_line)

        // 获取切换月份的按钮实例
        selectMonth = view.findViewById(R.id.select_month)
        selectMonth?.typeface = app.getAttrs("Arvil_Sans")
        selectMonth?.text = app.getMonth(selectedMonth)
        selectMonth?.setOnClickListener(this)

        // 获取切换年份的按钮实例
        selectYear = view.findViewById(R.id.select_year)
        selectYear?.typeface = app.getAttrs("Arvil_Sans")
        selectYear?.text = selectedYear.toString()
        selectYear?.setOnClickListener(this)

        // 获取添加当天日记的按钮实例
        addToday = view.findViewById(R.id.add_today)
        addToday?.setOnClickListener(this)

        // 获取前往设置的按钮实例
        val goSetting: ImageButton = view.findViewById(R.id.go_setting)
        // 监听该按钮的点击事件
        goSetting.setOnClickListener(this)

        // 获取切换 ListView 子项布局的按钮实例
        val switch_view: ImageView = view.findViewById(R.id.switch_view)
        switch_view.setOnClickListener(this)

        return view
    }


    override fun onResume() {
        super.onResume()
        loadSettings()

        // 创建定时更新时间的线程
        Thread(this).start()
    }

    //活动被销毁的时候调用
    override fun onDestroy() {
        // 清空消息队列，防止内存泄漏
        handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // 处理 ListView 的点击事件
    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.e("MyDayGram", "点击")
        // 记录被点击的子项索引
        this.position = p2
        // 获取被点击的子项所对应的日记
        val diary = p0?.getItemAtPosition(p2) as Diary
        selectDay = diary.getDay()
        Log.e("MyDayGram", diary.getYear().toString())

        val bundle = Bundle()
        bundle.putInt("year", diary.getYear())
        bundle.putInt("month", diary.getMonth())
        bundle.putInt("day", diary.getDay())
        bundle.putInt("week", diary.getWeek())
        bundle.putString("diary", diary.getDiary())

        (activity as MainActivity).switchFragment("DiaryFragment", bundle)
    }

    override fun run() {
        while(!isPause) {
            try {
                // 添加到消息队列
                handler?.sendMessage(Message())
                // 线程休眠一秒
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onItemLongClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long): Boolean {
        // 获取被点击的子项所对应的日记
        val diary = p0?.getItemAtPosition(p2) as Diary
        // 当被点击的子项所用于的日记内容不为空时，执行删除操作
        if (diary.getDiary() != "") {
            // 创建指定样式的对话框
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("DayGram")
                .setMessage(String.format(resources.getString(R.string.context_delete_confirm), diary.getYear(), diary.getMonth(), diary.getDay()))
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    LitePal.deleteAll<Diary>("year = ? and month = ? and day = ?",
                        diary.getYear().toString(), diary.getMonth().toString(), diary.getDay().toString())

                        // 重新加载日记集合
                        loadDiary()
                        diaryAdapter1.notifyDataSetChanged()
                        diaryAdapter2.notifyDataSetChanged()

                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorAccent)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorAccent)
        }
        return true
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            // 添加今天日记的按钮
            R.id.add_today -> {
                // 查询数据库是否有今天的日记
                val results = LitePal.where("year = ? and month = ? and day = ?",
                    currentYear.toString(), currentMonth.toString(), currentDay.toString()).find<Diary>() as List<Diary?>

                val bundle = Bundle()
                bundle.putInt("year", currentYear)
                bundle.putInt("month", currentMonth)
                bundle.putInt("day", currentDay)
                bundle.putInt("week", currentWeek)

                if (results.isEmpty()) {
                    bundle.putString("diary", null)
                } else {
                    bundle.putString("diary", (results[0] as Diary).getDiary())
                }
                (activity as MainActivity).switchFragment("DiaryFragment", bundle)
            }

            // 切换年份的按钮
            R.id.select_year -> {
                // 创建指定样式的对话框
                val chooseYear = createChooser()

                // 获取对话框的窗体
                val window = chooseYear.window

                // 设置对话框的布局
                window?.setContentView(R.layout.choose_year)

                // 获取对话框上的 RecyclerView 控件实例
                val yearRecyclerView = window?.findViewById<RecyclerView>(R.id.year_recycler_view)

                // 将 RecyclerView 设置为横向滑动
                val recyclerViewManager = LinearLayoutManager(requireActivity())
                recyclerViewManager.orientation = LinearLayoutManager.HORIZONTAL
                yearRecyclerView?.layoutManager = recyclerViewManager

                // 设置 RecyclerView 的适配器
                val yearAdapter = ChooseYearAdapter(requireContext(), currentYear, selectedYear)
                yearRecyclerView?.adapter = yearAdapter

                // 将 RecyclerView 滚动到所选定的年份
                yearRecyclerView?.scrollToPosition(selectedYear - 2000)

                // 设置 RecyclerView 子项的点击事件
                yearAdapter.setOnItemClickListener(object : ChooseYearAdapter.OnItemClickListener {
                    override fun onItemClick(selectedYear: Int) {
                        // 根据所点击的子项设置当前选中的年份
                        this@MainFragment.selectedYear = selectedYear
                        // 更改切换年份的按钮文本为所选年份
                        selectYear!!.text = selectedYear.toString()

                        // 翻到该月第一天
                        selectDay = 1

                        // 当选择的年份等于当前年，注意月份不能超出
                        if (this@MainFragment.selectedYear == currentYear) {
                            if (selectedMonth > currentMonth) {
                                selectedMonth = currentMonth
                            }

                            // 更改切换月份的按钮文本为所选月份
                            selectMonth!!.text = app.getMonth(selectedMonth)
                        }

                        switchAnimation()

                        // 关闭对话框
                        chooseYear.dismiss()
                    }
                })
            }

            // 切换月份的按钮
            R.id.select_month -> {
                // 创建指定样式的对话框
                val chooseYear = createChooser()

                // 获取对话框的窗体
                val window = chooseYear.window

                // 设置对话框的布局
                window?.setContentView(R.layout.choose_month)

                // 获取对话框上的 RecyclerView 控件实例
                val monthRecyclerView = window?.findViewById<RecyclerView>(R.id.month_recycler_view)

                // 将 RecyclerView 设置为横向滑动
                val recyclerViewManager = LinearLayoutManager(requireActivity())
                recyclerViewManager.orientation = LinearLayoutManager.HORIZONTAL
                monthRecyclerView?.layoutManager = recyclerViewManager

                // 设置 RecyclerView 的适配器
                val monthAdapter = ChooseMonthAdapter(requireContext(), currentYear, selectedYear, currentMonth, selectedMonth)
                monthRecyclerView?.adapter = monthAdapter

                // 将 RecyclerView 滚动到所选定的月份
                monthRecyclerView?.scrollToPosition(selectedMonth)

                // 设置 RecyclerView 子项的点击事件
                monthAdapter.setOnItemClickListener(object : ChooseMonthAdapter.OnItemClickListener {
                    override fun onItemClick(selectedMonth: Int) {

                        // 根据所点击的子项设置当前选中的月份
                        this@MainFragment.selectedMonth = selectedMonth

                        // 更改切换月份的按钮文本为所选月份
                        selectMonth!!.text = app.getMonth(selectedMonth)

                        // 翻到该月第一天
                        selectDay = 1

                        switchAnimation()

                        // 关闭对话框
                        chooseYear.dismiss()
                    }
                })
            }

            // 切换试图的按钮
            R.id.switch_view -> {
                // 当前使用的是类型 2 的子项布局时
                if (isItemType2) {
                    isItemType2 = false
                    // 重新加载日记
                    switchAnimation()
                } else {
                    isItemType2 = true
                    switchAnimation()
                }
            }

            //转到设置
            R.id.go_setting -> {

                (activity as MainActivity).switchFragment("SettingFragment", null)
            }
        }

    }



    //执行滑动过程中的一些操作
    override fun onSwipe(Y: Int, scrollY: Int) {
        // 根据滑动距离设置按钮背景的透明度
        if (Y < scrollY) {
            // 根据手指的滑动值设置按钮的透明度
            addToday?.background?.alpha =
                ((scrollY - Y * 0.5) / scrollY * 200).toInt()
        } else {
            // 当 ListView 不可滑动时，切换按钮的图标
            addToday?.setBackgroundResource(R.drawable.search_icon)
        }
    }

    override fun onRelease(isBlock: Boolean) {
        // 重设按钮的图标
        addToday?.background?.alpha = 255

        // 当 ListView 底部滑动到最大内偏距并且手指不是抛动时
        if (isBlock){
            Log.e("MyDayGram", "准备跳转至 Search Fragment")
            (activity as MainActivity).switchFragment("SearchFragment", null)

            //addToday?.setBackgroundResource(R.drawable.add_today)

        }
    }

    private fun switchAnimation() {
        myListView.animate()
            .alpha(0f)
            .setDuration(135)
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
                override fun onAnimationStart(p0: Animator) {
                    noDiary.animate()
                        .alpha(0f)
                        .setDuration(135)
                        .setListener(null)
                }
                override fun onAnimationEnd(p0: Animator) {
                    // 设置对应的适配器
                    myListView.adapter = if (isItemType2) diaryAdapter2 else diaryAdapter1

                    // 设置 ListView 的分隔线高度
                    myListView.dividerHeight =
                        if (isItemType2) app.dp_to_px(4.0f) else app.dp_to_px(9.0f)

                    loadDiary()
                    refresh()

                    // 播放动画
                    if (isItemType2) {
                        noDiary.animate()
                            .alpha(1f)
                            .setDuration(135)
                            .setListener(null)
                    }
                    myListView.animate()
                        .alpha(1f)
                        .setDuration(135)
                        .setListener(null)
                }
            })
    }


}