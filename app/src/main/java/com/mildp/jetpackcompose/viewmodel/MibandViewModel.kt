package com.mildp.jetpackcompose.viewmodel

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mildp.jetpackcompose.App
import com.mildp.jetpackcompose.activity.SurveyActivity
import com.mildp.jetpackcompose.utils.Constants.kv
import com.mildp.jetpackcompose.utils.Helper

class MibandViewModel:ViewModel() {

    companion object {
        private const val TAG: String = "MibandViewModel"
    }

    private lateinit var packageManager: PackageManager
    var bandMacSet by mutableStateOf(kv.decodeString("bandMacSet","").toString())

    fun onStored() {
        kv.encode("bandMacSet",formatMacAddress(bandMacSet))
    }

    private fun formatMacAddress(mac: String): String {
        val formattedMac = StringBuilder()
        for (i in mac.indices step 2) {
            if (i > 0) {
                formattedMac.append(':')
            }
            formattedMac.append(mac.substring(i, i + 2))
        }
        return formattedMac.toString()
    }

    fun onDownloadedOrLaunched() {
        if(!appInstalledOrNot()){
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.xiaomi.hm.health&hl=zh_TW&gl=US"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance().startActivity(intent)
        } else {
            val launchIntent = packageManager
                .getLaunchIntentForPackage("com.xiaomi.hm.health")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                App.instance().startActivity(launchIntent)
            }
        }
    }

    private fun appInstalledOrNot(): Boolean {
        packageManager = App.instance().packageManager
        return try {
            packageManager.getPackageInfo("com.xiaomi.hm.health", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Helper().log(TAG, "還沒下載Zepp Life應用程式")
            false
        }
    }
}

