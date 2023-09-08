package com.mildp.jetpackcompose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mildp.jetpackcompose.model.service.ForegroundService
import com.mildp.jetpackcompose.utils.Constants.kv

class StopExpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val stopIntent = Intent(context,ForegroundService::class.java)
        stopIntent.action = "stopExp"
        kv.encode("stopExp",true)
        context.stopService(stopIntent)
    }
}