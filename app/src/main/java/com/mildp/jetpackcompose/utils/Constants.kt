package com.mildp.jetpackcompose.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import com.mildp.jetpackcompose.model.BottomNavItem
import com.tencent.mmkv.MMKV

object Constants {

    var kv: MMKV = MMKV.defaultMMKV()

    const val CHANNEL_ID = "Foreground"
    const val NOTIFICATION_ID = 5454
    const val CHANNEL_ID2 = "Survey"
    const val NOTIFICATION_ID2 = 154
    const val CHANNEL_ID3 = "Upload"
    const val NOTIFICATION_ID3 = 455

    val BottomNavItems = listOf(
        BottomNavItem(
            label = "首頁",
            icon = Icons.Filled.Home,
            route = "home"
        ),
        BottomNavItem(
            label = "設定",
            icon = Icons.Filled.Settings,
            route = "setting"
        ),
        BottomNavItem(
            label = "手環",
            icon = Icons.Filled.Face,
            route = "miband"
        ),
        BottomNavItem(
            label = "上傳",
            icon = Icons.Filled.Send,
            route = "upload"
        )

    )

}