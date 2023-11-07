package com.mildp.jetpackcompose.receiver

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.UsageData
import com.mildp.jetpackcompose.model.service.ForegroundService
import com.mildp.jetpackcompose.model.service.UploadService
import com.mildp.jetpackcompose.utils.AppUsageHelper
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class StopExpReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "StopExpReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(checkPermission()) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    for (stats in AppUsageHelper().getDailyAppUsage(Calendar.getInstance())){
                        val usageData = UsageData(Helper().databaseDay(),stats.app, stats.category, stats.startTimeString,stats.endTimeString)
                        App.instance().dataDao.insertUsage(usageData)
                    }
                }
            } catch (e:Exception) {
                Helper().log(TAG,"Get App Usage failed: $e")
            }
        } else {
            Helper().log(TAG,"There's App Usage permission issues.")
        }

        val stopIntent = Intent(context,ForegroundService::class.java)
        stopIntent.action = "stopExp"
        kv.encode("stopExp",true)
        context.stopService(stopIntent)

        context.startService(Intent(context, UploadService::class.java))
    }

    private fun checkPermission(): Boolean {
        val appOpsManager = App.instance().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(), App.instance().packageName
            )
        }
        else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(), App.instance().packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

}