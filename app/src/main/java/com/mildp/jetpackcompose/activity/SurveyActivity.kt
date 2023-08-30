package com.mildp.jetpackcompose.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mildp.jetpackcompose.ui.components.HanderScreen
import com.mildp.jetpackcompose.ui.components.MoodScreen
import com.mildp.jetpackcompose.ui.components.TMTScreen
import com.mildp.jetpackcompose.ui.theme.AppTheme
import com.mildp.jetpackcompose.viewmodel.HanderViewModel
import com.mildp.jetpackcompose.viewmodel.MoodViewModel
import com.mildp.jetpackcompose.viewmodel.TMTViewModel

class SurveyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val tmtViewModel = viewModel<TMTViewModel>()
                val handerViewModel = viewModel<HanderViewModel>()
                val moodViewModel = viewModel<MoodViewModel>()

                NavHost(navController, startDestination = "tmtScreen") {
                    composable("tmtScreen") {
                        TMTScreen(tmtViewModel, navController = navController)
                    }
                    composable("handerScreen") {
                        HanderScreen(handerViewModel, navController = navController)
                    }
                    composable("moodScreen") {
                        MoodScreen(moodViewModel)
                    }
                }
            }
        }
    }
}


