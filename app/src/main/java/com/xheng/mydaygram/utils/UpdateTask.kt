package com.xheng.mydaygram.utils

import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class UpdateTask {
    private val url = URL("http://47.94.131.162/download/ex.json")

    suspend fun checkJSON(): JSONObject {
        val str = StringBuilder()
        var json: JSONObject
        val job = MainScope().async {
            delay(3000)
            try {
                val uc = withContext(Dispatchers.IO) {
                    url.openConnection()
                }
                val read = BufferedReader(InputStreamReader(withContext(Dispatchers.IO) {
                    uc.getInputStream()
                }, "GBK"))
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
            json = JSONObject(str.toString())
            json
        }

        return job.await()
    }

    fun download(context: Context) {
        val request = Request(Uri.parse("http://47.94.131.162/download/app-release.apk"))
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE or Request.NETWORK_WIFI)
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle("下载")
        request.setDescription("正在下载")
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "test.apk")
        Log.e("MyDayGram", Environment.DIRECTORY_DOWNLOADS)
        val downManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = downManager.enqueue(request)
        context.registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    install(context)
                } else if (p1?.action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                    val viewDownload = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                    viewDownload.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(viewDownload)
                }
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun install(context: Context) {
        val apk = File(context.getExternalFilesDir(null).toString(), "/download/test.apk")
        Log.e("MyDayGram", context.getExternalFilesDir(null).toString())
        if (apk.exists())
            Log.e("MyDayGram", "Yes")
        else {
            Log.e("MyDayGram", "No")
//            if (!apk.mkdirs())
//                Log.e("MyDayGram", "false")
//            else
//                Log.e("MyDayGram", "true")
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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