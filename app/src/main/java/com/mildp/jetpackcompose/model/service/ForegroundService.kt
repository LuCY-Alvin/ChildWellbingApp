package com.mildp.jetpackcompose.model.service

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.activity.MainActivity
import com.mildp.jetpackcompose.model.database.AcceleratorData
import com.mildp.jetpackcompose.model.database.GyroData
import com.mildp.jetpackcompose.model.database.RSSIData
import com.mildp.jetpackcompose.receiver.ActivityRecognitionReceiver
import com.mildp.jetpackcompose.receiver.CheckReceiver
import com.mildp.jetpackcompose.receiver.RestartReceiver
import com.mildp.jetpackcompose.receiver.ScreenReceiver
import com.mildp.jetpackcompose.utils.Constants.CHANNEL_ID
import com.mildp.jetpackcompose.utils.Constants.NOTIFICATION_ID
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.MyListener
import kotlinx.coroutines.*
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import java.util.*

class ForegroundService : Service(), SensorEventListener, MyListener {

    companion object {
        private const val TAG: String = "ForegroundService"
        private const val NOTIFICATION_ID_BLUETOOTH = 24
        private const val NOTIFICATION_ID_GPS = 25
        private const val NOTIFICATION_ID_USAGE = 26
        private const val NOTIFICATION_ID_ACCESSIBILITY = 27
        private const val NOTIFICATION_ID_NOTIFICATION_PERMISSION = 28
    }

    private lateinit var manager: NotificationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var acc: Sensor
    private lateinit var gyro: Sensor
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var wakeLock: PowerManager.WakeLock

    private val mReceiver = ScreenReceiver()

    private var advertisingCallback: AdvertiseCallback? = null
    private var mScanning = false
    private var mybroadcast = ""
    private var partnerbroadcast = ""

    private var bleJob: Job = Job()
    private var checkPermissionJob: Job = Job()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Helper().log(TAG,"Start Service @ ${Helper().timeString(Calendar.getInstance().timeInMillis)}")

        mybroadcast = kv.decodeString("MyBroadcast","").toString()
        partnerbroadcast = kv.decodeString("PartnerBroadcast","").toString()
        Helper().log(TAG,"我的廣播：$mybroadcast、伴侶的廣播：$partnerbroadcast")

        NotificationListener().setListener(this)

        activityRecognitionClient = ActivityRecognition.getClient(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "familywellbeings:wakeLog")

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mReceiver,filter)

        try {
            createNotificationChannel()
            showNotification()
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

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel =
                NotificationChannel(CHANNEL_ID, "MY SERVICE", NotificationManager.IMPORTANCE_LOW)
            serviceChannel.setSound(null, null)
            serviceChannel.setShowBadge(false)

            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun showNotification(){
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(notificationIntent)
            getPendingIntent(1998, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
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
                advertise()
                scanBleDevices()
                delay(60 * 1000)
            }
        }
    }

////BluetoothAdvertise Function/////
    private fun advertise() {
        val bluetoothManager = App.instance().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val mBluetoothAdapter = bluetoothManager?.adapter
        val bluetoothLeAdvertiser by lazy { mBluetoothAdapter?.bluetoothLeAdvertiser }

        if (advertisingCallback == null) {
            val advSettings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setConnectable(false)
                .build()
            val data = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid.fromString(mybroadcast))
                .build()
            val scanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()
            advertisingCallback = advertiseCallback()

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        ) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothLeAdvertiser?.startAdvertising(
                            advSettings,
                            data,
                            scanResponse,
                            advertisingCallback
                        )
                        Helper().log(TAG,"start ADVERTISE")
                    } else {
                        Helper().log(TAG,"You don't have the permission to advertise")
                    }
                } else {
                    bluetoothLeAdvertiser?.startAdvertising(
                        advSettings,
                        data,
                        scanResponse,
                        advertisingCallback
                    )
                    Helper().log(TAG,"start ADVERTISE")
                }
            } catch(e: Exception){
                Helper().log(TAG,"Advertisement error: $e")
            }
        }
    }

    private fun advertiseCallback() = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Helper().log(TAG,"廣播成功")
        }
        override fun onStartFailure(errorCode: Int) {
            Helper().log(TAG,"Advertising onStartFailure: $errorCode")
            super.onStartFailure(errorCode)
        }
    }

////BluetoothScan Function/////
    private fun scanBleDevices() {
        val bluetoothManager = App.instance().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val mBluetoothAdapter = bluetoothManager?.adapter
        if (mBluetoothAdapter != null) {
            val scanner by lazy { BluetoothLeScannerCompat.getScanner() }
            val setting = no.nordicsemi.android.support.v18.scanner.ScanSettings.Builder()
                .setScanMode(no.nordicsemi.android.support.v18.scanner.ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
            if(!mScanning) {
                mScanning = true
                try {
                    scanner.startScan(scanFilters(), setting, scanCallback)
                } catch(e: Exception) {
                    Helper().log(TAG,"Start scan error: $e")
                }
            } else {
                mScanning = false
                try{
                    scanner.stopScan(scanCallback)
                } catch(e: Exception) {
                    Helper().log(TAG,"Stop scan error: $e")
                }
            }
        }
    }

    private val scanCallback = object : no.nordicsemi.android.support.v18.scanner.ScanCallback(){
        val devices = mutableListOf<Map<String, String>>()
        override fun onScanResult(
            callbackType: Int,
            result: no.nordicsemi.android.support.v18.scanner.ScanResult
        ) {
            super.onScanResult(callbackType, result)
            Helper().log(TAG, "result: ${result.scanRecord.toString()}")
            val device: BluetoothDevice = result.device
            val rssi = result.rssi
            val rxTimestampMillis: Long = System.currentTimeMillis() - SystemClock.elapsedRealtime() + result.timestampNanos / 1000000

            val uuid = result.scanRecord?.serviceUuids?.toString()
            val macAddress = result.device.address

            val partnerUUID = kv.decodeString("PartnerBroadcast","")
            val bandMacSet = kv.decodeString("bandMacSet","")

            val map = mutableMapOf<String, String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        App.instance(),
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    device.name?.let { name ->
                        map["name"] = name
                    }
                    map["RSSI"] = rssi.toString()
                    map["time"] = Helper().timeString(rxTimestampMillis)
                } else {
                    Helper().log(TAG,"You don't have the permission to scan")
                }
            } else {
                device.name?.let { name ->
                    map["name"] = name
                }
                map["RSSI"] = rssi.toString()
                map["time"] = Helper().timeString(rxTimestampMillis)
            }

            devices.add(map)

            if (uuid == "[$partnerUUID]") {
                kv.encode("uuidScan", uuid)
                kv.encode("partnerMac", macAddress)
            } else if (macAddress == bandMacSet) {
                kv.encode("bandMacScan", macAddress)
            }

            val rssiData = RSSIData(Helper().databaseDay(),map["name"] ?: "", map["RSSI"]?.toInt() ?: 0, map["time"] ?: "",rxTimestampMillis)
            App.instance().dataDao.insertRSSI(rssiData)
        }

        override fun onBatchScanResults(results: MutableList<no.nordicsemi.android.support.v18.scanner.ScanResult>) {
            Helper().log(TAG,"onBatchScanResults: ")
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            Helper().log(TAG,"onScanFailed: $errorCode")
            super.onScanFailed(errorCode)
        }
    }

    private fun scanFilters(): List<ScanFilter> {
        val list: MutableList<ScanFilter> = ArrayList()
        val bandMacSet = kv.decodeString("bandMacSet","").toString()

        val scanFilterMiband =
            ScanFilter.Builder().setDeviceAddress(bandMacSet).build()
        val scanFilterMobileBroadcast =
            ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(partnerbroadcast)).build()

        val partnerMAC = kv.decodeString("partnerMac","")
        if(!partnerMAC.isNullOrEmpty()) {
            val scanFilterMobileMAC =
                ScanFilter.Builder().setDeviceAddress(partnerMAC).build()
            if (!list.contains(scanFilterMobileMAC)) {
                list.add(scanFilterMobileMAC)
            }
        }

        list.add(scanFilterMiband)
        list.add(scanFilterMobileBroadcast)
        return list
    }

    private fun repeatCheckPermission(): Job{
        return CoroutineScope(Dispatchers.IO + checkPermissionJob).launch {
            while(true){
                check()
                getSensorData()
                delay(30 * 1000)
                cancelSensor()
                delay(270 * 1000)
            }
        }
    }

////Check Permission and Devices Function/////
    private fun check() {
        val checkManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val bluetoothManager = App.instance().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val mBluetoothAdapter = bluetoothManager?.adapter
        if (mBluetoothAdapter?.isEnabled == false) {
            Helper().log(TAG,"藍芽被關閉：Bluetooth disable, try to auto-enable")
            mBluetoothAdapter.isEnabled
            if (!mBluetoothAdapter.isEnabled) {
                val statusIntent = Intent()
                statusIntent.action = "BlueTooth"
                statusIntent.setClass(this, CheckReceiver::class.java)
                this.sendBroadcast(statusIntent)
            } else {
                checkManager.cancel(NOTIFICATION_ID_BLUETOOTH)
            }
        }

        val gpsManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Helper().log(TAG,"GPS被關閉：GPS disable,try to call attention to subject")
            val statusIntent = Intent()
            statusIntent.action = "GPS"
            statusIntent.setClass(this, CheckReceiver::class.java)
            this.sendBroadcast(statusIntent)
        } else {
            checkManager.cancel(NOTIFICATION_ID_GPS)
        }

        Helper().permissionCheck(App.instance(), TAG,"appUsage")
        Helper().permissionCheck(App.instance(), TAG,"notificationService")
        Helper().permissionCheck(App.instance(), TAG,"accessibilityService")
        val appUsageCheck = kv.decodeBool("isAppUsageGranted",false)
        val accessibilityCheck = kv.decodeBool("isAccessibilityGranted",false)
        val notificationCheck = kv.decodeBool("isNotificationListenerGranted",false)

        if(!appUsageCheck){
            Helper().log(TAG,"應用程式設定被關閉：Usage Permission disable,try to call attention to subject")
            val statusIntent = Intent()
            statusIntent.action = "Usage_Permission"
            statusIntent.setClass(this, CheckReceiver::class.java)
            this.sendBroadcast(statusIntent)
        } else {
            checkManager.cancel(NOTIFICATION_ID_USAGE)
        }
        if(!accessibilityCheck){
            Helper().log(TAG,"無障礙設定被關閉：Accessibility Permission disable,try to call attention to subject")
            val statusIntent = Intent()
            statusIntent.action = "Accessibility_Permission"
            statusIntent.setClass(this, CheckReceiver::class.java)
            this.sendBroadcast(statusIntent)
        } else {
            checkManager.cancel(NOTIFICATION_ID_ACCESSIBILITY)
        }
        if(!notificationCheck){
            Helper().log(TAG,"存取通知設定被關閉：Notification Permission disable,try to call attention to subject")
            val statusIntent = Intent()
            statusIntent.action = "Notification_Permission"
            statusIntent.setClass(this, CheckReceiver::class.java)
            this.sendBroadcast(statusIntent)
        } else {
            checkManager.cancel(NOTIFICATION_ID_NOTIFICATION_PERMISSION)
        }
    }

////Sensor Function/////
    private fun getSensorData() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        try {
            sensorAvailableCheck()
            if (::acc.isInitialized){
                sensorManager.registerListener(this,acc,SensorManager.SENSOR_DELAY_NORMAL)
                Helper().log(TAG,"Initialize ACC")
            }
            if (::gyro.isInitialized){
                sensorManager.registerListener(this,gyro,SensorManager.SENSOR_DELAY_NORMAL)
                Helper().log(TAG,"Initialize Gyro")
            }
        } catch (e: Exception) {
            Helper().log(TAG,"Get Sensor Data Error: $e")
        }
    }

    private fun sensorAvailableCheck() {
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensorList) {
            when (sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> acc = sensor
                Sensor.TYPE_GYROSCOPE -> gyro = sensor
                Sensor.TYPE_ACCELEROMETER -> if (!::acc.isInitialized) acc = sensor
            }
        }
    }

    private fun cancelSensor() {
        if (::acc.isInitialized) {
            sensorManager.unregisterListener(this, acc)
            Helper().log(TAG, "Cancel ACC")
        } else {
            Helper().log(TAG, "Unable to Cancel ACC")
        }

        if (::gyro.isInitialized) {
            sensorManager.unregisterListener(this, gyro)
            Helper().log(TAG, "Cancel GYRO")
        } else {
            Helper().log(TAG, "Unable to Cancel GYRO")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values
        val alpha = values?.get(0)
        val beta = values?.get(1)
        val gamma = values?.get(2)
        val milliseconds = Calendar.getInstance().timeInMillis

        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION || event.sensor.type == Sensor.TYPE_ACCELEROMETER){
                val acceleratorData = AcceleratorData(Helper().databaseDay(),alpha,beta,gamma,Helper().timeString(milliseconds),milliseconds)
                App.instance().dataDao.insertAccelerator(acceleratorData)
            } else {
                val gyroData = GyroData(Helper().databaseDay(),alpha,beta,gamma,Helper().timeString(milliseconds),milliseconds)
                App.instance().dataDao.insertGyro(gyroData)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do function when sensor accuracy changed
    }

    override fun setValue(packageName: String?) {
        // do function
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
        val broadcastIntent = Intent()
        broadcastIntent.action = "restart_service"
        broadcastIntent.setClass(this, RestartReceiver::class.java)
        this.sendBroadcast(broadcastIntent)

        super.onDestroy()
    }
}