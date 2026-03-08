package com.browser.app.model

class BrowserManager {
    private val tabs = mutableListOf<BrowserTab>()
    private var currentTabId = -1
    private var tabIdCounter = 0

    fun createNewTab(): BrowserTab {
        BrowserTab(id = tabIdCounter++).also { tab ->
            tabs.add(tab)
            currentTabId = tab.id
        }
        return tabs.last()
    }

    fun getTab(id: Int) = tabs.find { it.id == id }
    fun getCurrentTab() = tabs.find { it.id == currentTabId }
    fun getAllTabs() = tabs.toList()

    fun setCurrentTab(id: Int) {
        tabs.forEach { it.isSelected = it.id == id }
        currentTabId = id
    }

    fun closeTab(id: Int) {
        tabs.find { it.id == id }?.let { tab ->
            tab.webView?.destroy()
            tabs.remove(tab)
            when {
                tabs.isEmpty() -> createNewTab()
                tab.isSelected -> setCurrentTab(tabs.last().id)
                else -> {}
            }
        }
    }
}
