package com.mildp.jetpackcompose.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.viewmodel.MibandViewModel
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp

@Composable
fun MibandScreen() {
    val mibandViewModel: MibandViewModel = viewModel()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.padding(10.sdp))

        Text(
            text = "手環設定",
            fontSize = 20.ssp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.padding(5.sdp))

        CustomOutlinedTextField(
            label = "小米手環藍芽編號",
            mibandViewModel
        )

        Spacer(modifier = Modifier.padding(5.sdp))

        TutorialBox(
            title = "下載與設定",
            content = "本實驗將透過小米官方應用程式 Zepp Life 蒐集小孩的活動資料，請您事先完成下載並綁定手環至您的手機。" +
                      "\n\n綁定步驟如下：開啟應用程式後，註冊建立Zepp帳號(請不要使用第三方帳號)，進入主頁面後點選右下角「我的」，添加設備並選擇手環，將權限都選擇同意後，選擇二維碼，並用手機掃描手環。確認綁定後即可在我的裝置中看到小米手環。"+
                      "\n\n設定步驟如下：點擊我的裝置內的手環，點擊「健康監測」，設定全天心率檢測1分鐘，並開啟活動心率檢測；設定輔助睡眠監測；設定全天壓力監測，退回上一頁後往下拉，找到藍芽廣播及運動心率廣播，並且都開啟，即完成設定。",
            mibandViewModel = mibandViewModel,
            videoId = "lbf7B-OtW4g"
        )

        TutorialBox(
            title = "同步手環資料",
            content = "開啟 Zepp Life，並與手環保持近距離，以同步更新每日活動資料。",
            mibandViewModel = mibandViewModel,
            videoId = "GMGZ9l2rg9I"
        )

        TutorialBox(
            title = "手環資料匯出",
            content = "待實驗者通知您實驗完成後，請協助實驗者將實驗期間的小米手環資料匯出。" +
                      "\n\n方法如下：開啟 Zepp Life應用程式，點選「我的」，拉至頁面最下方，點選「設定」，點選倒數第二的「個人資訊安全與隱私」，點選「行使使用者權利」，選擇「匯出資料」，勾選活動、睡眠、心率、體脂、運動並依照實驗者指示選定匯出日期。",
            mibandViewModel = mibandViewModel,
            videoId = "YCiE9dbawl8"
        )

        Button(
            onClick = { mibandViewModel.onDownloadedOrLaunched() },
            modifier = Modifier
                .padding(10.sdp),
        ) {
            Text(
                text = "Zepp Life",
            )
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    label: String,
    mibandViewModel: MibandViewModel
) {
    OutlinedTextField(
        value = mibandViewModel.bandMacSet,
        onValueChange = { newValue ->
            if(newValue.length <= 12 && newValue.all { it.isUpperCase() || it.isDigit() }) {
                mibandViewModel.bandMacSet = newValue
            } else if (newValue.length < mibandViewModel.bandMacSet.length && mibandViewModel.isEditing.value){
                mibandViewModel.bandMacSet = newValue
            }
        },
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        visualTransformation = MacTransformation(),
        trailingIcon = {
            if(mibandViewModel.bandMacSet.length == 12) {
                IconButton(
                    onClick = { mibandViewModel.onStored() },
                    enabled = mibandViewModel.isEditing.value
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "儲存變更"
                    )
                }
            } else {
                IconButton(
                    onClick = { mibandViewModel.onEdited() },
                    enabled = !mibandViewModel.isEditing.value,
                ){
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "填寫"
                    )
                }
            }
        }
    )
}

class MacTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val filteredText = text.text.filter { it.isUpperCase() || it.isDigit() }
        val maxLength = minOf(filteredText.length, 12)
        val sb = StringBuilder(maxLength + (maxLength / 2))

        for (i in 0 until maxLength) {
            sb.append(filteredText[i])

            if (i % 2 == 1 && i < maxLength - 1) {
                sb.append(":")
            }
        }

        return TransformedText(AnnotatedString(sb.toString()), offsetMapping)
    }

    private val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val colonCount = offset / 3
            return offset + colonCount
        }

        override fun transformedToOriginal(offset: Int): Int {
            val colonCount = offset / 3
            return offset - colonCount
        }

    }
}

@Composable
fun TutorialBox(
    title: String,
    content: String,
    mibandViewModel: MibandViewModel,
    videoId: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    val sync = kv.decodeBool("uploadServiceReady", true)

    Column(
        modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .padding(10.sdp)
            .border(
                BorderStroke(1.sdp, color = if(sync && title == "同步手環資料") Color.Red else MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium,
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(1.sdp),
            color = if(sync && title == "同步手環資料") Color.Red else MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.sdp))

        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(all = 8.sdp),
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                textAlign = TextAlign.Center,
                color = if(sync && title == "同步手環資料") Color.Red else MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.sdp))

        Button(onClick = {
            mibandViewModel.onTutorialVideoClicked(videoId)
        }) {
            Text(text = "教學影片")
        }

        Spacer(modifier = Modifier.height(4.sdp))
    }
}
