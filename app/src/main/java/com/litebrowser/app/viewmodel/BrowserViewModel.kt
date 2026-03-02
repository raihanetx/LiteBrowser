package com.litebrowser.app.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import com.litebrowser.app.manager.TabManager
import com.litebrowser.app.model.BrowserTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val tabManager = TabManager(application)

    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    private val HOMEPAGE = "https://www.google.com"
    val ZOOM_MIN = 50
    val ZOOM_MAX = 300
    val ZOOM_STEP = 25

    init {
        openNewTab()

        tabManager.getTabZoomLevel = { tabId ->
            _tabs.value.find { it.id == tabId }?.zoomLevel ?: 100
        }

        tabManager.getTabDesktopMode = { tabId ->
            _tabs.value.find { it.id == tabId }?.isDesktopMode ?: false
        }

        tabManager.onPageStarted = { tabId, url ->
            updateTab(tabId) { it.copy(isLoading = true, url = url) }
        }

        tabManager.onPageFinished = { tabId, url, title ->
            updateTab(tabId) { it.copy(isLoading = false, url = url, title = title) }
        }

        tabManager.onProgressChanged = { tabId, progress ->
            updateTab(tabId) { it.copy(progress = progress) }
        }

        tabManager.onHistoryChanged = { tabId, canBack, canFwd ->
            updateTab(tabId) { it.copy(canGoBack = canBack, canGoForward = canFwd) }
        }
    }

    private fun createTab(): BrowserTab {
        val tab = BrowserTab(id = UUID.randomUUID().toString())
        val wv = tabManager.createWebView(tab)
        tab.webView = wv
        return tab
    }

    fun openNewTab() {
        val currentActiveTab = activeTab
        if (currentActiveTab != null && currentActiveTab.webView != null) {
            tabManager.pauseWebView(currentActiveTab.webView!!)
        }

        val newTab = createTab()

        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id

        newTab.webView?.loadUrl(HOMEPAGE)
    }

    fun switchToTab(tabId: String) {
        if (tabId == _activeTabId.value) return

        val currentActiveTab = activeTab
        if (currentActiveTab != null && currentActiveTab.webView != null) {
            tabManager.pauseWebView(currentActiveTab.webView!!)
        }

        val targetTab = _tabs.value.find { it.id == tabId } ?: return

        if (targetTab.webView == null) {
            val wv = tabManager.createWebView(targetTab)
            val savedState = targetTab.savedState
            if (savedState != null) {
                wv.restoreState(savedState)
            } else {
                val url = targetTab.url.ifEmpty { HOMEPAGE }
                wv.loadUrl(url)
            }
            targetTab.webView = wv
            targetTab.savedState = null
            
            tabManager.resumeWebView(wv)
            tabManager.applyZoomImmediate(wv, targetTab.zoomLevel)
        } else {
            tabManager.resumeWebView(targetTab.webView!!)
            tabManager.applyZoomImmediate(targetTab.webView!!, targetTab.zoomLevel)
        }

        _activeTabId.value = tabId
    }

    fun closeTab(tabId: String) {
        val tabIndex = _tabs.value.indexOfFirst { it.id == tabId }
        if (tabIndex < 0) return

        val tabToClose = _tabs.value[tabIndex]
        tabToClose.webView?.destroy()

        val remaining = _tabs.value.toMutableList()
        remaining.removeAt(tabIndex)
        _tabs.value = remaining

        if (tabId == _activeTabId.value) {
            if (remaining.isEmpty()) {
                openNewTab()
            } else {
                val nextIndex = (tabIndex - 1).coerceAtLeast(0)
                val nextTab = remaining[nextIndex]
                if (nextTab.webView != null) {
                    tabManager.resumeWebView(nextTab.webView!!)
                    tabManager.applyZoomImmediate(nextTab.webView!!, nextTab.zoomLevel)
                }
                _activeTabId.value = nextTab.id
            }
        }
    }

    fun navigate(input: String) {
        val url = sanitizeInput(input)
        activeTab?.webView?.loadUrl(url)
    }

    fun goBack() {
        activeTab?.webView?.goBack()
    }

    fun goForward() {
        activeTab?.webView?.goForward()
    }

    fun refresh() {
        val tab = activeTab ?: return
        val wv = tab.webView ?: return
        
        tabManager.applyZoomImmediate(wv, tab.zoomLevel)
        
        handler.postDelayed({
            tabManager.applyZoomImmediate(wv, tab.zoomLevel)
        }, 100)
        
        wv.reload()
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    fun zoomIn() {
        val tab = activeTab ?: return
        val newZoom = (tab.zoomLevel + ZOOM_STEP).coerceAtMost(ZOOM_MAX)
        updateTab(tab.id) { it.copy(zoomLevel = newZoom) }
        
        tab.webView?.let { wv ->
            tabManager.applyZoomImmediate(wv, newZoom)
            
            handler.postDelayed({
                tabManager.applyZoomImmediate(wv, newZoom)
            }, 50)
        }
    }

    fun zoomOut() {
        val tab = activeTab ?: return
        val newZoom = (tab.zoomLevel - ZOOM_STEP).coerceAtLeast(ZOOM_MIN)
        updateTab(tab.id) { it.copy(zoomLevel = newZoom) }
        
        tab.webView?.let { wv ->
            tabManager.applyZoomImmediate(wv, newZoom)
            
            handler.postDelayed({
                tabManager.applyZoomImmediate(wv, newZoom)
            }, 50)
        }
    }

    fun zoomReset() {
        val tab = activeTab ?: return
        updateTab(tab.id) { it.copy(zoomLevel = 100) }
        
        tab.webView?.let { wv ->
            tabManager.applyZoomImmediate(wv, 100)
            
            handler.postDelayed({
                tabManager.applyZoomImmediate(wv, 100)
            }, 50)
        }
    }

    fun toggleDesktopMode() {
        val tab = activeTab ?: return
        val currentUrl = tab.url.ifEmpty { HOMEPAGE }
        
        val oldWebView = tab.webView
        if (oldWebView != null) {
            try {
                oldWebView.stopLoading()
                oldWebView.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val newDesktopMode = !tab.isDesktopMode
        
        tab.isDesktopMode = newDesktopMode
        tab.webView = null
        tab.savedState = null
        tab.url = currentUrl
        
        updateTab(tab.id) { it.copy(isDesktopMode = newDesktopMode) }
        
        val newWebView = tabManager.createWebView(tab)
        tab.webView = newWebView
        
        newWebView.loadUrl(currentUrl)
    }

    fun onLowMemory() {
        _tabs.value
            .filter { it.id != _activeTabId.value }
            .forEach { tab ->
                tab.webView?.let { wv ->
                    try {
                        val bundle = Bundle()
                        wv.saveState(bundle)
                        wv.destroy()
                        tab.webView = null
                        tab.savedState = bundle
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.value.forEach { tab ->
            try {
                tab.webView?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTab(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) transform(tab) else tab
        }
    }

    private fun sanitizeInput(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
        }
    }
}
