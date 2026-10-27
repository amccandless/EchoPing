package com.booknerd.echoping

import com.booknerd.echoping.ui.permissions.hasAllPermissions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember

import com.booknerd.echoping.ui.theme.EchoPingTheme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.booknerd.echoping.controllers.ReminderApp
import com.booknerd.echoping.ui.permissions.LocationPermissionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoPingTheme {
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