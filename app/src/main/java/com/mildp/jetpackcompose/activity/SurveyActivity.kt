package com.mildp.jetpackcompose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mildp.jetpackcompose.ui.components.HanderScreen
import com.mildp.jetpackcompose.ui.components.MoodScreen
import com.mildp.jetpackcompose.ui.components.TMTScreen
import com.mildp.jetpackcompose.ui.theme.AppTheme

class SurveyActivity : ComponentActivity() {
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
    }
}


