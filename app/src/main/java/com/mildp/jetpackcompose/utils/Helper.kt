package com.mildp.jetpackcompose.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.AlarmStatus
import com.mildp.jetpackcompose.model.database.Debug
import com.mildp.jetpackcompose.model.service.AccessibilityService
import com.mildp.jetpackcompose.receiver.CheckReceiver
import com.mildp.jetpackcompose.receiver.MyAlarm
import com.mildp.jetpackcompose.utils.Constants.NOTIFICATION_ID2
import com.mildp.jetpackcompose.utils.Constants.kv
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Helper {

    fun timeString(milliseconds: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.TAIWAN)
        return  dateFormat.format(Date(milliseconds))
    }

    fun log(TAG: String, log: String){
        App.instance().dataDao.insertDebug(Debug(databaseDay(),TAG,log,timeString(Calendar.getInstance().timeInMillis)))
        Log.d(TAG,log)
    }

    fun databaseDay(): Int{
        val initTime = kv.decodeLong("initTime",0)
        val realTime = System.currentTimeMillis()

        return ( (realTime - initTime).toInt() / 86400000) + 1
    }

    fun setAlarm(TAG: String, time: Long){
        val alarmIntent = Intent(App.instance(), MyAlarm::class.java)
        val alarmManager = App.instance().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent.action = "survey"
        val pendingIntent =
            PendingIntent.getBroadcast(App.instance(), time.toInt(), alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
        log(TAG,"set alarm @ ${Helper().timeString(time)}")
    }

    fun permissionCheck(context: Context, TAG: String, permission: String) {
        when (permission) {
            "appUsage" -> {
                val appUsageGranted = isAppUsageGranted(context)
                log(TAG, "App usage permission is ${if (appUsageGranted) "granted" else "not granted"}")
                kv.encode("isAppUsageGranted", appUsageGranted)
            }
            "notificationService" -> {
                val notificationServiceGranted = isNotificationServiceGranted(context)
                log(TAG, "Notification service permission is ${if (notificationServiceGranted) "granted" else "not granted"}")
                kv.encode("isNotificationListenerGranted", notificationServiceGranted)
            }
            "accessibilityService" -> {
                val accessibilityServiceGranted = isAccessibilityService(context, AccessibilityService::class.java)
                log(TAG, "Accessibility service permission is ${if (accessibilityServiceGranted) "granted" else "not granted"}")
                kv.encode("isAccessibilityGranted", accessibilityServiceGranted)
            }
        }
    }

    fun checkPermission(
        checkBoolean:Boolean,
        TAG: String,
        logText: String,
        action: String,
        notificationId: Int
    ) {
        val checkManager = App.instance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(!checkBoolean) {
            log(TAG,logText)
            val statusIntent = Intent()
            statusIntent.action = action
            statusIntent.setClass(App.instance(), CheckReceiver::class.java)
            App.instance().sendBroadcast(statusIntent)
        } else {
            checkManager.cancel(notificationId)
        }
    }

    private fun isAppUsageGranted(context: Context): Boolean {
        val appOpsManager = context.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
        val packageManager = context.packageManager
        val appInfo: ApplicationInfo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appInfo = packageManager.getApplicationInfo(context.packageName, PackageManager.ApplicationInfoFlags.of(0))
        }else{
            @Suppress("DEPRECATION")
            appInfo = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        }
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(), appInfo.packageName
            )
        }
        else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),  appInfo.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isNotificationServiceGranted(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver,"enabled_notification_listeners")
        if(!TextUtils.isEmpty(flat)){
            val names = flat.split(":")
            for (element in names) {
                val cn = ComponentName.unflattenFromString(element)
                if (cn != null) {
                    if (TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isAccessibilityService(context: Context, service: Class<AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enableServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (i in enableServices){
            val enableServiceInfo = i.resolveInfo.serviceInfo
            if (enableServiceInfo.packageName == context.packageName && enableServiceInfo.name == service.name)
                return true
        }
        return false
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalSerializationApi::class)
    fun scheduleNotificationDismissal(notificationManager: NotificationManager, timeMillis: Long, TAG: String) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(timeMillis)
            val surveyCancelled = kv.decodeBool("surveyCancelled", false)
            if (!surveyCancelled) {
                log(TAG,"Survey AutoRemove @ ${System.currentTimeMillis()}")

                notificationManager.cancel(NOTIFICATION_ID2)
                kv.encode("surveyCancelled",true)

                val storedAlarmStatusJson = kv.decodeString("alarmStatus", null)
                val alarmStatus: MutableList<Pair<Long, AlarmStatus>> =
                    if (storedAlarmStatusJson != null) {
                        Json.decodeFromString(storedAlarmStatusJson)
                    } else {
                        mutableListOf()
                    }

                val firstFalseIndex = alarmStatus.indexOfFirst { it.second == AlarmStatus.PREPARING }
                if (firstFalseIndex >= 0) {
                    alarmStatus[firstFalseIndex] = Pair(alarmStatus[firstFalseIndex].first, AlarmStatus.MISSED)
                    val updatedAlarmStatusJson = Json.encodeToString(alarmStatus)
                    kv.encode("alarmStatus", updatedAlarmStatusJson)
                }

                kv.encode("uploadServiceReady", true)
            } else {
                log(TAG, "通知已取消 @ ${System.currentTimeMillis()}")
            }
        }
    }

    fun myWorkManager(){
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)

        val myRequest = PeriodicWorkRequest.Builder(
            Worker::class.java,
            15,
            TimeUnit.MINUTES
        ).setConstraints(constraints.build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .addTag("worker")
            .build()
        WorkManager.getInstance(App.instance())
            .enqueueUniquePeriodicWork(
                "worker",
                ExistingPeriodicWorkPolicy.KEEP,
                myRequest
            )
    }
}