package com.mildp.jetpackcompose.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mildp.jetpackcompose.R
import com.mildp.jetpackcompose.activity.MainActivity
import com.mildp.jetpackcompose.utils.Constants.CHANNEL_ID2
import com.mildp.jetpackcompose.utils.Constants.kv

class NotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "NotificationHelper"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSurveyNotification() {
        val id = System.currentTimeMillis().toInt()
        val surveyIntent = Intent(context, MainActivity::class.java)

        surveyIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        surveyIntent.action = id.toString()

        //TaskStackBuilder
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(surveyIntent)
            getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_layout)
        kv.encode("childSurveyDone", false)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID2)
            .setContentTitle("")
            .setContentText("")
            .setSmallIcon(R.drawable.survey_notificaiton)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setColor(Color.YELLOW)
            .setColorized(true)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID2,notification)
        kv.encode("surveyCancelled",false)

        Helper().scheduleNotificationDismissal(notificationManager, 2 * 60 * 60 * 1000,TAG)
    }

    fun showBleNotification(service: Service){
        val notificationIntent = Intent(context, MainActivity::class.java)

        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(notificationIntent)
            getPendingIntent(1998, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat
            .Builder(context, Constants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(pendingIntent)
            .build()

        service.startForeground(Constants.NOTIFICATION_ID, notification)
    }

}
