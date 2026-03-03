package com.litebrowser.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litebrowser.app.ui.theme.*
import com.litebrowser.app.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val activeTab = tabs.find { it.id == activeTabId }
    
    var isHomePage by remember { mutableStateOf(activeTab?.url?.isEmpty() != false) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentZoom by remember { mutableStateOf(activeTab?.zoomLevel ?: 100) }

    // Update zoom when active tab changes
    LaunchedEffect(activeTab?.zoomLevel) {
        currentZoom = activeTab?.zoomLevel ?: 100
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            EvanlyHeader(
                url = activeTab?.url ?: "",
                tabCount = tabs.size,
                onUrlChange = { viewModel.navigate(it) },
                onHomeClick = { 
                    isHomePage = true
                    viewModel.navigate("")
                },
                onMoreClick = { showSheet = true },
                onSearch = { isHomePage = false }
            )

            HorizontalDivider(thickness = 1.dp, color = Zinc100)

            // Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isHomePage) {
                    EvanlyHomePage { selectedUrl ->
                        isHomePage = false
                        viewModel.navigate(selectedUrl)
                    }
                } else {
                    activeTab?.let { tab ->
                        if (tab.webView != null) {
                            AndroidView(
                                factory = { tab.webView!! },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheet
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { 
                    Box(
                        Modifier
                            .padding(vertical = 16.dp)
                            .width(48.dp)
                            .height(4.dp)
                            .background(Zinc200, RoundedCornerShape(50))
                    ) 
                }
            ) {
                BottomSheetContent(
                    tabs = tabs,
                    activeTabId = activeTabId ?: 0L,
                    zoom = currentZoom,
                    isDesktop = activeTab?.isDesktopMode ?: false,
                    onZoomIn = { 
                        currentZoom = (currentZoom + 25).coerceAtMost(200)
                        viewModel.zoomIn()
                    },
                    onZoomOut = { 
                        currentZoom = (currentZoom - 25).coerceAtLeast(50)
                        viewModel.zoomOut()
                    },
                    onDesktopToggle = { viewModel.toggleDesktopMode() },
                    onTabSelect = { id ->
                        viewModel.switchToTab(id)
                        showSheet = false
                        isHomePage = false
                    },
                    onCloseTab = { id ->
                        viewModel.closeTab(id)
                    },
                    onAddTab = {
                        viewModel.openNewTab()
                    }
                )
            }
        }
    }
}

@Composable
fun EvanlyHeader(
    url: String,
    tabCount: Int,
    onUrlChange: (String) -> Unit,
    onHomeClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf(url) }

    LaunchedEffect(url) {
        inputText = url
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onHomeClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Rounded.Home, contentDescription = "Home", tint = Zinc900, modifier = Modifier.size(24.dp))
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(Zinc50, CircleShape)
                .border(1.dp, Zinc200, CircleShape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = Zinc400, modifier = Modifier.size(20.dp))
            BasicTextField(
                value = inputText,
                onValueChange = { 
                    inputText = it
                    onUrlChange(it)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { 
                    onSearch()
                    focusManager.clearFocus()
                }),
                textStyle = TextStyle(color = Zinc900, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text("Search or enter URL", color = Zinc400, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    innerTextField()
                }
            )
        }

        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onMoreClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = Zinc900, modifier = Modifier.size(24.dp))
            }
            if (tabCount > 0) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp),
                    color = Zinc900,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Text(
                        text = tabCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EvanlyHomePage(onShortcutClick: (String) -> Unit) {
    val shortcuts = listOf(
        Triple("YOUTUBE", "https://m.youtube.com", Color(0xFFFF0000)),
        Triple("GOOGLE", "https://www.google.com", Color(0xFF4285F4)),
        Triple("GITHUB", "https://github.com", Color(0xFF181717)),
        Triple("REDDIT", "https://reddit.com", Color(0xFFFF4500))
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Zinc900),
                contentAlignment = Alignment.Center
            ) {
                Text("E", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Text("LitEBrowser", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Zinc900)
        }
        
        Text(
            "FAST • LIGHTWEIGHT • PRIVATE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = Zinc400,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(shortcuts) { (name, url, color) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onShortcutClick(url) }
                ) {
                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(1), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Zinc600, modifier = Modifier.padding(top = 8.dp))
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape).background(Zinc50).border(2.dp, Zinc200, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Zinc400, modifier = Modifier.size(28.dp))
                    }
                    Text("ADD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Zinc400, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun BottomSheetContent(
    tabs: List<com.litebrowser.app.model.BrowserTab>,
    activeTabId: Long,
    zoom: Int,
    isDesktop: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onDesktopToggle: () -> Unit,
    onTabSelect: (Long) -> Unit,
    onCloseTab: (Long) -> Unit,
    onAddTab: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp, top = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("SESSIONS", fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Zinc400)
            TextButton(onClick = onAddTab) {
                Text("+ NEW", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Zinc900)
            }
        }

        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isActive = tab.id == activeTabId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) Zinc900 else Zinc50)
                        .border(1.dp, if (isActive) Zinc900 else Zinc200, RoundedCornerShape(12.dp))
                        .clickable { onTabSelect(tab.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tab.title, color = if (isActive) Color.White else Zinc900, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if(tab.url.isEmpty()) "Home" else tab.url, 
                            color = if (isActive) Color.White.copy(0.6f) else Zinc500, 
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onCloseTab(tab.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isActive) Color.White.copy(0.6f) else Zinc400, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Zinc100)

        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("ZOOM", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Zinc900)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = onZoomOut, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Zoom out", tint = Zinc600, modifier = Modifier.size(20.dp))
                }
                Text("$zoom%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Zinc900, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                IconButton(onClick = onZoomIn, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = "Zoom in", tint = Zinc600, modifier = Modifier.size(20.dp))
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Zinc100)

        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Rounded.DesktopWindows, contentDescription = null, tint = Zinc900, modifier = Modifier.size(20.dp))
                Text("DESKTOP MODE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Zinc900)
            }
            Switch(
                checked = isDesktop,
                onCheckedChange = { onDesktopToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Zinc900, uncheckedThumbColor = Color.White, uncheckedTrackColor = Zinc300)
            )
        }
    }
}