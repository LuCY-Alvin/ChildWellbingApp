package com.mildp.jetpackcompose.model.database

import androidx.room.Insert
import androidx.room.OnConflictStrategy

@androidx.room.Dao
interface Dao {

    @Insert
    fun insertRSSI(rssiData: RSSIData)
    @Insert
    fun insertAccessibility(accessibilityData: AccessibilityData)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsage(usageData: UsageData)
    @Insert
    fun insertMood(moodData: MoodData)
    @Insert
    fun insertNotification(notificationData: NotificationData)
    @Insert
    fun insertAccelerator(acceleratorData: AcceleratorData)
    @Insert
    fun insertGyro(gyroData: GyroData)
    @Insert
    fun insertTMT(tmtData: TMTData)
    @Insert
    fun insertDebug(debugLog: Debug)
    @Insert
    fun insertPoint(pointTMTData: PointData)
    @Insert
    fun insertStopPoint(tmtStopData: TMTStopData)
    @Insert
    fun insertActivityRecognition(activityRecognitionData: ActivityRecognitionData)
}