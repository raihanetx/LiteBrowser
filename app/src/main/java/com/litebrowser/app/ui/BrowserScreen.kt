package com.litebrowser.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val tabs        by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab      = tabs.find { it.id == activeTabId }

    var menuOpen by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = (activeTab?.progress ?: 0) / 100f,
        label = "progress",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .systemBarsPadding()
    ) {
        TabStrip(
            tabs        = tabs,
            activeTabId = activeTabId,
            onTabClick  = { viewModel.switchToTab(it) },
            onTabClose  = { viewModel.closeTab(it) },
            onNewTab    = { viewModel.openNewTab() },
        )

        Box {
            UrlBar(
                activeTab  = activeTab,
                onNavigate = { viewModel.navigate(it) },
                onBack     = { viewModel.goBack() },
                onForward  = { viewModel.goForward() },
                onRefresh  = { viewModel.refresh() },
                onMenuOpen = { menuOpen = true },
            )

            BrowserMenu(
                expanded        = menuOpen,
                activeTab       = activeTab,
                onDismiss       = { menuOpen = false },
                onZoomIn        = { viewModel.zoomIn() },
                onZoomOut       = { viewModel.zoomOut() },
                onZoomReset     = { viewModel.zoomReset() },
                onToggleDesktop = { viewModel.toggleDesktopMode(); menuOpen = false },
            )
        }

        if (activeTab?.isLoading == true) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Grey200)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(Blue600)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            activeTab?.let { tab ->
                if (tab.webView != null) {
                    key(tab.id) {
                        WebViewContainer(
                            tab      = tab,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
