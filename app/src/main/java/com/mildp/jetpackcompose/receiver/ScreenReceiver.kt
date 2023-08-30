package com.mildp.jetpackcompose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mildp.jetpackcompose.model.service.ForegroundService

class ScreenReceiver : BroadcastReceiver() {

    private var isScreenOff = false
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> isScreenOff = true
            Intent.ACTION_SCREEN_ON -> isScreenOff = false
        }
        val i = Intent(context, ForegroundService::class.java).apply {
            putExtra("Screen_state", isScreenOff)
        }
        ContextCompat.startForegroundService(context, i)
    }
}