package com.litebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litebrowser.app.ui.theme.*
import com.litebrowser.app.viewmodel.BrowserViewModel

@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab = tabs.find { it.id == activeTabId }

    var menuOpen by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TabStrip(
                tabs = tabs,
                activeTabId = activeTabId,
                onTabClick = { viewModel.switchToTab(it) },
                onTabClose = { viewModel.closeTab(it) },
                onNewTab = { viewModel.openNewTab() },
            )
        },
        bottomBar = {
            Box {
                UrlBar(
                    activeTab = activeTab,
                    onNavigate = { viewModel.navigate(it) },
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onRefresh = { viewModel.refresh() },
                    onMenuOpen = { menuOpen = true },
                )

                BrowserMenu(
                    expanded = menuOpen,
                    activeTab = activeTab,
                    onDismiss = { menuOpen = false },
                    onZoomIn = { viewModel.zoomIn() },
                    onZoomOut = { viewModel.zoomOut() },
                    onZoomReset = { viewModel.zoomReset() },
                    onToggleDesktop = { viewModel.toggleDesktopMode(); menuOpen = false },
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragOffset > 100f -> viewModel.goBack()
                                dragOffset < -100f -> viewModel.goForward()
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    )
                }
        ) {
            activeTab?.let { tab ->
                if (tab.webView != null) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { tab.webView!! },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
