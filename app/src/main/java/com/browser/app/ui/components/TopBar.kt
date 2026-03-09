package com.browser.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.browser.app.util.WebViewHolder
import com.browser.app.viewmodel.BrowserViewModel

@Composable
fun TopBar(
    viewModel: BrowserViewModel = viewModel()
) {
    val tabs by viewModel.tabs.collectAsState()
    val currentTabIndex by viewModel.currentTabIndex.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTab = if (currentTabIndex in tabs.indices) tabs[currentTabIndex] else null
    val urlDisplay = currentTab?.url ?: ""

    var showMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = searchQuery.ifEmpty { urlDisplay },
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search or enter URL", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            val query = searchQuery.trim()
                            if (query.isNotEmpty()) {
                                val url = if (!query.startsWith("http")) {
                                    "https://www.google.com/search?q=${query}"
                                } else {
                                    query
                                }
                                viewModel.updateCurrentTabUrl(url)
                                viewModel.setSearchQuery("")
                                focusManager.clearFocus()
                            }
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TabBadge(
                tabCount = tabs.size,
                onClick = { viewModel.setShowTabManager(true) }
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = { showMenu = !showMenu }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Zoom In") },
                    onClick = {
                        WebViewHolder.getWebView()?.zoomIn()
                        showMenu = false
                    },
                    leadingIcon = {
                        Text("🔍", fontSize = 18.sp)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Zoom Out") },
                    onClick = {
                        WebViewHolder.getWebView()?.zoomOut()
                        showMenu = false
                    },
                    leadingIcon = {
                        Text("🔍", fontSize = 18.sp)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (viewModel.nightMode.value) "Day Mode" else "Night Mode") },
                    onClick = {
                        viewModel.toggleNightMode()
                        showMenu = false
                    },
                    leadingIcon = {
                        Text(if (viewModel.nightMode.value) "☀️" else "🌙", fontSize = 18.sp)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (viewModel.desktopMode.value) "Mobile Site" else "Desktop Site") },
                    onClick = {
                        viewModel.toggleDesktopMode()
                        showMenu = false
                    },
                    leadingIcon = {
                        Text("🖥️", fontSize = 18.sp)
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text("New Tab") },
                    onClick = {
                        viewModel.addNewTab()
                        showMenu = false
                    },
                    leadingIcon = {
                        Text("➕", fontSize = 18.sp)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Bookmarks") },
                    onClick = { showMenu = false },
                    leadingIcon = {
                        Text("🔖", fontSize = 18.sp)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = { showMenu = false },
                    leadingIcon = {
                        Text("⚙️", fontSize = 18.sp)
                    }
                )
            }
        }
    }
}

@Composable
fun TabBadge(
    tabCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tabCount.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
