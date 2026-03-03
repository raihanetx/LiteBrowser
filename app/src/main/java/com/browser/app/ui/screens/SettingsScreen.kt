package com.browser.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.browser.app.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    onBack: () -> Unit
) {
    // Access the state directly - Compose will observe changes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Accessibility Section
            Text(
                text = "Accessibility",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Text Zoom Setting
                    Text(
                        text = "Text Size",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Adjust text size for better readability. This applies to all websites.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Current value display
                    Text(
                        text = "${viewModel.textZoomLevel.value}%",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Slider
                    Slider(
                        value = viewModel.textZoomLevel.value.toFloat(),
                        onValueChange = { viewModel.textZoomLevel.value = it.toInt() },
                        onValueChangeFinished = { viewModel.setTextZoom(viewModel.textZoomLevel.value) },
                        valueRange = 80f..200f,
                        steps = 11, // 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Min/Max labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("80%", style = MaterialTheme.typography.bodySmall)
                        Text("100%", style = MaterialTheme.typography.bodySmall)
                        Text("200%", style = MaterialTheme.typography.bodySmall)
                    }

                    // Reset button
                    TextButton(
                        onClick = { viewModel.setTextZoom(100) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset to Default")
                    }
                }
            }

            // About Zoom Features
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Page Zoom",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Use the + and - buttons in the browser toolbar to zoom in and out of web pages. Pinch to zoom gestures are also supported.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Zoom levels are saved per website.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
