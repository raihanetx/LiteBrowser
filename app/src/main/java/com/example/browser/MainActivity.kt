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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val PrimaryBlue = Color(0xFF4285F4)
private val PrimaryRed = Color(0xFFEA4335)
private val PrimaryYellow = Color(0xFFFBBC05)
private val PrimaryGreen = Color(0xFF34A853)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SurfaceDark = Color(0xFF1E1E1E)
private val BackgroundLight = Color(0xFFF8F9FA)
private val BackgroundDark = Color(0xFF121212)
private val TextPrimary = Color(0xFF202124)
private val TextSecondary = Color(0xFF5F6368)
private val DividerLight = Color(0xFFE8EAED)
private val DividerDark = Color(0xFF3C4043)

data class TabData(
    val id: Int,
    val title: String,
    val url: String,
    val favicon: Bitmap? = null,
    var isLoading: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfessionalBrowserApp()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = false
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProfessionalBrowserApp() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var showTabs by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showHomePage by remember { mutableStateOf(true) }

    var currentUrl by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("New Tab") }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    var tabCounter by remember { mutableIntStateOf(1) }
    val tabs = remember {
        mutableStateListOf(
            TabData(id = 1, title = "New Tab", url = "")
        )
    }

    val colors = listOf(PrimaryBlue, PrimaryRed, PrimaryYellow, PrimaryGreen)

    val backgroundColor = if (isDarkMode) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDarkMode) SurfaceDark else SurfaceWhite
    val textPrimary = if (isDarkMode) SurfaceWhite else TextPrimary
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else TextSecondary
    val dividerColor = if (isDarkMode) DividerDark else DividerLight

    fun loadUrl(url: String) {
        showHomePage = false
        var fullUrl = url.trim()
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            fullUrl = if (fullUrl.contains(".") && !fullUrl.contains(" ")) {
                "https://$fullUrl"
            } else {
                "https://www.google.com/search?q=${fullUrl.replace(" ", "+")}"
            }
        }
        searchQuery = fullUrl
        webView?.loadUrl(fullUrl)
    }

    fun openNewTab() {
        tabCounter++
        tabs.add(TabData(id = tabCounter, title = "New Tab", url = ""))
        showHomePage = true
        webView?.loadUrl("about:blank")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu Button
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Search Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isDarkMode) Color(0xFF303134) else Color(0xFFEEEEEE))
                        .clickable { isSearchFocused = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (loadingProgress > 0 && loadingProgress < 1) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        if (!isSearchFocused && searchQuery.isEmpty()) {
                            Text(
                                "Search or enter URL",
                                color = textSecondary,
                                fontSize = 14.sp
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = textPrimary,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(PrimaryBlue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    loadUrl(searchQuery)
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isSearchFocused = it.isFocused }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tab Counter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue)
                        .clickable { showTabs = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tabs.size.toString(),
                        color = SurfaceWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Loading Progress
            val animatedProgress by animateFloatAsState(
                targetValue = loadingProgress,
                animationSpec = tween(300)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(if (isDarkMode) DividerDark else DividerLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(PrimaryBlue)
                )
            }

            // WebView or Home Page
            if (showHomePage) {
                HomePageContent(
                    isDarkMode = isDarkMode,
                    onSearch = { loadUrl(it) }
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
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
                                allowContentAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    loadingProgress = 0.1f
                                    url?.let {
                                        currentUrl = it
                                        searchQuery = it
                                    }
                                    super.onPageStarted(view, url, favicon)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    loadingProgress = 0f
                                    url?.let { currentUrl = it }
                                    pageTitle = view?.title ?: "Untitled"

                                    if (tabs.isNotEmpty()) {
                                        tabs[0] = tabs[0].copy(
                                            title = view?.title ?: "Untitled",
                                            url = url ?: ""
                                        )
                                    }
                                    super.onPageFinished(view, url)
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadingProgress = newProgress / 100f
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    pageTitle = title ?: "Untitled"
                                    super.onReceivedTitle(view, title)
                                }
                            }

                            setOnKeyListener { _, keyCode, event ->
                                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                                    if (canGoBack()) {
                                        goBack()
                                        true
                                    } else {
                                        false
                                    }
                                } else false
                            }
                        }
                    },
                    update = { wv ->
                        webView = wv
                        if (isDesktopMode) {
                            wv.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        } else {
                            wv.settings.userAgentString = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            // Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavButton(
                    icon = Icons.Default.ArrowBack,
                    label = "Back",
                    enabled = !showHomePage,
                    isDarkMode = isDarkMode,
                    onClick = { webView?.goBack() }
                )
                NavButton(
                    icon = Icons.Default.ArrowForward,
                    label = "Forward",
                    enabled = !showHomePage,
                    isDarkMode = isDarkMode,
                    onClick = { webView?.goForward() }
                )
                NavButton(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isDarkMode = isDarkMode,
                    onClick = {
                        showHomePage = true
                        webView?.loadUrl("about:blank")
                    }
                )
                NavButton(
                    icon = Icons.Default.Add,
                    label = "Tabs",
                    isDarkMode = isDarkMode,
                    onClick = { showTabs = true }
                )
                NavButton(
                    icon = Icons.Default.MoreVert,
                    label = "Menu",
                    isDarkMode = isDarkMode,
                    onClick = { showMenu = true }
                )
            }
        }

        // Tab Sheet
        AnimatedVisibility(
            visible = showTabs,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showTabs = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(surfaceColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${tabs.size} Tabs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            "+ New Tab",
                            fontSize = 14.sp,
                            color = PrimaryBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f))
                                .clickable { openNewTab() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Divider(color = dividerColor, thickness = 1.dp)

                    LazyRow(
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tabs) { tab ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(100.dp)
                                    .clickable {
                                        webView?.loadUrl(tab.url.ifEmpty { "https://www.google.com" })
                                        showTabs = false
                                        showHomePage = false
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors[tab.id % colors.size]),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tab.title == "New Tab") {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = SurfaceWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = tab.title.take(1).uppercase(),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SurfaceWhite
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showTabs = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
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
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(surfaceColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary,
                        modifier = Modifier.padding(20.dp)
                    )

                    Divider(color = dividerColor)

                    MenuItem(
                        icon = Icons.Default.Home,
                        title = "Home",
                        subtitle = "Go to homepage",
                        isDarkMode = isDarkMode,
                        onClick = {
                            showHomePage = true
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.Refresh,
                        title = "Refresh",
                        subtitle = "Reload current page",
                        isDarkMode = isDarkMode,
                        onClick = {
                            webView?.reload()
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.ArrowBack,
                        title = "Back",
                        subtitle = "Go back",
                        isDarkMode = isDarkMode,
                        onClick = {
                            webView?.goBack()
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.ArrowForward,
                        title = "Forward",
                        subtitle = "Go forward",
                        isDarkMode = isDarkMode,
                        onClick = {
                            webView?.goForward()
                            showMenu = false
                        }
                    )

                    Divider(color = dividerColor)

                    MenuItem(
                        icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        title = if (isDarkMode) "Light Mode" else "Dark Mode",
                        subtitle = "Toggle dark/light theme",
                        isDarkMode = isDarkMode,
                        onClick = {
                            isDarkMode = !isDarkMode
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.DesktopWindows,
                        title = "Desktop Mode",
                        subtitle = if (isDesktopMode) "Currently on" else "Currently off",
                        isDarkMode = isDarkMode,
                        onClick = {
                            isDesktopMode = !isDesktopMode
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.ZoomIn,
                        title = "Zoom In",
                        subtitle = "Increase zoom level",
                        isDarkMode = isDarkMode,
                        onClick = {
                            webView?.zoomIn()
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.ZoomOut,
                        title = "Zoom Out",
                        subtitle = "Decrease zoom level",
                        isDarkMode = isDarkMode,
                        onClick = {
                            webView?.zoomOut()
                            showMenu = false
                        }
                    )

                    Divider(color = dividerColor)

                    MenuItem(
                        icon = Icons.Default.Share,
                        title = "Share",
                        subtitle = "Share current page",
                        isDarkMode = isDarkMode,
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, currentUrl)
                                type = "text/plain"
                            }
                            showMenu = false
                        }
                    )

                    MenuItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "App version 1.0",
                        isDarkMode = isDarkMode,
                        onClick = { showMenu = false }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NavButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else TextSecondary
    val iconColor = if (enabled) (if (isDarkMode) Color.White else TextPrimary) else textSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = iconColor
        )
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val textPrimary = if (isDarkMode) Color.White else TextPrimary
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = textSecondary
            )
        }
    }
}

@Composable
fun HomePageContent(
    isDarkMode: Boolean,
    onSearch: (String) -> Unit
) {
    val backgroundColor = if (isDarkMode) BackgroundDark else BackgroundLight
    val textPrimary = if (isDarkMode) Color.White else TextPrimary
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else TextSecondary

    var searchText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "L",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Text(
                text = "i",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )
            Text(
                text = "t",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow
            )
            Text(
                text = "e",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
            Text(
                text = "Browser",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Search Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(if (isDarkMode) Color(0xFF303134) else Color.White)
                .clickable { }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    textStyle = TextStyle(
                        color = textPrimary,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(PrimaryBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearch(searchText)
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Links
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            QuickLink(
                icon = Icons.Default.Search,
                label = "Search",
                isDarkMode = isDarkMode,
                onClick = { onSearch(searchText) }
            )
            QuickLink(
                icon = Icons.Default.Language,
                label = "Visit",
                isDarkMode = isDarkMode,
                onClick = { onSearch(searchText) }
            )
        }
    }
}

@Composable
fun QuickLink(
    icon: ImageVector,
    label: String,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val surfaceColor = if (isDarkMode) SurfaceDark else SurfaceWhite

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryBlue,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDarkMode) Color.White else TextPrimary
        )
    }
}
