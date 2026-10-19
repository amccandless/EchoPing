package com.booknerd.echopin

import LocationPermissionScreen
import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.booknerd.echopin.controllers.ReminderApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var isLocationPermissionGranted by rememberSaveable {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    )
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
//    @Composable
//    fun Greeting(name: String, modifier: Modifier = Modifier) {
//        Text(
//            text = "Hello $name!",
//            modifier = modifier
//        )
//    }
//
//    @Preview(showBackground = true)
//    @Composable
//    fun GreetingPreview() {
//        MyApplicationTheme {
//            Greeting("Android 15")
//        }
//    }
