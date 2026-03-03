package com.litebrowser.app.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import com.litebrowser.app.manager.HomepageManager
import com.litebrowser.app.manager.TabManager
import com.litebrowser.app.model.BrowserTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val tabManager = TabManager(application)

    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<Long?>(null)
    val activeTabId: StateFlow<Long?> = _activeTabId.asStateFlow()

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    private val HOMEPAGE = "about:blank"
    private var nextTabId = 0L

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
        val tab = BrowserTab(id = nextTabId++)
        val wv = tabManager.createWebView(tab)
        tab.webView = wv
        return tab
    }

    fun openNewTab() {
        val newTab = createTab()

        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id

        newTab.webView?.loadDataWithBaseURL(
            "https://litebrowser.local",
            HomepageManager.getHomepageHtml(),
            "text/html",
            "UTF-8",
            null
        )
    }

    fun switchToTab(tabId: Long) {
        if (tabId == _activeTabId.value) return

        val targetTab = _tabs.value.find { it.id == tabId } ?: return

        if (targetTab.webView == null) {
            val wv = tabManager.createWebView(targetTab)
            val savedState = targetTab.savedState
            if (savedState != null) {
                wv.restoreState(savedState)
            } else {
                val url = targetTab.url.ifEmpty { HOMEPAGE }
                if (url == HOMEPAGE) {
                    wv.loadDataWithBaseURL(
                        "https://litebrowser.local",
                        HomepageManager.getHomepageHtml(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else {
                    wv.loadUrl(url)
                }
            }
            targetTab.webView = wv
            targetTab.savedState = null
        }

        _activeTabId.value = tabId
    }

    fun closeTab(tabId: Long) {
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
                _activeTabId.value = remaining[nextIndex].id
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
        activeTab?.webView?.reload()
    }

    fun zoomIn() {
        val tab = activeTab ?: return
        val newZoom = (tab.zoomLevel + 25).coerceAtMost(200)
        updateTab(tab.id) { it.copy(zoomLevel = newZoom) }
        tab.webView?.let { tabManager.applyZoomImmediate(it, newZoom) }
    }

    fun zoomOut() {
        val tab = activeTab ?: return
        val newZoom = (tab.zoomLevel - 25).coerceAtLeast(50)
        updateTab(tab.id) { it.copy(zoomLevel = newZoom) }
        tab.webView?.let { tabManager.applyZoomImmediate(it, newZoom) }
    }

    fun toggleDesktopMode() {
        val tab = activeTab ?: return
        val currentUrl = tab.url.ifEmpty { HOMEPAGE }
        
        val oldWebView = tab.webView
        oldWebView?.stopLoading()
        oldWebView?.destroy()

        val newDesktopMode = !tab.isDesktopMode
        
        tab.isDesktopMode = newDesktopMode
        tab.webView = null
        tab.savedState = null
        tab.url = currentUrl
        
        updateTab(tab.id) { it.copy(isDesktopMode = newDesktopMode) }
        
        val newWebView = tabManager.createWebView(tab)
        tab.webView = newWebView
        
        if (currentUrl == HOMEPAGE || currentUrl.startsWith("about:")) {
            newWebView.loadDataWithBaseURL(
                "https://litebrowser.local",
                HomepageManager.getHomepageHtml(),
                "text/html",
                "UTF-8",
                null
            )
        } else {
            newWebView.loadUrl(currentUrl)
        }
    }

    fun onLowMemory() {
        _tabs.value
            .filter { it.id != _activeTabId.value }
            .forEach { tab ->
                tab.webView?.let { wv ->
                    val bundle = Bundle()
                    wv.saveState(bundle)
                    wv.destroy()
                    tab.webView = null
                    tab.savedState = bundle
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.value.forEach { tab ->
            tab.webView?.destroy()
        }
    }

    private fun updateTab(tabId: Long, transform: (BrowserTab) -> BrowserTab) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) transform(tab) else tab
        }
    }

    private fun sanitizeInput(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "${HomepageManager.SEARCH_ENGINE}${trimmed.replace(" ", "+")}"
        }
    }
}
