package com.xheng.mydaygram.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RemindTask: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("MyDayGram", "消息")
    }

}