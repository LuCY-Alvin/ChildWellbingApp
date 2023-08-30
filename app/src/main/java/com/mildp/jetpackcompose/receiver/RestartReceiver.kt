package com.mildp.jetpackcompose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mildp.jetpackcompose.model.service.ForegroundService
import com.mildp.jetpackcompose.utils.Helper

class RestartReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG: String = "RestartReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Helper().log(TAG,"Service tried to stop")
        val serviceIntent = Intent(context, ForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}