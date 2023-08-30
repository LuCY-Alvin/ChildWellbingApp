package com.mildp.jetpackcompose.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.model.database.DataBase
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.ZipUtils
import com.mildp.jetpackcompose.viewmodel.UploadViewModel
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp

@Composable
fun UploadScreen(
    uploadViewModel: UploadViewModel = viewModel()
) {
    var progress by remember { mutableStateOf(0) }
    uploadViewModel.getUploadProgressLiveData().observeAsState().value?.let { newProgress ->
        progress = newProgress
    }

    val path = App.instance().dataDir.canonicalPath
    val id = kv.decodeString("subID","")

    DataBase.getDatabase(App.instance())?.backupDatabase(App.instance(),"temp")
    ZipUtils.zipFolders("$path/files", "$path/zipFile_${id}_temp.zip")

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.padding(10.sdp))

        Text(
            text = "上傳資料",
            fontSize = 20.ssp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.padding(5.sdp))

        Button(
            onClick = {
                uploadViewModel.uploadFile("$path/databases/database-$id-backup-temp",0)
                uploadViewModel.uploadFile("$path/zipFile_${id}_temp.zip",1)
            },
            modifier = Modifier,
        ) {
            Text(
                text = "開始上傳",
            )
        }

        Spacer(modifier = Modifier.padding(12.sdp))

        Text(
            text = "上傳進度：$progress %",
        )

        Spacer(modifier = Modifier.padding(12.sdp))

        LinearProgressIndicator(
            progress = progress.toFloat()/100f,
            modifier = Modifier
                .height(10.sdp)
        )
    }
}
