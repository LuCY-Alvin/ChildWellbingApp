package com.mildp.jetpackcompose.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.AcceleratorData
import com.mildp.jetpackcompose.model.database.GyroData
import com.mildp.jetpackcompose.utils.Helper
import kotlinx.coroutines.launch
import java.util.*

class SensorViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        private const val TAG = "SensorViewModel"
    }
    private val sensorManager: SensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var acc: Sensor? = null
    private var gyro: Sensor? = null

    init {
        initializeSensors()
    }

    private fun initializeSensors() {
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensorList) {
            when (sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> acc = sensor
                Sensor.TYPE_GYROSCOPE -> gyro = sensor
                Sensor.TYPE_ACCELEROMETER -> if (acc == null) acc = sensor
            }
        }
    }

    fun startSensors() {
        Helper().log(TAG,"Start to get SensorData")
        acc?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyro?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stopSensors() {
        Helper().log(TAG,"Cancel Sensor")
        sensorManager.unregisterListener(sensorListener)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val values = event?.values
            val alpha = values?.get(0)
            val beta = values?.get(1)
            val gamma = values?.get(2)
            val milliseconds = Calendar.getInstance().timeInMillis

            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION || event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val acceleratorData = AcceleratorData(
                        Helper().databaseDay(),
                        alpha,
                        beta,
                        gamma,
                        Helper().timeString(milliseconds),
                        milliseconds
                    )
                    saveAcceleratorData(acceleratorData)
                } else {
                    val gyroData = GyroData(
                        Helper().databaseDay(),
                        alpha,
                        beta,
                        gamma,
                        Helper().timeString(milliseconds),
                        milliseconds
                    )
                    saveGyroData(gyroData)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // do function when sensor accuracy changed
        }
    }

    private fun saveAcceleratorData(data: AcceleratorData) {
        viewModelScope.launch {
            App.instance().dataDao.insertAccelerator(data)
        }
    }

    private fun saveGyroData(data: GyroData) {
        viewModelScope.launch {
            App.instance().dataDao.insertGyro(data)
        }
    }

}