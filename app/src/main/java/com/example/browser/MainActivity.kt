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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
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
val Gray100 = ComposeColor(0xFFEEEEEE)
val Gray200 = ComposeColor(0xFFE0E0E0)
val Gray400 = ComposeColor(0xFF9E9E9E)
val Gray600 = ComposeColor(0xFF757575)
val Gray800 = ComposeColor(0xFF424242)
val TextDark = ComposeColor(0xFF212121)

data class BrowserTab(
    val title: String,
    val url: String,
    val isActive: Boolean = false,
    val faviconColor: ComposeColor = PrimaryBlue
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray50)
            ) {
                BrowserAppContent()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = false
}

@Composable
fun BrowserAppContent() {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("https://www.google.com") }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    var showTabsSheet by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }

    var isDarkMode by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }

    val tabs = remember {
        mutableStateListOf(
            BrowserTab("Google", "https://www.google.com", true, PrimaryBlue)
        )
    }

    val colors = listOf(
        PrimaryBlue, ComposeColor(0xFFE91E63),
        ComposeColor(0xFF4CAF50), ComposeColor(0xFFFF9800),
        ComposeColor(0xFF9C27B0), ComposeColor(0xFF00BCD4)
    )

    fun simulateLoading() {
        coroutineScope.launch {
            loadingProgress = 0f
            for (i in 1..10) {
                loadingProgress = i / 10f
                delay(50)
            }
            delay(300)
            loadingProgress = 0f
        }
    }

    fun loadUrl(url: String) {
        var fullUrl = url.trim()
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            fullUrl = if (fullUrl.contains(".")) "https://$fullUrl" else "https://www.google.com/search?q=$fullUrl"
        }
        searchQuery = fullUrl
        webViewRef?.loadUrl(fullUrl)
        simulateLoading()
    }

    val bgColor = if (isDarkMode) ComposeColor(0xFF121212) else Gray50
    val cardColor = if (isDarkMode) ComposeColor(0xFF1E1E1E) else SurfaceWhite
    val textColor = if (isDarkMode) SurfaceWhite else TextDark
    val hintColor = if (isDarkMode) ComposeColor(0xFF888888) else Gray400
    val iconColor = if (isDarkMode) SurfaceWhite else Gray600
    val searchBg = if (isDarkMode) ComposeColor(0xFF2C2C2C) else Gray100

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lite",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlue)
                        .clickable { showTabsSheet = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tabs.size.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { showMenuSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = iconColor
                    )
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(searchBg)
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
                                fontSize = 14.sp,
                                color = textColor
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                focusManager.clearFocus()
                                loadUrl(searchQuery)
                            }),
                            cursorBrush = SolidColor(PrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { webViewRef?.reload() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = PrimaryBlue
                    )
                }
            }

            // Progress Bar
            val animatedProgress by animateFloatAsState(
                targetValue = loadingProgress,
                animationSpec = tween(durationMillis = 300)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(if (isDarkMode) ComposeColor(0xFF2C2C2C) else Gray200)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(PrimaryBlue)
                )
            }

            // WebView
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
                                loadingProgress = newProgress / 100f
                            }
                        }

                        loadUrl("https://www.google.com")
                    }
                },
                update = { webView ->
                    webViewRef = webView
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

            // Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Triple(Icons.Default.ArrowBack, "Back") { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() },
                    Triple(Icons.Default.ArrowForward, "Forward") { if (webViewRef?.canGoForward() == true) webViewRef?.goForward() },
                    Triple(Icons.Default.Home, "Home") { webViewRef?.loadUrl("https://www.google.com") },
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            fontSize = 10.sp,
                            color = iconColor
                        )
                    }
                }
            }
        }

        // Tab Sheet Overlay
        AnimatedVisibility(visible = showTabsSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComposeColor.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showTabsSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(cardColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryBlue)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tabs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Text(
                            "+ New",
                            fontSize = 12.sp,
                            color = SurfaceWhite,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ComposeColor.White.copy(alpha = 0.2f))
                                .clickable {
                                    tabs.add(
                                        BrowserTab(
                                            "New Tab",
                                            "https://www.google.com",
                                            true,
                                            colors[tabs.size % colors.size]
                                        )
                                    )
                                    webViewRef?.loadUrl("https://www.google.com")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tabs) { tab ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable {
                                        webViewRef?.loadUrl(tab.url)
                                        showTabsSheet = false
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(tab.faviconColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.title.take(1).uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SurfaceWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showTabsSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Menu Sheet Overlay
        AnimatedVisibility(visible = showMenuSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComposeColor.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenuSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(cardColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )

                    val menuItems = listOf(
                        Triple(Icons.Default.Refresh, "Refresh") {
                            webViewRef?.reload()
                            showMenuSheet = false
                        },
                        Triple(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            if (isDarkMode) "Light Mode" else "Dark Mode"
                        ) {
                            isDarkMode = !isDarkMode
                            showMenuSheet = false
                        },
                        Triple(Icons.Default.DesktopWindows, "Desktop Mode") {
                            isDesktopMode = !isDesktopMode
                            showMenuSheet = false
                        },
                        Triple(Icons.Default.ZoomIn, "Zoom In") {
                            webViewRef?.zoomIn()
                            showMenuSheet = false
                        },
                        Triple(Icons.Default.ZoomOut, "Zoom Out") {
                            webViewRef?.zoomOut()
                            showMenuSheet = false
                        },
                        Triple(Icons.Default.Share, "Share") {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, searchQuery)
                                type = "text/plain"
                            }
                            showMenuSheet = false
                        },
                        Triple(Icons.Default.Info, "About") {
                            showMenuSheet = false
                        }
                    )

                    menuItems.forEach { (icon, title, action) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { action() }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                    }

                    Button(
                        onClick = { showMenuSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
