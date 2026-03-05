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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.browser.app.data.SettingsManager
import com.browser.app.model.Tab

class BrowserViewModel(private val context: Context) : ViewModel() {

    val tabs = mutableStateListOf<Tab>()
    val currentTabIndex = mutableStateOf(-1)
    val showTabsOverview = mutableStateOf(false)
    val urlInput = mutableStateOf("")
    val showSettings = mutableStateOf(false)
    val textZoomLevel = mutableStateOf(100)
    var isDarkMode by mutableStateOf(false)

    val settingsManager = SettingsManager(context)

    companion object {
        private const val DUCKDUCKGO_LITE = "https://duckduckgo.com/lite"
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        private const val ZOOM_STEP = 20 // 20% increments for better feel
        private const val MIN_ZOOM = 50  // 50% minimum
        private const val MAX_ZOOM = 300 // 300% maximum
    }

    init {
        // Load saved text zoom
        textZoomLevel.value = settingsManager.getTextZoom()
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
        if (index in tabs.indices && index != currentTabIndex.value) {
            // Save current tab state before switching
            val currentTab = getCurrentTab()
            currentTab?.webView?.let { webView ->
                // WebView state is automatically saved in WebViewManager
            }
            
            // Switch to new tab
            currentTabIndex.value = index
            showTabsOverview.value = false
            updateUrlInputFromCurrentTab()
            
            // Apply settings to new tab after a short delay to ensure WebView is ready
            val newTab = tabs[index]
            newTab.webView?.post {
                // Apply domain-specific zoom
                applyDomainZoomToCurrentTab()
                // Apply text zoom
                newTab.webView?.settings?.textZoom = settingsManager.getTextZoom()
                // Apply desktop mode if enabled
                if (newTab.desktopMode) {
                    injectDesktopModeScripts(newTab.webView)
                }
            }
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
                builtInZoomControls = true        // Enable pinch zoom
                displayZoomControls = false       // Hide default zoom buttons
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)              // Enable zoom support
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                mediaPlaybackRequiresUserGesture = false

                // Form data saving for signup/login forms
                saveFormData = true
                savePassword = false              // Disabled for security

                // Apply saved text zoom
                textZoom = settingsManager.getTextZoom()

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

                    // Apply domain-specific zoom
                    url?.let { applyZoomForDomain(view, it) }

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

                    if (tab.desktopMode) {
                        injectDesktopModeScripts(view)
                    }
                }

                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    tab.url = url ?: ""
                    updateUrlInputFromCurrentTab()

                    if (tab.desktopMode && !isReload) {
                        injectDesktopModeScripts(view)
                    }
                }

                override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                    super.onScaleChanged(view, oldScale, newScale)
                    // Save zoom when user pinches to zoom
                    val currentUrl = tab.url
                    if (currentUrl.isNotEmpty()) {
                        val zoomPercent = (newScale * 100).toInt()
                        settingsManager.setDomainZoom(
                            settingsManager.extractDomain(currentUrl),
                            zoomPercent
                        )
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

    // ZOOM METHODS - Using JavaScript for reliable zoom
    fun zoomIn() {
        val webView = getCurrentTab()?.webView ?: return
        val currentUrl = getCurrentTab()?.url ?: return
        val domain = settingsManager.extractDomain(currentUrl)

        val currentZoom = settingsManager.getDomainZoom(domain)
        val newZoom = (currentZoom + ZOOM_STEP).coerceAtMost(MAX_ZOOM)

        settingsManager.setDomainZoom(domain, newZoom)
        applyZoomToWebView(webView, newZoom)
    }

    fun zoomOut() {
        val webView = getCurrentTab()?.webView ?: return
        val currentUrl = getCurrentTab()?.url ?: return
        val domain = settingsManager.extractDomain(currentUrl)

        val currentZoom = settingsManager.getDomainZoom(domain)
        val newZoom = (currentZoom - ZOOM_STEP).coerceAtLeast(MIN_ZOOM)

        settingsManager.setDomainZoom(domain, newZoom)
        applyZoomToWebView(webView, newZoom)
    }

    fun resetZoom() {
        val webView = getCurrentTab()?.webView ?: return
        val currentUrl = getCurrentTab()?.url ?: return
        val domain = settingsManager.extractDomain(currentUrl)

        settingsManager.setDomainZoom(domain, 100)
        applyZoomToWebView(webView, 100)
    }

    fun applyZoomToWebView(webView: WebView, zoomPercent: Int) {
        val zoomLevel = zoomPercent / 100.0
        val script = """
            (function() {
                // Store current scroll position relative to zoom
                var scrollX = window.scrollX;
                var scrollY = window.scrollY;
                var docWidth = document.documentElement.scrollWidth;
                var docHeight = document.documentElement.scrollHeight;
                
                // Calculate center point before zoom
                var centerX = scrollX + window.innerWidth / 2;
                var centerY = scrollY + window.innerHeight / 2;
                
                // Apply zoom with smooth transition
                var style = document.getElementById('zoom-style');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'zoom-style';
                    document.head.appendChild(style);
                }
                
                // CSS for zoom with smooth transition
                style.textContent = `
                    html {
                        transition: transform 0.2s ease-out !important;
                        transform-origin: center top !important;
                        transform: scale(${zoomLevel}) !important;
                    }
                    body {
                        width: ${100.0 / zoomLevel}% !important;
                        min-width: ${100.0 / zoomLevel}% !important;
                    }
                `;
                
                // Restore scroll position centered on same point
                setTimeout(function() {
                    var newDocWidth = document.documentElement.scrollWidth;
                    var newDocHeight = document.documentElement.scrollHeight;
                    var scaleRatio = newDocWidth / docWidth;
                    
                    window.scrollTo(
                        centerX * scaleRatio - window.innerWidth / 2,
                        centerY * scaleRatio - window.innerHeight / 2
                    );
                }, 210);
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    fun getCurrentZoomLevel(): Int {
        val currentUrl = getCurrentTab()?.url ?: return 100
        val domain = settingsManager.extractDomain(currentUrl)
        return settingsManager.getDomainZoom(domain)
    }

    // Apply zoom for specific domain
    private fun applyZoomForDomain(webView: WebView?, url: String) {
        webView ?: return
        val domain = settingsManager.extractDomain(url)
        val savedZoom = settingsManager.getDomainZoom(domain)
        // Apply zoom via JavaScript
        applyZoomToWebView(webView, savedZoom)
    }

    fun applyDomainZoomToCurrentTab() {
        val tab = getCurrentTab() ?: return
        val webView = tab.webView ?: return
        applyZoomForDomain(webView, tab.url)
    }

    // TEXT ZOOM METHODS - For Accessibility (using JavaScript for reliability)
    fun setTextZoom(percentage: Int) {
        val coercedZoom = percentage.coerceIn(80, 200)
        textZoomLevel.value = coercedZoom
        settingsManager.setTextZoom(coercedZoom)

        // Apply to all tabs using JavaScript instead of native textZoom
        tabs.forEach { tab ->
            tab.webView?.let { webView ->
                applyTextZoomToWebView(webView, coercedZoom)
            }
        }
    }

    private fun applyTextZoomToWebView(webView: WebView, percentage: Int) {
        val scale = percentage / 100.0
        val script = """
            (function() {
                var style = document.getElementById('text-zoom-style');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'text-zoom-style';
                    document.head.appendChild(style);
                }
                style.textContent = 'body { font-size: ${scale}em !important; }';
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    fun getTextZoom(): Int {
        return settingsManager.getTextZoom()
    }

    // Desktop Mode JavaScript Injection
    fun injectDesktopModeScripts(webView: WebView?) {
        webView ?: return

        val desktopModeScript = """
            (function() {
                var viewportMeta = document.querySelector('meta[name="viewport"]');
                if (viewportMeta) {
                    viewportMeta.content = 'width=1280, initial-scale=1.0';
                } else {
                    var meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=1280, initial-scale=1.0';
                    document.head.appendChild(meta);
                }

                delete window.ontouchstart;
                delete window.ontouchmove;
                delete window.ontouchend;
                delete window.ontouchcancel;

                Object.defineProperty(navigator, 'maxTouchPoints', {
                    get: function() { return 0; }
                });

                Object.defineProperty(screen, 'width', { get: function() { return 1920; } });
                Object.defineProperty(screen, 'height', { get: function() { return 1080; } });
                Object.defineProperty(screen, 'availWidth', { get: function() { return 1920; } });
                Object.defineProperty(screen, 'availHeight', { get: function() { return 1040; } });
                Object.defineProperty(window, 'innerWidth', { get: function() { return 1280; } });
                Object.defineProperty(window, 'innerHeight', { get: function() { return 720; } });
                Object.defineProperty(window, 'devicePixelRatio', { get: function() { return 1; } });

                if (window.PointerEvent) {
                    const originalPointerEvent = window.PointerEvent;
                    window.PointerEvent = function(type, init) {
                        if (init && init.pointerType === 'touch') {
                            init.pointerType = 'mouse';
                        }
                        return new originalPointerEvent(type, init);
                    };
                }

                const originalMatchMedia = window.matchMedia;
                window.matchMedia = function(query) {
                    if (query.includes('pointer: coarse')) return { matches: false, media: query };
                    if (query.includes('pointer: fine')) return { matches: true, media: query };
                    if (query.includes('hover: none')) return { matches: false, media: query };
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
        val currentUrl = currentTab.url

        webView.settings.apply {
            userAgentString = if (currentTab.desktopMode) {
                DESKTOP_USER_AGENT
            } else {
                WebSettings.getDefaultUserAgent(context)
            }
            useWideViewPort = currentTab.desktopMode
            loadWithOverviewMode = currentTab.desktopMode
        }

        webView.clearCache(true)
        webView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.loadUrl(currentUrl)
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

        if (currentTab.desktopMode) {
            webView.postDelayed({
                injectDesktopModeScripts(webView)
            }, 500)
        }
    }

    fun navigateToUrl(url: String) {
        val currentTab = getCurrentTab() ?: return
        if (url.isBlank()) return
        
        var finalUrl = url.trim()

        // If it's a search query (contains spaces or no dots), use DuckDuckGo
        if (!finalUrl.contains(".") || finalUrl.contains(" ")) {
            finalUrl = "https://duckduckgo.com/?q=${finalUrl.replace(" ", "+")}"
        } else if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }

        // Update tab URL immediately
        currentTab.url = finalUrl
        
        // Load in WebView
        if (currentTab.webView != null) {
            currentTab.webView?.loadUrl(finalUrl)
        } else {
            // If WebView not created yet, it will load when created
            urlInput.value = finalUrl
        }
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

    override fun onCleared() {
        super.onCleared()
        tabs.forEach { it.webView?.destroy() }
    }
}
