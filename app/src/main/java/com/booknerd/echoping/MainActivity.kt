package com.booknerd.echoping

import com.booknerd.echoping.ui.permissions.hasAllPermissions
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

import com.booknerd.echoping.ui.theme.EchoPingTheme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booknerd.echoping.controllers.ReminderApp
import com.booknerd.echoping.ui.permissions.LocationPermissionScreen
import com.booknerd.echoping.ui.theme.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkPreference by themePreferences.darkMode.collectAsStateWithLifecycle()
            val isDark = darkPreference ?: isSystemInDarkTheme()

            // Keep status/navigation bar icons readable when the app overrides the system theme.
            LaunchedEffect(isDark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            EchoPingTheme(darkTheme = isDark) {
                var isLocationPermissionGranted by rememberSaveable {
                    mutableStateOf(hasAllPermissions(this@MainActivity))
                }

                val snackbarHostState = remember { SnackbarHostState() }

                if (isLocationPermissionGranted) {
                    ReminderApp(
                        snackbarHostState = snackbarHostState,
                        isDarkTheme = isDark,
                        onToggleTheme = { themePreferences.setDarkMode(!isDark) }
                    )
                } else {
                    LocationPermissionScreen(
                        onPermissionGranted = { isLocationPermissionGranted = true }
                    )
                }
            }
        }
    }
}
