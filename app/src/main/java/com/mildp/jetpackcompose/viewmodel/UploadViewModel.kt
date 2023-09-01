package com.mildp.jetpackcompose.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.ProgressRequestBody
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class UploadViewModel: ViewModel() {

    companion object {
        private const val TAG: String = "UploadPage"
    }

    private val uploadProgressLiveData = MutableLiveData<Int>()
    private var firstValue = 0L
    private var secondValue = 0L
    private var total = 0L

    fun getUploadProgressLiveData(): LiveData<Int> {
        return uploadProgressLiveData
    }

    fun uploadFile(fileUri: String, num: Int) {
        try {
            val file = File(fileUri)
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    Helper().log(TAG, "File Create Failed: $e")
                }
            }

            total += file.length()

            viewModelScope.launch {
                val requestBody: RequestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "my_file",
                        file.name,
                        file.asRequestBody("text/plain".toMediaTypeOrNull())
                    )
                    .addFormDataPart("some-field", "some-value")
                    .build()

                val request = Request.Builder()
                    .url("http://mil.psy.ntu.edu.tw/~lualvin/Upload_txt.php")
                    .post(
                        ProgressRequestBody(requestBody) { bytesWritten, _ ->
                            if (num == 0) firstValue = bytesWritten
                            else secondValue = bytesWritten
                            val progress = ((firstValue + secondValue) *100 / total).toInt()
                            uploadProgressLiveData.postValue(progress)
                        })
                    .build()


                val client = OkHttpClient.Builder()
                    .connectTimeout(7, TimeUnit.MINUTES)
                    .writeTimeout(7, TimeUnit.MINUTES)
                    .readTimeout(7, TimeUnit.MINUTES)
                    .build()

                with(client) {
                    newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Helper().log(
                                TAG,
                                "send fail:${file.name}, error: $e"
                            )
                            Toast.makeText(App.instance(),"上傳失敗，請關閉App後重試",Toast.LENGTH_SHORT).show()
                            call.cancel()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                Helper().log(
                                    TAG,
                                    "send success:${file.name}, over Response: + ${response.body?.string()}"
                                )
                                Toast.makeText(App.instance(),"上傳成功",Toast.LENGTH_SHORT).show()
                            } catch (e: IOException) {
                                Helper().log(
                                    TAG,
                                    "send fail:${file.name}, error: $e"
                                )
                                Toast.makeText(App.instance(),"上傳失敗，請關閉App後重試",Toast.LENGTH_SHORT).show()
                            } finally {
                                response.close()
                            }
                        }
                    })
                }
            }
        } catch (e:Exception) {
            Helper().log(TAG,e.toString())
        }
    }
}