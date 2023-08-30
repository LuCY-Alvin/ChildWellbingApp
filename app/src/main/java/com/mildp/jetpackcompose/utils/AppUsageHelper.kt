package com.mildp.jetpackcompose.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.EventTypeData
import com.mildp.jetpackcompose.model.ForegroundStats
import java.util.*

class AppUsageHelper {

    companion object {
        private const val TAG = "appUsageHelper"
    }

    fun getDailyAppUsage(startDay: Calendar): List<ForegroundStats> {

        var currentEvents: UsageEvents.Event
        val mUsageStatsManager =
            App.instance().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = App.instance().packageManager
        val events: LinkedList<EventTypeData> = LinkedList()

        startDay.set(Calendar.HOUR_OF_DAY, 0)
        startDay.set(Calendar.MINUTE, 0)
        startDay.set(Calendar.SECOND, 1)

        val usageEvents =
            mUsageStatsManager.queryEvents(startDay.timeInMillis, System.currentTimeMillis())

        while (usageEvents.hasNextEvent()) {
            currentEvents = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvents)

            if (currentEvents.eventType == UsageEvents.Event.ACTIVITY_PAUSED || currentEvents.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = currentEvents.timeStamp
                var appLabel = ""
                var appCategory =""
                try {
                    val appInfo: ApplicationInfo
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        appInfo = packageManager.getApplicationInfo(
                            currentEvents.packageName,
                            PackageManager.ApplicationInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        appInfo = packageManager.getApplicationInfo(
                            currentEvents.packageName,
                            PackageManager.GET_META_DATA
                        )
                    }
                    appLabel = packageManager.getApplicationLabel(appInfo).toString()
                    appCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appInfo.category.toString()
                    } else {
                        ""
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Helper().log(TAG, "Get UsageEvent Failed:$e")
                }

                val data = EventTypeData(
                    currentEvents.eventType,
                    appLabel,
                    appCategory,
                    calendar
                )
                events.add(data)
            }
        }
        completeFirstAndLast(events)
        return statsList(events)
    }

    private fun statsList(events: LinkedList<EventTypeData>): List<ForegroundStats> {
        val list: MutableList<ForegroundStats> = LinkedList()
        var start: EventTypeData?
        var end: EventTypeData?
        while (events.size >= 2) {
            start = events.poll()
            if(start?.type == 1){
                end = events.poll()
                if (end?.type ==2) {
                    list.add(ForegroundStats.fromEventType(start,end))
                }
            }
        }
        return list
    }

    private fun completeFirstAndLast(events: LinkedList<EventTypeData>) {
        val first: EventTypeData = events.first
        val last = events.last

        if (first.type == 2) {
            val calendar = first.time.clone() as Calendar
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val newFirst = EventTypeData(
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    first.app,
                    first.category,
                    calendar
                )
                events.add(0, newFirst)
            } else {
                @Suppress("DEPRECATION")
                val newFirst = EventTypeData(
                    UsageEvents.Event.MOVE_TO_FOREGROUND,
                    first.app,
                    first.category,
                    calendar
                )
                events.add(0, newFirst)
            }
        }

        if (last.type == 1) {
            val calendar = last.time.clone() as Calendar
            calendar[Calendar.HOUR_OF_DAY] = 23
            calendar[Calendar.MINUTE] = 59
            calendar[Calendar.SECOND] = 59
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val newLast = EventTypeData(
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    last.app,
                    last.category,
                    calendar
                )
                events.add(newLast)
            } else {
                @Suppress("DEPRECATION")
                val newLast = EventTypeData(
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    last.app,
                    last.category,
                    calendar
                )
                events.add(newLast)
            }
        }
    }
}