package com.browser.app.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
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
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    tab.isLoading = false
                    tab.url = url ?: ""
                    tab.title = view?.title ?: tab.url
                    tab.canGoBack = view?.canGoBack() ?: false
                    tab.canGoForward = view?.canGoForward() ?: false
                    updateUrlInputFromCurrentTab()
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
        val webView = getCurrentTab()?.webView ?: return
        val currentZoom = webView.settings.textZoom
        webView.settings.textZoom = (currentZoom + 10).coerceAtMost(200)
    }

    fun zoomOut() {
        val webView = getCurrentTab()?.webView ?: return
        val currentZoom = webView.settings.textZoom
        webView.settings.textZoom = (currentZoom - 10).coerceAtLeast(50)
    }
    
    fun toggleDesktopMode() {
        val currentTab = getCurrentTab() ?: return
        currentTab.desktopMode = !currentTab.desktopMode

        val webView = currentTab.webView ?: return
        val context = webView.context

        webView.settings.apply {
            userAgentString = if (currentTab.desktopMode) {
                DESKTOP_USER_AGENT
            } else {
                WebSettings.getDefaultUserAgent(context)
            }
        }

        // Clear cache to ensure the page reloads with new user agent
        webView.clearCache(false)
        webView.reload()
    }
    
    override fun onCleared() {
        super.onCleared()
        tabs.forEach { it.webView?.destroy() }
    }
}
