package com.mildp.jetpackcompose

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mildp.jetpackcompose.model.database.Dao
import com.mildp.jetpackcompose.model.database.DataBase
import com.tencent.mmkv.MMKV

class App:Application() {

    companion object{
        private lateinit var instance: App
        fun instance() = instance
    }

    lateinit var dataDao: Dao
        private set

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        instance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        dataDao = DataBase.getDatabase(this)?.getDataDao()!!
    }
}