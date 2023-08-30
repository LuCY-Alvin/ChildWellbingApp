package com.mildp.jetpackcompose.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RSSIData")
data class RSSIData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "subID")
    var deviceName: String ="",

    @ColumnInfo(name = "RSSI")
    var RSSI: Int,

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(Day:Int, deviceName: String, RSSI: Int, Time: String, MilliSeconds: Long)
            : this(0, Day ,deviceName, RSSI, Time, MilliSeconds)
}

@Entity(tableName = "AccessibilityData")
data class AccessibilityData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "AppName")
    var appName: String? ="",

    @ColumnInfo(name = "Type")
    var Type: String = "",

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(Day:Int, appName: String?, Type: String,Time: String, MilliSeconds: Long)
            : this(0, Day ,appName, Type, Time, MilliSeconds)
}

@Entity(tableName = "UsageData")
data class UsageData(
    @PrimaryKey
    @ColumnInfo(name = "startTimeStamp")
    var startTime: String = "",

    @ColumnInfo(name = "endTimeStamp")
    var endTime: String = "",

    @ColumnInfo(name = "Day")
    var day: Int = 0,

    @ColumnInfo(name = "AppName")
    var appName: String? = "",

    @ColumnInfo(name = "AppCategory")
    var appCategory: String? = ""
) {
    constructor(day: Int, appName: String?, appCategory: String?, startTime: String, endTime: String) : this() {
        this.day = day
        this.appName = appName
        this.appCategory = appCategory
        this.startTime = startTime
        this.endTime = endTime
    }
}

@Entity(tableName = "MoodData")
data class MoodData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "Participant")
    var participant: String = "",

    @ColumnInfo(name = "Valence")
    var Valence: Int,

    @ColumnInfo(name = "Arousal")
    var Arousal: Int,

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(Day:Int, participant: String, Valence: Int, Arousal: Int, Time: String, MilliSeconds: Long)
            : this(0, Day, participant, Valence, Arousal, Time,MilliSeconds)
}

@Entity(tableName = "NotificationData")
data class NotificationData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "Type")
    var Type: String ="",

    @ColumnInfo(name = "AppName")
    var appName: String? ="",

    @ColumnInfo(name = "Title")
    var Title: String ="",

    @ColumnInfo(name = "Content")
    var Content: String ="",

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(Day:Int, Type: String, appName: String?,Title: String,Content: String , Time: String, MilliSeconds: Long)
            : this(0, Day ,Type, appName, Title, Content, Time, MilliSeconds)
}

@Entity(tableName = "AcceleratorData")
data class AcceleratorData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "X")
    var X: Float? =0f,

    @ColumnInfo(name = "Y")
    var Y: Float? =0f,

    @ColumnInfo(name = "Z")
    var Z: Float? =0f,

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(Day:Int, X: Float?, Y: Float?, Z: Float?, Time: String, MilliSeconds: Long)
            : this(0, Day ,X, Y, Z, Time, MilliSeconds)
}

@Entity(tableName = "GyroData")
data class GyroData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "X")
    var X: Float? =0f,

    @ColumnInfo(name = "Y")
    var Y: Float? =0f,

    @ColumnInfo(name = "Z")
    var Z: Float? =0f,

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(Day:Int, X: Float?, Y: Float?, Z: Float?, Time: String, MilliSeconds: Long)
            : this(0, Day ,X, Y, Z, Time, MilliSeconds)
}

@Entity(tableName = "TMTData")
data class TMTData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "Participant")
    var participant: String = "",

    @ColumnInfo(name = "Configuration")
    var configuration: ArrayList<Int>,

    @ColumnInfo(name = "TimeList")
    var TimeList: ArrayList<Int>,

    @ColumnInfo(name = "ReactionTime")
    var reactionTime: Long,

    @ColumnInfo(name = "Length")
    var Length: ArrayList<Int>,

    @ColumnInfo(name = "TimeStamp")
    var TimeStamp: String = ""

){
    constructor(Day:Int, participant: String, configuration: ArrayList<Int>, TimeList: ArrayList<Int>, reactionTime: Long, Length: ArrayList<Int>, TimeStamp: String)
            : this(0, Day, participant, configuration ,TimeList, reactionTime, Length, TimeStamp)
}

@Entity(tableName = "Debug")
data class Debug(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "File")
    var File: String? ="",

    @ColumnInfo(name = "Log")
    var Log: String ="",

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    ){
    constructor(Day:Int, File: String, Log: String, Time: String)
            : this(0, Day, File, Log, Time)
}

@Entity(tableName = "TMTPoint")
data class PointData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "Participant")
    var participant: String = "",

    @ColumnInfo(name = "X")
    var X: Float? =0f,

    @ColumnInfo(name = "Y")
    var Y: Float? =0f,

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0

) {
    constructor(Day: Int, participant: String, X: Float?, Y: Float?, Time: String, MilliSeconds: Long)
            : this(0, Day, participant, X, Y, Time, MilliSeconds)
}

@Entity(tableName = "TMTStopTable")
data class TMTStopData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "Participant")
    var participant: String = "",

    @ColumnInfo(name = "stopPoint")
    var stopPoint: Int? = 0,

    @ColumnInfo(name = "StopTime")
    var stopTime: Long? = 0,

    ) {
    constructor(Day: Int, participant: String, stopPoint: Int?, stopTime: Long?)
            : this(0, Day, participant, stopPoint, stopTime)
}

@Entity(tableName = "ActivityRecognitionData")
data class ActivityRecognitionData(

    @PrimaryKey(autoGenerate = true)
    var uid: Int,

    @ColumnInfo(name = "Day")
    var Day: Int = 0,

    @ColumnInfo(name = "InVehicle")
    var inVehicle: Int =0,

    @ColumnInfo(name = "OnBike")
    var onBike: Int =0,

    @ColumnInfo(name = "OnFoot")
    var onFoot: Int =0,

    @ColumnInfo(name = "Running")
    var running: Int =0,

    @ColumnInfo(name = "Still")
    var still: Int =0,

    @ColumnInfo(name = "Tilting")
    var tilting: Int =0,

    @ColumnInfo(name = "Walking")
    var walking: Int =0,

    @ColumnInfo(name = "Unknown")
    var unknown: Int =0,

    @ColumnInfo(name = "TimeStamp")
    var Time: String = "",

    @ColumnInfo(name = "TimeInMillis")
    var MilliSeconds: Long = 0
){
    constructor(
        Day: Int, inVehicle: Int, onBike: Int, onFoot: Int, running: Int,
        still: Int, tilting: Int, walking: Int, unknown: Int, Time: String, MilliSeconds: Long,
    ) : this(0, Day, inVehicle, onBike, onFoot,
        running, still, tilting, walking, unknown,
        Time, MilliSeconds)
}
