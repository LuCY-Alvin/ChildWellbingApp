package com.mildp.jetpackcompose.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.kpstv.compose.kapture.ScreenshotController
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.BoxData
import com.mildp.jetpackcompose.model.database.PointData
import com.mildp.jetpackcompose.model.database.TMTData
import com.mildp.jetpackcompose.model.database.TMTStopData
import com.mildp.jetpackcompose.utils.Constants
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.TMTItems.randomArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class TMTViewModel: ViewModel() {

    companion object {
        private const val TAG = "TMTViewModel"
    }

    var participant by mutableStateOf(Constants.kv.decodeString("Participant","").toString())

    private val _sequenceLiveData: MutableLiveData<Array<Int>> = MutableLiveData()
    val sequenceLiveData: LiveData<Array<Int>> = _sequenceLiveData

    private val boxData: MutableMap<Int, BoxData> = mutableMapOf()
    private val activeStates = mutableStateMapOf<Int, Boolean>()

    private val _currentExpectedNumber = mutableStateOf(1)
    val currentExpectedNumber: State<Int> = _currentExpectedNumber

    private val reactTime = Calendar.getInstance().timeInMillis

    var startTime = 0L
    private var lastTime = 0L
    private var lengthTemp = 0F
    private var last: Offset = Offset.Zero

    private val reactionTimes = mutableMapOf<Int, Long>()
    private val lengths = mutableMapOf<Int, Float>()

    init {
        generateNumbers()
    }

    private fun generateNumbers() {
        val numbersList = randomArray.shuffled().random()
        _sequenceLiveData.value = numbersList
    }

    private fun setCurrentExpectedNumber(number: Int) {
        _currentExpectedNumber.value = number
    }

    fun incrementExpectedNumber(navController: NavHostController, screenshotController: ScreenshotController) {
        val currentNumber = currentExpectedNumber.value
        val currentTime = Calendar.getInstance().timeInMillis

        lastTime = if (currentNumber ==1){
            currentTime
        } else{
            val elapsedTime = currentTime - lastTime
            recordReactionTime(currentNumber, elapsedTime)
            currentTime
        }

        recordLength(currentNumber, lengthTemp)
        lengthTemp = 0F

        if (currentNumber < 9) {
            setCurrentExpectedNumber(currentNumber + 1)
        } else {
            if(currentNumber != 10) {
                setCurrentExpectedNumber(10)

                val tmtData = TMTData(
                    Helper().databaseDay(),
                    participant,
                    ArrayList(sequenceLiveData.value?.toList() ?: emptyList()),
                    ArrayList(reactionTimes.values.map { it.toInt() }),
                    System.currentTimeMillis() - reactTime,
                    ArrayList(lengths.values.map { it.toInt() }),
                    Helper().timeString(System.currentTimeMillis())
                )
                App.instance().dataDao.insertTMT(tmtData)
                viewModelScope.launch {
                    storeCanvas(screenshotController)
                }
            }
            navController.navigate("handerScreen")
        }
    }

    fun updateBoxPosition(number: Int, Position: Offset, width: Float, height: Float) {
        boxData[number] = BoxData(Position, number, width, height)
    }

    fun isActive(number: Int): Boolean {
        return activeStates[number] ?: false
    }

    fun updateActiveState(number: Int, isActive: Boolean) {
        activeStates[number] = isActive
    }

    fun getPosition(number: Int): Offset? {
        return boxData[number]?.Position
    }

    fun getWidth(number: Int): Float? {
        return boxData[number]?.width
    }

    fun getHeight(number: Int): Float? {
        return boxData[number]?.height
    }

    private fun recordReactionTime(number: Int, time: Long) {
        reactionTimes[number] = time
    }

    private fun recordLength(number: Int, length: Float) {
        lengths[number] = length
    }

    fun calculatePoint(now: Offset) {
        val distance = sqrt((last.x - now.x).pow(2f) + (last.y - now.y).pow(2f))
        lengthTemp += distance
        last = now
    }

    fun calculateDistance(point1: Offset, point2: Offset, width: Float, height: Float): Boolean {

        val centerX = point2.x + width/2
        val centerY = point2.y + height/2
        val dx = point1.x - centerX
        val dy = point1.y - centerY

        return (width/2 * height/2) >= (dx*dx) + (dy*dy)
    }

    fun onPointPosition(touchPosition: Offset) {
        if(touchPosition != Offset.Unspecified) {
            val pointData = PointData(
                Helper().databaseDay(), participant,
                touchPosition.x, touchPosition.y,
                Helper().timeString(System.currentTimeMillis()), System.currentTimeMillis()
            )
            App.instance().dataDao.insertPoint(pointData)
        }
    }

    fun vibrate(vibrator: Vibrator, hapticFeedback: HapticFeedback) {

        viewModelScope.launch(Dispatchers.Default) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.cancel()
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 1))
                } else{
                    vibrator.cancel()
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        }
    }

    fun handleFingerDown(number: Int, restartTime: Long){
        val stopTMTData = TMTStopData(
            Helper().databaseDay(), participant,
            number, restartTime
        )
        App.instance().dataDao.insertStopPoint(stopTMTData)
    }

    private suspend fun storeCanvas(screenshotController: ScreenshotController){
        val bitmap: Result<Bitmap> = screenshotController.captureToBitmap(
            config = Bitmap.Config.ARGB_8888
        )
        if(bitmap.isSuccess) {
            val fos = App.instance().openFileOutput(
                "${participant}_day${Helper().databaseDay()}_${timeNow()}.png",
                Context.MODE_PRIVATE
            )
            bitmap.getOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, fos)
            withContext(Dispatchers.IO) {
                fos.close()
            }
        } else {
            Helper().log(TAG,"bitmap Error")
        }
    }

    private fun timeNow(): String {
        return when (val currentTime = LocalTime.now()) {
            in LocalTime.of(7, 0)..LocalTime.of(11, 59) -> {
                "morning"
            }
            in LocalTime.of(13, 0)..LocalTime.of(17, 59) -> {
                "afternoon"
            }
            in LocalTime.of(19, 0)..LocalTime.of(23, 59) -> {
                "night"
            }
            else -> {
                currentTime.toString()
            }
        }
    }
}
