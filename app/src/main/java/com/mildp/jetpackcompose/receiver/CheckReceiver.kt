package com.mildp.jetpackcompose.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mildp.jetpackcompose.R
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.NotificationHelper

class CheckReceiver : BroadcastReceiver() {

    private lateinit var checkManager: NotificationManager
    companion object {
        private const val TAG: String = "CheckReceiver"
        private const val CHANNEL_ID = "CheckStatusChannel"
        private const val NOTIFICATION_ID_GPS = 25
        private const val NOTIFICATION_ID_USAGE = 26
        private const val NOTIFICATION_ID_ACCESSIBILITY = 27
        private const val NOTIFICATION_ID_NOTIFICATION_PERMISSION = 28
        private const val NOTIFICATION_ID_SERVICE = 29
    }

    override fun onReceive(context: Context, intent: Intent) {

        checkManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationHelper = NotificationHelper(context)
        notificationHelper.createNotificationChannel(CHANNEL_ID, "CheckStatus", NotificationManager.IMPORTANCE_HIGH)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("您有尚未打開的設備，請您檢查以下功能是否開啟")
            .setSmallIcon(R.drawable.warning_hint_notification)

        when (intent.action) {
            "GPS" -> {
                notification.setContentText("由於實驗需求，盼請您開啟GPS，以求資料完整。重新設定後可以直接滑掉此通知。")
                checkManager.notify(NOTIFICATION_ID_GPS, notification.build())
                Helper().log(TAG, "未打開GPS")
            }
            "Usage_Permission" -> {
                notification.setContentText("您的權限可能被系統關閉，盼請您開啟應用程式設定。重新設定後可以直接滑掉此通知")
                checkManager.notify(NOTIFICATION_ID_USAGE, notification.build())
                Helper().log(TAG, "未打開應用程式設定")
            }
            "Accessibility_Permission" -> {
                notification.setContentText("您的權限可能被系統關閉，盼請您開啟應用程式設定。重新設定後可以直接滑掉此通知")
                checkManager.notify(NOTIFICATION_ID_ACCESSIBILITY, notification.build())
                Helper().log(TAG, "未打開無障礙設定")
            }
            "Notification_Permission" -> {
                notification.setContentText("您的權限可能被系統關閉，盼請您開啟應用程式設定。重新設定後可以直接滑掉此通知")
                checkManager.notify(NOTIFICATION_ID_NOTIFICATION_PERMISSION, notification.build())
                Helper().log(TAG, "未打開通知設定")
            }
            "Service_Stopped" -> {
                notification.setContentText("您的實驗可能被關閉，盼請您進入App設定頁面按下開始實驗。重新設定後可以直接滑掉此通知")
                checkManager.notify(NOTIFICATION_ID_SERVICE, notification.build())
                Helper().log(TAG, "Service關閉")
            }
        }
    }
}