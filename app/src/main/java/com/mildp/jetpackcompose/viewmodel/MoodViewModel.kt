package com.mildp.jetpackcompose.viewmodel

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.R
import com.mildp.jetpackcompose.activity.MainActivity
import com.mildp.jetpackcompose.model.AlarmStatus
import com.mildp.jetpackcompose.model.database.MoodData
import com.mildp.jetpackcompose.model.database.UsageData
import com.mildp.jetpackcompose.utils.AppUsageHelper
import com.mildp.jetpackcompose.utils.Constants
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.ZipUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalTime
import java.util.*

class MoodViewModel : ViewModel() {

    companion object {
        private const val TAG = ""
    }

    private val _selectedImageId = mutableStateOf(R.drawable._13)
    val selectedImageId: State<Int> = _selectedImageId
    var participant by mutableStateOf(kv.decodeString("Participant","").toString())
    private lateinit var moodData: MoodData
    private val notificationManager =
        App.instance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val storedAlarmStatusJson = kv.decodeString("alarmStatus", null)
    @OptIn(ExperimentalSerializationApi::class)
    val alarmStatus: MutableList<Pair<Long, AlarmStatus>> = if (storedAlarmStatusJson != null) {
        Json.decodeFromString(storedAlarmStatusJson)
    } else {
        mutableListOf()
    }

    fun onAppUsageData() {

        moodData = MoodData(
            Helper().databaseDay(), participant,
            0, 0,
            Helper().timeString(System.currentTimeMillis()), System.currentTimeMillis()
        )

        val currentTime = LocalTime.now()
        if(participant == "家長" && currentTime in LocalTime.of(19, 0)..LocalTime.of(23, 59)) {
            if(checkPermission()) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        for (stats in AppUsageHelper().getDailyAppUsage(Calendar.getInstance())){
                            val usageData = UsageData(Helper().databaseDay(),stats.app, stats.category, stats.startTimeString,stats.endTimeString)
                            App.instance().dataDao.insertUsage(usageData)
                        }
                    }
                } catch (e:Exception) {
                    Helper().log(TAG,"Get App Usage failed: $e")
                }
            } else {
                Helper().log(TAG,"There's App Usage permission issues.")
            }
        }
    }

    private fun checkPermission(): Boolean {
        val appOpsManager = App.instance().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(), App.instance().packageName
            )
        }
        else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(), App.instance().packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun onEmojiSelected(imageId: Int, imageContent: Pair<Int,Int>) {
        _selectedImageId.value = imageId
        moodData = MoodData(
            Helper().databaseDay(), participant,
            imageContent.first, imageContent.second,
            Helper().timeString(System.currentTimeMillis()), System.currentTimeMillis()
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun onDoneSurvey(){
        if(participant == "家長") {
            val currentTime = LocalTime.now()

            notificationManager.cancel(Constants.NOTIFICATION_ID2)
            kv.encode("surveyCancelled", true)

            val path = App.instance().dataDir.canonicalPath
            val id = kv.decodeString("subID", "")
            val number = kv.decodeInt("uploadNumber", 1)

            App.instance().dataDao.insertMood(moodData)

            val firstFalseIndex = alarmStatus.indexOfFirst { it.second == AlarmStatus.PREPARING }
            if (firstFalseIndex >= 0) {
                alarmStatus[firstFalseIndex] = Pair(alarmStatus[firstFalseIndex].first, AlarmStatus.FINISHED)
                val updatedAlarmStatusJson = Json.encodeToString(alarmStatus)
                kv.encode("alarmStatus", updatedAlarmStatusJson)
            }

            if (currentTime in LocalTime.of(19, 0)..LocalTime.of(23, 59)) {

                ZipUtils.zipFolders("$path/files", "$path/zipFile_${id}_day$number.zip")
                kv.encode("uploadServiceReady", true)
                Helper().log(TAG,"Upload Ready!!")

                val intent = Intent(App.instance(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                App.instance().startActivity(intent)
                Toast.makeText(App.instance(),"感謝完成今日測驗，準備上傳今日資料",Toast.LENGTH_SHORT).show()

            } else {
                val intent = Intent(App.instance(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                App.instance().startActivity(intent)
                Toast.makeText(App.instance(),"感謝完成本次測驗",Toast.LENGTH_SHORT).show()
            }

        } else {

            App.instance().dataDao.insertMood(moodData)

            kv.encode("childSurveyDone", true)
            val intent = Intent(App.instance(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance().startActivity(intent)
            Toast.makeText(App.instance(),"感謝小朋友的幫忙，請家長也要完成測驗唷",Toast.LENGTH_SHORT).show()
        }
    }

}