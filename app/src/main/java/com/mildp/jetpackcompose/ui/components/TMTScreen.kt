package com.mildp.jetpackcompose.ui.components

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kpstv.compose.kapture.ScreenshotController
import com.kpstv.compose.kapture.attachController
import com.kpstv.compose.kapture.rememberScreenshotController
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.viewmodel.TMTViewModel
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp
import java.util.*

@Composable
fun TMTScreen(
    tmtViewModel: TMTViewModel = viewModel(),
    navController: NavHostController
) {
    val screenshotController = rememberScreenshotController()
    val sequence by tmtViewModel.sequenceLiveData.observeAsState(emptyArray())
    var size by remember { mutableStateOf(IntSize.Zero) }

    BackHandler(enabled = true) {
        Toast.makeText(App.instance(),"請繼續作答，不要退回前一頁，感謝。",Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .attachController(screenshotController)
            .layout{ measurable, constraints ->
                val placeable = measurable.measure(constraints)
                size = IntSize(placeable.width, placeable.height)
                layout(placeable.width, placeable.height){
                    placeable.place(0,0)
                }
            }
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = tmtViewModel.participant + "的TMT測驗",
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 26.ssp
            )
            Spacer(modifier = Modifier.padding(10.sdp))
            Text(
                text = "請依序觸碰數字1–9完成連線\n過程中手指不要移開螢幕",
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(1.sdp)
            ) {
                items(sequence) { number ->
                    NumberItem(number,tmtViewModel.isActive(number),tmtViewModel)
                }
            }
        }
        DrawCanvas(viewModel = tmtViewModel, navController = navController, screenshotController = screenshotController)
    }
}

@Composable
fun DrawCanvas(
    viewModel: TMTViewModel,
    navController: NavHostController,
    screenshotController: ScreenshotController
) {
    val points = remember { mutableStateListOf<Offset>() }
    var touchCoordinates by remember { mutableStateOf(Offset(0f, 0f)) }
    val haptic = LocalHapticFeedback.current
    lateinit var vibrator: Vibrator
    var fingerUpTime = 0L

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            App.instance().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        vibrator = App.instance().getSystemService(ComponentActivity.VIBRATOR_SERVICE) as Vibrator
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        touchCoordinates = event.calculateCentroid()
                        viewModel.onPointPosition(touchCoordinates)
                        val expectedNumber = viewModel.currentExpectedNumber.value
                        val position = viewModel.getPosition(expectedNumber)
                        val width = viewModel.getWidth(expectedNumber)
                        val height = viewModel.getHeight(expectedNumber)
                        if (touchCoordinates != Offset.Unspecified && position != null && width != null && height != null) {
                            viewModel.startTime = Calendar.getInstance().timeInMillis
                            viewModel.calculatePoint(touchCoordinates)
                            val distance = viewModel.calculateDistance(
                                touchCoordinates,
                                position,
                                width,
                                height
                            )
                            if (distance) {
                                viewModel.updateBoxPosition(expectedNumber, position, width, height)
                                for (i in 1..9) {
                                    viewModel.updateActiveState(i, false)
                                }
                                viewModel.updateActiveState(expectedNumber, true)
                                viewModel.vibrate(vibrator, haptic)
                                viewModel.incrementExpectedNumber(navController, screenshotController)
                            }
                        }
                        if (event.changes.any { it.changedToDown() }) {
                            if (fingerUpTime != 0L) {
                                val restartTime = System.currentTimeMillis()
                                viewModel.handleFingerDown(
                                    expectedNumber,
                                    restartTime - fingerUpTime
                                )
                            }
                        }
                        if (event.changes.any { it.changedToUp() }) {
                            fingerUpTime = System.currentTimeMillis()
                        }
                    }
                }
            }
    ){
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        points.add(change.position)
                    }

                },
        ){
            drawPath(
                path = Path().apply {
                    points.forEachIndexed { i, point ->
                        if (i == 0) {
                            moveTo(point.x, point.y)
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }
                },
                color = Color.Black,
                style = Stroke(10f)
            )
        }
    }
}

@Composable
fun NumberItem(
    number: Int,
    isActive: Boolean,
    viewModel: TMTViewModel
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(20.sdp)
            .background(
                color = Color.White,
                shape = CircleShape,
            )
            .border(
                width = 1.sdp,
                color = if (isActive) Color.Red else Color.Black,
                shape = CircleShape
            )
            .onGloballyPositioned { coordinates ->
                val position = Offset(
                    x = coordinates.positionInRoot().x,
                    y = coordinates.positionInRoot().y
                )
                val width = coordinates.size.width.toFloat()
                val height = coordinates.size.height.toFloat()
                viewModel.updateBoxPosition(number, position, width, height)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            fontSize = 16.ssp,
            fontWeight = FontWeight.Bold
        )
    }
}
