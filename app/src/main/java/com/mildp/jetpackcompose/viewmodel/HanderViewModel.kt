package com.mildp.jetpackcompose.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.utils.Helper

class HanderViewModel : ViewModel() {

    companion object{
        private const val TAG = "HanderViewModel"
    }

    private val doubleClickDuration = 500 // Change this to your desired double-click duration
    private var lastClickTime: Long = 0

    fun onClicked(content: String, navController: NavHostController) {
        val currentTime = System.currentTimeMillis()
        val lastClickTime = lastClickTime
        this.lastClickTime = currentTime

        if (currentTime - lastClickTime < doubleClickDuration) {
            Helper().log(TAG,content)

            navController.navigate("moodScreen")
        } else {
            Toast.makeText(App.instance(), "請快速點擊兩次確認", Toast.LENGTH_SHORT).show()
        }
    }
}