package com.xheng.mydaygram.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xheng.mydaygram.R
import com.xheng.mydaygram.model.Diary
import com.xheng.mydaygram.ui.MyTextView
import org.litepal.LitePal
import org.litepal.extension.find
import java.text.SimpleDateFormat
import java.util.*


class DiaryFragment: BaseFragment(), View.OnClickListener {
    // 定义日记
    private lateinit var diary: Diary

    private lateinit var view: View

    // 定义日记的年份
    private var year = 0

    // 定义日记的月份
    private var month = 0

    // 定义日记的日期
    private var day = 0

    // 定义日记的日期
    private var week = 0


    // 定义滚动控件
    private lateinit var scrollView: ScrollView

    // 定义日记编辑器
    private lateinit var diaryEditor: EditText

    // 定义悬浮按钮
    private lateinit var addTimeBut: FloatingActionButton

    private lateinit var doneBut: FloatingActionButton

    private fun saveDiary() {
        //查找数据库
        val results = LitePal.where("year = ? and month = ? and day = ?",
            year.toString(), month.toString(), day.toString()).find<Diary>() as List<Diary?>

        // 如果没有日记
        if (results.isEmpty() && diaryEditor.text.isNotEmpty()){
            if (diaryEditor.text.toString().trim().isEmpty())
                diary.setDiary("")
            else
                diary.setDiary(diaryEditor.text.toString())
            if (diary.save())
                Log.e("MyDayGram", "成功保存日记")

            else
                Log.e("MyDayGram", "保存日记失败")

        } else if (results.isNotEmpty()) {
            if (diaryEditor.text.toString().trim().isEmpty())
                diary.setDiary("")
            else
                diary.setDiary(diaryEditor.text.toString())
            diary.updateAll("year = ? and month = ? and day = ?",
                year.toString(), month.toString(), day.toString())

            Log.e("MyDayGram", "成功更新日记")
        }
    }

    private fun load() {
        // 获取设置
        val settings = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)

        // 设置文本
        val content = diary.getDiary()
        if (content != null) {
            diaryEditor.setText(content)
        } else
            diaryEditor.text = null

        // 设置字体与大小
        // 设置默认字体
        val isUseSysFont = settings?.getBoolean("system.font.enabled", false)
        if (isUseSysFont == true) {
            diaryEditor.typeface = Typeface.DEFAULT
        } else {
            diaryEditor.typeface = app.getAttrs("smileySans")
        }

        val size = settings?.getInt("font.size", 2)
        diaryEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, app.getTextSize(size!!))

        // 边框
        val useSideBar = settings.getBoolean("use.sidebar", true)
        Log.e("MyDayGram", useSideBar.toString())
        if (!useSideBar) {
            val content_layout = view.findViewById<LinearLayout>(R.id.content_layout)
            content_layout.setBackgroundResource(R.drawable.background_1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("MyDayGram", "Diary Fragment onCreate")

        if (arguments != null) {
            diary = Diary(
                requireArguments().getInt("year"),
                requireArguments().getInt("month"),
                requireArguments().getInt("day"),
                requireArguments().getInt("week"),
                requireArguments().getString("diary"),
            )
            year = diary.getYear()
            month = diary.getMonth()
            day = diary.getDay()
            week = diary.getWeek()
        }
    }

    @SuppressLint("ResourceAsColor", "ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        view = inflater.inflate(R.layout.fragment_write_diary, container, false)

        // 设置滚动控件的实例
        scrollView = view.findViewById(R.id.scroll_view)

        // 获取显示日记具体日期的 TextView 实例
        val showDate = view.findViewById<MyTextView>(R.id.show_date)

        // 创建 SpannableString 变量 style 设置日期格式
        val style = SpannableString(app.getWeek(week) + " / " + app.getMonth(month) + " " + day + " / " + year)
        // 当星期为星期天时
        if (week == 1) {
            // 将 dateStyle 前面六个字符设置成红色（Sunday 为六个字符），INCLUSIVE_EXCLUSIVE 表示包括起始下标、不包括终止下标
            style.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireActivity(), R.color.red)), 0, 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        // 将文本应用到 TextView
        showDate.text = style

        // 获取日记编辑器的实例
        diaryEditor = view.findViewById(R.id.diary_editor)
        diaryEditor.setOnClickListener(this)

        addTimeBut = view.findViewById(R.id.floating_addTime)
        addTimeBut.setMaxImageSize(135)
        addTimeBut.setOnClickListener(this)

        doneBut = view.findViewById(R.id.floating_done)
        doneBut.setMaxImageSize(150)
        doneBut.setOnClickListener(this)


        return view
    }

    override fun onPause() {
        saveDiary()
        val manager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
        super.onPause()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            // 添加当前时间
            R.id.floating_addTime -> {
                val time = SimpleDateFormat("h:mma", Locale.ENGLISH).format(Date()).toString()
                val start = diaryEditor.selectionStart.coerceAtLeast(0)
                val end = diaryEditor.selectionEnd.coerceAtLeast(0)
                diaryEditor.text.replace(start.coerceAtMost(end), start.coerceAtLeast(end), time, 0, time.length)
            }

            // 完成日记
            R.id.floating_done -> {
                saveDiary()
//                (activity as MainActivity).freshMainFragment()
//                (activity as MainActivity).freshSearch()
                activity?.onKeyDown(KeyEvent.KEYCODE_BACK, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        load()
        // 自动弹出软键盘
        diaryEditor.postDelayed({ // 输入框请求获取焦点
            // 定位到最后一位
            diaryEditor.setSelection(diaryEditor.text.length)
            diaryEditor.requestFocus()
            // 弹出软键盘
            (Objects.requireNonNull(requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager).showSoftInput(
                diaryEditor,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        }, 250)
    }

}