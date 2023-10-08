package com.mildp.jetpackcompose.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.mildp.jetpackcompose.utils.Helper

class BleAdvViewModel(application: Application): AndroidViewModel(application)  {
    companion object {
        private const val TAG = "BleAdvViewModel"
    }

    private val bluetoothManager: BluetoothManager =
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val mBluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var advertisingCallback: AdvertiseCallback? = null

    fun startAdvertise(myBroadcast: String) {
        if (advertisingCallback == null) {

            val bluetoothLeAdvertiser = mBluetoothAdapter?.bluetoothLeAdvertiser
            val advSettings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setConnectable(false)
                .build()
            val data = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid.fromString(myBroadcast))
                .build()
            val scanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .build()
            advertisingCallback = createAdvertiseCallback()

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(
                            getApplication(),
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        bluetoothLeAdvertiser?.startAdvertising(
                            advSettings,
                            data,
                            scanResponse,
                            advertisingCallback
                        )
                        Helper().log(TAG,"start Advertise")
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
            } catch (e: Exception) {
                Helper().log(TAG,"Advertisement error: $e")
            }
        }
    }

    private fun createAdvertiseCallback() = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Helper().log(TAG,"廣播成功")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Helper().log(TAG,"Advertising onStartFailure: $errorCode")
        }
    }
}