package com.mildp.jetpackcompose.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.ui.components.HanderScreen
import com.mildp.jetpackcompose.ui.components.MoodScreen
import com.mildp.jetpackcompose.ui.components.TMTScreen
import com.mildp.jetpackcompose.ui.theme.AppTheme
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.viewmodel.SensorViewModel

class SurveyActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SurveyActivity"
    }
    private lateinit var sensorViewModel: SensorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "tmtScreen") {
                    composable("tmtScreen") {
                        TMTScreen(navController = navController)
                    }
                    composable("handerScreen") {
                        HanderScreen(navController = navController)
                    }
                    composable("moodScreen") {
                        MoodScreen()
                    }
                }
            }
        }

        sensorViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(SensorViewModel::class.java)
        sensorViewModel.startSensors()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent?.action == "goBackMain"){
            val goBackIntent = Intent(this, MainActivity::class.java)
            goBackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(goBackIntent)
            finish()
        }
    }

    override fun onStart() {
        Helper().log(TAG,"Start Survey Activity")
        sensorViewModel.startSensors()
        super.onStart()
    }

    override fun onResume() {
        Helper().log(TAG,"Resume Survey Activity")
        sensorViewModel.startSensors()
        super.onResume()
    }

    override fun onStop() {
        Helper().log(TAG,"Stop Survey Activity")
        sensorViewModel.stopSensors()
        super.onStop()
    }

    override fun onPause() {
        Helper().log(TAG,"Pause Survey Activity")
        sensorViewModel.stopSensors()
        super.onPause()
    }

    override fun onDestroy() {
        Helper().log(TAG,"Destroy Survey Activity")
        sensorViewModel.stopSensors()
        super.onDestroy()
    }
}


