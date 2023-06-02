package com.xheng.mydaygram.utils

import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class UpdateTask(
    private val context: Context
) {
    private val url = URL("http://47.94.131.162/download/ver.json")

    private lateinit var version: String

    private var json: JSONObject? = null

    var id: Long = -1

    init {
        MainScope().launch {
            checkJSON()
            Log.e("MyDayGram", "init")
        }
    }

    suspend fun checkJSON(): JSONObject? {
        val str = StringBuilder()
        // 版本信息
        if (json == null) {
            val job = MainScope().async {
                try {
                    val uc = withContext(Dispatchers.IO) {
                        url.openConnection()
                    }
                    val read = BufferedReader(InputStreamReader(withContext(Dispatchers.IO) {
                        uc.getInputStream()
                    }))
                    var inputLine: String?
                    inputLine = withContext(Dispatchers.IO) {
                        read.readLine()
                    }
                    while (inputLine != null) {
                        Log.e("MyDayGram", inputLine)
                        str.append(inputLine)
                        inputLine = withContext(Dispatchers.IO) {
                            read.readLine()
                        }
                    }
                    withContext(Dispatchers.IO) {
                        read.close()
                    }
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                Log.e("MyDayGram", str.toString())
                if (str.toString() != "")
                    json = JSONObject(str.toString())
                version = json?.getString("version").toString()
                // 判断
                val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (version == packageInfo.versionName)
                    json = null

                json
            }

            return job.await()
        } else
            return json
    }

    fun download() {
        when (id) {
            -1L -> {
                val request = Request(Uri.parse("http://47.94.131.162/download/app-release.apk"))
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$version.apk")
                request.setAllowedNetworkTypes(Request.NETWORK_MOBILE or Request.NETWORK_WIFI)
                request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setTitle("下载更新")
                request.setDescription("正在下载")
                request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "$version.apk")
                Log.e("MyDayGram", Environment.DIRECTORY_DOWNLOADS)
                val downManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
                id = downManager.enqueue(request)
                context.registerReceiver(object: BroadcastReceiver() {
                    override fun onReceive(p0: Context?, p1: Intent?) {
                        if (p1?.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                            p0?.let { install(it) }
                            id  = -10
                        }
                    }
                }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }

            -10L -> {
                install(context)
            }

            else -> {
                Toast.makeText(context, "正在下载...", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun install(context: Context) {
        val apk = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$version.apk")
        if (apk.exists()) {
            Log.e("MyDayGram", "install")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(context, "com.xheng.mydaygram.fileProvider", apk)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun delete() {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        if (version == packageInfo.versionName) {
            val apk = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$version.apk")
            if (apk.exists()) {
                try {
                    val result = apk.delete()
                    if (result) {
                        Log.e("MyDayGram", "delete")
                    } else {
                        Log.e("MyDayGram", "delete fail")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Log.e("MyDayGram", "no delete")
            }
        }
        else {
            Log.e("MyDayGram", "No file")
        }
    }
}