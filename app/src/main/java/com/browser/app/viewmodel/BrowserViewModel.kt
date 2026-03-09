package com.browser.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.browser.app.model.Tab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserViewModel : ViewModel() {
    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs: StateFlow<List<Tab>> = _tabs.asStateFlow()

    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    private val _showTabManager = MutableStateFlow(false)
    val showTabManager: StateFlow<Boolean> = _showTabManager.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _nightMode = MutableStateFlow(false)
    val nightMode: StateFlow<Boolean> = _nightMode.asStateFlow()

    private val _desktopMode = MutableStateFlow(false)
    val desktopMode: StateFlow<Boolean> = _desktopMode.asStateFlow()

    init {
        addNewTab()
    }

    fun addNewTab(url: String = "https://www.google.com") {
        val newTab = Tab(
            id = if (_tabs.value.isEmpty()) 0 else _tabs.value.maxOf { it.id } + 1,
            url = url,
            title = "New Tab"
        )
        _tabs.value = _tabs.value + newTab
        _currentTabIndex.value = _tabs.value.size - 1
        _searchQuery.value = ""
    }

    fun closeTab(tabId: Int) {
        val currentList = _tabs.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == tabId }
        if (index != -1) {
            val closingCurrent = index == _currentTabIndex.value
            currentList.removeAt(index)
            if (currentList.isEmpty()) {
                addNewTab()
            } else {
                val newIndex = if (_currentTabIndex.value >= currentList.size) {
                    currentList.size - 1
                } else {
                    _currentTabIndex.value
                }
                _currentTabIndex.value = newIndex
                if (closingCurrent) {
                    _searchQuery.value = ""
                }
            }
            _tabs.value = currentList
        }
    }

    fun switchTab(tabId: Int) {
        val index = _tabs.value.indexOfFirst { it.id == tabId }
        if (index != -1) {
            _currentTabIndex.value = index
            _showTabManager.value = false
            _searchQuery.value = ""
        }
    }

    fun updateCurrentTabUrl(url: String) {
        updateTab(_currentTabIndex.value) { it.copy(url = url) }
    }

    fun updateCurrentTabTitle(title: String) {
        updateTab(_currentTabIndex.value) { it.copy(title = title) }
    }

    fun updateCurrentTabFavicon(favicon: Bitmap) {
        updateTab(_currentTabIndex.value) { it.copy(favicon = favicon) }
    }

    private fun updateTab(index: Int, update: (Tab) -> Tab) {
        val currentList = _tabs.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = update(currentList[index])
            _tabs.value = currentList
        }
    }

    fun setShowTabManager(show: Boolean) {
        _showTabManager.value = show
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setIsLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setProgress(progress: Int) {
        _progress.value = progress
    }

    fun toggleNightMode() {
        _nightMode.value = !_nightMode.value
    }

    fun toggleDesktopMode() {
        _desktopMode.value = !_desktopMode.value
    }

    fun changeZoom(delta: Float) {
        val currentTab = getCurrentTab()
        if (currentTab != null) {
            val newZoom = (currentTab.zoomLevel + delta).coerceIn(0.25f, 5.0f)
            updateTab(_currentTabIndex.value) { it.copy(zoomLevel = newZoom) }
        }
    }
        val currentIndex = _currentTabIndex.value
        return currentIndex in _tabs.value.indices && _tabs.value[currentIndex].url != "about:blank"
    }

    fun getCurrentTab(): Tab? {
        return _currentTabIndex.value.let { index ->
            if (index in _tabs.value.indices) _tabs.value[index] else null
        }
    }
}
