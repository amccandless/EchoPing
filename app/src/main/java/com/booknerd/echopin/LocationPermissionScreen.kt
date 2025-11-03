import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
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

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // A state to hold whether all permissions are granted
    var allPermissionsGranted by remember {
        mutableStateOf(areAllPermissionsGranted(context, locationPermissions))
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // For testing, relaying only on the button click logic below.
        }
    )

    if (allPermissionsGranted) {
        onPermissionGranted()
    } else {
        Scaffold()
         { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding from the Scaffold
                    .padding(16.dp), // Then apply your own padding
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "This app needs location permissions to provide location-based notifications.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionsLauncher.launch(locationPermissions.toTypedArray()) }
                ) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}

private fun areAllPermissionsGranted(context: Context, permissions: List<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

//This is the actual function, commented out for testing
//@Composable
//fun LocationPermissionScreen(
//    onPermissionGranted: () -> Unit
//) {
//    val context = LocalContext.current
//
//    // Build the list of permissions we need
//    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        listOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION
//        )
//    } else {
//        listOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//    }
//
//    // A state to hold whether all permissions are granted
//    var allPermissionsGranted by remember {
//        mutableStateOf(areAllPermissionsGranted(context, locationPermissions))
//    }
//
//    // The launcher that will request the permissions
//    val permissionsLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            // Check if all permissions in the map are granted
//            if (permissions.values.all { it }) {
//                allPermissionsGranted = true
//            }
//            // Here you could add logic to handle cases where some permissions were denied
//        }
//    )
//
//    if (allPermissionsGranted) {
//        // If we have permission, call the lambda to signal we're ready
//        onPermissionGranted()
//    } else {
//        // If we don't have permissions, show the request UI
//        Column(
//            modifier = Modifier.fillMaxSize().padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("This app needs location permissions to provide location-based notifications.")
//            Button(
//                onClick = { permissionsLauncher.launch(locationPermissions.toTypedArray()) },
//                modifier = Modifier.padding(top = 16.dp)
//            ) {
//                Text("Grant Permissions")
//            }
//        }
//    }
//}
//
//// Helper function to check if all permissions are granted
//private fun areAllPermissionsGranted(context: Context, permissions: List<String>): Boolean {
//    return permissions.all {
//        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
//    }
//}