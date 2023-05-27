package com.xheng.mydaygram.utils

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class UpdateTask {
    private val url = URL("http://127.0.0.1")

    private fun getJson(): JSONObject {
        val str = StringBuilder()
        try {
            val uc = url.openConnection()
            val read = BufferedReader(InputStreamReader(uc.getInputStream(), "UTF-8"))
            var inputLine: String? = null
            inputLine = read.readLine()
            while (inputLine != null) {
                str.append(inputLine)
                inputLine = read.readLine()
            }
            read.close()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return JSONObject(str.toString())
    }

    fun checkJSON() {
        val json = getJson()
        Log.e("MyDayGram", json.getString("version"))
    }
}