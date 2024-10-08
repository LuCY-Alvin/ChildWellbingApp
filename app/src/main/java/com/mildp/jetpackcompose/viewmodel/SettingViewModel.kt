package com.mildp.jetpackcompose.viewmodel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.AlarmStatus
import com.mildp.jetpackcompose.model.service.ForegroundService
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.*
import java.util.*

class SettingViewModel: ViewModel() {

    companion object {
        private const val TAG: String = "SettingsPage"
    }

    var subID by mutableStateOf(kv.decodeString("subID",null).toString())

    private val _isEditing = mutableStateOf(kv.decodeBool("edit",true))
    val isEditing: State<Boolean> = _isEditing

    var pickedDate: LocalDate by mutableStateOf(LocalDate.now())
    var pickedMorning: LocalTime by mutableStateOf(LocalTime.of(8,0))
    var pickedAfternoon: LocalTime by mutableStateOf(LocalTime.of(14,0))
    var pickedNight: LocalTime by mutableStateOf(LocalTime.of(20,0))

    private val storedAlarmStatusJson = kv.decodeString("alarmStatus", null)
    @OptIn(ExperimentalSerializationApi::class)
    val alarmStatus: MutableList<Pair<Long, AlarmStatus>> = if (storedAlarmStatusJson != null) {
        Json.decodeFromString(storedAlarmStatusJson)
    } else {
        mutableListOf()
    }

    init {
        pickedDate = loadPickedDate()
        pickedMorning = loadPickedMorningTime()
        pickedAfternoon = loadPickedAfternoonTime()
        pickedNight = loadPickedNightTime()
    }

    private fun loadPickedDate(): LocalDate {
        val storedTimeMillis = kv.decodeLong("picked_date", -1L)
        return if (storedTimeMillis != -1L) {
            LocalDate.ofEpochDay(storedTimeMillis)
        } else {
            LocalDate.now()
        }
    }

    private fun loadPickedMorningTime(): LocalTime {
        val storedTimeMillis = kv.decodeLong("picked_morning", -1L)
        return if (storedTimeMillis != -1L) {
            LocalTime.ofNanoOfDay(storedTimeMillis)
        } else {
            LocalTime.of(8,0)
        }
    }

    private fun loadPickedAfternoonTime(): LocalTime {
        val storedTimeMillis = kv.decodeLong("picked_afternoon", -1L)
        return if (storedTimeMillis != -1L) {
            LocalTime.ofNanoOfDay(storedTimeMillis)
        } else {
            LocalTime.of(14,0)
        }
    }

    private fun loadPickedNightTime(): LocalTime {
        val storedTimeMillis = kv.decodeLong("picked_night", -1L)
        return if (storedTimeMillis != -1L) {
            LocalTime.ofNanoOfDay(storedTimeMillis)
        } else {
            LocalTime.of(20,0)
        }
    }

    var isBatteryOptimizeClosed: Boolean
        get() = kv.decodeBool("isBatteryOptimizeClosed", false)
        set(value) = saveBatteryOptimizeState(value)

    private fun saveBatteryOptimizeState(value: Boolean) {
        viewModelScope.launch {
            kv.encode("isBatteryOptimizeClosed", value)
        }
    }

    var isAppUsageGranted: Boolean
        get() = kv.decodeBool("isAppUsageGranted", false)
        set(value) = saveAppUsageState(value)

    private fun saveAppUsageState(value: Boolean) {
        viewModelScope.launch {
            kv.encode("isAppUsageGranted", value)
        }
    }

    var isAccessibilityGranted: Boolean
        get() = kv.decodeBool("isAccessibilityGranted", false)
        set(value) = saveAccessibilityState(value)

    private fun saveAccessibilityState(value: Boolean) {
        viewModelScope.launch {
            kv.encode("isAccessibilityGranted", value)
        }
    }

    var isNotificationListenerGranted: Boolean
        get() = kv.decodeBool("isNotificationListenerGranted", false)
        set(value) = saveNotificationListenerState(value)

    private fun saveNotificationListenerState(value: Boolean) {
        viewModelScope.launch {
            kv.encode("isNotificationListenerGranted", value)
        }
    }

    fun checkPermission(){
        Helper().permissionCheck(App.instance(),TAG,"appUsage")
        Helper().permissionCheck(App.instance(),TAG,"accessibilityService")
        Helper().permissionCheck(App.instance(),TAG,"notificationService")
    }

    @SuppressLint("BatteryLife")
    fun onBatteryButtonClick() {
        val pm = App.instance().getSystemService(POWER_SERVICE) as PowerManager
        val packageName = App.instance().packageName

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance().startActivity(intent)
            saveBatteryOptimizeState(true)
        } else {
            saveBatteryOptimizeState(true)
            Toast.makeText(App.instance(),"已關閉省電最佳化",Toast.LENGTH_SHORT).show()
        }

    }

    fun onAppUsageButtonClick(appUsageCheck: Boolean) {
        if (!appUsageCheck) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance().startActivity(intent)
        }
    }

    fun onAccessibilityButtonClick(accessibilityCheck: Boolean) {
        if (!accessibilityCheck) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance().startActivity(intent)
        }
    }

    fun onNotificationButtonClick(notificationCheck: Boolean) {
        if (!notificationCheck) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance().startActivity(intent)
        }
    }

    fun onBootCompletedClicked() {
        val componentName: ComponentName?
        val context = App.instance()
        Helper().log(TAG,Build.MANUFACTURER)
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            when (Build.MANUFACTURER.lowercase()) {
                "xiaomi" -> {
                    componentName = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                    intent.component = componentName
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                "vivo" -> {
                    componentName = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                    intent.component = componentName
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                "asus" -> {
                    componentName = ComponentName(
                        "com.asus.mobilemanager",
                        "com.asus.mobilemanager.entry.FunctionActivity"
                    )
                    intent.component = componentName
                    intent.data = Uri.parse("mobilemanager://function/entry/AutoStart")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                "oppo" -> {
                    componentName = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                    intent.component = componentName
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                "huawei" -> {
                    componentName = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                    intent.component = componentName
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                else -> {
                    intent.action = Settings.ACTION_SETTINGS
                    context.startActivity(intent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            Helper().log(TAG, "There is an error in Boot Setting: $e")
            Toast.makeText(context, "Boot setting not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    fun onAppInformClicked() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", App.instance().packageName, null)
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        App.instance().startActivity(intent)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun onStoreOrEdit() {
        if(isEditing.value) {
            kv.encode("subID", subID)
            kv.encode("edit",false)
            _isEditing.value = false
            uuidGenerator()

            if (storedAlarmStatusJson == null) {
                val morningInstant = ZonedDateTime.of(pickedDate, pickedMorning, ZoneOffset.systemDefault()).toInstant()
                val afternoonInstant = ZonedDateTime.of(pickedDate, pickedAfternoon, ZoneOffset.systemDefault()).toInstant()
                val nightInstant = ZonedDateTime.of(pickedDate, pickedNight, ZoneOffset.systemDefault()).toInstant()

                kv.encode("picked_date", pickedDate.toEpochDay())
                kv.encode("picked_morning", pickedMorning.toNanoOfDay())
                kv.encode("picked_afternoon", pickedAfternoon.toNanoOfDay())
                kv.encode("picked_night", pickedNight.toNanoOfDay())

                val alarmStatus = mutableListOf<Pair<Long, AlarmStatus>>()

                for (i in 0..6) {
                    addAlarm(alarmStatus, morningInstant, i)
                    addAlarm(alarmStatus, afternoonInstant, i)
                    addAlarm(alarmStatus, nightInstant, i)
                }

                val initTime = ZonedDateTime.of(pickedDate, LocalTime.MIDNIGHT, ZoneOffset.systemDefault())
                val initTimeInMillis = initTime.toInstant().toEpochMilli()

                val alarmStatusJson = Json.encodeToString(alarmStatus)
                kv.encode("alarmStatus", alarmStatusJson)
                kv.encode("initTime", initTimeInMillis)

                alarmStatus.forEach {
                    if (it.second == AlarmStatus.PREPARING) Helper().setAlarm(TAG, it.first)
                }
            }

            Firebase.crashlytics.setUserId(subID)
            alarmStatus.forEach {
                if (it.second == AlarmStatus.PREPARING) Helper().setAlarm(TAG, it.first)
            }
        } else {
            kv.encode("edit",true)
            _isEditing.value = true
        }
    }

    private fun addAlarm(alarmStatus: MutableList<Pair<Long,AlarmStatus>>,instant: Instant, i: Int) {
        val currentTime = System.currentTimeMillis()
        if(currentTime > instant.toEpochMilli() + i * 86400000) {
            alarmStatus.add(Pair(instant.toEpochMilli() + i * 86400000, AlarmStatus.MISSED))
        } else {
            alarmStatus.add(Pair(instant.toEpochMilli() + i * 86400000, AlarmStatus.PREPARING))
        }
    }

    private fun uuidGenerator(){
        val id = kv.decodeString("subID", "")
        if (id != null) {
            val myUuid = UUID.nameUUIDFromBytes(id.toByteArray()).toString()
            kv.encode("MyBroadcast", myUuid)
            if (id.contains("F")) {
                val partnerId = id.replace("F", "M")
                val partnerUuid = UUID.nameUUIDFromBytes(partnerId.toByteArray()).toString()
                kv.encode("PartnerBroadcast", partnerUuid)
            } else if (id.contains("M")) {
                val partnerId = id.replace("M", "F")
                val partnerUuid = UUID.nameUUIDFromBytes(partnerId.toByteArray()).toString()
                kv.encode("PartnerBroadcast", partnerUuid)
            } else {
                Toast.makeText(App.instance(),"編號設定不符合要求，請重新確認。",Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(App.instance(),"請先設定實驗編號！",Toast.LENGTH_SHORT).show()
        }
    }

    fun startMyProject() {
        val bluetoothManager = App.instance().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val mBluetoothAdapter = bluetoothManager?.adapter
        val gpsManager = App.instance().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val bandMacSet = kv.decodeString("bandMacSet", "")

        if (mBluetoothAdapter?.isEnabled == false || !gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(App.instance(), "請先開啟藍芽及GPS定位系統再儲存設定", Toast.LENGTH_SHORT).show()
        } else if (!isBatteryOptimizeClosed || !isAppUsageGranted || !isAccessibilityGranted || !isNotificationListenerGranted) {
            Toast.makeText(App.instance(), "請點擊設定開啟所有系統權限", Toast.LENGTH_SHORT).show()
        } else if (subID == "") {
            Toast.makeText(App.instance(), "請設定參與編號", Toast.LENGTH_SHORT).show()
        } else if (bandMacSet == ""){
            Toast.makeText(App.instance(), "請先設定小米手環", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(App.instance(), "測驗將在您設定的時間通知您", Toast.LENGTH_SHORT).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                App.instance().startForegroundService(Intent(App.instance(), ForegroundService::class.java))
            } else {
                App.instance().startService(Intent(App.instance(), ForegroundService::class.java))
            }
        }
    }

    fun isMyServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Integer.MAX_VALUE)
        for (service in services) {
            if (ForegroundService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun resetUnfinishedAlarms() {

        val nowTimeInMillis = System.currentTimeMillis()
        val storedAlarmStatusJson = kv.decodeString("alarmStatus", null)
        val alarmStatus: MutableList<Pair<Long, AlarmStatus>> = if (storedAlarmStatusJson != null) {
            Json.decodeFromString(storedAlarmStatusJson)
        } else {
            mutableListOf()
        }

        alarmStatus.forEachIndexed { index, (timeInMillis, completed) ->
            if (timeInMillis + 2 * 60 * 60 * 1000 > nowTimeInMillis) {
                alarmStatus[index] = timeInMillis to AlarmStatus.PREPARING
                Helper().setAlarm(TAG, timeInMillis)
            } else if (timeInMillis + 2 * 60 * 60 * 1000 < nowTimeInMillis && completed == AlarmStatus.FINISHED) {
                alarmStatus[index] = timeInMillis to AlarmStatus.FINISHED
            } else {
                alarmStatus[index] = timeInMillis to AlarmStatus.MISSED
            }
        }

        val updatedAlarmStatusJson = Json.encodeToString(alarmStatus)
        kv.encode("alarmStatus", updatedAlarmStatusJson)
        Toast.makeText(App.instance(),"已成功重置測驗，請到首頁確認",Toast.LENGTH_SHORT).show()
    }
}