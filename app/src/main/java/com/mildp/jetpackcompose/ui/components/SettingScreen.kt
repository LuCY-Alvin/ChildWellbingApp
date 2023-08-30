package com.mildp.jetpackcompose.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mildp.jetpackcompose.viewmodel.SettingViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingScreen(
   settingViewModel: SettingViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    settingViewModel.checkPermission()

    Surface(
        modifier = Modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(15.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.padding(10.sdp))

            Text(
                text = "設定",
                fontSize = 20.ssp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.padding(5.sdp))

            OutlinedTextField(
                value = settingViewModel.subID,
                onValueChange = {
                    settingViewModel.subID = it
                },
                label = { Text("實驗編號") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
            )

            Spacer(modifier = Modifier.padding(15.sdp))

            Text(text = "實驗時間")

            Spacer(modifier = Modifier.padding(5.sdp))

            val formattedDate by remember {
                derivedStateOf {
                    DateTimeFormatter
                        .ofPattern("yyyy年MM月dd日")
                        .format(settingViewModel.pickedDate)
                }
            }
            val dateDialogState = rememberMaterialDialogState()

            Button(
                onClick = { dateDialogState.show() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = formattedDate,
                )
            }
            MaterialDialog(
                dialogState = dateDialogState,
                buttons = {
                    positiveButton(text = "確認", textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondaryContainer))
                    negativeButton(text = "取消", textStyle = TextStyle(color = MaterialTheme.colorScheme.onErrorContainer))
                },
                backgroundColor = MaterialTheme.colorScheme.background
            ) {
                datepicker(
                    initialDate = settingViewModel.pickedDate,
                    title = "選擇開始實驗的日期",
                    allowedDateValidator = {
                        it >= LocalDate.now()
                    },
                    colors = DatePickerDefaults.colors(
                        headerBackgroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        headerTextColor = MaterialTheme.colorScheme.primaryContainer,
                        calendarHeaderTextColor = MaterialTheme.colorScheme.onBackground,
                        dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                        dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
                        dateInactiveBackgroundColor = MaterialTheme.colorScheme.background,
                        dateInactiveTextColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    settingViewModel.pickedDate = it
                }
            }


            Row(
                modifier = Modifier
                    .padding(5.sdp),
               horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "早上",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "下午",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "晚上",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .padding(bottom = 10.sdp),
            ) {
                val formattedMorning by remember {
                    derivedStateOf {
                        DateTimeFormatter
                            .ofPattern("HH:mm")
                            .format(settingViewModel.pickedMorning)
                    }
                }
                val morningDialogState = rememberMaterialDialogState()
                Button(
                    onClick = { morningDialogState.show()},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(5.sdp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = formattedMorning,
                    )
                }
                MaterialDialog(
                    dialogState = morningDialogState,
                    buttons = {
                        positiveButton(text = "確認", textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondaryContainer))
                        negativeButton(text = "取消", textStyle = TextStyle(color = MaterialTheme.colorScheme.onErrorContainer))
                    }
                ) {
                    timepicker(
                        initialTime = settingViewModel.pickedMorning,
                        title = "請選擇時間",
                        timeRange = LocalTime.of(7, 0)..LocalTime.of(9, 59),
                        colors = com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults.colors(
                            activeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            inactiveBackgroundColor = MaterialTheme.colorScheme.background,
                            activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveTextColor = MaterialTheme.colorScheme.onBackground,
                            selectorColor = MaterialTheme.colorScheme.secondary,
                            selectorTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        settingViewModel.pickedMorning = it
                    }
                }

                val formattedAfternoon by remember {
                    derivedStateOf {
                        DateTimeFormatter
                            .ofPattern("HH:mm")
                            .format(settingViewModel.pickedAfternoon)
                    }
                }
                val afternoonDialogState = rememberMaterialDialogState()
                Button(
                    onClick = {afternoonDialogState.show()},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(5.sdp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = formattedAfternoon,
                    )
                }
                MaterialDialog(
                    dialogState = afternoonDialogState,
                    buttons = {
                        positiveButton(text = "確認", textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondaryContainer))
                        negativeButton(text = "取消", textStyle = TextStyle(color = MaterialTheme.colorScheme.onErrorContainer))
                    }
                ) {
                    timepicker(
                        initialTime = settingViewModel.pickedAfternoon,
                        title = "請選擇時間",
                        timeRange = LocalTime.of(13, 0)..LocalTime.of(15, 59),
                        colors = com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults.colors(
                            activeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            inactiveBackgroundColor = MaterialTheme.colorScheme.background,
                            activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveTextColor = MaterialTheme.colorScheme.onBackground,
                            selectorColor = MaterialTheme.colorScheme.secondary,
                            selectorTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        settingViewModel.pickedAfternoon = it
                    }
                }

                val formattedNight by remember {
                    derivedStateOf {
                        DateTimeFormatter
                            .ofPattern("HH:mm")
                            .format(settingViewModel.pickedNight)
                    }
                }
                val nightDialogState = rememberMaterialDialogState()
                Button(
                    onClick = {nightDialogState.show()},
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(5.sdp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = formattedNight,
                    )
                }
                MaterialDialog(
                    dialogState = nightDialogState,
                    buttons = {
                        positiveButton(text = "確認", textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondaryContainer))
                        negativeButton(text = "取消", textStyle = TextStyle(color = MaterialTheme.colorScheme.onErrorContainer))
                    }
                ) {
                    timepicker(
                        initialTime = settingViewModel.pickedNight,
                        title = "請選擇時間",
                        timeRange = LocalTime.of(19, 0)..LocalTime.of(21, 59),
                        colors = com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults.colors(
                            activeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            inactiveBackgroundColor = MaterialTheme.colorScheme.background,
                            activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveTextColor = MaterialTheme.colorScheme.onBackground,
                            selectorColor = MaterialTheme.colorScheme.secondary,
                            selectorTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        settingViewModel.pickedNight = it
                    }
                }
            }

            Text(
                text = "本實驗將從手機後台持續蒐集資料\n，故請允許下方所有設定。",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.ssp
                )
            )

            Spacer(modifier = Modifier.padding(10.sdp))
            
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "關閉省電最佳化",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = {
                        settingViewModel.onBatteryButtonClick()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !settingViewModel.isBatteryOptimizeClosed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = settingViewModel.isBatteryOptimizeClosed.let { if (it) "已開啟" else "未開啟" },
                    )
                }
            }

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "應用程式存取權限",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = {
                        settingViewModel.onAppUsageButtonClick(settingViewModel.isAppUsageGranted)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !settingViewModel.isAppUsageGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = settingViewModel.isAppUsageGranted.let { if (it) "已開啟" else "未開啟" },
                    )
                }
            }

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "協作功能權限",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        settingViewModel.onAccessibilityButtonClick(settingViewModel.isAccessibilityGranted)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !settingViewModel.isAccessibilityGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = settingViewModel.isAccessibilityGranted.let { if (it) "已開啟" else "未開啟" }
                    )
                }
            }

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "通知存取權限",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        settingViewModel.onNotificationButtonClick(settingViewModel.isNotificationListenerGranted)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !settingViewModel.isNotificationListenerGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = settingViewModel.isNotificationListenerGranted.let { if (it) "已開啟" else "未開啟" }
                    )
                }
            }

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "開機自啟動",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = {
                        settingViewModel.onBootCompletedClicked()
                                 },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "前往")
                }
            }
            Spacer(modifier = Modifier.padding(5.sdp))

            Text(text = "確認上方資料都沒有問題後請您按下儲存")

            Spacer(modifier = Modifier.padding(5.sdp))

            Button(
                onClick = {
                    settingViewModel.onStoreAllChange()
                    settingViewModel.startMyProject()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(text = "儲存")
            }

            if(settingViewModel.phoneFoundLiveData.value == false || settingViewModel.miBandFoundLiveData.value == false) {
                Spacer(modifier = Modifier.padding(5.sdp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "伴侶手機",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (settingViewModel.phoneFoundLiveData.value == true) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = "Search",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "小孩手環",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (settingViewModel.miBandFoundLiveData.value == true) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = "Search",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}