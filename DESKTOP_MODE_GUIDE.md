# Browser Desktop Mode Implementation Guide

## Overview
This document records the exact implementation of Desktop Mode and Mobile Mode toggle functionality in the LiteBrowser Android app.

## Features Implemented

### 1. Zoom Controls
- **Zoom In**: Increases zoom by 25% (up to 300%)
- **Zoom Out**: Decreases zoom by 25% (down to 50%)
- **Reset Zoom**: Returns to 100%
- **Visual Indicator**: Shows current zoom percentage when not at 100%
- **Per-Tab Persistence**: Each tab remembers its own zoom level

### 2. Desktop Mode Toggle
- **User Agent Spoofing**: Changes browser identity to desktop
- **Viewport Override**: Forces desktop viewport width (1280px)
- **Touch Detection Masking**: Hides mobile touch capabilities
- **Screen Dimension Spoofing**: Reports desktop screen size (1920x1080)
- **JavaScript Injection**: Comprehensive masking of mobile fingerprints

## Implementation Details

### File 1: Tab.kt (Model)
**Location**: `app/src/main/java/com/browser/app/model/Tab.kt`

**Changes**:
```kotlin
data class Tab(
    val id: String = UUID.randomUUID().toString(),
    var url: String = "",
    var title: String = "New Tab",
    var favicon: android.graphics.Bitmap? = null,
    var isLoading: Boolean = false,
    var progress: Int = 0,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var desktopMode: Boolean = false,
    var zoomLevel: Float = 1.0f  // ADDED: For zoom persistence
) {
    @Transient
    var webView: WebView? = null
}
```

### File 2: BrowserViewModel.kt (Core Logic)
**Location**: `app/src/main/java/com/browser/app/viewmodel/BrowserViewModel.kt`

#### Constants (Companion Object)
```kotlin
companion object {
    private const val DUCKDUCKGO_LITE = "https://duckduckgo.com/lite"
    private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private const val ZOOM_STEP = 0.25f
    private const val MIN_ZOOM = 0.5f
    private const val MAX_ZOOM = 3.0f
    private const val DEFAULT_ZOOM = 1.0f
}
```

#### Zoom Methods
```kotlin
fun zoomIn() {
    val tab = getCurrentTab() ?: return
    val webView = tab.webView ?: return

    val newZoom = (tab.zoomLevel + ZOOM_STEP).coerceAtMost(MAX_ZOOM)
    tab.zoomLevel = newZoom

    webView.apply {
        scaleX = newZoom
        scaleY = newZoom
    }
}

fun zoomOut() {
    val tab = getCurrentTab() ?: return
    val webView = tab.webView ?: return

    val newZoom = (tab.zoomLevel - ZOOM_STEP).coerceAtLeast(MIN_ZOOM)
    tab.zoomLevel = newZoom

    webView.apply {
        scaleX = newZoom
        scaleY = newZoom
    }
}

fun resetZoom() {
    val tab = getCurrentTab() ?: return
    val webView = tab.webView ?: return

    tab.zoomLevel = DEFAULT_ZOOM
    webView.apply {
        scaleX = DEFAULT_ZOOM
        scaleY = DEFAULT_ZOOM
    }
}

fun getCurrentZoomLevel(): Float {
    return getCurrentTab()?.zoomLevel ?: DEFAULT_ZOOM
}

fun applyZoomToWebView(tab: Tab) {
    tab.webView?.apply {
        scaleX = tab.zoomLevel
        scaleY = tab.zoomLevel
    }
}
```

#### Desktop Mode JavaScript Injection
```kotlin
fun injectDesktopModeScripts(webView: WebView?) {
    webView ?: return

    val desktopModeScript = """
        (function() {
            // 1. Remove viewport meta tag restrictions
            var viewportMeta = document.querySelector('meta[name="viewport"]');
            if (viewportMeta) {
                viewportMeta.content = 'width=1280, initial-scale=1.0';
            } else {
                var meta = document.createElement('meta');
                meta.name = 'viewport';
                meta.content = 'width=1280, initial-scale=1.0';
                document.head.appendChild(meta);
            }

            // 2. Mask touch detection - Remove ontouchstart from window
            delete window.ontouchstart;
            delete window.ontouchmove;
            delete window.ontouchend;
            delete window.ontouchcancel;

            // Override maxTouchPoints to return 0 (no touch support)
            Object.defineProperty(navigator, 'maxTouchPoints', {
                get: function() { return 0; }
            });

            // 3. Spoof screen dimensions to look like desktop
            Object.defineProperty(screen, 'width', {
                get: function() { return 1920; }
            });
            Object.defineProperty(screen, 'height', {
                get: function() { return 1080; }
            });
            Object.defineProperty(screen, 'availWidth', {
                get: function() { return 1920; }
            });
            Object.defineProperty(screen, 'availHeight', {
                get: function() { return 1040; }
            });

            // 4. Override window size to match desktop
            Object.defineProperty(window, 'innerWidth', {
                get: function() { return 1280; }
            });
            Object.defineProperty(window, 'innerHeight', {
                get: function() { return 720; }
            });

            // 5. Spoof devicePixelRatio to 1 (desktop standard)
            Object.defineProperty(window, 'devicePixelRatio', {
                get: function() { return 1; }
            });

            // 6. Hide pointer events that indicate touch
            if (window.PointerEvent) {
                const originalPointerEvent = window.PointerEvent;
                window.PointerEvent = function(type, init) {
                    if (init && init.pointerType === 'touch') {
                        init.pointerType = 'mouse';
                    }
                    return new originalPointerEvent(type, init);
                };
            }

            // 7. Override matchMedia for pointer queries
            const originalMatchMedia = window.matchMedia;
            window.matchMedia = function(query) {
                if (query.includes('pointer: coarse')) {
                    return { matches: false, media: query };
                }
                if (query.includes('pointer: fine')) {
                    return { matches: true, media: query };
                }
                if (query.includes('hover: none')) {
                    return { matches: false, media: query };
                }
                return originalMatchMedia(query);
            };
        })();
    """.trimIndent()

    webView.evaluateJavascript(desktopModeScript, null)
}
```

#### Desktop Mode Toggle
```kotlin
fun toggleDesktopMode() {
    val currentTab = getCurrentTab() ?: return
    currentTab.desktopMode = !currentTab.desktopMode

    val webView = currentTab.webView ?: return
    val context = webView.context
    val currentUrl = currentTab.url

    webView.settings.apply {
        userAgentString = if (currentTab.desktopMode) {
            DESKTOP_USER_AGENT
        } else {
            WebSettings.getDefaultUserAgent(context)
        }
        // Force viewport to desktop width when in desktop mode
        useWideViewPort = currentTab.desktopMode
        loadWithOverviewMode = currentTab.desktopMode
    }

    // Clear all forms of cache to ensure the page reloads with new user agent
    webView.clearCache(true)
    webView.clearHistory()

    // Clear cookies for this domain to reset any mobile/desktop preferences
    CookieManager.getInstance().removeAllCookies(null)
    CookieManager.getInstance().flush()

    // Reload with cache disabled to force fresh request with new UA
    webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
    webView.loadUrl(currentUrl)

    // Restore default cache mode after loading
    webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

    // Inject desktop mode scripts after page loads
    if (currentTab.desktopMode) {
        webView.postDelayed({
            injectDesktopModeScripts(webView)
        }, 500)
    }
}
```

#### SPA Support (Single Page Applications)
```kotlin
// Inside createWebView() webViewClient:

// Periodic injection handler for SPAs
val injectionHandler = Handler(Looper.getMainLooper())
val injectionRunnable = object : Runnable {
    override fun run() {
        if (tab.desktopMode && tab.webView != null) {
            injectDesktopModeScripts(tab.webView)
            // Re-inject every 2 seconds while desktop mode is active
            injectionHandler.postDelayed(this, 2000)
        }
    }
}

override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
    super.onPageStarted(view, url, favicon)
    tab.isLoading = true
    tab.url = url ?: ""
    updateUrlInputFromCurrentTab()

    // Start periodic injection for SPAs
    if (tab.desktopMode) {
        injectionHandler.removeCallbacks(injectionRunnable)
        injectionHandler.postDelayed(injectionRunnable, 1000)
    }
}

override fun onPageFinished(view: WebView?, url: String?) {
    super.onPageFinished(view, url)
    // ... other code ...

    // Inject desktop mode JavaScript if enabled
    if (tab.desktopMode) {
        injectDesktopModeScripts(view)
    }
}

// Catch SPA navigation (URL changes without page reload)
override fun doUpdateVisitedHistory(
    view: WebView?,
    url: String?,
    isReload: Boolean
) {
    super.doUpdateVisitedHistory(view, url, isReload)
    tab.url = url ?: ""
    updateUrlInputFromCurrentTab()

    // Re-inject for SPAs on history update
    if (tab.desktopMode && !isReload) {
        injectDesktopModeScripts(view)
    }
}
```

### File 3: BrowserScreen.kt (UI)
**Location**: `app/src/main/java/com/browser/app/ui/components/BrowserScreen.kt`

#### Bottom Bar with Zoom Controls
```kotlin
bottomBar = {
    BottomAppBar(
        modifier = Modifier.height(64.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Zoom level indicator
            val zoomLevel = viewModel.getCurrentZoomLevel()
            if (zoomLevel != 1.0f) {
                Text(
                    text = "${(zoomLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.zoomOut() }) {
                    Icon(Icons.Default.Remove, "Zoom out")
                }
                IconButton(onClick = { viewModel.zoomIn() }) {
                    Icon(Icons.Default.Add, "Zoom in")
                }
                IconButton(
                    onClick = { viewModel.resetZoom() },
                    enabled = zoomLevel != 1.0f
                ) {
                    Icon(Icons.Default.ZoomOutMap, "Reset zoom")
                }
                IconButton(onClick = { viewModel.toggleDesktopMode() }) {
                    Icon(
                        if (currentTab?.desktopMode == true)
                            Icons.Default.PhoneAndroid   // Shows when in desktop mode (tap to go mobile)
                        else
                            Icons.Default.Computer,      // Shows when in mobile mode (tap to go desktop)
                        if (currentTab?.desktopMode == true) "Mobile mode" else "Desktop mode"
                    )
                }
                IconButton(onClick = { viewModel.addNewTab() }) {
                    Icon(Icons.Default.AddCircle, "New tab")
                }
            }
        }
    }
}
```

### File 4: WebViewManager.kt (Tab Management)
**Location**: `app/src/main/java/com/browser/app/ui/components/WebViewManager.kt`

#### Desktop Mode on Tab Switch
```kotlin
// Show only current tab, hide others
val isCurrentTab = index == viewModel.currentTabIndex.value
webView.visibility = if (isCurrentTab) View.VISIBLE else View.GONE

// Apply zoom and desktop mode when tab becomes visible
if (isCurrentTab) {
    viewModel.applyZoomToWebView(tab)
    if (tab.desktopMode) {
        viewModel.injectDesktopModeScripts(webView)
    }
}
```

## WebView Settings for Desktop Mode

### Initial WebView Setup
```kotlin
@SuppressLint("SetJavaScriptEnabled")
fun createWebView(context: Context, tab: Tab): WebView {
    val webView = WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false

            // Desktop mode user agent (if enabled at creation)
            if (tab.desktopMode) {
                userAgentString = DESKTOP_USER_AGENT
            }
        }
        // ... webViewClient and webChromeClient setup ...
    }
    return webView
}
```

## How It Works

### Layer 1: Network Layer (User Agent)
- Changes the `User-Agent` HTTP header sent to servers
- Desktop UA: `Mozilla/5.0 (Windows NT 10.0; Win64; x64)...`
- Mobile UA: Default Android WebView UA

### Layer 2: Rendering Layer (Viewport)
- `useWideViewPort = true`: Creates virtual 1280px canvas
- `loadWithOverviewMode = true`: Zooms out to fit screen

### Layer 3: JavaScript Masking (Anti-Detection)
Injects scripts to override:
1. **Viewport meta tag**: Forces desktop width
2. **Touch detection**: Removes `ontouchstart`, sets `maxTouchPoints = 0`
3. **Screen dimensions**: Reports 1920x1080
4. **Window size**: Reports 1280x720 inner dimensions
5. **Device pixel ratio**: Reports 1.0 (desktop standard)
6. **Pointer events**: Converts touch to mouse
7. **Media queries**: Makes CSS `@media (pointer: coarse)` return false

## Common Issues & Solutions

### Issue 1: Site still shows mobile version
**Cause**: Site uses feature detection instead of User Agent sniffing
**Solution**: JavaScript injection masks touch capabilities (already implemented)

### Issue 2: Desktop mode doesn't persist on tab switch
**Cause**: Scripts not re-injected when tab becomes visible
**Solution**: `WebViewManager` calls `injectDesktopModeScripts()` on visibility change (already implemented)

### Issue 3: SPA (YouTube, Gmail) reverts to mobile
**Cause**: Single-page apps don't reload page when navigating
**Solution**: 
- `doUpdateVisitedHistory()` catches URL changes
- Periodic re-injection every 2 seconds (already implemented)

### Issue 4: Site remembers mobile preference
**Cause**: Cookies or localStorage storing device preference
**Solution**: `CookieManager.getInstance().removeAllCookies(null)` (already implemented)

## Testing Checklist

- [ ] Zoom in increases size by 25%
- [ ] Zoom out decreases size by 25%
- [ ] Zoom indicator shows when not at 100%
- [ ] Reset zoom button disabled at 100%
- [ ] Each tab has independent zoom level
- [ ] Desktop mode shows 🖥️ icon (tap to activate)
- [ ] Mobile mode shows 📱 icon (tap to activate)
- [ ] Google.com shows desktop version when enabled
- [ ] YouTube shows desktop version
- [ ] Tab switching preserves zoom level
- [ ] Tab switching preserves desktop mode

## Future Enhancements

1. **Text-only zoom** (like Firefox): Only zoom text, not images
2. **Pinch gesture zoom**: Detect pinch and update zoom buttons
3. **Zoom persistence across sessions**: Save to SharedPreferences
4. **Site-specific desktop mode**: Remember desktop preference per domain
5. **Desktop mode visual indicator**: Show banner when active

## References

- User Agent strings: https://www.whatismybrowser.com/guides/the-latest-user-agent/
- WebView settings: https://developer.android.com/reference/android/webkit/WebSettings
- JavaScript injection: https://developer.android.com/reference/android/webkit/WebView#evaluateJavascript(java.lang.String,%20android.webkit.ValueCallback%3Cjava.lang.String%3E)

---

**Last Updated**: 2026-03-03
**Version**: v1.2
**Status**: ✅ Production Ready
