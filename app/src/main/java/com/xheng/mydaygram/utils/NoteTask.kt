package com.xheng.mydaygram.utils


import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xheng.mydaygram.MainActivity
import com.xheng.mydaygram.R

class NoteTask(
    private val context: Context
) {

    private lateinit var myNotification: NotificationCompat.Builder

    private val myNotificationId = 1000

    companion object {

        const val CHANNEL_ID = "com.kotlin.kotlin_start_ch18.CHANNEL_ID"
        const val CHANNEL_NAME = "Sample Notification"
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun notifyMessage(){
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val res = context.resources
        val myNotificationManager = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.setShowBadge(true)
        myNotificationManager.createNotificationChannel(channel)

        myNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("重要通知")
            .setContentText("重要通知内容cccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
            .setAutoCancel(true)
            .setNumber(999) // 自定义桌面通知数量
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pendingIntent, true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        myNotificationManager.notify(myNotificationId, myNotification.build())

    }



}