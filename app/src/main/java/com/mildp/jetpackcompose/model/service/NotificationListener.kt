package com.mildp.jetpackcompose.model.service

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.NotificationData
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.MyListener

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG: String = "NotificationListener"
    }

    private var myListener: MyListener? = null

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Helper().log(TAG,"NotificationListener called")
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Helper().log(TAG,"NotificationListener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Helper().log(TAG,"NotificationListener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val title = sbn.notification.extras.getCharSequence("android.title").toString()
        val text = sbn.notification.extras.getCharSequence("android.text").toString()

        try {
            myListener?.setValue("Post: ${sbn.packageName}")
            insertData(
                "Appear",
                sbn.packageName,
                title,
                text,
                Helper().timeString(sbn.postTime),
                sbn.postTime
            )
        } catch (e: Exception) {
            Helper().log(TAG,"Error in Notification Appeared： $e")
        }
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val title = sbn.notification.extras.getCharSequence("android.title").toString()
        val text = sbn.notification.extras.getCharSequence("android.text").toString()
        try {
            myListener?.setValue("Remove: ${sbn.packageName}")
            insertData(
                "Remove",
                sbn.packageName,
                title,
                text,
                Helper().timeString(sbn.postTime),
                sbn.postTime
            )
        } catch (e: Exception) {
            Helper().log(TAG,"Error in Notification Removed： $e")
        }
        super.onNotificationRemoved(sbn)
    }

    fun setListener(myListener: ForegroundService) {
        this.myListener = myListener
    }

    private fun insertData(Type: String, appName: String?,Title: String,Content: String , Time: String, MilliSeconds: Long){
        val notificationData = NotificationData(Helper().databaseDay(), Type, appName, Title, Content, Time, MilliSeconds)
        App.instance().dataDao.insertNotification(notificationData)
    }
}