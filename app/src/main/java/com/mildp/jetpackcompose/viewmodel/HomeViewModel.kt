package com.mildp.jetpackcompose.viewmodel

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.activity.SurveyActivity
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class HomeViewModel: ViewModel() {

    companion object {
        private const val TAG: String = "HomePage"
    }

    var childSurveyDone = mutableStateOf(kv.decodeBool("childSurveyDone",true))

    private val storedAlarmStatusJson = kv.decodeString("alarmStatus", null)
    @OptIn(ExperimentalSerializationApi::class)
    val alarmStatus: MutableList<Pair<Long, Boolean>> = if (storedAlarmStatusJson != null) {
        Json.decodeFromString(storedAlarmStatusJson.toString())
    } else {
        mutableListOf()
    }

    fun onSurveyStarted(participant: String){
        val currentTime = System.currentTimeMillis()
        val pendingAlarm = alarmStatus.firstOrNull { !it.second }

        if (pendingAlarm != null) {
            if(permissionCheck()) {
                val twoHoursAfterSurveyStart = pendingAlarm.first + 2 * 60 * 60 * 1000

                if (currentTime >= pendingAlarm.first && currentTime <= twoHoursAfterSurveyStart) {
                    kv.encode("Participant", participant)
                    val intent = Intent(App.instance(), SurveyActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.instance().startActivity(intent)
                } else {
                    Toast.makeText(App.instance(), "測驗時間還沒開始唷", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(App.instance(), "請重新確認設定頁面的權限都打開囉", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(App.instance(), "沒有測驗", Toast.LENGTH_SHORT).show()
        }
    }

    private fun permissionCheck():Boolean {

        Helper().permissionCheck(App.instance(),TAG,"appUsage")
        Helper().permissionCheck(App.instance(),TAG,"notificationService")
        Helper().permissionCheck(App.instance(),TAG,"accessibilityService")

        val isAppUsageGranted = kv.decodeBool("isAppUsageGranted",false)
        val isAccessibilityGranted = kv.decodeBool("isAccessibilityGranted",false)
        val isNotificationListenerGranted = kv.decodeBool("isNotificationListenerGranted",false)

        return isAppUsageGranted &&
                isAccessibilityGranted &&
                isNotificationListenerGranted
    }

}