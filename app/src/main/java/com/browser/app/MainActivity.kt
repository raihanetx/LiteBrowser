package com.browser.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.browser.app.ui.theme.VT323
import com.browser.app.viewmodel.BrowserViewModel
import com.browser.app.viewmodel.BrowserViewModelFactory
import kotlinx.coroutines.delay
import java.net.URI

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BrowserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = BrowserViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[BrowserViewModel::class.java]

        setContent {
            val isDarkMode by remember { derivedStateOf { viewModel.isDarkMode } }
            
            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    onBackground = Color.White,
                    onSurface = Color.White
                ) else lightColorScheme(
                    background = Color.White,
                    surface = Color.White,
                    onBackground = Color.Black,
                    onSurface = Color.Black
                )
            ) {
                EvenlyBrowserApp(viewModel)
            }
        }
    }
}

// --- CUSTOM ICONS ---
@Composable
fun CloseIcon(color: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
fun PlusIcon(color: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawLine(color, Offset(size.width/2, 0f), Offset(size.width/2, size.height), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
fun MenuIcon(color: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val h = size.height
        val w = size.width
        drawLine(color, Offset(0f, h*0.25f), Offset(w, h*0.25f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, h*0.5f), Offset(w, h*0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, h*0.75f), Offset(w, h*0.75f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
fun BrowsersIcon(color: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(color, topLeft = Offset(w*0.2f, 0f), size = Size(w*0.8f, h*0.8f), style = Stroke(width = 3f))
        drawRect(color, topLeft = Offset(0f, h*0.2f), size = Size(w*0.8f, h*0.8f), style = Stroke(width = 3f))
    }
}

// --- PIXEL FACE LOGO ---
@Composable
fun PixelFace(scale: Float = 1f, color: Color = MaterialTheme.colorScheme.onBackground) {
    Canvas(modifier = Modifier.size((42 * scale).dp)) {
        val p = (6f * scale).dp.toPx()
        
        // Left Eye
        drawRect(color, Offset(p*1, p*1), Size(p, p))
        drawRect(color, Offset(p*2, p*1), Size(p, p))
        drawRect(color, Offset(p*1, p*2), Size(p, p))
        drawRect(color, Offset(p*2, p*2), Size(p, p))
        
        // Right Eye
        drawRect(color, Offset(p*5, p*1), Size(p, p))
        drawRect(color, Offset(p*6, p*1), Size(p, p))
        drawRect(color, Offset(p*5, p*2), Size(p, p))
        drawRect(color, Offset(p*6, p*2), Size(p, p))

        // Smile
        drawRect(color, Offset(p*1, p*5), Size(p, p))
        drawRect(color, Offset(p*2, p*6), Size(p, p))
        drawRect(color, Offset(p*3, p*6), Size(p, p))
        drawRect(color, Offset(p*4, p*6), Size(p, p))
        drawRect(color, Offset(p*5, p*6), Size(p, p))
        drawRect(color, Offset(p*6, p*5), Size(p, p))
    }
}

// --- DATA MODELS ---
data class Site(val id: Int, val label: String, val url: String, val fav: String)

// --- MAIN APP ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvenlyBrowserApp(viewModel: BrowserViewModel) {
    val fg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background
    
    var tabsOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EvenlyHeaderBar(
                viewModel = viewModel,
                onTabsClick = { tabsOpen = true },
                onMenuClick = { menuOpen = !menuOpen }
            )
        },
        containerColor = bg
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            val currentTab = viewModel.getCurrentTab()
            
            if (currentTab == null || currentTab.url.isEmpty() || currentTab.url == "about:blank") {
                EvenlyHomePage(viewModel)
            } else {
                EvenlyWebView(url = currentTab.url, viewModel = viewModel)
            }
        }

        // Tab Sheet
        if (tabsOpen) {
            ModalBottomSheet(
                onDismissRequest = { tabsOpen = false },
                containerColor = bg,
                scrimColor = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { 
                    Box(modifier = Modifier
                        .padding(top = 12.dp)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(fg.copy(alpha = 0.2f))) 
                }
            ) {
                EvenlyTabSheet(viewModel = viewModel, onClose = { tabsOpen = false })
            }
        }

        // Menu
        if (menuOpen) {
            EvenlyMenu(viewModel = viewModel, onDismiss = { menuOpen = false })
        }
    }
}

@Composable
fun EvenlyHeaderBar(viewModel: BrowserViewModel, onTabsClick: () -> Unit, onMenuClick: () -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val currentTab = viewModel.getCurrentTab()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(bg.copy(alpha = 0.98f))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Address Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .background(
                    if (viewModel.isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF4F4F4), 
                    CircleShape
                )
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (currentTab != null && currentTab.url.isNotEmpty() && currentTab.url != "about:blank") {
                    val domain = try { URI(currentTab.url).host?.replace("www.", "") ?: "" } catch(e: Exception) { "" }
                    if (domain.isNotEmpty()) {
                        AsyncImage(
                            model = "https://www.google.com/s2/favicons?sz=128&domain=$domain",
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clip(RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
                
                BasicTextField(
                    value = viewModel.urlInput.value,
                    onValueChange = { viewModel.urlInput.value = it },
                    textStyle = TextStyle(
                        color = fg, 
                        fontSize = 18.sp, 
                        fontFamily = VT323, 
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { viewModel.navigateToUrl(viewModel.urlInput.value) }),
                    modifier = Modifier.weight(1f),
                    cursorBrush = SolidColor(fg),
                    decorationBox = { innerTextField ->
                        if (viewModel.urlInput.value.isEmpty()) {
                            val hint = if (currentTab == null || currentTab.url == "about:blank") 
                                "Enter URL or Search" 
                            else 
                                currentTab.url.replace("https://", "").replace("http://", "")
                            Text(
                                text = hint, 
                                color = fg.copy(alpha = 0.5f), 
                                fontSize = 18.sp, 
                                fontFamily = VT323, 
                                textAlign = TextAlign.Center, 
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Tab Counter
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onTabsClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(22.dp).border(2.dp, fg.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    viewModel.tabs.size.toString(), 
                    color = fg.copy(alpha = 0.6f), 
                    fontFamily = VT323, 
                    fontSize = 15.sp, 
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        }

        // Menu
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            MenuIcon(color = fg, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun EvenlyHomePage(viewModel: BrowserViewModel) {
    val fg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background
    
    // Default sites
    val sites = remember {
        listOf(
            Site(1, "YouTube", "https://youtube.com", "https://www.google.com/s2/favicons?sz=128&domain=youtube.com"),
            Site(2, "GitHub", "https://github.com", "https://www.google.com/s2/favicons?sz=128&domain=github.com"),
            Site(3, "Next.js", "https://nextjs.org", "https://www.google.com/s2/favicons?sz=128&domain=nextjs.org"),
            Site(4, "MDN", "https://developer.mozilla.org", "https://www.google.com/s2/favicons?sz=128&domain=developer.mozilla.org"),
            Site(5, "Stack", "https://stackoverflow.com", "https://www.google.com/s2/favicons?sz=128&domain=stackoverflow.com"),
            Site(6, "Vercel", "https://vercel.com", "https://www.google.com/s2/favicons?sz=128&domain=vercel.com")
        )
    }
    
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 64.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelFace(scale = 0.9f, color = fg)
            Spacer(Modifier.width(16.dp))
            Text(
                "Evenly", 
                color = fg, 
                fontSize = 46.sp, 
                fontFamily = VT323,
                style = TextStyle(shadow = Shadow(color = fg, offset = Offset(1f, 0f)))
            )
        }

        // Typewriter Slogan
        var displayedText by remember { mutableStateOf("") }
        val fullText = "Explore the internet through evenly"
        LaunchedEffect(Unit) {
            displayedText = ""
            for (i in fullText.indices) {
                displayedText = fullText.substring(0, i + 1)
                delay((30..60).random().toLong())
            }
        }
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 0f,
            animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.offset(y = (-4).dp)) {
            Text(
                displayedText, 
                color = fg.copy(alpha = 0.6f), 
                fontSize = 18.sp, 
                fontFamily = VT323, 
                style = TextStyle(shadow = Shadow(color = fg.copy(alpha = 0.4f), offset = Offset(0.5f, 0f)))
            )
            Box(modifier = Modifier.padding(start = 4.dp).size(6.dp, 16.dp).background(fg.copy(alpha = alpha * 0.6f)))
        }

        Spacer(Modifier.height(48.dp))

        // Favorites Header
        Row(
            modifier = Modifier.width(340.dp).padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FAVORITES", color = fg.copy(alpha = 0.5f), fontSize = 16.sp, fontFamily = VT323, modifier = Modifier.padding(bottom = 2.dp))
            if (sites.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .border(1.dp, fg, RoundedCornerShape(4.dp))
                        .background(if (isEditing) fg else Color.Transparent)
                        .clickable { isEditing = !isEditing }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isEditing) "DONE" else "EDIT", 
                        color = if (isEditing) bg else fg, 
                        fontSize = 14.sp, 
                        fontFamily = VT323
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.width(340.dp).height(500.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp), 
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sites) { site ->
                SiteItem(
                    site = site, 
                    isEditing = isEditing,
                    onClick = {
                        if (!isEditing) {
                            viewModel.urlInput.value = site.url
                            viewModel.navigateToUrl(site.url)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SiteItem(site: Site, isEditing: Boolean, onClick: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(1.5.dp, fg.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = site.fav, 
                    contentDescription = null, 
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .background(bg, CircleShape)
                        .border(1.5.dp, fg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawLine(fg, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth = 3f)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            site.label, 
            color = fg, 
            fontSize = 16.sp, 
            fontFamily = VT323, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.width(70.dp), 
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EvenlyTabSheet(viewModel: BrowserViewModel, onClose: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background

    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrowsersIcon(color = fg, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${viewModel.tabs.size} Tab${if (viewModel.tabs.size != 1) "s" else ""}", 
                    color = fg, 
                    fontSize = 22.sp, 
                    fontFamily = VT323
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Close All", 
                    color = fg.copy(alpha = 0.6f), 
                    fontSize = 18.sp, 
                    fontFamily = VT323, 
                    modifier = Modifier.clickable { 
                        viewModel.tabs.clear()
                        viewModel.addNewTab()
                    }
                )
                Spacer(Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .background(fg, RoundedCornerShape(12.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text("Done", color = bg, fontSize = 18.sp, fontFamily = VT323)
                }
            }
        }
        HorizontalDivider(color = fg.copy(alpha = 0.1f))

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp), 
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.tabs.size) { index ->
                val tab = viewModel.tabs[index]
                EvenlyTabItem(
                    tab = tab,
                    isActive = index == viewModel.currentTabIndex.value,
                    onSelect = { 
                        viewModel.switchToTab(index)
                        onClose()
                    },
                    onClose = { viewModel.closeTab(index) }
                )
            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.addNewTab() }
                ) {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(54.dp)) {
                            drawCircle(
                                color = fg.copy(alpha = 0.4f), 
                                style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                            )
                        }
                        PlusIcon(color = fg.copy(0.5f), modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("New Tab", color = fg.copy(alpha = 0.5f), fontSize = 16.sp, fontFamily = VT323)
                }
            }
        }
    }
}

@Composable
fun EvenlyTabItem(tab: com.browser.app.model.Tab, isActive: Boolean, onSelect: () -> Unit, onClose: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background
    
    val label = if (tab.url.isEmpty() || tab.url == "about:blank") "New Tab" 
    else try { URI(tab.url).host?.replace("www.", "")?.substringBefore(".")?.uppercase() ?: "Tab" } 
    catch(e: Exception) { "Tab" }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelect() }
    ) {
        Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .border(
                        if (isActive) 2.dp else 1.5.dp, 
                        if (isActive) fg else fg.copy(alpha = 0.2f), 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tab.url == "about:blank" || tab.url.isEmpty()) {
                    PixelFace(scale = 0.65f, color = fg)
                } else {
                    val domain = try { URI(tab.url).host?.replace("www.", "") ?: "" } catch(e: Exception) { "" }
                    if (domain.isNotEmpty()) {
                        AsyncImage(
                            model = "https://www.google.com/s2/favicons?sz=128&domain=$domain",
                            contentDescription = null,
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                        )
                    } else {
                        Canvas(modifier = Modifier.size(28.dp)) {
                            drawCircle(fg.copy(0.5f), style = Stroke(2f))
                            drawLine(fg.copy(0.5f), Offset(0f, size.height/2), Offset(size.width, size.height/2))
                            drawLine(fg.copy(0.5f), Offset(size.width/2, 0f), Offset(size.width/2, size.height))
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .background(bg, CircleShape)
                    .border(1.5.dp, fg, CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                CloseIcon(color = fg, modifier = Modifier.size(8.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label, 
            color = fg, 
            fontSize = 16.sp, 
            fontFamily = VT323, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.width(72.dp), 
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EvenlyMenu(viewModel: BrowserViewModel, onDismiss: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxSize()) {
        // Dismiss area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        )
        
        // Menu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
                .padding(top = 60.dp, end = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bg,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, Color.Gray.copy(0.2f)),
                modifier = Modifier.width(240.dp)
            ) {
                Column {
                    EvenlyMenuItem(
                        text = "Dark Mode", 
                        checked = viewModel.isDarkMode, 
                        onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }
                    )
                    HorizontalDivider(color = Color.Gray.copy(0.1f), thickness = 1.dp)
                    
                    EvenlyMenuItem(
                        text = "Desktop Site", 
                        checked = viewModel.getCurrentTab()?.desktopMode == true, 
                        onClick = { viewModel.toggleDesktopMode() }
                    )
                    HorizontalDivider(color = Color.Gray.copy(0.1f), thickness = 1.dp)
                    
                    EvenlyMenuItem(text = "Zoom In", onClick = { viewModel.zoomIn(); onDismiss() })
                    EvenlyMenuItem(text = "Zoom Out", onClick = { viewModel.zoomOut(); onDismiss() })
                    EvenlyMenuItem(text = "Reset Zoom", onClick = { viewModel.resetZoom(); onDismiss() })
                }
            }
        }
    }
}

@Composable
fun EvenlyMenuItem(text: String, checked: Boolean = false, onClick: () -> Unit) {
    val fg = MaterialTheme.colorScheme.onBackground
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = fg, fontSize = 18.sp, fontFamily = VT323)
        if (checked) {
            Box(modifier = Modifier.size(16.dp).background(fg, CircleShape))
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EvenlyWebView(url: String, viewModel: BrowserViewModel) {
    val currentTab = viewModel.getCurrentTab()
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.saveFormData = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Apply zoom after page loads
                        currentTab?.let { tab ->
                            val domain = viewModel.settingsManager.extractDomain(tab.url)
                            val zoom = viewModel.settingsManager.getDomainZoom(domain)
                            if (zoom != 100) {
                                viewModel.applyZoomToWebView(view!!, zoom)
                            }
                        }
                    }
                }
                
                loadUrl(url)
            }
        },
        update = { webView ->
            // Apply desktop mode if changed
            if (currentTab?.desktopMode == true) {
                webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                webView.settings.useWideViewPort = true
            } else {
                webView.settings.userAgentString = null
                webView.settings.useWideViewPort = false
            }
            
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
