package com.mildp.jetpackcompose.model.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.AccessibilityData
import com.mildp.jetpackcompose.utils.Helper
import java.text.SimpleDateFormat
import java.util.*

class AccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG: String = "AccessibilityService"
    }

    override fun onServiceConnected() {
        Helper().log(TAG,"Accessibility service connected")
        val info = AccessibilityServiceInfo()
        info.eventTypes =
            AccessibilityEvent.TYPE_VIEW_CLICKED or
            AccessibilityEvent.TYPE_VIEW_SCROLLED or
            AccessibilityEvent.TYPE_VIEW_FOCUSED or
            AccessibilityEvent.TYPE_VIEW_SELECTED or
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100

        this.serviceInfo
    }

    @SuppressLint("SwitchIntDef")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val eventType = event?.eventType
        var appLabel = getApplicationLabel(event?.packageName.toString())
        if (appLabel == ""){
            appLabel = "NameNull"
        }
        val calendarMilliseconds = Calendar.getInstance().timeInMillis
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.TAIWAN).format(Date(calendarMilliseconds))

        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val type = "TYPE_VIEW_CLICKED"
                insertData(appLabel,type,time,calendarMilliseconds)
            }
            AccessibilityEvent.TYPE_VIEW_SELECTED -> {
                val type = "TYPE_VIEW_SELECTED"
                insertData(appLabel,type,time,calendarMilliseconds)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                val type = "TYPE_VIEW_TEXT_SELECTION_CHANGED"
                insertData(appLabel,type,time,calendarMilliseconds)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                val type = "TYPE_VIEW_SCROLLED"
                insertData(appLabel,type,time,calendarMilliseconds)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                val type = "TYPE_VIEW_FOCUSED"
                insertData(appLabel,type,time,calendarMilliseconds)
            }
        }
    }

    override fun onInterrupt() {
        Helper().log(TAG,"Accessibility onInterrupt")
    }

    private fun insertData(appLabel:String?, type:String, time:String, calendarMilliseconds:Long){
        val accessibilityData = AccessibilityData(Helper().databaseDay(), appLabel, type, time, calendarMilliseconds)
        App.instance().dataDao.insertAccessibility(accessibilityData)
    }

    private fun getApplicationLabel(packageName: String?): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val appInfo = packageManager.getApplicationInfo(packageName.toString(), PackageManager.ApplicationInfoFlags.of(0))
                packageManager.getApplicationLabel(appInfo).toString()
            }else {
                @Suppress("DEPRECATION")
                val appInfo = packageManager.getApplicationInfo(packageName.toString(), PackageManager.GET_META_DATA)
                packageManager.getApplicationLabel(appInfo).toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Helper().log(TAG,"Name Not Found: $e")
            packageName
        }
    }
}