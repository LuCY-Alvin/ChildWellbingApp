package com.mildp.jetpackcompose.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mildp.jetpackcompose.ui.components.HomeScreen
import com.mildp.jetpackcompose.ui.components.MibandScreen
import com.mildp.jetpackcompose.ui.components.SettingScreen
import com.mildp.jetpackcompose.ui.components.UploadScreen
import com.mildp.jetpackcompose.ui.theme.AppTheme
import com.mildp.jetpackcompose.utils.Constants
import com.mildp.jetpackcompose.utils.Helper
import ir.kaaveh.sdpcompose.ssp


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }, content = { padding ->
                        NavHostContainer(navController = navController, padding = padding)
                    }
                )
            }
        }
        onPermissionGranted()
        Helper().myWorkManager()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            when(it.action){
                "setting" -> navController.navigate("setting")
                "miband" -> navController.navigate("miband")
            }
        }
    }

    private fun onPermissionGranted() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasPermissions = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!hasPermissions) {
            requestPermissions()
        }
    }

    private fun requestPermissions(){
        val permissions = mutableListOf(
            Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        ActivityCompat.requestPermissions(
            this, permissions.toTypedArray(), 600
        )
    }

}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues,
) {

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues = padding),

        builder = {
            composable("home") {
                HomeScreen()
            }
            composable("setting") {
                SettingScreen()
            }
            composable("miband") {
                MibandScreen()
            }
            composable("upload") {
                UploadScreen()
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
    ) {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        Constants.BottomNavItems.forEach { navItem ->

            BottomNavigationItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route)
                },
                icon = {
                    Icon(
                        imageVector = navItem.icon,
                        contentDescription = navItem.label,
                        tint = if (currentRoute == navItem.route) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                },
                label = {
                    Text(
                        text = navItem.label,
                        fontSize = 12.ssp,
                        color = if (currentRoute == navItem.route) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

