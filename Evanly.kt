import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

// --- DATA MODELS ---
data class BrowserTab(
    val id: Long,
    val title: String,
    val url: String
)

data class Shortcut(
    val name: String,
    val url: String,
    val color: Color,
    val icon: ImageVector
)

// --- STYLING CONSTANTS ---
val Zinc50 = Color(0xFFFAFAFA)
val Zinc100 = Color(0xFFF4F4F5)
val Zinc200 = Color(0xFFE4E4E7)
val Zinc400 = Color(0xFFA1A1AA)
val Zinc900 = Color(0xFF18181B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvanlyBrowserApp() {
    var isHomePage by remember { mutableStateOf(true) }
    var urlInput by remember { mutableStateOf("") }
    var activeTabId by remember { mutableStateOf(0L) }
    var tabs by remember { mutableStateOf(listOf(BrowserTab(0L, "EVANLY HOME", ""))) }
    var zoomLevel by remember { mutableStateOf(100) }
    var isDesktopMode by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val shortcuts = listOf(
        Shortcut("YOUTUBE", "https://m.youtube.com", Color(0xFFFF0000), Icons.Rounded.PlayCircle),
        Shortcut("GOOGLE", "https://www.google.com", Color(0xFF4285F4), Icons.Rounded.Search),
        Shortcut("GITHUB", "https://github.com", Color(0xFF181717), Icons.Rounded.Code),
        Shortcut("REDDIT", "https://reddit.com", Color(0xFFFF4500), Icons.Rounded.Forum)
    )

    Scaffold(
        topBar = {
            EvanlyHeader(
                url = urlInput,
                tabCount = tabs.size,
                onUrlChange = { urlInput = it },
                onHomeClick = { 
                    isHomePage = true
                    urlInput = ""
                },
                onMoreClick = { showSheet = true },
                onSearch = { 
                    isHomePage = false 
                    // Logic to handle search/URL loading would go here
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            if (isHomePage) {
                EvanlyHomePage(shortcuts) { selectedUrl ->
                    urlInput = selectedUrl
                    isHomePage = false
                }
            } else {
                BrowserView(
                    url = urlInput,
                    zoom = zoomLevel,
                    isDesktop = isDesktopMode
                )
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = { Box(Modifier.padding(vertical = 22.dp).width(40.dp).height(4.dp).background(Zinc200, RoundedCornerShape(50))) }
                ) {
                    UniversalSliderContent(
                        tabs = tabs,
                        activeTabId = activeTabId,
                        zoom = zoomLevel,
                        isDesktop = isDesktopMode,
                        onZoomChange = { zoomLevel = it },
                        onDesktopToggle = { 
                            isDesktopMode = it
                            if (it) zoomLevel = 50 else zoomLevel = 100
                        },
                        onTabSelect = { 
                            activeTabId = it
                            showSheet = false
                            isHomePage = false
                        },
                        onCloseTab = { id ->
                            tabs = tabs.filter { it.id != id }
                            if (tabs.isEmpty()) {
                                tabs = listOf(BrowserTab(0L, "EVANLY HOME", ""))
                                isHomePage = true
                            }
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
            Icon(Icons.Rounded.Home, contentDescription = null, tint = Zinc900, modifier = Modifier.size(24.dp))
        }

        // --- ROUND PILL SEARCH BAR ---
        Row(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .background(Zinc50, CircleShape)
                .border(1.dp, Zinc200, CircleShape)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(18.dp))
            BasicTextField(
                value = url,
                onValueChange = onUrlChange,
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
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                decorationBox = { innerTextField ->
                    if (url.isEmpty()) Text("SEARCH OR ENTER URL", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    innerTextField()
                }
            )
        }

        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = Zinc900, modifier = Modifier.size(26.dp))
            }
            // Tab counter integrated into the icon area
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
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
fun EvanlyHomePage(shortcuts: List<Shortcut>, onShortcutClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- BRANDING ---
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AsyncImage(
                model = "https://i.postimg.cc/BZK3x2Rc/1000020799.png",
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = "EVANLY",
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

        // --- SHORTCUTS GRID ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.width(320.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(shortcuts) { site ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onShortcutClick(site.url) }) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(site.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(site.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Text(site.name, fontSize = 8.sp, fontWeight = FontWeight.Black, color = Zinc400, modifier = Modifier.padding(top = 8.dp))
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Zinc50).border(2.dp, Zinc100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Zinc200)
                    }
                    Text("ADD", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Zinc200, modifier = Modifier.padding(top = 8.dp))
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
    onCloseTab: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("SESSION MANAGER", fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Zinc400)
            TextButton(onClick = { /* Add Tab */ }) {
                Text("+ NEW TAB", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Zinc900)
            }
        }

        LazyColumn(modifier = Modifier.heightIn(max = 240.dp).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs) { tab ->
                val isActive = tab.id == activeTabId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isActive) Zinc900 else Zinc50)
                        .border(1.dp, if (isActive) Zinc900 else Zinc100, RoundedCornerShape(16.dp))
                        .clickable { onTabSelect(tab.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tab.title, color = if (isActive) Color.White else Zinc900, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(if(tab.url.isEmpty()) "DEFAULT_VIEW" else tab.url.uppercase(), color = if (isActive) Color.White.copy(0.5f) else Zinc400, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    }
                    IconButton(onClick = { onCloseTab(tab.id) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = if (isActive) Color.White.copy(0.5f) else Zinc200)
                    }
                }
            }
        }

        Divider(color = Zinc50, modifier = Modifier.padding(vertical = 12.dp))

        // --- ZOOM CONTROLS ---
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PAGE ZOOM", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Zinc900)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                IconButton(onClick = { onZoomChange((zoom - 10).coerceAtLeast(50)) }) {
                    Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null, tint = Zinc200)
                }
                Text("$zoom%", fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                IconButton(onClick = { onZoomChange((zoom + 10).coerceAtMost(200)) }) {
                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, tint = Zinc200)
                }
            }
        }

        // --- DESKTOP TOGGLE ---
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Rounded.Monitor, contentDescription = null, tint = Zinc900)
                Text("DESKTOP ENVIRONMENT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Zinc900)
            }
            Switch(
                checked = isDesktop,
                onCheckedChange = onDesktopToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Zinc900)
            )
        }
    }
}

@Composable
fun BrowserView(url: String, zoom: Int, isDesktop: Boolean) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            WebView(it).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                }
            }
        },
        update = { webView ->
            // Desktop Mode User Agent Logic
            if (isDesktop) {
                webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
            } else {
                webView.settings.userAgentString = null // Default mobile
            }
            
            // Zoom Logic
            webView.setInitialScale(zoom)
            
            // Loading Logic
            if (webView.url != url) {
                val finalUrl = if (url.contains(".")) {
                    if (url.startsWith("http")) url else "https://$url"
                } else {
                    "https://lite.duckduckgo.com/lite/?q=$url"
                }
                webView.loadUrl(finalUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
