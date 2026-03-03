package com.browser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.browser.app.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val currentTab = viewModel.getCurrentTab()
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        // Address Bar
                        OutlinedTextField(
                            value = viewModel.urlInput.value,
                            onValueChange = { viewModel.urlInput.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            placeholder = { Text("Search or enter address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = { viewModel.navigateToUrl(viewModel.urlInput.value) }
                            ),
                            trailingIcon = {
                                if (currentTab?.isLoading == true) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(onClick = { viewModel.navigateToUrl(viewModel.urlInput.value) }) {
                                        Icon(Icons.Default.Search, "Go")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    navigationIcon = {
                        Row {
                            IconButton(
                                onClick = { viewModel.goBack() },
                                enabled = currentTab?.canGoBack ?: false
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                            IconButton(
                                onClick = { viewModel.goForward() },
                                enabled = currentTab?.canGoForward ?: false
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.reload() }) {
                            Icon(Icons.Default.Refresh, "Reload")
                        }
                        IconButton(onClick = { viewModel.showTabsOverview.value = true }) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(viewModel.tabs.size.toString())
                                    }
                                }
                            ) {
                                Icon(Icons.Default.List, "Tabs")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                // Progress bar
                if (currentTab?.isLoading == true) {
                    LinearProgressIndicator(
                        progress = { currentTab.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.zoomOut() }) {
                        Icon(Icons.Default.Remove, "Zoom out")
                    }
                    IconButton(onClick = { viewModel.zoomIn() }) {
                        Icon(Icons.Default.Add, "Zoom in")
                    }
                    IconButton(onClick = { viewModel.toggleDesktopMode() }) {
                        Icon(
                            if (currentTab?.desktopMode == true) 
                                Icons.Default.PhoneAndroid 
                            else 
                                Icons.Default.Computer,
                            if (currentTab?.desktopMode == true) "Mobile mode" else "Desktop mode"
                        )
                    }
                    IconButton(onClick = { viewModel.addNewTab() }) {
                        Icon(Icons.Default.AddCircle, "New tab")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            currentTab?.let { tab ->
                key(tab.id) {
                    WebViewContainer(
                        tab = tab,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // Tabs Overview Dialog
    if (viewModel.showTabsOverview.value) {
        TabsOverview(
            viewModel = viewModel,
            onClose = { viewModel.showTabsOverview.value = false }
        )
    }
}
