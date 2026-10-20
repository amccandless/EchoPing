package com.booknerd.echopin

import com.booknerd.echopin.ui.permissions.hasAllPermissions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember

import com.booknerd.echopin.ui.theme.MyApplicationTheme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.booknerd.echopin.controllers.ReminderApp
import com.booknerd.echopin.ui.permissions.LocationPermissionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var isLocationPermissionGranted by rememberSaveable {
                    mutableStateOf(hasAllPermissions(this@MainActivity))
                }

                val snackbarHostState = remember { SnackbarHostState() }

                if (isLocationPermissionGranted) {
                    ReminderApp(snackbarHostState = snackbarHostState)
                } else {
                    LocationPermissionScreen(
                        onPermissionGranted = { isLocationPermissionGranted = true }
                    )
                }
            }
        }
    }
}