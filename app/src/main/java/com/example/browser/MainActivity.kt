package com.example.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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

private val Blue = Color(0xFF4285F4)
private val Red = Color(0xFFEA4335)
private val Yellow = Color(0xFFFBBC05)
private val Green = Color(0xFF34A853)
private val White = Color(0xFFFFFFFF)
private val DarkSurface = Color(0xFF1E1E1E)
private val LightBg = Color(0xFFF8F9FA)
private val DarkBg = Color(0xFF121212)
private val DarkText = Color(0xFF202124)
private val GrayText = Color(0xFF5F6368)
private val LightDivider = Color(0xFFE8EAED)
private val DarkDivider = Color(0xFF3C4043)

data class BrowserTab(
    val id: Int,
    var title: String = "New Tab",
    var url: String = "",
    var desktopMode: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BrowserApp() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = false
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserApp() {
    val focus = LocalFocusManager.current
    var search by remember { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var showTabs by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showHome by remember { mutableStateOf(true) }
    var loading by remember { mutableFloatStateOf(0f) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    var tabCount by remember { mutableIntStateOf(1) }
    var activeTabId by remember { mutableIntStateOf(1) }
    
    val tabs = remember { mutableStateListOf(BrowserTab(id = 1, title = "New Tab", url = "")) }
    val activeTab = tabs.find { it.id == activeTabId }
    val tabColors = listOf(Blue, Red, Yellow, Green)
    
    val bg = if (darkMode) DarkBg else LightBg
    val surface = if (darkMode) DarkSurface else White
    val textMain = if (darkMode) White else DarkText
    val textGray = if (darkMode) Color(0xFF9AA0A6) else GrayText
    val divider = if (darkMode) DarkDivider else LightDivider

    fun loadUrl(url: String) {
        showHome = false
        var full = url.trim()
        if (!full.startsWith("http://") && !full.startsWith("https://")) {
            full = if (full.contains(".") && !full.contains(" ")) "https://$full"
            else "https://www.google.com/search?q=${full.replace(" ", "+")}"
        }
        search = full
        webView?.loadUrl(full)
        activeTab?.url = full
    }

    fun newTab() {
        tabCount++
        tabs.add(BrowserTab(id = tabCount, title = "New Tab", url = ""))
        activeTabId = tabCount
        showHome = true
        search = ""
        webView?.loadUrl("about:blank")
        showTabs = false
    }

    fun switchTab(id: Int) {
        val tab = tabs.find { it.id == id } ?: return
        activeTabId = id
        showHome = tab.url.isEmpty()
        search = tab.url
        if (tab.url.isNotEmpty()) webView?.loadUrl(tab.url) else webView?.loadUrl("about:blank")
        showTabs = false
    }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(surface).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = textGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(22.dp))
                        .background(if (darkMode) Color(0xFF303134) else Color(0xFFEEEEEE))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = textGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        if (!focused && search.isEmpty()) {
                            Text("Search or enter URL", color = textGray, fontSize = 14.sp)
                        }
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            textStyle = TextStyle(textMain, fontSize = 14.sp),
                            singleLine = true,
                            cursorBrush = SolidColor(Blue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { focus.clearFocus(); loadUrl(search) }),
                            modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused }
                        )
                        if (search.isNotEmpty()) {
                            IconButton(onClick = { search = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = textGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Blue).clickable { showTabs = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(tabs.size.toString(), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Progress
            val anim by animateFloatAsState(targetValue = loading, animationSpec = tween(300))
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(divider)) {
                Box(modifier = Modifier.fillMaxWidth(anim).fillMaxHeight().background(Blue))
            }

            // WebView or Home
            if (showHome) {
                HomePage(darkMode = darkMode, onSearch = { loadUrl(it) })
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                setSupportZoom(true)
                                cacheMode = WebSettings.LOAD_DEFAULT
                                allowFileAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(v: WebView?, u: String?, f: Bitmap?) {
                                    loading = 0.1f
                                    u?.let { search = it; activeTab?.url = it }
                                }
                                override fun onPageFinished(v: WebView?, u: String?) {
                                    loading = 0f
                                    u?.let { activeTab?.url = it }
                                    val t = v?.title ?: "Untitled"
                                    activeTab?.title = t
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(v: WebView?, p: Int) { loading = p / 100f }
                            }
                            setOnKeyListener { _, k, e -> 
                                if (k == KeyEvent.KEYCODE_BACK && e.action == KeyEvent.ACTION_UP && canGoBack()) { goBack(); true } else false 
                            }
                        }
                    },
                    update = { wv -> webView = wv },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }

            // Bottom Nav
            Row(
                modifier = Modifier.fillMaxWidth().background(surface).padding(4.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavBtn(Icons.Default.ArrowBack, "Back", !showHome, darkMode) { webView?.goBack() }
                NavBtn(Icons.Default.ArrowForward, "Forward", !showHome, darkMode) { webView?.goForward() }
                NavBtn(Icons.Default.Home, "Home", true, darkMode) { showHome = true; webView?.loadUrl("about:blank") }
                NavBtn(Icons.Default.Add, "Tabs", true, darkMode) { showTabs = true }
                NavBtn(Icons.Default.MoreVert, "Menu", true, darkMode) { showMenu = true }
            }
        }

        // Tab Sheet
        AnimatedVisibility(
            visible = showTabs,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showTabs = false }
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(surface)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${tabs.size} Tabs", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("+ New", color = Blue, fontSize = 14.sp, 
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Blue.copy(alpha = 0.1f))
                                .clickable { newTab() }.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                    Divider(color = divider)
                    LazyRow(contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(tabs) { tab ->
                            Column(
                                modifier = Modifier.width(100.dp).clickable { switchTab(tab.id) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp))
                                        .background(if (tab.id == activeTabId) Blue else tabColors[tab.id % 4]),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tab.title == "New Tab" || tab.url.isEmpty()) 
                                        Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(28.dp))
                                    else Text(tab.title.take(1).uppercase(), color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(if (tab.title == "New Tab") "New Tab" else tab.title, color = textMain, fontSize = 12.sp, 
                                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Button(
                        onClick = { showTabs = false },
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Menu Sheet
        AnimatedVisibility(
            visible = showMenu,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showMenu = false }
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(surface)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                ) {
                    Text("Settings", color = textMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(20.dp))
                    Divider(color = divider)
                    
                    Text("Navigation", color = textGray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                    MenuBtn(Icons.Default.Home, "Home", "Go to homepage", darkMode) { showHome = true; showMenu = false }
                    MenuBtn(Icons.Default.Refresh, "Refresh", "Reload page", darkMode) { webView?.reload(); showMenu = false }
                    MenuBtn(Icons.Default.ArrowBack, "Back", "Go back", darkMode) { webView?.goBack(); showMenu = false }
                    MenuBtn(Icons.Default.ArrowForward, "Forward", "Go forward", darkMode) { webView?.goForward(); showMenu = false }
                    
                    Divider(color = divider)
                    Text("Display", color = textGray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                    
                    val isDesktop = activeTab?.desktopMode == true
                    MenuBtn(
                        if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode, 
                        if (darkMode) "Light Mode" else "Dark Mode", 
                        "Toggle theme", darkMode
                    ) { darkMode = !darkMode; showMenu = false }
                    
                    MenuBtn(
                        Icons.Default.DesktopWindows, "Desktop Mode", 
                        if (isDesktop) "Currently ON" else "Currently OFF", 
                        darkMode, isDesktop
                    ) { 
                        activeTab?.desktopMode = !activeTab?.desktopMode!!
                        webView?.settings?.userAgentString = if (activeTab?.desktopMode == true) "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" else null
                        webView?.reload()
                        showMenu = false 
                    }
                    
                    MenuBtn(Icons.Default.ZoomIn, "Zoom In", "Increase zoom", darkMode) { webView?.zoomIn(); showMenu = false }
                    MenuBtn(Icons.Default.ZoomOut, "Zoom Out", "Decrease zoom", darkMode) { webView?.zoomOut(); showMenu = false }
                    
                    Divider(color = divider)
                    MenuBtn(Icons.Default.Share, "Share", "Share page", darkMode) { showMenu = false }
                    MenuBtn(Icons.Default.Info, "About", "Version 1.0", darkMode) { showMenu = false }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NavBtn(icon: ImageVector, label: String, enabled: Boolean, dark: Boolean, onClick: () -> Unit) {
    val color = if (enabled) (if (dark) White else DarkText) else (if (dark) Color(0xFF9AA0A6) else GrayText)
    Column(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(text = label, color = color, fontSize = 10.sp)
    }
}

@Composable
fun MenuBtn(icon: ImageVector, title: String, subtitle: String, dark: Boolean, active: Boolean = false, onClick: () -> Unit) {
    val txt = if (dark) White else DarkText
    val gray = if (dark) Color(0xFF9AA0A6) else GrayText
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(if (active) Blue else Blue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) { Icon(imageVector = icon, contentDescription = null, tint = if (active) White else Blue, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = txt, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = gray, fontSize = 12.sp)
        }
        if (active) Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Blue, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun HomePage(darkMode: Boolean, onSearch: (String) -> Unit) {
    val bg = if (darkMode) DarkBg else LightBg
    val txt = if (darkMode) White else DarkText
    val gray = if (darkMode) Color(0xFF9AA0A6) else GrayText
    var text by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize().background(bg).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(text = "L", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Blue)
            Text(text = "i", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Red)
            Text(text = "t", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Yellow)
            Text(text = "e", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Green)
            Text(text = "Browser", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = txt)
        }
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp))
                .background(if (darkMode) Color(0xFF303134) else White).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(txt, fontSize = 16.sp),
                    singleLine = true,
                    cursorBrush = SolidColor(Blue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); onSearch(text) }),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (darkMode) DarkSurface else White)
                    .clickable { onSearch(text) }.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Blue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Search", color = if (darkMode) White else DarkText, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (darkMode) DarkSurface else White)
                    .clickable { onSearch(text) }.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = Blue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Visit", color = if (darkMode) White else DarkText, fontSize = 12.sp)
            }
        }
    }
}
