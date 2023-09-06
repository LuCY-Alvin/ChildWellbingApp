package com.mildp.jetpackcompose.model.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.R
import com.mildp.jetpackcompose.model.database.DataBase
import com.mildp.jetpackcompose.utils.Constants.CHANNEL_ID3
import com.mildp.jetpackcompose.utils.Constants.NOTIFICATION_ID3
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.NotificationHelper
import com.mildp.jetpackcompose.utils.ProgressRequestBody
import com.mildp.jetpackcompose.utils.ZipUtils
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class UploadService : Service() {

    companion object {
        private const val TAG: String = "UploadService"
    }
    private val uploadProgressLiveData = MutableLiveData<Int>()
    private fun updateNotification(message: Int) {
        uploadProgressLiveData.postValue(message)
    }
    private var firstValue = 0L
    private var secondValue = 0L
    private var total = 0L

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val manager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val builder = NotificationCompat
        .Builder(this, CHANNEL_ID3)
        .setContentTitle("檔案上傳")
        .setContentText("請連接網路，並等待檔案上傳")
        .setSmallIcon(R.drawable.baseline_file_upload_24)
        .setOngoing(true)
        .setOnlyAlertOnce(true)

    private val uploadMap = HashMap<Int,Boolean>()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationHelper = NotificationHelper(this)
                notificationHelper.createNotificationChannel(
                    CHANNEL_ID3,
                    "Upload Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            }
            manager.notify(NOTIFICATION_ID3, builder.build())
            upload()
        } catch(e: Exception) {
            Helper().log(TAG,"Error in UploadServiceStartCommand: $e")
        }
        return START_NOT_STICKY
    }

    private suspend fun uploadFile(sourceFileUri: String, num: Int) {
        try {
            val file = File(sourceFileUri)
            if (!file.exists()) {
                withContext(Dispatchers.IO) {
                    try {
                        file.createNewFile()
                    } catch (e: IOException) {
                        Helper().log(TAG, "File Create Failed: $e")
                    }
                }
            }
            total += file.length().toInt()
            uploadMap[num] = false

            val client = OkHttpClient.Builder()
                .connectTimeout(7, TimeUnit.MINUTES)
                .writeTimeout(7, TimeUnit.MINUTES)
                .readTimeout(7, TimeUnit.MINUTES)
                .build()

            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "my_file", file.name,
                    file.asRequestBody("text/plain".toMediaTypeOrNull())
                )
                .addFormDataPart("some-field", "some-value")
                .build()

            val request: Request = Request.Builder()
                .url("http://mil.psy.ntu.edu.tw/~lualvin/Upload_txt.php")
                .post(
                    ProgressRequestBody(requestBody) { bytesWritten, _ ->
                        if (num == 0) firstValue = bytesWritten
                        else secondValue = bytesWritten
                        val progress = ((firstValue + secondValue) *100 / total).toInt()
                        updateNotification(progress)
                    })
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Helper().log(TAG,"send fail:${file.name}, error: $e")
                    builder.setContentTitle("檔案上傳錯誤").setContentText("請重啟網路，15分鐘後將自動重新上傳")
                    manager.notify(NOTIFICATION_ID3,builder.build())
                    kv.encode("uploadServiceReady",true)

                    call.cancel()
                }

                override fun onResponse(call: Call, response: Response) {
                    try{
                        Handler(Looper.getMainLooper()).post {
                            uploadProgressLiveData.observeForever { message ->
                                builder.setContentText("檔案上傳完成 $message %")
                                    .setProgress(100, message, false)
                                manager.notify(NOTIFICATION_ID3, builder.build())

                                Helper().log(
                                    TAG,
                                    "send success:${file.name}, over Response: + ${response.body}"
                                )

                                uploadMap[num] = true

                                if (uploadMap.values.all { it }) {
                                    manager.cancel(NOTIFICATION_ID3)
                                    kv.encode("uploadServiceReady", false)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Helper().log(TAG,"There's Error In Response:$e")
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (ex: Exception) {
            Helper().log(TAG,"There's Error In Upload:$ex")
        }
    }

    private fun upload() {
        coroutineScope.launch {
            val path = App.instance().dataDir.canonicalPath
            val id = kv.decodeString("subID", "")

            Handler(Looper.getMainLooper()).post {
                uploadProgressLiveData.observeForever { message ->
                    builder.setContentText("請保持網路連接，檔案上傳中，請稍後")
                        .setProgress(100, message, false)
                    manager.notify(NOTIFICATION_ID3, builder.build())
                }
            }

            try {
                val num = Helper().databaseDay()

                val zipCompleted = async {
                    DataBase.getDatabase(App.instance())?.backupDatabase(this@UploadService, num.toString())
                    ZipUtils.zipFolders("$path/files", "$path/zipFile_${id}_day$num.zip")
                    true // 表示壓縮完成
                }

                if (zipCompleted.await()) {
                    uploadFile("$path/databases/database-$id-backup-$num", 0)
                    uploadFile("$path/zipFile_${id}_day$num.zip", 1)

                    if (num >= 2) {
                        this@UploadService.deleteDatabase("database-$id-backup-${num - 1}")
                        this@UploadService.deleteDatabase("database-$id-backup-${num - 1}-wal")
                        this@UploadService.deleteDatabase("database-$id-backup-${num - 1}-shm")
                    }
                } else {
                    Helper().log(TAG, "壓縮尚未完成")
                }
            } catch (e: Exception) {
                Helper().log(TAG, "upload failed: $e")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
