package com.browser.app.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.browser.app.model.Tab

class BrowserViewModel : ViewModel() {

    val tabs = mutableStateListOf<Tab>()
    val currentTabIndex = mutableStateOf(-1)
    val showTabsOverview = mutableStateOf(false)
    val urlInput = mutableStateOf("")

    companion object {
        private const val DUCKDUCKGO_LITE = "https://duckduckgo.com/lite"
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        private const val ZOOM_STEP = 0.25f
        private const val MIN_ZOOM = 0.5f
        private const val MAX_ZOOM = 3.0f
        private const val DEFAULT_ZOOM = 1.0f
    }

    init {
        addNewTab()
    }
    
    fun addNewTab(url: String = ""): Tab {
        val tab = Tab(url = url, title = if (url.isEmpty()) "New Tab" else url)
        tabs.add(tab)
        currentTabIndex.value = tabs.size - 1
        return tab
    }
    
    fun closeTab(index: Int) {
        if (index < 0 || index >= tabs.size) return
        
        tabs[index].webView?.destroy()
        tabs.removeAt(index)
        
        when {
            tabs.isEmpty() -> addNewTab()
            currentTabIndex.value >= index -> currentTabIndex.value = (currentTabIndex.value - 1).coerceAtLeast(0)
        }
    }
    
    fun switchToTab(index: Int) {
        if (index in tabs.indices) {
            currentTabIndex.value = index
            showTabsOverview.value = false
            updateUrlInputFromCurrentTab()
        }
    }
    
    fun updateUrlInputFromCurrentTab() {
        val currentTab = getCurrentTab()
        urlInput.value = currentTab?.url ?: ""
    }
    
    fun getCurrentTab(): Tab? {
        return if (currentTabIndex.value in tabs.indices) {
            tabs[currentTabIndex.value]
        } else null
    }
    
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

                // Desktop mode user agent
                if (tab.desktopMode) {
                    userAgentString = DESKTOP_USER_AGENT
                }
            }
            
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

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
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
                    tab.isLoading = false
                    tab.url = url ?: ""
                    tab.title = view?.title ?: tab.url
                    tab.canGoBack = view?.canGoBack() ?: false
                    tab.canGoForward = view?.canGoForward() ?: false
                    updateUrlInputFromCurrentTab()

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
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    tab.progress = newProgress
                }
                
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    tab.favicon = icon
                }
                
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    tab.title = title ?: tab.url
                }
            }
        }
        
        tab.webView = webView

        if (tab.url.isNotEmpty()) {
            webView.loadUrl(tab.url)
        } else {
            webView.loadUrl(DUCKDUCKGO_LITE)
        }

        return webView
    }
    
    fun navigateToUrl(url: String) {
        val currentTab = getCurrentTab() ?: return
        var finalUrl = url
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            finalUrl = "https://$url"
        }
        
        currentTab.webView?.loadUrl(finalUrl)
    }
    
    fun goBack() {
        getCurrentTab()?.webView?.goBack()
    }
    
    fun goForward() {
        getCurrentTab()?.webView?.goForward()
    }
    
    fun reload() {
        getCurrentTab()?.webView?.reload()
    }

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
    
    override fun onCleared() {
        super.onCleared()
        tabs.forEach { it.webView?.destroy() }
    }
}
