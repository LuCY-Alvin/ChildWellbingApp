package com.mildp.jetpackcompose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.ActivityRecognitionData
import com.mildp.jetpackcompose.utils.Helper

class ActivityRecognitionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG: String = "RecognitionActivity"
    }

    private var inVehicle = 0
    private var onBike = 0
    private var onFoot = 0
    private var running = 0
    private var still = 0
    private var walking = 0
    private var tilting = 0
    private var unknown = 0

    override fun onReceive(context: Context, intent: Intent) {
        Helper().log(TAG,"Receive BroadCast of Activity Recognition")
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val probableActivities = result?.probableActivities

            if (probableActivities != null) {
                for (activity in probableActivities) {
                    when (activity.type) {
                        DetectedActivity.IN_VEHICLE -> inVehicle = activity.confidence
                        DetectedActivity.ON_BICYCLE -> onBike = activity.confidence
                        DetectedActivity.ON_FOOT -> onFoot = activity.confidence
                        DetectedActivity.RUNNING -> running = activity.confidence
                        DetectedActivity.STILL -> still = activity.confidence
                        DetectedActivity.TILTING -> tilting = activity.confidence
                        DetectedActivity.WALKING -> walking = activity.confidence
                        else -> unknown = activity.confidence
                    }
                }

                val arData = ActivityRecognitionData(
                    Helper().databaseDay(),
                    inVehicle, onBike, onFoot, running,
                    still, tilting, walking, unknown,
                    Helper().timeString(result.time), result.time
                )
                App.instance().dataDao.insertActivityRecognition(arData)
            }
        }
    }
}