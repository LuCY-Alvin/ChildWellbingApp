package com.mildp.jetpackcompose.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mildp.jetpackcompose.utils.Helper
import com.mildp.jetpackcompose.utils.ProgressRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
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
        viewModelScope.launch {
            try {
                val file = File(fileUri)
                if (!file.exists()) {
                    Helper().log(TAG, "File does not exist: $fileUri")
                    return@launch
                }

                total += file.length()
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
                            val progress = ((firstValue + secondValue) * 100 / total).toInt()
                            uploadProgressLiveData.postValue(progress)
                        })
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(7, TimeUnit.MINUTES)
                    .writeTimeout(7, TimeUnit.MINUTES)
                    .readTimeout(7, TimeUnit.MINUTES)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    Helper().log(TAG, "Upload successful: ${file.name}")
                } else {
                    Helper().log(TAG, "Upload failed: ${file.name}, Response: ${response.code}")
                }

                response.close()

            } catch (e: Exception) {
                Helper().log(TAG, e.toString())
            }
        }
    }

    private val zipCompletedLiveData = MutableLiveData<Boolean>()

    fun getZipCompletedLiveData(): LiveData<Boolean> {
        return zipCompletedLiveData
    }

    fun setZipCompleted(completed: Boolean) {
        zipCompletedLiveData.postValue(completed)
    }
}