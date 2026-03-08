package com.browser.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.browser.app.R
import com.browser.app.model.BrowserManager
import com.browser.app.model.BrowserTab
import com.browser.app.webview.WebViewFactory
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "browser_prefs"
        private const val KEY_TABS = "saved_tabs"
        private const val KEY_CURRENT_TAB = "current_tab_id"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_PAGE_ZOOM = "page_zoom_percent"
        private const val ZOOM_MIN = 50
        private const val ZOOM_MAX = 150
        private const val ZOOM_DEFAULT = 100
    }

    private val browserManager = BrowserManager()
    private lateinit var prefs: SharedPreferences
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var webViewContainer: FrameLayout
    private lateinit var tabSlider: LinearLayout
    private lateinit var tabsContainer: LinearLayout
    private lateinit var menuDesktop: TextView
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var zoomPercentage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        initViews()
        setupListeners()
        
        // Try to restore saved tabs, otherwise create new tab
        if (!restoreTabs()) {
            createNewTab()
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        urlEditText = findViewById(R.id.urlEditText)
        progressBar = findViewById(R.id.progressBar)
        webViewContainer = findViewById(R.id.webViewContainer)
        tabSlider = findViewById(R.id.tabSlider)
        tabsContainer = findViewById(R.id.tabsContainer)
        menuDesktop = findViewById(R.id.menuDesktop)
        zoomSeekBar = findViewById(R.id.zoomSeekBar)
        zoomPercentage = findViewById(R.id.zoomPercentage)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener { 
            drawerLayout.openDrawer(GravityCompat.START) 
        }
        findViewById<ImageButton>(R.id.addTabButton).setOnClickListener { toggleTabSlider() }
        
        findViewById<TextView>(R.id.menuHome).setOnClickListener { 
            loadUrl(getString(R.string.default_url))
            drawerLayout.closeDrawers()
        }
        
        // Desktop Mode toggle
        menuDesktop.setOnClickListener { 
            toggleDesktopMode()
            drawerLayout.closeDrawers()
        }
        
        // Zoom SeekBar listener
        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val zoomPercent = progress.coerceIn(ZOOM_MIN, ZOOM_MAX)
                    applyPageZoom(zoomPercent)
                    zoomPercentage.text = "$zoomPercent%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        findViewById<ImageButton>(R.id.menuBack).setOnClickListener { browserManager.getCurrentTab()?.webView?.goBack() }
        findViewById<ImageButton>(R.id.menuRefresh).setOnClickListener { browserManager.getCurrentTab()?.webView?.reload() }

        urlEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                loadUrl(urlEditText.text.toString())
                true
            } else false
        }
    }

    private fun toggleTabSlider() {
        tabSlider.isVisible = !tabSlider.isVisible
        if (tabSlider.isVisible) updateTabSlider()
    }

    private fun updateTabSlider() {
        tabsContainer.removeAllViews()
        
        browserManager.getAllTabs().forEach { tab ->
            val tabView = layoutInflater.inflate(R.layout.item_tab, tabsContainer, false)
            tabView.findViewById<TextView>(R.id.tabTitle).text = tab.title.ifEmpty { "Tab ${tab.id + 1}" }
            if (tab.isSelected) tabView.setBackgroundColor(getColor(R.color.tab_selected))
            tabView.setOnClickListener { showTab(tab.id) }
            tabView.findViewById<ImageButton>(R.id.closeTabButton).setOnClickListener { closeTab(tab.id) }
            tabsContainer.addView(tabView)
        }
        
        layoutInflater.inflate(R.layout.item_tab, tabsContainer, false).apply {
            findViewById<TextView>(R.id.tabTitle).text = "+"
            findViewById<ImageView>(R.id.tabIcon).isVisible = false
            setOnClickListener { createNewTab() }
            tabsContainer.addView(this)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createNewTab(loadUrlNow: Boolean = true) {
        val currentTab = browserManager.getCurrentTab()
        val isDesktopMode = currentTab?.isDesktopMode ?: prefs.getBoolean(KEY_DESKTOP_MODE, false)
        
        val tab = browserManager.createNewTab().apply {
            this.isDesktopMode = isDesktopMode
            this.url = ""
            this.title = "New Tab"
        }
        val webView = WebViewFactory.createWebView(this, isDesktopMode).apply { tag = tab.id }
        tab.webView = webView
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible = true
                urlEditText.setText(url ?: "")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false
                browserManager.getCurrentTab()?.apply {
                    this.url = url ?: ""
                    this.title = view?.title ?: "New Tab"
                }
                
                // Inject viewport fix to neutralize hostile meta tags
                view?.let { WebViewFactory.injectViewportFix(it) }
                
                // Apply saved page zoom
                val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
                applyPageZoomSilent(view, savedZoom)
                
                if (tabSlider.isVisible) updateTabSlider()
                updateMenuState()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, progress: Int) {
                progressBar.progress = progress
                progressBar.isVisible = progress < 100
            }
        }
        
        webViewContainer.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        
        // Apply saved page zoom
        val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
        applyPageZoomSilent(webView, savedZoom)
        
        showTab(tab.id)
        
        if (loadUrlNow) {
            loadUrl(getString(R.string.default_url))
        }
    }

    private fun showTab(tabId: Int) {
        browserManager.setCurrentTab(tabId)
        tabSlider.isVisible = false
        
        // Show/hide WebViews - no reload, just visibility change
        for (i in 0 until webViewContainer.childCount) {
            webViewContainer.getChildAt(i).isVisible = webViewContainer.getChildAt(i).tag == tabId
        }
        
        browserManager.getCurrentTab()?.let { tab ->
            urlEditText.setText(tab.url)
            tab.webView?.let { wv ->
                // Ensure desktop mode is applied
                WebViewFactory.setDesktopModeNoReload(wv, tab.isDesktopMode)
                // Apply current page zoom
                val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
                applyPageZoomSilent(wv, savedZoom)
            }
        }
        
        updateMenuState()
        hideKeyboard()
    }

    private fun updateMenuState() {
        val tab = browserManager.getCurrentTab()
        
        // Update desktop mode indicator
        menuDesktop.text = if (tab?.isDesktopMode == true) "Desktop Mode ✓" else "Desktop Mode"
        
        // Update zoom slider
        val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
        zoomSeekBar.progress = savedZoom
        zoomPercentage.text = "$savedZoom%"
    }

    private fun closeTab(tabId: Int) {
        val tab = browserManager.getTab(tabId)
        val webView = tab?.webView
        webView?.let {
            webViewContainer.removeView(it)
            it.destroy()
        }
        browserManager.closeTab(tabId)
        
        // Save tabs after closing
        saveTabs()
        
        browserManager.getCurrentTab()?.let { 
            showTab(it.id) 
        } ?: run {
            createNewTab()
        }
    }

    private fun loadUrl(input: String) {
        hideKeyboard()
        
        val url = input.trim().let {
            when {
                it.startsWith("http://") || it.startsWith("https://") -> it
                it.contains(".") && !it.contains(" ") -> "https://$it"
                else -> "https://lite.duckduckgo.com/lite/?q=$it"
            }
        }
        browserManager.getCurrentTab()?.webView?.loadUrl(url)
        browserManager.getCurrentTab()?.url = url
        urlEditText.setText(url)
        tabSlider.isVisible = false
    }

    // Page Zoom - scales entire page (real browser zoom)
    private fun applyPageZoom(percent: Int) {
        val zoomPercent = percent.coerceIn(ZOOM_MIN, ZOOM_MAX)
        prefs.edit().putInt(KEY_PAGE_ZOOM, zoomPercent).apply()
        
        browserManager.getCurrentTab()?.webView?.let { wv ->
            applyPageZoomSilent(wv, zoomPercent)
        }
        
        zoomPercentage.text = "$zoomPercent%"
    }

    private fun applyPageZoomSilent(webView: WebView?, percent: Int) {
        webView?.let { wv ->
            // Use setInitialScale for proper page scaling
            val scalePercent = percent.toFloat() / 100f
            wv.settings.setSupportZoom(true)
            wv.settings.builtInZoomControls = true
            wv.settings.displayZoomControls = false
            
            // Set initial scale - this scales the entire page
            wv.setInitialScale((scalePercent * 100).toInt())
        }
    }

    private fun toggleDesktopMode() {
        browserManager.getCurrentTab()?.let { tab ->
            tab.isDesktopMode = !tab.isDesktopMode
            // Save to preferences for persistence
            prefs.edit().putBoolean(KEY_DESKTOP_MODE, tab.isDesktopMode).apply()
            
            tab.webView?.let { wv ->
                WebViewFactory.setDesktopMode(wv, tab.isDesktopMode)
            }
            
            // Apply to all tabs for consistency
            browserManager.getAllTabs().forEach { t ->
                t.isDesktopMode = tab.isDesktopMode
            }
            
            // Save tabs
            saveTabs()
            
            updateMenuState()
            showToast(if (tab.isDesktopMode) "Desktop Mode ON" else "Desktop Mode OFF")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // Tab persistence - save tabs to SharedPreferences
    private fun saveTabs() {
        val tabsArray = JSONArray()
        var currentTabId = -1
        
        browserManager.getAllTabs().forEach { tab ->
            val tabObj = JSONObject().apply {
                put("id", tab.id)
                put("url", tab.url)
                put("title", tab.title)
                put("isDesktopMode", tab.isDesktopMode)
                put("isSelected", tab.isSelected)
            }
            tabsArray.put(tabObj)
            
            if (tab.isSelected) {
                currentTabId = tab.id
            }
        }
        
        prefs.edit()
            .putString(KEY_TABS, tabsArray.toString())
            .putInt(KEY_CURRENT_TAB, currentTabId)
            .putBoolean(KEY_DESKTOP_MODE, browserManager.getCurrentTab()?.isDesktopMode ?: false)
            .putInt(KEY_PAGE_ZOOM, prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT))
            .apply()
    }

    // Restore tabs from SharedPreferences
    @SuppressLint("SetJavaScriptEnabled")
    private fun restoreTabs(): Boolean {
        val savedTabsJson = prefs.getString(KEY_TABS, null) ?: return false
        val savedCurrentTabId = prefs.getInt(KEY_CURRENT_TAB, 0)
        val isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
        
        return try {
            val tabsArray = JSONArray(savedTabsJson)
            if (tabsArray.length() == 0) return false
            
            // Clear current WebViews
            webViewContainer.removeAllViews()
            browserManager.clearAllTabs()
            
            for (i in 0 until tabsArray.length()) {
                val tabObj = tabsArray.getJSONObject(i)
                val tabId = tabObj.getInt("id")
                val url = tabObj.optString("url", "")
                val title = tabObj.optString("title", "New Tab")
                val tabDesktopMode = tabObj.optBoolean("isDesktopMode", isDesktopMode)
                val isSelected = tabObj.optBoolean("isSelected", false)
                
                val tab = BrowserTab(
                    id = tabId,
                    url = url,
                    title = title,
                    isDesktopMode = tabDesktopMode,
                    isSelected = isSelected
                )
                
                // Recreate WebView
                val webView = WebViewFactory.createWebView(this, tabDesktopMode).apply { tag = tab.id }
                tab.webView = webView
                
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        progressBar.isVisible = true
                        urlEditText.setText(url ?: "")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        progressBar.isVisible = false
                        browserManager.getCurrentTab()?.apply {
                            this.url = url ?: ""
                            this.title = view?.title ?: "New Tab"
                        }
                        
                        view?.let { WebViewFactory.injectViewportFix(it) }
                        
                        val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
                        applyPageZoomSilent(view, savedZoom)
                        
                        if (tabSlider.isVisible) updateTabSlider()
                        updateMenuState()
                    }
                }

                webView.webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, progress: Int) {
                        progressBar.progress = progress
                        progressBar.isVisible = progress < 100
                    }
                }
                
                webViewContainer.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                browserManager.addTab(tab)
            }
            
            // Show the saved current tab
            val currentTab = if (savedCurrentTabId >= 0) browserManager.getTab(savedCurrentTabId) else browserManager.getCurrentTab()
            currentTab?.let { showTab(it.id) }
            
            // Load URL if tab has saved URL
            currentTab?.let { tab ->
                if (tab.url.isNotEmpty()) {
                    tab.webView?.loadUrl(tab.url)
                }
            }
            
            // Apply saved page zoom
            val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
            currentTab?.webView?.let { applyPageZoomSilent(it, savedZoom) }
            
            updateMenuState()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onPause() {
        super.onPause()
        browserManager.getCurrentTab()?.webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        browserManager.getCurrentTab()?.webView?.onResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            tabSlider.isVisible -> tabSlider.isVisible = false
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawers()
            browserManager.getCurrentTab()?.webView?.canGoBack() == true -> browserManager.getCurrentTab()?.webView?.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        // Save tabs when app goes to background
        saveTabs()
    }

    override fun onDestroy() {
        // Save tabs before destroying
        saveTabs()
        browserManager.getAllTabs().forEach { it.webView?.destroy() }
        super.onDestroy()
    }
}
