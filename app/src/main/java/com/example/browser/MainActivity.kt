package com.example.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val PrimaryBlue = ComposeColor(0xFF2196F3)
val SurfaceWhite = ComposeColor(0xFFFFFFFF)
val Gray50 = ComposeColor(0xFFF5F5F5)
val Gray100 = ComposeColor(0xFFF5F5F5)
val Gray200 = ComposeColor(0xFFE0E0E0)
val Gray400 = ComposeColor(0xFF9E9E9E)
val Gray600 = ComposeColor(0xFF757575)
val Gray800 = ComposeColor(0xFF424242)
val TextDark = ComposeColor(0xFF212121)

data class BrowserTab(val title: String, val url: String, val isActive: Boolean = false, val faviconColor: ComposeColor = PrimaryBlue)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Gray50, modifier = Modifier.fillMaxSize()) {
                    BrowserApp()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Handled in composable
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserApp() {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var isSearchFocused by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("https://www.google.com") }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var actualProgress by remember { mutableIntStateOf(0) }

    var showTabsSheet by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }

    var zoomLevel by remember { mutableIntStateOf(100) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }

    val tabs = remember { mutableStateListOf(
        BrowserTab("Google", "https://www.google.com", true, PrimaryBlue)
    )}

    val colors = listOf(
        PrimaryBlue, ComposeColor(0xFFE91E63),
        ComposeColor(0xFF4CAF50), ComposeColor(0xFFFF9800),
        ComposeColor(0xFF9C27B0), ComposeColor(0xFF00BCD4)
    )

    fun simulateLoading() {
        coroutineScope.launch {
            loadingProgress = 0f
            delay(50)
            loadingProgress = 0.3f
            delay(250)
            loadingProgress = 0.7f
            delay(400)
            loadingProgress = 1f
            delay(300)
            loadingProgress = 0f
        }
    }

    fun clearFocusAndOverlays() {
        focusManager.clearFocus()
        showTabsSheet = false
        showMenuSheet = false
    }

    fun loadUrl(url: String) {
        var fullUrl = url.trim()
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            fullUrl = if (fullUrl.contains(".")) "https://$fullUrl" else "https://www.google.com/search?q=$fullUrl"
        }
        searchQuery = fullUrl
        simulateLoading()
    }

    val bgColor = if (isDarkMode) ComposeColor(0xFF121212) else Gray50
    val cardColor = if (isDarkMode) ComposeColor(0xFF1E1E1E) else SurfaceWhite
    val textColor = if (isDarkMode) SurfaceWhite else TextDark
    val hintColor = if (isDarkMode) ComposeColor(0xFF888888) else Gray400
    val iconColor = if (isDarkMode) SurfaceWhite else Gray600

    Box(modifier = Modifier
        .fillMaxSize()
        .background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lite",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlue)
                        .clickable { showTabsSheet = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tabs.size.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = iconColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showMenuSheet = true }
                )
            }

            // --- SEARCH BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isDarkMode) ComposeColor(0xFF2C2C2C) else Gray100)
                        .border(1.dp, if (isSearchFocused) PrimaryBlue else ComposeColor.Transparent, RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = hintColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = textColor
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                focusManager.clearFocus()
                                loadUrl(searchQuery)
                            }),
                            cursorBrush = SolidColor(PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = PrimaryBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { simulateLoading() }
                )
            }

            // --- LOADING PROGRESS ---
            val animatedProgress by animateFloatAsState(targetValue = loadingProgress, animationSpec = tween(durationMillis = 300))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(if (isDarkMode) ComposeColor(0xFF2C2C2C) else Gray100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(PrimaryBlue)
                )
            }

            // --- WEBVIEW ---
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.setSupportZoom(true)
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                actualProgress = 0
                                loadingProgress = 0.1f
                                super.onPageStarted(view, url, favicon)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                loadingProgress = 0f
                                url?.let {
                                    searchQuery = it
                                    val title = view?.title ?: "Untitled"
                                    if (tabs.isNotEmpty()) {
                                        tabs[0] = tabs[0].copy(title = title, url = it)
                                    }
                                }
                                super.onPageFinished(view, url)
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                actualProgress = newProgress
                                loadingProgress = newProgress / 100f
                            }
                        }
                    }
                },
                update = { webView ->
                    if (isDesktopMode) {
                        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                    } else {
                        webView.settings.userAgentString = null
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // --- BOTTOM NAVIGATION ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Triple(Icons.Default.ArrowBack, "Back") { /* handled by back press */ },
                    Triple(Icons.Default.ArrowForward, "Forward") { /* handled by back press */ },
                    Triple(Icons.Default.Home, "Home") { loadUrl("https://www.google.com") },
                    Triple(Icons.Default.Add, "Tabs") { showTabsSheet = true },
                    Triple(Icons.Default.MoreVert, "Menu") { showMenuSheet = true }
                )

                navItems.forEach { (icon, desc, action) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { action() }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = desc,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- OVERLAY ---
        AnimatedVisibility(visible = showTabsSheet || showMenuSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComposeColor.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { clearFocusAndOverlays() }
            )
        }

        // --- TABS BOTTOM SHEET ---
        if (showTabsSheet) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(cardColor)
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .background(Gray200, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tabs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        "+ New",
                        fontSize = 14.sp,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.1f))
                            .clickable {
                                tabs.add(BrowserTab("New Tab", "https://www.google.com", true, colors[tabs.size % colors.size]))
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Tabs List
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    itemsIndexed(tabs) { index, tab ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable {
                                    loadUrl(tab.url)
                                    clearFocusAndOverlays()
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(SurfaceWhite, CircleShape)
                                    .border(if (tab.isActive) 3.dp else 1.dp, if (tab.isActive) PrimaryBlue else Gray200, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tab.faviconColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tab.url.replace("https://", "").replace("http://", "").take(15),
                                fontSize = 8.sp,
                                color = hintColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(PrimaryBlue)
                        .clickable { showTabsSheet = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "DONE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                }
            }
        }

        // --- MENU BOTTOM SHEET ---
        if (showMenuSheet) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(cardColor)
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .background(Gray200, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                // Title
                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                // Menu Items
                MenuItem(
                    icon = Icons.Default.Refresh,
                    title = "Refresh",
                    onClick = {
                        simulateLoading()
                        clearFocusAndOverlays()
                    }
                )

                MenuItem(
                    icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    title = if (isDarkMode) "Light Mode" else "Dark Mode",
                    onClick = {
                        isDarkMode = !isDarkMode
                        clearFocusAndOverlays()
                    }
                )

                MenuItem(
                    icon = Icons.Default.DesktopWindows,
                    title = "Desktop Mode",
                    onClick = {
                        isDesktopMode = !isDesktopMode
                        clearFocusAndOverlays()
                    }
                )

                MenuItem(
                    icon = Icons.Default.ZoomIn,
                    title = "Zoom In",
                    onClick = {
                        zoomLevel += 10
                        clearFocusAndOverlays()
                    }
                )

                MenuItem(
                    icon = Icons.Default.ZoomOut,
                    title = "Zoom Out",
                    onClick = {
                        if (zoomLevel > 50) zoomLevel -= 10
                        clearFocusAndOverlays()
                    }
                )

                MenuItem(
                    icon = Icons.Default.Share,
                    title = "Share Page",
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, searchQuery)
                            type = "text/plain"
                        }
                        clearFocusAndOverlays()
                    }
                )

                MenuItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    onClick = {
                        clearFocusAndOverlays()
                    }
                )

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(TextDark)
                        .clickable { showMenuSheet = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CLOSE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Gray600,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = TextDark
        )
    }
}
