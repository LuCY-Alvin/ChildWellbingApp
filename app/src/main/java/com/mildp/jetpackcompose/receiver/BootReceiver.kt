package com.mildp.jetpackcompose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.AlarmStatus
import com.mildp.jetpackcompose.model.service.ForegroundService
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG: String = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {

        Helper().log(TAG,"Boot Completed")

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            resetUnfinishedAlarms()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, ForegroundService::class.java))
            } else {
                context.startService(Intent(App.instance(), ForegroundService::class.java))
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun resetUnfinishedAlarms() {

        val bootTimeInMillis = System.currentTimeMillis()
        val storedAlarmStatusJson = kv.decodeString("alarmStatus", null)
        val alarmStatus: MutableList<Pair<Long, AlarmStatus>> = if (storedAlarmStatusJson != null) {
            Json.decodeFromString(storedAlarmStatusJson)
        } else {
            mutableListOf()
        }

        alarmStatus.forEachIndexed { index, (timeInMillis, completed) ->
            if (timeInMillis + 2 * 60 * 60 * 1000 > bootTimeInMillis) {
                alarmStatus[index] = timeInMillis to AlarmStatus.PREPARING
                Helper().setAlarm(TAG, timeInMillis)
            } else if (timeInMillis + 2 * 60 * 60 * 1000 < bootTimeInMillis && completed == AlarmStatus.FINISHED) {
                alarmStatus[index] = timeInMillis to AlarmStatus.FINISHED
            } else {
                alarmStatus[index] = timeInMillis to AlarmStatus.MISSED
            }
        }

        val updatedAlarmStatusJson = Json.encodeToString(alarmStatus)
        kv.encode("alarmStatus", updatedAlarmStatusJson)
    }

}