package com.mildp.jetpackcompose.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.R
import com.mildp.jetpackcompose.viewmodel.HanderViewModel
import ir.kaaveh.sdpcompose.sdp

@Composable
fun HanderScreen(
    handerViewModel: HanderViewModel = viewModel(),
    navController: NavHostController
) {
    BackHandler(enabled = true) {
        Toast.makeText(App.instance(),"請繼續作答，不要退回前一頁，感謝。", Toast.LENGTH_SHORT).show()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "您剛剛進行測驗的方式與何種最相似？\n請點擊圖片兩次",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(30.sdp)
            )

            Row(Modifier.padding(10.sdp)) {
                Text(
                    text = "左手",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "右手",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(thickness = 1.sdp, modifier = Modifier.padding(start = 20.sdp,end = 20.sdp))

            Text(
                text = "單手持手機",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(2.sdp)
            )

            Divider(thickness = 1.sdp, modifier = Modifier.padding(start = 20.sdp,end = 20.sdp))

            Row(Modifier.padding(10.sdp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_left_single),
                    contentDescription = "Left and Single",
                    modifier = Modifier
                        .size(60.sdp)
                        .weight(1f)
                        .clickable { handerViewModel.onClicked("Left and Single", navController) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_right_single),
                    contentDescription = "Right and Single",
                    modifier = Modifier
                        .size(60.sdp)
                        .weight(1f)
                        .clickable { handerViewModel.onClicked("Right and Single", navController) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }

            Divider(thickness = 1.sdp, modifier = Modifier.padding(start = 20.sdp,end = 20.sdp))

            Text(
                text = "雙手持手機",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(2.sdp)
            )

            Divider(thickness = 1.sdp, modifier = Modifier.padding(start = 20.sdp,end = 20.sdp))

            Row(Modifier.padding(10.sdp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_left_double),
                    contentDescription = "Left and Double",
                    modifier = Modifier
                        .size(60.sdp)
                        .weight(1f)
                        .clickable { handerViewModel.onClicked("Left and Double", navController) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_right_double),
                    contentDescription = "Right and Double",
                    modifier = Modifier
                        .size(60.sdp)
                        .weight(1f)
                        .clickable { handerViewModel.onClicked("Right and Double", navController) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }

            Divider(thickness = 1.sdp, modifier = Modifier.padding(start = 20.sdp,end = 20.sdp))

            Text(
                text = "手機放置平面，以手指滑動",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(2.sdp)
            )

            Divider(thickness = 1.sdp, modifier = Modifier.padding(start = 20.sdp,end = 20.sdp))

            Row(Modifier.padding(10.sdp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_left_table),
                    contentDescription = "Left and Table",
                    modifier = Modifier
                        .size(60.sdp)
                        .weight(1f)
                        .clickable { handerViewModel.onClicked("Left and Table", navController) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_right_table),
                    contentDescription = "Right and Table",
                    modifier = Modifier
                        .size(60.sdp)
                        .weight(1f)
                        .clickable { handerViewModel.onClicked("Right and Table", navController) },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }

        }
    }
}
