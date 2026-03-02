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

    private val HOMEPAGE  = "https://www.google.com"
    val ZOOM_MIN          = 50
    val ZOOM_MAX          = 300
    val ZOOM_STEP         = 25

    init {
        openNewTab()

        tabManager.getTabZoomLevel = { tabId ->
            _tabs.value.find { it.id == tabId }?.zoomLevel ?: 100
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

    fun openNewTab() {
        val tab = BrowserTab(id = UUID.randomUUID().toString())
        val wv = tabManager.createWebView(tab)
        tab.webView = wv

        activeTab?.webView?.let { tabManager.pauseWebView(it) }

        _tabs.value = _tabs.value + tab
        _activeTabId.value = tab.id

        wv.loadUrl(HOMEPAGE)
    }

    fun switchToTab(tabId: String) {
        if (tabId == _activeTabId.value) return

        activeTab?.webView?.let { tabManager.pauseWebView(it) }

        val target = _tabs.value.find { it.id == tabId } ?: return

        if (target.webView == null) {
            val wv = tabManager.createWebView(target)
            if (target.savedState != null) {
                wv.restoreState(target.savedState!!)
            } else {
                wv.loadUrl(target.url.ifEmpty { HOMEPAGE })
            }
            updateTab(tabId) { it.copy(webView = wv, savedState = null) }
            tabManager.resumeWebView(wv)
            tabManager.applyZoom(wv, target.zoomLevel)
        } else {
            target.webView?.let { wv ->
                tabManager.resumeWebView(wv)
                tabManager.applyZoom(wv, target.zoomLevel)
            }
        }

        _activeTabId.value = tabId
    }

    fun closeTab(tabId: String) {
        val tabIndex = _tabs.value.indexOfFirst { it.id == tabId }
        if (tabIndex < 0) return

        _tabs.value[tabIndex].webView?.destroy()

        val remaining = _tabs.value.toMutableList().also { it.removeAt(tabIndex) }
        _tabs.value = remaining

        if (tabId == _activeTabId.value) {
            if (remaining.isEmpty()) {
                openNewTab()
            } else {
                val nextIndex = (tabIndex - 1).coerceAtLeast(0)
                val nextTab = remaining[nextIndex]
                nextTab.webView?.let { tabManager.resumeWebView(it) }
                _activeTabId.value = nextTab.id
            }
        }
    }

    fun navigate(input: String) {
        activeTab?.webView?.loadUrl(sanitizeInput(input))
    }

    fun goBack() {
        activeTab?.webView?.takeIf { it.canGoBack() }?.goBack()
    }

    fun goForward() {
        activeTab?.webView?.takeIf { it.canGoForward() }?.goForward()
    }

    fun refresh() {
        val tab = activeTab ?: return
        tab.webView?.let { wv ->
            tabManager.applyZoom(wv, tab.zoomLevel)
            wv.reload()
        }
    }

    fun zoomIn() {
        val tab = activeTab ?: return
        val newZoom = (tab.zoomLevel + ZOOM_STEP).coerceAtMost(ZOOM_MAX)
        updateTab(tab.id) { it.copy(zoomLevel = newZoom) }
        tab.webView?.let { tabManager.applyZoom(it, newZoom) }
    }

    fun zoomOut() {
        val tab = activeTab ?: return
        val newZoom = (tab.zoomLevel - ZOOM_STEP).coerceAtLeast(ZOOM_MIN)
        updateTab(tab.id) { it.copy(zoomLevel = newZoom) }
        tab.webView?.let { tabManager.applyZoom(it, newZoom) }
    }

    fun zoomReset() {
        val tab = activeTab ?: return
        updateTab(tab.id) { it.copy(zoomLevel = 100) }
        tab.webView?.let { tabManager.applyZoom(it, 100) }
    }

    fun toggleDesktopMode() {
        val tab = activeTab ?: return
        val newMode = !tab.isDesktopMode
        val currentUrl = tab.url.ifEmpty { HOMEPAGE }
        
        tab.webView?.destroy()
        
        val newTab = tab.copy(
            isDesktopMode = newMode,
            webView = null,
            savedState = null
        )
        
        updateTab(tab.id) { newTab }
        
        val wv = tabManager.createWebView(newTab)
        newTab.webView = wv
        wv.loadUrl(currentUrl)
    }

    fun onLowMemory() {
        _tabs.value
            .filter { it.id != _activeTabId.value }
            .forEach { tab ->
                tab.webView?.let { wv ->
                    val bundle = Bundle()
                    wv.saveState(bundle)
                    wv.destroy()
                    updateTab(tab.id) { it.copy(webView = null, savedState = bundle) }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.value.forEach { it.webView?.destroy() }
    }

    private fun updateTab(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        _tabs.value = _tabs.value.map { if (it.id == tabId) transform(it) else it }
    }

    private fun sanitizeInput(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") &&
            !trimmed.contains(" ")         -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
        }
    }
}
