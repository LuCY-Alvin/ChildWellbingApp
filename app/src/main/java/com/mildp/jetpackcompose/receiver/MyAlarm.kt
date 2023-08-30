package com.mildp.jetpackcompose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mildp.jetpackcompose.utils.NotificationHelper

class MyAlarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)

        if (intent.action == "survey") {
            notificationHelper.createNotificationChannel()
            notificationHelper.showSurveyNotification()
        }
    }
}