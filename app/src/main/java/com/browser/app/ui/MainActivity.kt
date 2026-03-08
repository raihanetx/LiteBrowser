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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.browser.app.R
import com.browser.app.model.BrowserManager
import com.browser.app.webview.WebViewFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "browser_prefs"
        private const val KEY_TEXT_ZOOM = "text_zoom_percent"
        private const val ZOOM_STEP = 25
        private const val ZOOM_MIN = 50
        private const val ZOOM_MAX = 300
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
    private lateinit var menuZoomIn: TextView
    private lateinit var menuZoomOut: TextView
    private lateinit var zoomToolbar: LinearLayout
    private lateinit var pageZoomIn: ImageButton
    private lateinit var pageZoomOut: ImageButton
    private lateinit var pageZoomReset: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        initViews()
        setupListeners()
        createNewTab()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        urlEditText = findViewById(R.id.urlEditText)
        progressBar = findViewById(R.id.progressBar)
        webViewContainer = findViewById(R.id.webViewContainer)
        tabSlider = findViewById(R.id.tabSlider)
        tabsContainer = findViewById(R.id.tabsContainer)
        menuDesktop = findViewById(R.id.menuDesktop)
        menuZoomIn = findViewById(R.id.menuZoomIn)
        menuZoomOut = findViewById(R.id.menuZoomOut)
        zoomToolbar = findViewById(R.id.zoomToolbar)
        pageZoomIn = findViewById(R.id.pageZoomIn)
        pageZoomOut = findViewById(R.id.pageZoomOut)
        pageZoomReset = findViewById(R.id.pageZoomReset)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        findViewById<ImageButton>(R.id.addTabButton).setOnClickListener { toggleTabSlider() }
        
        findViewById<TextView>(R.id.menuHome).setOnClickListener { 
            loadUrl(getString(R.string.default_url))
            drawerLayout.closeDrawers()
        }
        
        // Text Zoom In (Layer 3 - Text zoom)
        menuZoomIn.setOnClickListener { 
            increaseTextZoom()
            drawerLayout.closeDrawers()
        }
        
        // Text Zoom Out (Layer 3 - Text zoom)
        menuZoomOut.setOnClickListener { 
            decreaseTextZoom()
            drawerLayout.closeDrawers()
        }
        
        // Desktop Mode
        menuDesktop.setOnClickListener { 
            toggleDesktopMode()
            drawerLayout.closeDrawers()
        }
        
        findViewById<ImageButton>(R.id.menuBack).setOnClickListener { browserManager.getCurrentTab()?.webView?.goBack() }
        findViewById<ImageButton>(R.id.menuRefresh).setOnClickListener { browserManager.getCurrentTab()?.webView?.reload() }

        // Layer 1: Page Zoom buttons (pinch-to-zoom equivalents)
        pageZoomIn.setOnClickListener { performPageZoomIn() }
        pageZoomOut.setOnClickListener { performPageZoomOut() }
        pageZoomReset.setOnClickListener { performPageZoomReset() }
        
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
    private fun createNewTab() {
        val currentTab = browserManager.getCurrentTab()
        val isDesktopMode = currentTab?.isDesktopMode ?: false
        
        val tab = browserManager.createNewTab().apply {
            this.isDesktopMode = isDesktopMode
        }
        val webView = WebViewFactory.createWebView(this, isDesktopMode).apply { tag = tab.id }
        tab.webView = webView
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible = true
                urlEditText.setText(url ?: "")
                zoomToolbar.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false
                browserManager.getCurrentTab()?.apply {
                    this.url = url ?: ""
                    this.title = view?.title ?: "New Tab"
                }
                
                // Layer 2: Inject viewport fix to neutralize hostile meta tags
                view?.let { WebViewFactory.injectViewportFix(it) }
                
                // Re-apply text zoom after page load
                val savedZoom = prefs.getInt(KEY_TEXT_ZOOM, getDefaultTextZoom())
                view?.settings?.setTextZoom(savedZoom)
                
                if (tabSlider.isVisible) updateTabSlider()
                updateMenuState()
                updatePageZoomButtons()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, progress: Int) {
                progressBar.progress = progress
                progressBar.isVisible = progress < 100
            }
        }
        
        webViewContainer.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        
        // Apply text zoom - use system font scale as default if no saved preference
        val savedZoom = prefs.getInt(KEY_TEXT_ZOOM, getDefaultTextZoom())
        webView.settings.setTextZoom(savedZoom)
        
        showTab(tab.id)
        loadUrl(getString(R.string.default_url))
    }

    private fun showTab(tabId: Int) {
        browserManager.setCurrentTab(tabId)
        tabSlider.isVisible = false
        
        for (i in 0 until webViewContainer.childCount) {
            webViewContainer.getChildAt(i).isVisible = webViewContainer.getChildAt(i).tag == tabId
        }
        
        browserManager.getCurrentTab()?.let { tab ->
            urlEditText.setText(tab.url)
            tab.webView?.let { wv ->
                WebViewFactory.setDesktopMode(wv, tab.isDesktopMode)
            }
        }
        
        updateMenuState()
        updatePageZoomButtons()
        hideKeyboard()
    }

    private fun updateMenuState() {
        val tab = browserManager.getCurrentTab()
        val textZoom = prefs.getInt(KEY_TEXT_ZOOM, getDefaultTextZoom())
        
        // Update desktop mode indicator
        menuDesktop.text = if (tab?.isDesktopMode == true) "Desktop Mode ✓" else "Desktop Mode"
        
        // Update zoom indicators
        menuZoomIn.text = "Zoom In ($textZoom%)"
        menuZoomOut.text = "Zoom Out ($textZoom%)"
    }

    private fun updatePageZoomButtons() {
        val webView = browserManager.getCurrentTab()?.webView ?: return
        
        // Enable/disable based on current zoom level (Layer 1)
        pageZoomIn.isEnabled = webView.canZoomIn()
        pageZoomOut.isEnabled = webView.canZoomOut()
        pageZoomIn.alpha = if (webView.canZoomIn()) 1.0f else 0.4f
        pageZoomOut.alpha = if (webView.canZoomOut()) 1.0f else 0.4f
    }

    private fun closeTab(tabId: Int) {
        val webView = browserManager.getTab(tabId)?.webView
        webView?.let {
            webViewContainer.removeView(it)
            it.destroy()
        }
        browserManager.closeTab(tabId)
        
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
        urlEditText.setText(url)
        tabSlider.isVisible = false
    }

    // Layer 1: Page Zoom (Native pinch-to-zoom equivalent)
    private fun performPageZoomIn() {
        browserManager.getCurrentTab()?.webView?.let { wv ->
            if (wv.canZoomIn()) {
                wv.zoomIn()
                updatePageZoomButtons()
            }
        }
    }

    private fun performPageZoomOut() {
        browserManager.getCurrentTab()?.webView?.let { wv ->
            if (wv.canZoomOut()) {
                wv.zoomOut()
                updatePageZoomButtons()
            }
        }
    }

    private fun performPageZoomReset() {
        browserManager.getCurrentTab()?.webView?.let { wv ->
            wv.setInitialScale(0)
            wv.settings.setTextZoom(100)
            prefs.edit().putInt(KEY_TEXT_ZOOM, 100).apply()
            updateMenuState()
            updatePageZoomButtons()
            showToast("Zoom reset to default")
        }
    }

    // Layer 3: Text Zoom (Accessibility zoom - separate from page zoom)
    private fun increaseTextZoom() {
        val currentZoom = prefs.getInt(KEY_TEXT_ZOOM, getDefaultTextZoom())
        val newZoom = (currentZoom + ZOOM_STEP).coerceAtMost(ZOOM_MAX)
        applyTextZoom(newZoom)
    }

    private fun decreaseTextZoom() {
        val currentZoom = prefs.getInt(KEY_TEXT_ZOOM, getDefaultTextZoom())
        val newZoom = (currentZoom - ZOOM_STEP).coerceAtLeast(ZOOM_MIN)
        applyTextZoom(newZoom)
    }

    private fun applyTextZoom(percent: Int) {
        prefs.edit().putInt(KEY_TEXT_ZOOM, percent).apply()
        
        browserManager.getCurrentTab()?.webView?.let { wv ->
            wv.settings.setTextZoom(percent)
        }
        
        updateMenuState()
        showToast("Text zoom: $percent%")
    }

    private fun toggleDesktopMode() {
        browserManager.getCurrentTab()?.let { tab ->
            tab.isDesktopMode = !tab.isDesktopMode
            tab.webView?.let { wv ->
                WebViewFactory.setDesktopMode(wv, tab.isDesktopMode)
            }
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

    // Get default text zoom - use system font scale
    private fun getDefaultTextZoom(): Int {
        val savedZoom = prefs.getInt(KEY_TEXT_ZOOM, 0)
        return WebViewFactory.getDefaultTextZoom(this, savedZoom)
    }

    override fun onPause() {
        super.onPause()
        browserManager.getCurrentTab()?.webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        browserManager.getCurrentTab()?.webView?.onResume()
        // Restore text zoom when returning to app
        browserManager.getCurrentTab()?.webView?.let { wv ->
            val savedZoom = prefs.getInt(KEY_TEXT_ZOOM, getDefaultTextZoom())
            wv.settings.setTextZoom(savedZoom)
        }
    }

    // Handle configuration changes - especially fontScale for accessibility
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Re-apply text zoom when system font size changes
        val systemFontScale = WebViewFactory.getSystemFontScale(this)
        browserManager.getCurrentTab()?.webView?.let { wv ->
            // Only apply if user hasn't set a custom zoom
            val savedZoom = prefs.getInt(KEY_TEXT_ZOOM, 0)
            if (savedZoom == 0) {
                wv.settings.setTextZoom(systemFontScale)
            }
        }
        
        showToast("System font scale: $systemFontScale%")
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

    override fun onDestroy() {
        browserManager.getAllTabs().forEach { it.webView?.destroy() }
        super.onDestroy()
    }
}
