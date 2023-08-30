package com.mildp.jetpackcompose.utils

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.receiver.Routine

class Worker (context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        val i = Intent(App.instance(), Routine::class.java)
        i.action = "doRoutine"
        App.instance().sendBroadcast(i)

        return Result.success()
    }
}