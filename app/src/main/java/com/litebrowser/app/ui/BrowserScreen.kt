package com.litebrowser.app.ui

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.platform.LocalContext
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

    Scaffold(
        topBar = {
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
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

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = { 
                        Box(
                            Modifier
                                .padding(vertical = 22.dp)
                                .width(40.dp)
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
            .height(58.dp)
            .background(Color.White)
            .border(1.dp, Zinc100)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onHomeClick) {
            Icon(
                Icons.Rounded.Home, 
                contentDescription = null, 
                tint = Zinc900, 
                modifier = Modifier.size(24.dp)
            )
        }

        // Round pill search bar
        Row(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .background(Zinc50, CircleShape)
                .border(1.dp, Zinc200, CircleShape)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.VerifiedUser, 
                contentDescription = null, 
                tint = Color(0xFF4285F4), 
                modifier = Modifier.size(18.dp)
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            "SEARCH OR ENTER URL", 
                            color = Zinc400, 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            )
        }

        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onMoreClick) {
                Icon(
                    Icons.Rounded.MoreVert, 
                    contentDescription = null, 
                    tint = Zinc900, 
                    modifier = Modifier.size(26.dp)
                )
            }
            // Tab counter badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp),
                color = Zinc900,
                shape = CircleShape,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = tabCount.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Branding
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "E",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Zinc900
            )
            Text(
                text = "LitEBrowser",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = Zinc900
            )
        }
        Text(
            text = "THE NEW STANDARD OF BROWSER",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = Zinc400,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Shortcuts grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.width(320.dp),
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
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(site.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            site.icon, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        site.name, 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Zinc400, 
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Zinc50)
                            .border(2.dp, Zinc100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Add, 
                            contentDescription = null, 
                            tint = Zinc200
                        )
                    }
                    Text(
                        "ADD", 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Zinc200, 
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
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SESSION MANAGER", 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 2.sp, 
                color = Zinc400
            )
            TextButton(onClick = onAddTab) {
                Text(
                    "+ NEW TAB", 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Zinc900
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .heightIn(max = 240.dp)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isActive = tab.id == activeTabId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isActive) Zinc900 else Zinc50)
                        .border(
                            1.dp, 
                            if (isActive) Zinc900 else Zinc100, 
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onTabSelect(tab.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            tab.title, 
                            color = if (isActive) Color.White else Zinc900, 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if(tab.url.isEmpty()) "DEFAULT_VIEW" else tab.url.uppercase(), 
                            color = if (isActive) Color.White.copy(0.5f) else Zinc400, 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { onCloseTab(tab.id) }, 
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close, 
                            contentDescription = null, 
                            tint = if (isActive) Color.White.copy(0.5f) else Zinc200
                        )
                    }
                }
            }
        }

        Divider(color = Zinc50, modifier = Modifier.padding(vertical = 12.dp))

        // Zoom controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PAGE ZOOM", 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Black, 
                color = Zinc900
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IconButton(onClick = { onZoomChange((zoom - 10).coerceAtLeast(50)) }) {
                    Icon(
                        Icons.Rounded.RemoveCircleOutline, 
                        contentDescription = null, 
                        tint = Zinc200
                    )
                }
                Text(
                    "$zoom%", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black, 
                    modifier = Modifier.width(40.dp), 
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { onZoomChange((zoom + 10).coerceAtMost(200)) }) {
                    Icon(
                        Icons.Rounded.AddCircleOutline, 
                        contentDescription = null, 
                        tint = Zinc200
                    )
                }
            }
        }

        // Desktop toggle
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.Monitor, contentDescription = null, tint = Zinc900)
                Text(
                    "DESKTOP ENVIRONMENT", 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Zinc900
                )
            }
            Switch(
                checked = isDesktop,
                onCheckedChange = onDesktopToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, 
                    checkedTrackColor = Zinc900
                )
            )
        }
    }
}
