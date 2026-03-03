package com.litebrowser.app.ui

import android.webkit.WebView
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litebrowser.app.model.BrowserTab
import com.litebrowser.app.model.Shortcut
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

    val shortcuts = listOf(
        Shortcut("YOUTUBE", "https://m.youtube.com", Color(0xFFFF0000), Icons.Rounded.PlayCircle),
        Shortcut("GOOGLE", "https://www.google.com", Color(0xFF4285F4), Icons.Rounded.Search),
        Shortcut("GITHUB", "https://github.com", Color(0xFF181717), Icons.Rounded.Code),
        Shortcut("REDDIT", "https://reddit.com", Color(0xFFFF4500), Icons.Rounded.Forum)
    )

    // Single unified background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding() // Fix cut-off by adding system bars padding
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header - Clean & Consistent
            EvanlyHeader(
                url = activeTab?.url ?: "",
                tabCount = tabs.size,
                onUrlChange = { viewModel.navigate(it) },
                onHomeClick = { 
                    isHomePage = true
                    viewModel.navigate("")
                },
                onMoreClick = { showSheet = true },
                onSearch = { 
                    isHomePage = false
                }
            )

            // Divider for visual separation
            HorizontalDivider(thickness = 1.dp, color = Zinc100)

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isHomePage || activeTab?.url?.isEmpty() != false) {
                    EvanlyHomePage(shortcuts) { selectedUrl ->
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

        // Bottom Sheet Menu
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
                UniversalSliderContent(
                    tabs = tabs.map { com.litebrowser.app.model.BrowserTab(id = it.id.toLong(), title = it.title, url = it.url) },
                    activeTabId = activeTab?.id?.toLong() ?: 0L,
                    zoom = activeTab?.zoomLevel ?: 100,
                    isDesktop = activeTab?.isDesktopMode ?: false,
                    onZoomChange = { 
                        if (it > (activeTab?.zoomLevel ?: 100)) viewModel.zoomIn()
                        else viewModel.zoomOut()
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

    // Fixed height with proper padding - no cut-off
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // Slightly taller
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp), // Consistent padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Home icon
        IconButton(
            onClick = onHomeClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Rounded.Home, 
                contentDescription = "Home", 
                tint = Zinc900, 
                modifier = Modifier.size(24.dp)
            )
        }

        // Search bar - unified style
        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(Zinc50, CircleShape)
                .border(1.dp, Zinc200, CircleShape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search, 
                contentDescription = null, 
                tint = Zinc400, 
                modifier = Modifier.size(20.dp)
            )
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
                textStyle = TextStyle(
                    color = Zinc900,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            "Search or enter URL", 
                            color = Zinc400, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Menu with tab counter
        Box(contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.MoreVert, 
                    contentDescription = "Menu", 
                    tint = Zinc900, 
                    modifier = Modifier.size(24.dp)
                )
            }
            // Tab counter badge
            if (tabCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp),
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
fun EvanlyHomePage(
    shortcuts: List<Shortcut>, 
    onShortcutClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Consistent white background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Branding - unified style
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Zinc900),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "E",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Text(
                text = "LitEBrowser",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Zinc900
            )
        }
        
        Text(
            text = "FAST • LIGHTWEIGHT • PRIVATE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = Zinc400,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Shortcuts grid - unified 4-column grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(shortcuts) { site ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onShortcutClick(site.url) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(site.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            site.icon, 
                            contentDescription = site.name, 
                            tint = Color.White, 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        site.name, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Zinc600, 
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // Add button
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Zinc50)
                            .border(2.dp, Zinc200, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Add, 
                            contentDescription = "Add", 
                            tint = Zinc400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        "ADD", 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Zinc400, 
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UniversalSliderContent(
    tabs: List<BrowserTab>,
    activeTabId: Long,
    zoom: Int,
    isDesktop: Boolean,
    onZoomChange: (Int) -> Unit,
    onDesktopToggle: (Boolean) -> Unit,
    onTabSelect: (Long) -> Unit,
    onCloseTab: (Long) -> Unit,
    onAddTab: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp, top = 8.dp)
    ) {
        // Title row
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SESSIONS", 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 1.sp, 
                color = Zinc400
            )
            TextButton(onClick = onAddTab) {
                Text(
                    "+ NEW", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Zinc900
                )
            }
        }

        // Tab list
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 200.dp)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isActive = tab.id == activeTabId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) Zinc900 else Zinc50)
                        .border(
                            1.dp, 
                            if (isActive) Zinc900 else Zinc200, 
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onTabSelect(tab.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            tab.title, 
                            color = if (isActive) Color.White else Zinc900, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if(tab.url.isEmpty()) "Home" else tab.url, 
                            color = if (isActive) Color.White.copy(0.6f) else Zinc500, 
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { onCloseTab(tab.id) }, 
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close, 
                            contentDescription = "Close", 
                            tint = if (isActive) Color.White.copy(0.6f) else Zinc400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Zinc100)

        // Zoom controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ZOOM", 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Black, 
                color = Zinc900
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { onZoomChange((zoom - 25).coerceAtLeast(50)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.Remove, 
                        contentDescription = "Zoom out", 
                        tint = Zinc600,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "$zoom%", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Zinc900,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { onZoomChange((zoom + 25).coerceAtMost(200)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.Add, 
                        contentDescription = "Zoom in", 
                        tint = Zinc600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Zinc100)

        // Desktop toggle
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.DesktopWindows, 
                    contentDescription = null, 
                    tint = Zinc900,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "DESKTOP MODE", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Zinc900
                )
            }
            Switch(
                checked = isDesktop,
                onCheckedChange = onDesktopToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, 
                    checkedTrackColor = Zinc900,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Zinc300
                )
            )
        }
    }
}
