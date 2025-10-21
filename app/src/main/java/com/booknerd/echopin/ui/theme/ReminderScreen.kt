package com.booknerd.echopin.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.booknerd.echopin.ui.theme.MyApplicationTheme

//added for FAB
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

//item UI
import androidx.compose.runtime.mutableStateListOf
@Composable
fun ReminderScreen(snackbarHostState: SnackbarHostState) {

    // CoroutineScope and State management live within the Composable.
    val coroutineScope = rememberCoroutineScope()
    val reminderItems = remember { mutableStateListOf<String>() }

    // Scaffold provides the overall screen structure.
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AddNew_FAB {
                // Logic to update state and show the Snackbar on button click
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Adding new reminder, please wait")
                    reminderItems.add("Reminder ${reminderItems.size + 1}")
                }
            }
        }
    ) { paddingValues ->
        // The main content body
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 70.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(reminderItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// Reusable FAB Composable
@Composable
fun AddNew_FAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, "Large FAB, add new item")
    }
}