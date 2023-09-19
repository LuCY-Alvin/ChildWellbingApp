package com.mildp.jetpackcompose.receiver

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.service.ForegroundService
import com.mildp.jetpackcompose.model.service.UploadService
import com.mildp.jetpackcompose.utils.Constants.NOTIFICATION_ID3
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper

class Routine : BroadcastReceiver() {

    companion object {
        private const val TAG: String = "RoutineReceiver"
        private const val NOTIFICATION_ID_SERVICE = 29
    }
    private val cancelManager by lazy { App.instance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    object AppRunning {
        fun isAppRunning(context: Context): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses
            if (appProcesses != null) {
                for (processInfo in appProcesses) {
                    if (processInfo.processName == "com.mildp.jetpackcompose") {
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val success = kv.decodeBool("uploadServiceReady", false)

            if (AppRunning.isAppRunning(context)) {
                Helper().log(TAG, "App is alive, nothing happened now")
            } else {
                Helper().log(TAG, "App is dead, restart app now")
                startNewActivity(context)
            }

            Helper().log(TAG,"Upload status: $success")

            if (success) {
                Helper().log(TAG, "Start Upload Service")
                context.startService(Intent(context, UploadService::class.java))
            } else {
                cancelManager.cancel(NOTIFICATION_ID3)
            }

            Helper().checkPermission(
                isMyServiceRunning(App.instance()),
                TAG,
                "實驗被關閉",
                "Service_Stopped",
                NOTIFICATION_ID_SERVICE
            )

        } catch (e:Exception) {
            Helper().log(TAG,"Error in WorkManager: $e")
        }
    }

    private fun startNewActivity(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage("com.mildp.jetpackcompose")
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val marketIntent = Intent(Intent.ACTION_VIEW)
            marketIntent.data = Uri.parse("market://details?id=com.mildp.jetpackcompose")
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(marketIntent)
        }
    }

    private fun isMyServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Integer.MAX_VALUE)
        for (service in services) {
            if (ForegroundService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}