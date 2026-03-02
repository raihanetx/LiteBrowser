package com.litebrowser.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litebrowser.app.ui.theme.Blue600
import com.litebrowser.app.ui.theme.Grey200
import com.litebrowser.app.ui.theme.White
import com.litebrowser.app.viewmodel.BrowserViewModel

@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab = tabs.find { it.id == activeTabId }

    var menuOpen by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = (activeTab?.progress ?: 0) / 100f,
        label = "progress",
    )

    Scaffold(
        topBar = {
            Column {
                // Tab Strip at top
                TabStrip(
                    tabs = tabs,
                    activeTabId = activeTabId,
                    onTabClick = { viewModel.switchToTab(it) },
                    onTabClose = { viewModel.closeTab(it) },
                    onNewTab = { viewModel.openNewTab() },
                )
                
                // Progress bar
                if (activeTab?.isLoading == true) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Blue600,
                        trackColor = Grey200,
                    )
                }
            }
        },
        bottomBar = {
            // URL Bar at bottom (thumb-friendly)
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
        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(White)
        ) {
            activeTab?.let { tab ->
                if (tab.webView != null) {
                    key("${tab.id}_${tab.isDesktopMode}") {
                        WebViewContainer(
                            tab = tab,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
