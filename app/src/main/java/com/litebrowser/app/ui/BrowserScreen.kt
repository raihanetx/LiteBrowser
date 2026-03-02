package com.litebrowser.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litebrowser.app.ui.theme.White
import com.litebrowser.app.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab = tabs.find { it.id == activeTabId }

    var menuOpen by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refresh()
            coroutineScope.launch {
                kotlinx.coroutines.delay(500)
                isRefreshing = false
            }
        }
    )
    
    // Swipe to go back/forward
    var dragOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 100.dp
    val swipeThresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                Column {
                    TabStrip(
                        tabs = tabs,
                        activeTabId = activeTabId,
                        onTabClick = { viewModel.switchToTab(it) },
                        onTabClose = { viewModel.closeTab(it) },
                        onNewTab = { viewModel.openNewTab() },
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragOffset > swipeThresholdPx -> {
                                    viewModel.goBack()
                                }
                                dragOffset < -swipeThresholdPx -> {
                                    viewModel.goForward()
                                }
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
            // WebView content
            activeTab?.let { tab ->
                if (tab.webView != null) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { tab.webView!! },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = White,
                contentColor = MaterialTheme.colorScheme.primary
            )
            
            // Swipe hint overlay
            if (kotlin.math.abs(dragOffset) > 20) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (dragOffset > 0) 
                                androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f)
                            else 
                                androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f)
                        ),
                    contentAlignment = if (dragOffset > 0) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Text(
                        text = if (dragOffset > 0) "← Back" else "Forward →",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
