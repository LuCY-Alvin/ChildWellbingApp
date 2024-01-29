package com.mildp.jetpackcompose.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.os.SystemClock
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.RSSIData
import com.mildp.jetpackcompose.utils.Constants
import com.mildp.jetpackcompose.utils.Helper
import no.nordicsemi.android.support.v18.scanner.*

class BleScanViewModel(application: Application): AndroidViewModel(application)  {
    companion object {
        private const val TAG = "BleScanViewModel"
    }

    private val bluetoothManager: BluetoothManager =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val mBluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()
    private var isScanning = false

    private val _scannedDevices = MutableLiveData<List<BluetoothDevice>>()
    val scannedDevices: LiveData<List<BluetoothDevice>>
        get() = _scannedDevices

    init {
        if (mBluetoothAdapter?.isEnabled == false) {
            Toast.makeText(application.applicationContext,"請重新開啟藍芽",Toast.LENGTH_SHORT).show()
        }
    }

    fun startScan(partnerbroadcast: String) {
        isScanning = if (!isScanning) {
            Helper().log(TAG,"Start Scan BleData")
            scanner.startScan(scanFilters(partnerbroadcast), scanSettings, scanCallback)
            true
        } else {
            Helper().log(TAG,"Stop Scan BleData")
            scanner.stopScan(scanCallback)
            false
        }
    }

    private val scanCallback = object : ScanCallback() {
        val devices = mutableListOf<Map<String, String>>()
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Helper().log(TAG, "result: ${result.scanRecord.toString()}")

            val device: BluetoothDevice = result.device
            val rssi = result.rssi
            val rxTimestampMillis: Long = System.currentTimeMillis() - SystemClock.elapsedRealtime() + result.timestampNanos / 1000000
            val map = mutableMapOf<String, String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        App.instance(),
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if(device.name == null){
                        map["name"] = "partnerPhone"
                    } else {
                        map["name"] = device.name
                    }
                    map["RSSI"] = rssi.toString()
                    map["time"] = Helper().timeString(rxTimestampMillis)
                } else {
                    Helper().log(TAG,"You don't have the permission to scan")
                }
            } else {
                if(device.name == null){
                    map["name"] = "partnerPhone"
                } else {
                    map["name"] = device.name
                }
                map["RSSI"] = rssi.toString()
                map["time"] = Helper().timeString(rxTimestampMillis)
            }
            devices.add(map)
            val rssiData = RSSIData(Helper().databaseDay(),map["name"] ?: "", map["RSSI"]?.toInt() ?: 0, map["time"] ?: "",rxTimestampMillis)
            App.instance().dataDao.insertRSSI(rssiData)

            val uuid = result.scanRecord?.serviceUuids?.toString()
            val macAddress = result.device.address
            val partnerUUID = Constants.kv.decodeString("PartnerBroadcast","")
            val bandMacSet = Constants.kv.decodeString("bandMacSet","")
            if (uuid == "[$partnerUUID]") {
                Constants.kv.encode("phoneFound", true)
                Constants.kv.encode("partnerMac", macAddress)
            } else if (macAddress == bandMacSet) {
                Constants.kv.encode("mibandFound", true)
            }

            val updatedDevices = _scannedDevices.value?.toMutableList() ?: mutableListOf()
            updatedDevices.add(device)
            _scannedDevices.postValue(updatedDevices)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            Helper().log(TAG,"onBatchScanResults: ")
        }

        override fun onScanFailed(errorCode: Int) {
            Helper().log(TAG,"onScanFailed: $errorCode")
        }
    }

    private fun scanFilters(partnerbroadcast: String): List<ScanFilter> {
        val list: MutableList<ScanFilter> = ArrayList()
        val bandMacSet = Constants.kv.decodeString("bandMacSet","").toString()

        val scanFilterMiband =
            ScanFilter.Builder().setDeviceAddress(bandMacSet).build()
        val scanFilterMobileBroadcast =
            ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(partnerbroadcast)).build()

        val partnerMAC = Constants.kv.decodeString("partnerMac","")
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
}