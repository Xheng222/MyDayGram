package com.xheng.mydaygram.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.xheng.mydaygram.MainActivity
import com.xheng.mydaygram.R
import com.xheng.mydaygram.ui.MyTextView
import com.xheng.mydaygram.utils.BackupTask
import com.xheng.mydaygram.utils.ExportTask

class SettingFragment: BaseFragment(), View.OnClickListener {

    // 定义设置
    private lateinit var settings: SharedPreferences

    // 定义设置字体大小的按钮数组
    private val fontSizes = arrayOfNulls<ImageButton>(5)

    // 定义切换预览类型的按钮数组
    private val previewTypes = arrayOfNulls<ImageButton>(2)

    // 定义切换日记本边栏按钮
    private lateinit var useSideBar: ImageButton

    private lateinit var noSideBar: ImageButton

    // 定义使用系统字体的按钮
    private lateinit var useSystemFont: ImageButton

    // 定义使用自定义字体的按钮
    private lateinit var useCustomFont: ImageButton

    // 定义开启密码保护的按钮
    private lateinit var passwordOn: ImageButton

    // 定义关闭密码保护的按钮
    private lateinit var passwordOff: ImageButton

    inner class FontSizeListener: View.OnClickListener {
        override fun onClick(p0: View?) {
            // 将全部设置字体大小的按钮还原为未选中状态
            for (fontSize in fontSizes) {
                fontSize?.isSelected = false
            }

            // 获取被点击的按钮索引
            val index = p0?.tag as Int

            // 将被点击的按钮设为选中状态
            fontSizes[index]?.isSelected = true

            // 保存设置
            settings.edit().putInt("font.size", index).apply()
        }
    }
    inner class PreviewTypeListener : View.OnClickListener {
        override fun onClick(p0: View?) {
            // 将全部设置预览类型的按钮还原为未选中状态
            for (previewType in previewTypes) {
                previewType?.isSelected = false
            }

            // 获取被点击的按钮索引
            val index = p0?.tag as Int

            // 将被点击的按钮设为选中状态
            previewTypes[index]?.isSelected = true

            // 保存设置
            settings.edit().putInt("preview.type", index).apply()
        }
    }
    fun loadSettings() {

        val fontSize = settings.getInt("font.size", 2)
        fontSizes[fontSize]!!.isSelected = true

        val previewType = settings.getInt("preview.type", 1)
        if (previewType <= 1)
            previewTypes[previewType]?.isSelected = true

        if (settings.getBoolean("system.font.enabled", false)) {
            useCustomFont.isSelected = false
            useSystemFont.isSelected = true
        } else {
            useCustomFont.isSelected = true
            useSystemFont.isSelected = false
        }

        if (settings.getBoolean("passwordOn", false)) {
            passwordOff.isSelected = false
            passwordOn.isSelected = true
        } else {
            passwordOff.isSelected = true
            passwordOn.isSelected = false
        }

        if (settings.getBoolean("use.sidebar", true)) {
            useSideBar.isSelected = true
            noSideBar.isSelected = false
        } else {
            useSideBar.isSelected = false
            noSideBar.isSelected = true
        }
    }

    fun backup() {
        // 获取资源
        val res = resources

        // 创建备份任务
        val backupTask = BackupTask()

        // 创建对话框
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.app_name)
            .setItems(
                arrayOf(
                    res.getString(R.string.setting_backup_sd_backup),
                    res.getString(R.string.setting_backup_sd_restore)
                )
            ) { _, which ->
                when (which) {
                    0 ->
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.setting_backup_confirm)
                            .setPositiveButton(R.string.button_ok) {
                                _, _ -> backupTask.backup(0, requireContext())
                            }
                            .setNegativeButton(R.string.button_cancel, null)
                            .show()
                    1 ->
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.setting_restore_confirm)
                            .setPositiveButton(R.string.button_ok) {
                                    _, _ -> backupTask.backup(1, requireContext())
                            }
                            .setNegativeButton(R.string.button_cancel, null)
                            .show()
                }
            }.show()

    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "DiscouragedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // 获取设置
        settings = requireActivity().getSharedPreferences("settings", MODE_PRIVATE)

        try {
            // 获取版本号
            val packageInfo: PackageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            // 显示本版号
            val versionCode: MyTextView = view.findViewById(R.id.version_code)
            versionCode.text = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        // 获取反馈按钮的实例
        val feedback = view.findViewById<Button>(R.id.send_feedback)
        // 设置按钮的字体
        feedback.typeface = app.getAttrs("Arvil_Sans")
        // 监听按钮的点击事件
        feedback.setOnClickListener(this)

        // 获取评价按钮的实例
        val appraise = view.findViewById<Button>(R.id.appraise)
        // 设置按钮的字体
        appraise.typeface = app.getAttrs("Arvil_Sans")
        // 监听按钮的点击事件
        appraise.setOnClickListener(this)

        val test = view.findViewById<Button>(R.id.test)
        test.isVisible = false


        // 初始化设置字体大小的按钮
        for (i in 0..4) {
            val buttonId = resources.getIdentifier("font_size$i", "id", requireActivity().packageName)
            fontSizes[i] = view.findViewById(buttonId)
            fontSizes[i]?.tag  = i
            fontSizes[i]?.setOnClickListener(FontSizeListener())
        }

        // 初始化设置预览类型的按钮
        for (i in previewTypes.indices) {
            val buttonId = resources.getIdentifier("preview_type$i", "id", requireActivity().packageName)
            previewTypes[i] = view.findViewById(buttonId)
            previewTypes[i]?.tag = i
            previewTypes[i]?.setOnClickListener(PreviewTypeListener())
        }

        // 获取换日记本边栏按钮实例
        useSideBar = view.findViewById(R.id.use_sidebar_on)
        useSideBar.setOnClickListener(this)

        noSideBar = view.findViewById(R.id.use_sidebar_off)
        noSideBar.setOnClickListener(this)

        // 获取使用系统字体的按钮实例
        useSystemFont = view.findViewById(R.id.use_system_font)
        // 监听按钮的点击事件
        useSystemFont.setOnClickListener(this)

        // 获取使用自定义字体的按钮实例
        useCustomFont = view.findViewById(R.id.use_custom_font)
        // 监听按钮的点击事件
        useCustomFont.setOnClickListener(this)

        // 获取开启密码保护的按钮实例
        passwordOn = view.findViewById(R.id.password_on)
        // 设置按钮的点击事件
        passwordOn.setOnClickListener(this)

        // 获取关闭密码保护的按钮实例
        passwordOff = view.findViewById(R.id.password_off)
        // 设置按钮的点击事件
        passwordOff.setOnClickListener(this)

        // 获取更换更换密码的按钮实例
        val changePassword = view.findViewById<ImageButton>(R.id.change_password)
        // 设置按钮的点击事件
        changePassword.setOnClickListener(this)

        // 获取备份按钮的实例
        val backup = view.findViewById<ImageButton>(R.id.backup)
        // 设置按钮的点击事件
        backup.setOnClickListener(this)

        // 获取导出日记的按钮实例
        val export = view.findViewById<ImageButton>(R.id.export)
        // 设置按钮的点击事件
        export.setOnClickListener(this)

        // 获取离开设置的按钮实例
        val outSetting = view.findViewById<ImageButton>(R.id.out_Setting)
        // 设置按钮的点击事件
        outSetting.setOnClickListener(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            // 反馈按钮
            R.id.send_feedback -> {
                Log.e("MyDayGram", "R.id.send_feedback")
                val res = resources
                // 创建跳转到邮箱 App 的 Intent
                val intent = Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", "xheng222@163.com", null))
                // 添加邮件主题
                intent.putExtra("android.intent.extra.SUBJECT", res.getString(R.string.feedback_subject))
                // 添加邮件内容
                intent.putExtra("android.intent.extra.TEXT", res.getString(R.string.feedback_body_greeting))
                startActivity(Intent.createChooser(intent, res.getString(R.string.feedback_header)))
            }

            // 评价按钮
            R.id.appraise -> {
                (activity as MainActivity).toast("还是不要评价了吧...")

            }
            R.id.use_sidebar_on -> {
                useSideBar.isSelected = true
                noSideBar.isSelected = false
                settings.edit().putBoolean("use.sidebar", true).apply()
            }

            R.id.use_sidebar_off -> {
                useSideBar.isSelected = false
                noSideBar.isSelected = true
                settings.edit().putBoolean("use.sidebar", false).apply()
            }

            // 点击使用系统按钮的字体
            R.id.use_system_font -> {
                // 将使用自定义字体的按钮设为未选中状态
                useCustomFont.isSelected = false

                // 将使用系统字体的按钮设为选中状态
                useSystemFont.isSelected = true

                // 保存设置
                settings.edit().putBoolean("system.font.enabled", true).apply()
            }

            // 点击使用自定义字体的按钮
            R.id.use_custom_font -> {
                // 将使用自定义字体的按钮设为选中状态
                useCustomFont.isSelected = true

                // 将使用系统字体的按钮设为未选中状态
                useSystemFont.isSelected = false

                // 保存设置
                settings.edit().putBoolean("system.font.enabled", false).apply()
            }

            // 点击开启密码保护的按钮
            R.id.password_on -> {
                passwordOn.isSelected = true
                passwordOff.isSelected = false

                // 当密码不存在时
                if (settings.getString("password", "null").equals("null")) {
                    val bundle = Bundle()
                    bundle.putInt("mode", 2)
                    (activity as MainActivity).switchFragment("PasswordFragment", bundle)
                }
            }

            // 点击关闭密码保护的按钮
            R.id.password_off -> {
                passwordOn.isSelected = false
                passwordOff.isSelected = true

                // 当密码存在时
                if (settings.getBoolean("passwordOn", false)) {
                    Log.e("MyDayGram", "password_off")
                    val bundle = Bundle()
                    bundle.putInt("mode", 4)
                    (activity as MainActivity).switchFragment("PasswordFragment", bundle)
                }
            }

            // 点击修改密码的按钮
            R.id.change_password -> {
                passwordOn.isSelected = true
                passwordOff.isSelected = false
                val bundle = Bundle()
                if (settings.getString("password", "null").equals("null")) {
                    bundle.putInt("mode", 2)
                } else {
                    bundle.putInt("mode", 3)
                }
                (activity as MainActivity).switchFragment("PasswordFragment", bundle)
            }

            // 点击导出日记的按钮
            R.id.export -> {
                // 获取资源
                val res = resources
                // 创建对话框
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.app_name)
                    .setItems(
                        arrayOf<String>(
                            res.getString(R.string.setting_export_as_mail),
                            res.getString(R.string.setting_export_as_text)
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                val intent = ExportTask().exportTo(0)
                                if (intent != null) {
                                    startActivity(Intent.createChooser(intent, res.getString(R.string.feedback_header)))
                                }
                            }
                            1 -> {
                                val intent = ExportTask().exportTo(1)
                                if (intent != null) {
                                    startActivity(Intent.createChooser(intent, res.getString(R.string.app_name)))
                                }
                            }
                        }
                    }.show()
            }

            // 点击备份的按钮
            R.id.backup -> {
                // 当获得存储空间权限时
                if (XXPermissions.isGranted(requireContext(), Permission.MANAGE_EXTERNAL_STORAGE)) {
                    // 开始备份
                    backup()
                } else {
                    XXPermissions.with(requireContext())
                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                        .request(object: OnPermissionCallback{
                            override fun onGranted(
                                permissions: MutableList<String>,
                                allGranted: Boolean
                            ) {
                                if (!allGranted)
                                    Toast.makeText(requireContext(), "获取权限失败", Toast.LENGTH_SHORT).show()
                            }

                            override fun onDenied(
                                permissions: MutableList<String>,
                                doNotAskAgain: Boolean
                            ) {
                                if (doNotAskAgain) {
                                    Toast.makeText(requireContext(), "获取权限失败，请手动设置", Toast.LENGTH_SHORT).show()
                                    XXPermissions.startPermissionActivity(requireContext(), permissions)
                                } else {
                                    Toast.makeText(requireContext(), "获取权限失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                }
            }

            // 点击离开设置的按钮
            R.id.out_Setting -> {
                (activity as MainActivity).pop()
            }
        }
    }

}