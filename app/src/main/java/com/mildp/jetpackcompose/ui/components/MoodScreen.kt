package com.mildp.jetpackcompose.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.R
import com.mildp.jetpackcompose.viewmodel.MoodViewModel
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp

@Composable
fun MoodScreen() {
    val  moodViewModel: MoodViewModel = viewModel()
    moodViewModel.onAppUsageData()

    val imageIds = listOf(
        R.drawable._1, R.drawable._2, R.drawable._3, R.drawable._4, R.drawable._5,
        R.drawable._6, R.drawable._7, R.drawable._8, R.drawable._9, R.drawable._10,
        R.drawable._11, R.drawable._12, R.drawable._13, R.drawable._14, R.drawable._15,
        R.drawable._16, R.drawable._17, R.drawable._18, R.drawable._19, R.drawable._20,
        R.drawable._21, R.drawable._22, R.drawable._23, R.drawable._24, R.drawable._25
    )
    val imageContent = listOf(
        Pair(-2,2),Pair(-1,2),Pair(0,2),Pair(1,2),Pair(2,2),
        Pair(-2,1),Pair(-1,1),Pair(0,1),Pair(1,1),Pair(2,1),
        Pair(-2,0),Pair(-1,0),Pair(0,0),Pair(1,0),Pair(2,0),
        Pair(-2,-1),Pair(-1,-1),Pair(0,-1),Pair(1,-1),Pair(2,-1),
        Pair(-2,-2),Pair(-1,-2),Pair(0,-2),Pair(1,-2),Pair(2,-2)
    )

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
                text = moodViewModel.participant + "的心情調查",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.sdp),
                fontSize = 26.ssp
            )

            Text(
                text = "目前的心情\n最符合哪個表情符號呢？",
                textAlign = TextAlign.Center,
                modifier = Modifier
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(30.sdp)
            ) {
                items(imageIds) { imageId ->
                    val imageContentForId = imageContent[imageIds.indexOf(imageId)]
                    ImageItem(
                        imageId = imageId,
                        imageContent = imageContentForId,
                        moodViewModel)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "您選擇的表情符號：",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Image(
                    painter = painterResource(moodViewModel.selectedImageId.value),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.sdp)
                        .weight(1f)
                )
            }

            Row{
                Button(
                    onClick = { moodViewModel.onDoneSurvey() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(15.sdp)
                ) {
                    Text(text = "完成")
                }
            }
        }
    }
}

@Composable
fun ImageItem(
    imageId: Int,
    imageContent: Pair<Int,Int>,
    moodViewModel: MoodViewModel
) {
    val imagePainter: Painter = painterResource(id = imageId)
    Image(
        painter = imagePainter,
        contentDescription = imageContent.toString(),
        modifier = Modifier
            .padding(10.sdp)
            .clickable {
                moodViewModel.onEmojiSelected(imageId,imageContent)
            }
            .size(30.sdp)
    )
}

@Preview
@Composable
fun moodPrev() {
    MaterialTheme{
        MoodScreen()
    }
}