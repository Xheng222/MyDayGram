package com.xheng.mydaygram.fragments

import android.annotation.SuppressLint
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.xheng.mydaygram.MainActivity
import com.xheng.mydaygram.R
import com.xheng.mydaygram.ui.MyTextView
import java.util.*


class PasswordFragment: BaseFragment() {
    private lateinit var view: View

    // 定义设置
    private lateinit var settings: SharedPreferences

    // 定义模式
    private var mode = 0

    // 定义活动的阶段
    private var state = 0

    // 定义圆点图片
    private val dotImages = arrayOfNulls<ImageView>(4)

    // 定义密码框
    private lateinit var passwordField: EditText

    // 定义提示布局
    private lateinit var hintLayout: FrameLayout

    // 定义提示语
    private lateinit var hintText: MyTextView

    // 定义提示语左边的竖线
    private lateinit var verticalLine1: View

    // 定义提示语右边的竖线
    private lateinit var verticalLine2: View

    // 取消按钮
    private lateinit var cancel: ImageButton

    // 定义密码缓存
    private lateinit var tempPass: String

    private fun dotImage() {
        // 将所有圆点图片切换成默认状态
        for (dotImage in dotImages)
            dotImage?.setImageResource(R.drawable.password_dot_off)
        // 获取输入的字符数目
        val lenth = passwordField.text.toString().length
        // 根据字符数目修改圆点图片
        for (i in 1..lenth)
            dotImages[i-1]?.setImageResource(R.drawable.password_dot_on)
    }

    private fun passwordError(context: String) {
        // 将所有圆点图片设为红色
        for (dotImage in dotImages)
            dotImage?.setImageResource(R.drawable.password_dot_red)

        // 提示密码错误
        hintText.setText(R.string.password_failed)

        // 定义密码错误时的颜色（红色）
        val red = ContextCompat.getColor(requireContext(), R.color.red)
        // 将提示语的字体颜色设为红色
        hintText.setTextColor(red)
        // 将两根竖线的颜色设为红色
        verticalLine1.setBackgroundColor(red)
        verticalLine2.setBackgroundColor(red)

        // 还原默认状态
        hintText.postDelayed({ // 定义默认颜色（灰色）// 定义默认颜色（灰色）
            val gray = Color.parseColor("#989486")
            // 将提示语的字体颜色设为灰色
            hintText.setTextColor(gray)
            // 将两根竖线的颜色设为灰色
            verticalLine1.setBackgroundColor(gray)
            verticalLine2.setBackgroundColor(gray)

            // 清除文本
            clearPasswordField()
            hintText.text = context
        }, 1000)


    }

    @SuppressLint("SuspiciousIndentation")
    private fun createPassword() {
        // 新密码
        if (state == 1) {
            // 暂存密码
            tempPass = passwordField.text.toString()
            // 提示再输入一次密码
            hintText.setText(R.string.password_reenter)
            // 将活动标记为再输入一次密码的阶段
            state = 2
            // 清除已经输入的密码
            clearPasswordField()
            return
        }

        if (state != 2)
            return

        // 重复新密码
        if (passwordField.text.toString() == tempPass) {
            // 保存密码
            settings.edit().putBoolean("passwordOn", true)
                .putString("password", passwordField.text.toString()).apply()
            // 将 App 设为有锁状态
            app.setUnlock(false)
            (activity as MainActivity).pop()
            clearPasswordField()
        } else {
            hintText.setText(R.string.password_enter_new)
            val context = hintText.text.toString()
                passwordError(context)
            // 当两次输入密码不同时，重新来
            // 将活动标记为新密码阶段
            state = 1
            // 清除已经输入的密码
            clearPasswordField()
        }
    }

    private fun clearPasswordField() {
        passwordField.setText("")
        dotImage()
    }


    private fun completeInput() {
        if (passwordField.text.length == 4) {
            // 根据模式采取相应的处理
            when (mode) {
                // 验证密码
                1 -> {
                    if (settings.getString("password", null).equals(passwordField.text.toString())) {

                        (activity as MainActivity).pop()
                        app.isLocked = false

                    } else
                        passwordError(hintText.text.toString())
                }

                // 创建密码
                2 -> {
                    createPassword()
                }

                // 修改密码
                3 -> {
                    // 验证旧密码
                    if (settings.getString("password", "null").equals(passwordField.text.toString())) {
                        // 将活动标记为创建密码阶段
                        state = 1
                        // 提示输入新密码
                        hintText.setText(R.string.password_enter_new)
                        // 清除文本
                        clearPasswordField()
                        mode = 2
                    } else {
                        passwordError(hintText.text.toString())
                        // 当密码验证失败时，清除文本
                        clearPasswordField()
                    }
                }

                // 取消密码
                4 -> {
                    // 当密码验证成功时
                    if (settings.getString("password", "null").equals(passwordField.text.toString())) {
                        // 关闭密码验证
                        settings.edit().putBoolean("passwordOn", false)
                            .putString("password", "null").apply()
                        // 解除 App 的锁定
                        app.setUnlock(true)
                        clearPasswordField()
                        (activity as MainActivity).pop()

                    } else {
                        // 当密码错误时
                        passwordError(hintText.text.toString())
                    }
                }
            }
        }
    }

    private fun initView() {
        if (arguments != null) {
            mode = requireArguments().getInt("mode")
        }

        // 获取设置
        settings = requireActivity().getSharedPreferences("settings", MODE_PRIVATE)

        // 当密码为空并且当前模式为修改密码的模式时
        if (settings.getString("password", "null") == "null" && mode == 3) {
            // 将模式改为创建密码模式
            mode = 2
        }

        // 显示
        cancel.isVisible = true
        // 显示
        hintLayout.isVisible = true

        when (mode) {
            // 验证密码
            1 -> {
                // 隐藏取消按钮
                cancel.isVisible = false
                hintText.setText(R.string.password_enter_app)
            }

            // 创建密码
            2 -> {
                // 设置为创建密码阶段
                state = 1

                // 显示取消按钮
                cancel.isVisible = true

                // 显示提示布局
                hintLayout.isVisible = true
                hintText.setText(R.string.password_enter_new)
            }

            // 修改密码
            3 -> {
                // 设置为修改密码阶段
                state = 2
                hintText.setText(R.string.password_enter_old)
            }

            // 取消密码
            4 -> {
                hintText.setText(R.string.password_off)
            }

        }
    }

    override fun onResume() {
        super.onResume()

        // 自动弹出软键盘
        passwordField.postDelayed({ // 密码框请求获取焦点
            passwordField.requestFocus()
            // 弹出软键盘
            (Objects.requireNonNull(requireContext().getSystemService(INPUT_METHOD_SERVICE)) as InputMethodManager).showSoftInput(
                passwordField,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        }, 250)
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_password, container, false)

        // 获取取消按钮的实例
        cancel = view.findViewById(R.id.cancel)

        // 监听按钮的点击事件
        cancel.setOnClickListener { // 将 App 活动设为解锁状态
            // 结束活动
            (activity as MainActivity).pop()
        }

        // 初始化圆点的 ImageView
        for (i in dotImages.indices) {
            val dotImgId = resources.getIdentifier("password_dot_img$i", "id", requireActivity().packageName)
            dotImages[i] = view.findViewById(dotImgId)
        }

        // 获取提示布局的实例
        hintLayout = view.findViewById(R.id.hint_layout)
        // 获取提示文本的实例
        hintText = view.findViewById(R.id.hint)
        // 获取两根竖线的实例
        verticalLine1 = view.findViewById(R.id.vertical_line1)
        verticalLine2 = view.findViewById(R.id.vertical_line2)

        // 根据模式进行相应的初始化
        initView()

        // 获取密码框的实例
        passwordField = view.findViewById(R.id.password_field)

        // 监听文本框的文本变化事件
        passwordField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                // 即时切换图片
                dotImage()
                // 当输入完成时
                completeInput()
            }
        })

        return view
    }

    override fun onPause() {
        val manager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
        super.onPause()
    }

}