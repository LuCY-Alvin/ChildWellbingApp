package com.mildp.jetpackcompose.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mildp.jetpackcompose.viewmodel.HomeViewModel
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    Surface(
        modifier = Modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (homeViewModel.alarmStatus.isEmpty()) {
                Icon(Icons.Default.Info, "請先進行設定", tint = MaterialTheme.colorScheme.error)
                Text(text = "請先進行設定", color = MaterialTheme.colorScheme.error)
            } else {
                Spacer(modifier = Modifier.padding(10.sdp))

                Text(
                    text = "測驗時間",
                    fontSize = 20.ssp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.padding(5.sdp))

                Countdown(alarmStatus = homeViewModel.alarmStatus)

                AlarmChecklist(alarmStatus = homeViewModel.alarmStatus)

                Row(
                    modifier = Modifier
                        .padding(5.sdp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { homeViewModel.onSurveyStarted("小孩") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.sdp),
                        enabled = !homeViewModel.childSurveyDone.value
                    ) {
                        Text(
                            text = "小孩測驗",
                        )
                    }

                    Button(
                        onClick = { homeViewModel.onSurveyStarted("家長") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.sdp),
                    ) {
                        Text(
                            text = "家長測驗",
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTimerItem(
    timestamp: Long
) {
    var remainingTime by remember { mutableStateOf(calculateRemainingTime(timestamp)) }

    LaunchedEffect(timestamp) {
        launch {
            while (remainingTime > 0) {
                remainingTime = calculateRemainingTime(timestamp)
                delay(1000)
            }
        }
    }

    Text(
        text = formatTime(remainingTime) ,
        style = TextStyle(fontSize = 40.ssp, fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center,
    )
}

fun calculateRemainingTime(timestamp: Long): Long {
    return timestamp - System.currentTimeMillis()
}

fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun Countdown(alarmStatus: MutableList<Pair<Long, Boolean>>) {
    val currentTime = System.currentTimeMillis()

    val closestTimestamp = alarmStatus
        .firstOrNull { !it.second && it.first + 2 * 60 * 60 * 1000 > currentTime }

    if (closestTimestamp != null) {
        if (currentTime >= closestTimestamp.first) {
            Text(
                text = "測驗時間已到\n\n請盡快完成回答",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 26.ssp,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            CountdownTimerItem(timestamp = closestTimestamp.first)
        }
    } else {
        Text(
            text = "實驗已結束",
            fontWeight = FontWeight.Bold,
            fontSize = 40.ssp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AlarmChecklist(alarmStatus: MutableList<Pair<Long, Boolean>>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.sdp,5.sdp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "時間",
                fontWeight = FontWeight.Bold,
                fontSize = 12.ssp,
                modifier = Modifier.padding(8.sdp))
            repeat(3) { index ->
                Text(
                    text = when (index) {
                        0 -> "早上"
                        1 -> "中午"
                        else -> "晚上"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.ssp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.sdp),
                    textAlign = TextAlign.Center
                )
            }
        }

        for (row in 1..7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "第${row}天",
                    fontSize = 12.ssp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.sdp)
                )

                for (col in 1..3) {
                    val alarmIndex = (row - 1) * 3 + (col - 1)
                    val alarmStatusText = if(alarmIndex < alarmStatus.size){
                        if(alarmStatus[alarmIndex].second) {
                            "已結束"
                        } else {
                            "準備中"
                        }
                    } else {
                        ""
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.sdp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = alarmStatusText,
                            fontSize = 12.ssp,
                            color = if (alarmStatusText == "已結束") MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                }
            }
        }
    }
}