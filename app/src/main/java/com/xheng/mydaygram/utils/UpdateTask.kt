package com.xheng.mydaygram.utils

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
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
}