package com.booknerd.echopin.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

private enum class Step { FOREGROUND, BACKGROUND, NOTIFICATIONS, DONE }

private fun hasPermission(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

fun hasAllPermissions(context: Context): Boolean =
    hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            (Build.VERSION.SDK_INT < 29 ||
                    hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) &&
            (Build.VERSION.SDK_INT < 33 ||
                    hasPermission(context, Manifest.permission.POST_NOTIFICATIONS))

private fun currentStep(context: Context): Step = when {
    !hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> Step.FOREGROUND
    Build.VERSION.SDK_INT >= 29 &&
            !hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> Step.BACKGROUND
    Build.VERSION.SDK_INT >= 33 &&
            !hasPermission(context, Manifest.permission.POST_NOTIFICATIONS) -> Step.NOTIFICATIONS
    else -> Step.DONE
}

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(currentStep(context)) }

    // Re-check when the user returns from a dialog or from Settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { step = currentStep(context) }

    LaunchedEffect(step) {
        if (step == Step.DONE) onPermissionGranted()
    }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { step = currentStep(context) }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { step = currentStep(context) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { step = currentStep(context) }

    fun openSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            Step.FOREGROUND -> PermissionStep(
                title = "Precise location",
                body = "EchoPin needs your precise location to know when you reach a reminder's address. Choose \"Precise\" when asked.",
                buttonText = "Allow location",
                onButtonClick = {
                    foregroundLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onOpenSettings = ::openSettings
            )

            Step.BACKGROUND -> PermissionStep(
                title = "Allow all the time",
                body = "To remind you when the app is closed, choose \"Allow all the time\" for location.",
                buttonText = "Continue",
                onButtonClick = {
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                },
                onOpenSettings = ::openSettings
            )

            Step.NOTIFICATIONS -> PermissionStep(
                title = "Notifications",
                body = "Allow notifications so EchoPin can show your reminder when you arrive.",
                buttonText = "Allow notifications",
                onButtonClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenSettings = ::openSettings
            )

            Step.DONE -> {}
        }
    }
}

@Composable
private fun PermissionStep(
    title: String,
    body: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Spacer(Modifier.height(8.dp))
    Text(
        body,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(24.dp))
    Button(onClick = onButtonClick) { Text(buttonText) }
    TextButton(onClick = onOpenSettings) { Text("Open Settings") }
}
