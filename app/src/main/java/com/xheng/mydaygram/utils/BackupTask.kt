package com.xheng.mydaygram.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.xheng.mydaygram.R
import com.xheng.mydaygram.application.MyLitePalApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class BackupTask {
    private val app = MyLitePalApplication.getInstance()

    private val resources = app.resources

    fun backup(patten: Int, context: Context) {
        // 定义数据库文件
        val dataBaseFile = File(app.getDatabasePath("demo").toString() + ".db")

        // 定义备份的目录
        val backupDir = File(Environment.getExternalStorageDirectory(), "DayGram")

        // 当备份目录不存在时
        if (!backupDir.exists()) {
            // 创建
            backupDir.mkdirs()
        }

        // 定义数据库副本
        val backupFile = File(backupDir, dataBaseFile.name)

        when (patten) {
            0 ->  {
                // 使用协程开始备份
                MainScope().launch {
                    // 当数据库不存在时
                    if (!dataBaseFile.exists()) {
                        Log.e("MyDayGram", resources.getString(R.string.database_not_found))
                        Toast.makeText(context, resources.getString(R.string.database_not_found), Toast.LENGTH_SHORT).show()
                    } else {

                        try {
                            // 创建副本文件
                            withContext(Dispatchers.IO) {
                                backupFile.createNewFile()
                            }
                                // 复制数据库到副本文件
                                backFile(dataBaseFile, backupFile)

                                Log.e("MyDayGram", resources.getString(R.string.backup_success))
                                Toast.makeText(context, resources.getString(R.string.backup_success), Toast.LENGTH_SHORT).show()

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("MyDayGram", resources.getString(R.string.backup_failed))
                            Toast.makeText(context, resources.getString(R.string.backup_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            1 -> {
                MainScope().launch {
                    // 当数据库副本不存在时
                    if (!backupFile.exists()) {
                        Log.e("MyDayGram", resources.getString(R.string.backup_not_found))
                        Toast.makeText(context, resources.getString(R.string.backup_not_found), Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            // 复制数据库副本到数据库
                            backFile(backupFile, dataBaseFile)

                            Log.e("MyDayGram", resources.getString(R.string.restore_success))
                            Toast.makeText(context, resources.getString(R.string.restore_success), Toast.LENGTH_SHORT).show()

                        } catch (e: Exception) {
                            Log.e("MyDayGram", resources.getString(R.string.restore_failed))
                            Toast.makeText(context, resources.getString(R.string.restore_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else -> {
                Log.e("MyDayGram", resources.getString(R.string.unknown_error))
                Toast.makeText(context, resources.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun backFile(file1: File, file2: File) {
        try {
            val inChannel = FileInputStream(file1).channel
            val outChannel = FileOutputStream(file2).channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
        }catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
}