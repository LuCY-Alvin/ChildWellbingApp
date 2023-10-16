package com.mildp.jetpackcompose.model.service

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.receiver.ActivityRecognitionReceiver
import com.mildp.jetpackcompose.receiver.RestartReceiver
import com.mildp.jetpackcompose.receiver.ScreenReceiver
import com.mildp.jetpackcompose.utils.Constants.CHANNEL_ID
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.MyListener
import com.mildp.jetpackcompose.utils.NotificationHelper
import com.mildp.jetpackcompose.viewmodel.BleAdvViewModel
import com.mildp.jetpackcompose.viewmodel.BleScanViewModel
import com.mildp.jetpackcompose.viewmodel.SensorViewModel
import kotlinx.coroutines.*
import java.util.*

class ForegroundService : Service(), MyListener {

    companion object {
        private const val TAG: String = "ForegroundService"
        private const val NOTIFICATION_ID_BLUETOOTH = 24
        private const val NOTIFICATION_ID_GPS = 25
        private const val NOTIFICATION_ID_USAGE = 26
        private const val NOTIFICATION_ID_ACCESSIBILITY = 27
        private const val NOTIFICATION_ID_NOTIFICATION_PERMISSION = 28
    }

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var sensorViewModel: SensorViewModel
    private lateinit var bleScanViewModel: BleScanViewModel
    private lateinit var bleAdvViewModel: BleAdvViewModel

    private val mReceiver = ScreenReceiver()

    private var myBroadcast = ""
    private var partnerbroadcast = ""

    private var bleJob: Job = Job()
    private var checkPermissionJob: Job = Job()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Helper().log(TAG,"Start Service @ ${Helper().timeString(Calendar.getInstance().timeInMillis)}")

        myBroadcast = kv.decodeString("MyBroadcast","").toString()
        partnerbroadcast = kv.decodeString("PartnerBroadcast","").toString()
        val bandMac = kv.decodeString("bandMacSet","")
        Helper().log(TAG,"我的廣播：$myBroadcast、伴侶的廣播：$partnerbroadcast")
        Helper().log(TAG,"手環的廣播：$bandMac")

        NotificationListener().setListener(this)

        activityRecognitionClient = ActivityRecognition.getClient(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "familywellbeings:wakeLog")

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mReceiver,filter)

        sensorViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(SensorViewModel::class.java)
        bleScanViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(BleScanViewModel::class.java)
        bleAdvViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(BleAdvViewModel::class.java)

        val notificationHelper = NotificationHelper(this)

        try {
            notificationHelper.createNotificationChannel(CHANNEL_ID, "MY SERVICE", NotificationManager.IMPORTANCE_LOW)
            notificationHelper.showBleNotification(this)
            initBlueAdapter()
            Helper().log(TAG,"Service: Initiate notification and adapter")
        } catch(e: Exception) {
            Helper().log(TAG,"Service Initiate failed, Error:$e")
        }

        bleJob = repeatScanAndAdvertise()
        checkPermissionJob = repeatCheckPermission()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startActivityRecognitionTask()
        acquireWakeLock()

        val screenOff = intent?.getBooleanExtra("Screen_state",false)
        if (screenOff == true) {
            val screenOffTime = Calendar.getInstance().timeInMillis
            Helper().log(TAG, "Screen Off: ${Helper().timeString(screenOffTime)}")
        } else {
            val screenOnTime = Calendar.getInstance().timeInMillis
            Helper().log(TAG, "Screen On: ${Helper().timeString(screenOnTime)}")
        }
        return START_REDELIVER_INTENT
    }

    private fun initBlueAdapter() {

        Helper().log(TAG,"Init BlueAdapter")
        val bluetoothManager = App.instance().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val mBluetoothAdapter = bluetoothManager?.adapter

        Helper().log(TAG,"Is Multiple Advertisement Supported: " + mBluetoothAdapter?.isMultipleAdvertisementSupported)
    }

    private fun repeatScanAndAdvertise(): Job {
        return  CoroutineScope(Dispatchers.IO + bleJob).launch {
            while(true) {
                bleAdvViewModel.startAdvertise(myBroadcast)
                bleScanViewModel.startScan(partnerbroadcast)
                delay(60 * 1000)
            }
        }
    }

    private fun repeatCheckPermission(): Job{
        return CoroutineScope(Dispatchers.IO + checkPermissionJob).launch {
            while(true){
                check()
                delay(5 * 60 * 1000)
            }
        }
    }

////Check Permission and Devices Function/////
    private fun check() {

        val gpsManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Helper().checkPermission(
            gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER),
            TAG,
            "GPS被關閉",
            "GPS",
            NOTIFICATION_ID_GPS
        )

        val bluetoothManager: BluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        Helper().checkPermission(
            mBluetoothAdapter?.isEnabled == false,
            TAG,
            "藍芽被關閉",
            "BlueTooth",
            NOTIFICATION_ID_BLUETOOTH
        )

        Helper().permissionCheck(App.instance(), TAG,"appUsage")
        val appUsageCheck = kv.decodeBool("isAppUsageGranted",false)
        Helper().checkPermission(
            appUsageCheck,
            TAG,
            "應用程式設定被關閉",
            "Usage_Permission",
            NOTIFICATION_ID_USAGE
        )

        Helper().permissionCheck(App.instance(), TAG,"accessibilityService")
        val accessibilityCheck = kv.decodeBool("isAccessibilityGranted",false)
        Helper().checkPermission(
            accessibilityCheck,
            TAG,
            "無障礙設定被關閉",
            "Accessibility_Permission",
            NOTIFICATION_ID_ACCESSIBILITY
        )

        Helper().permissionCheck(App.instance(), TAG,"notificationService")
        val notificationCheck = kv.decodeBool("isNotificationListenerGranted",false)
        Helper().checkPermission(
            notificationCheck,
            TAG,
            "存取通知設定被關閉",
            "Notification_Permission",
            NOTIFICATION_ID_NOTIFICATION_PERMISSION
        )
    }

////ActivityRecognition Function/////
    private fun startActivityRecognitionTask() {
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED) {
            val task = activityRecognitionClient.requestActivityUpdates(
                10000L,
                getActivityRecognitionPendingIntent()
            )
            task.addOnSuccessListener {
                Helper().log(TAG, "Activity recognition started")
            }
            task.addOnFailureListener {
                Helper().log(TAG, "Activity recognition request failed: $it")
            }
        } else {
            Helper().log(TAG, "Is Activity recognition permission granted?")
        }
    }

    private fun getActivityRecognitionPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(
                this,
                19981221,
                intent,
                flags
        )
    }

////WakeLock Function/////
    private fun acquireWakeLock() {
        wakeLock.acquire(7 * 24 * 60 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onDestroy() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED) {
            activityRecognitionClient.removeActivityUpdates(getActivityRecognitionPendingIntent())
        }
        unregisterReceiver(mReceiver)
        releaseWakeLock()
        Helper().log(TAG, "app被關閉: Restart Receiver")

        val stopExp = kv.decodeBool("stopExp", false)

        if(!stopExp) {
            val broadcastIntent = Intent()
            broadcastIntent.action = "restart_service"
            broadcastIntent.setClass(this, RestartReceiver::class.java)
            this.sendBroadcast(broadcastIntent)
        } else {
            bleJob.cancel()
            checkPermissionJob.cancel()
        }

        super.onDestroy()
    }

    override fun setValue(packageName: String?) {
    }
}