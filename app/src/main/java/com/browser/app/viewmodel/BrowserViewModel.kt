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
                
                // Desktop mode user agent
                if (tab.desktopMode) {
                    userAgentString = userAgentString.replace("Android", "diordnA")
                        .replace("Mobile", "eliboM")
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
            webView.loadUrl("https://www.google.com")
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
        getCurrentTab()?.webView?.zoomIn()
    }
    
    fun zoomOut() {
        getCurrentTab()?.webView?.zoomOut()
    }
    
    fun toggleDesktopMode() {
        val currentTab = getCurrentTab() ?: return
        currentTab.desktopMode = !currentTab.desktopMode
        
        currentTab.webView?.settings?.apply {
            userAgentString = if (currentTab.desktopMode) {
                userAgentString.replace("Android", "diordnA")
                    .replace("Mobile", "eliboM")
            } else {
                WebSettings.getDefaultUserAgent(currentTab.webView?.context ?: return)
            }
        }
        
        currentTab.webView?.reload()
    }
    
    override fun onCleared() {
        super.onCleared()
        tabs.forEach { it.webView?.destroy() }
    }
}
