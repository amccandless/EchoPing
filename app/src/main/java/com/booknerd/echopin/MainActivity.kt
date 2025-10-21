package com.booknerd.echopin

import LocationPermissionScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.booknerd.echopin.ui.theme.MyApplicationTheme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.booknerd.echopin.ui.theme.ReminderScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var isLocationPermissionGranted by remember { mutableStateOf(false) }

                // Define state required at the Activity/Scaffold level
                val snackbarHostState = remember { SnackbarHostState() }

                if (isLocationPermissionGranted) {
                    // Call your main content screen here!
                    ReminderScreen(snackbarHostState = snackbarHostState)
                } else {
                    LocationPermissionScreen(
                        onPermissionGranted = {
                            isLocationPermissionGranted = true
                        })
                }
            }
        }
    }
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyApplicationTheme {
            Greeting("Android 15")
        }
    }
}