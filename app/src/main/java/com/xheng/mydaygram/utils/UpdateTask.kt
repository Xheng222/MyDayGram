package com.xheng.mydaygram.utils

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class UpdateTask {
    private val url = URL("http://47.94.131.162/download/ex.json")

    private fun getJson() {


    }

    @OptIn(DelicateCoroutinesApi::class)
    fun checkJSON() {
        GlobalScope.launch {
            val str = StringBuilder()
            try {
                val uc = url.openConnection()
                val read = BufferedReader(InputStreamReader(uc.getInputStream(), "UTF-8"))
                var inputLine: String? = null
                inputLine = read.readLine()
                while (inputLine != null) {
                    Log.e("MyDayGram", inputLine)
                    str.append(inputLine)
                    inputLine = read.readLine()
                }
                read.close()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val json = JSONObject(str.toString())
            Log.e("MyDayGram", json.getString("version"))
        }

    }
}