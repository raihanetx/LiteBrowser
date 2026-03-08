package com.browser.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.CookieManager
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.browser.app.R
import com.browser.app.model.BrowserManager
import com.browser.app.model.BrowserTab
import com.browser.app.utils.BrowserPreferences
import com.browser.app.utils.DownloadUtils
import com.browser.app.webview.WebViewFactory

class MainActivity : AppCompatActivity() {

    // ========== Constants (ZOOM - DON'T TOUCH) ==========
    companion object {
        private const val ZOOM_MIN = 50
        private const val ZOOM_MAX = 150
        private const val ZOOM_DEFAULT = 100
    }

    // ========== Smart Preferences ==========
    private lateinit var prefs: BrowserPreferences

    // ========== Browser Manager ==========
    private val browserManager = BrowserManager()

    // ========== Views ==========
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var webViewContainer: FrameLayout
    private lateinit var tabSlider: LinearLayout
    private lateinit var tabsContainer: LinearLayout
    private lateinit var menuDesktop: TextView
    private lateinit var menuIncognito: TextView
    private lateinit var menuClearData: TextView
    private lateinit var menuCookies: TextView
    private lateinit var menuDownloads: TextView
    private lateinit var menuHistory: TextView
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var zoomPercentage: TextView

    // ========== Lifecycle ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Smart initialization - use preferences manager
        prefs = BrowserPreferences(this)
        
        initViews()
        setupListeners()
        
        if (!restoreTabsSmart()) {
            createNewTab()
        }
    }

    // ========== Initialization ==========
    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        urlEditText = findViewById(R.id.urlEditText)
        progressBar = findViewById(R.id.progressBar)
        webViewContainer = findViewById(R.id.webViewContainer)
        tabSlider = findViewById(R.id.tabSlider)
        tabsContainer = findViewById(R.id.tabsContainer)
        menuDesktop = findViewById(R.id.menuDesktop)
        menuIncognito = findViewById(R.id.menuIncognito)
        menuClearData = findViewById(R.id.menuClearData)
        menuCookies = findViewById(R.id.menuCookies)
        menuDownloads = findViewById(R.id.menuDownloads)
        menuHistory = findViewById(R.id.menuHistory)
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
        
        // Desktop Mode toggle - DON'T TOUCH
        menuDesktop.setOnClickListener { 
            toggleDesktopMode()
            drawerLayout.closeDrawers()
        }

        // Incognito Mode - SMART
        menuIncognito.setOnClickListener {
            toggleIncognitoModeSmart()
            drawerLayout.closeDrawers()
        }

        // Clear Data - SMART
        menuCookies.setOnClickListener {
            showCookiesDialog()
            drawerLayout.closeDrawers()
        }

        menuDownloads.setOnClickListener {
            openDownloadsFolder()
            drawerLayout.closeDrawers()
        }

        menuHistory.setOnClickListener {
            showHistoryDialog()
            drawerLayout.closeDrawers()
        }

        menuClearData.setOnClickListener {
            showClearDataDialog()
            drawerLayout.closeDrawers()
        }
        
        // Zoom SeekBar - DON'T TOUCH
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

    // ========== Tab Management ==========
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

    // ========== Create Tab - DON'T TOUCH ZOOM/DESKTOP ==========
    @SuppressLint("SetJavaScriptEnabled")
    private fun createNewTab(loadUrlNow: Boolean = true) {
        val isDesktopMode = prefs.isDesktopMode
        val isIncognito = prefs.isIncognitoMode
        
        val tab = browserManager.createNewTab().apply {
            this.isDesktopMode = isDesktopMode
            this.isIncognito = isIncognito
            this.url = ""
            this.title = "New Tab"
        }

        val webView = WebViewFactory.createWebView(this, isDesktopMode, isIncognito).apply { tag = tab.id }
        
        // SMART: Set download listener
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val success = DownloadUtils.download(this, url, userAgent)
            Toast.makeText(this, if (success) "Download started" else "Download failed", Toast.LENGTH_SHORT).show()
        }
        
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
                
                // DON'T TOUCH - ZOOM
                val savedZoom = prefs.pageZoom
                applyPageZoomJS(view, savedZoom)
                
                // Save to history
                if (!url.isNullOrEmpty()) {
                    val browserDb = com.browser.app.utils.BrowserDatabase(this@MainActivity)
                    browserDb.addHistory(url, view?.title ?: "")
                }
                
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
        
        showTab(tab.id)
        
        if (loadUrlNow) {
            loadUrl(getString(R.string.default_url))
        }
    }

    // ========== Show Tab - DON'T TOUCH ==========
    private fun showTab(tabId: Int) {
        browserManager.setCurrentTab(tabId)
        tabSlider.isVisible = false
        
        for (i in 0 until webViewContainer.childCount) {
            webViewContainer.getChildAt(i).isVisible = webViewContainer.getChildAt(i).tag == tabId
        }
        
        browserManager.getCurrentTab()?.let { tab ->
            urlEditText.setText(tab.url)
            tab.webView?.let { 
                // DON'T TOUCH - ZOOM
                applyPageZoomJS(it, prefs.pageZoom)
            }
        }
        
        updateMenuState()
        hideKeyboard()
    }

    // ========== Update Menu State ==========
    private fun updateMenuState() {
        // DON'T TOUCH - Desktop Mode
        menuDesktop.text = if (prefs.isDesktopMode) "Desktop Mode ✓" else "Desktop Mode"
        
        // SMART - Incognito
        menuIncognito.text = if (prefs.isIncognitoMode) "Incognito Mode ✓" else "Incognito Mode"
        
        // DON'T TOUCH - Zoom
        zoomSeekBar.progress = prefs.pageZoom
        zoomPercentage.text = "${prefs.pageZoom}%"
    }

    // ========== Close Tab ==========
    private fun closeTab(tabId: Int) {
        val tab = browserManager.getTab(tabId)
        tab?.webView?.let { wv ->
            webViewContainer.removeView(wv)
            wv.destroy()
        }
        
        browserManager.closeTab(tabId)
        saveTabsSmart()
        
        if (browserManager.getAllTabs().isEmpty()) {
            createNewTab()
        } else {
            browserManager.getCurrentTab()?.let { showTab(it.id) }
        }
    }

    // ========== Load URL ==========
    private fun loadUrl(input: String) {
        hideKeyboard()
        
        if (browserManager.getCurrentTab() == null) {
            createNewTab()
        }
        
        val url = input.trim().let {
            when {
                it.startsWith("http://") || it.startsWith("https://") -> it
                it.contains(".") && !it.contains(" ") -> "https://$it"
                else -> "https://lite.duckduckgo.com/lite/?q=$it"
            }
        }
        
        browserManager.getCurrentTab()?.apply {
            webView?.loadUrl(url)
            this.url = url
        }
        urlEditText.setText(url)
        tabSlider.isVisible = false
    }

    // ========== NATIVE ZOOM - No more JavaScript zoom! ==========
    private fun applyPageZoom(percent: Int) {
        val zoomPercent = percent.coerceIn(ZOOM_MIN, ZOOM_MAX)
        prefs.pageZoom = zoomPercent
        
        browserManager.getCurrentTab()?.webView?.let { wv ->
            // Use native zoom controls instead of JavaScript
            applyNativeZoom(wv, zoomPercent)
        }
        
        zoomPercentage.text = "$zoomPercent%"
    }

    private fun applyNativeZoom(webView: WebView, percent: Int) {
        // Convert percentage to scale
        val scale = percent / 100f
        
        // Use built-in zoom with setInitialScale
        webView.settings.apply {
            // Set initial scale based on percentage (multiply by 100 for setInitialScale)
            val initialScale = (percent * 100 / 100).coerceIn(25, 400)
            webView.setInitialScale(initialScale)
            
            // Let pinch-to-zoom work naturally
            builtInZoomControls = true
            setSupportZoom(true)
        }
    }

    // Keep applyPageZoomJS for viewport fix only (not for zoom control)
    private fun applyPageZoomJS(webView: WebView?, percent: Int) {
        // Only inject viewport fix for proper rendering - NOT for zoom
        webView?.let { wv ->
            // Just ensure viewport is properly set
            wv.settings.useWideViewPort = true
            wv.settings.loadWithOverviewMode = true
        }
    }

    // ========== DESKTOP MODE - Fixed ==========
    private fun toggleDesktopMode() {
        prefs.isDesktopMode = !prefs.isDesktopMode
        
        browserManager.getAllTabs().forEach { tab ->
            tab.isDesktopMode = prefs.isDesktopMode
            tab.webView?.let { wv ->
                // Update user agent
                wv.settings.userAgentString = if (prefs.isDesktopMode) {
                    WebViewFactory.DESKTOP_UA
                } else {
                    WebViewFactory.MOBILE_UA
                }
                
                // Reset zoom to avoid half-screen - this is the key fix!
                WebViewFactory.resetZoom(wv)
                
                // Reload to apply changes
                if (!wv.url.isNullOrEmpty()) {
                    wv.reload()
                }
            }
        }
        
        saveTabsSmart()
        updateMenuState()
        showToast(if (prefs.isDesktopMode) "Desktop Mode ON" else "Desktop Mode OFF")
    }

    // ========== SMART - Incognito Mode ==========
    private fun toggleIncognitoModeSmart() {
        prefs.isIncognitoMode = !prefs.isIncognitoMode
        
        if (prefs.isIncognitoMode) {
            // Clear all data when entering incognito
            WebViewFactory.clearAllCookies()
            browserManager.getAllTabs().forEach { tab ->
                tab.webView?.clearCache(true)
                tab.webView?.clearHistory()
                tab.webView?.clearFormData()
            }
            showToast("Incognito Mode ON")
        }
        
        // Reload all tabs
        browserManager.getAllTabs().forEach { tab ->
            tab.isIncognito = prefs.isIncognitoMode
            tab.webView?.let { wv ->
                wv.settings.cacheMode = if (prefs.isIncognitoMode) {
                    android.webkit.WebSettings.LOAD_NO_CACHE
                } else {
                    android.webkit.WebSettings.LOAD_DEFAULT
                }
                if (!wv.url.isNullOrEmpty()) {
                    wv.reload()
                }
            }
        }
        
        saveTabsSmart()
        updateMenuState()
        
        if (!prefs.isIncognitoMode) {
            showToast("Incognito Mode OFF")
        }
    }

    // ========== SMART - Clear Data ==========
    private fun showClearDataDialog() {
        val options = arrayOf("Clear Cookies", "Clear Cache", "Clear History", "Clear All Data")
        
        AlertDialog.Builder(this)
            .setTitle("Clear Data")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> clearCookiesSmart()
                    1 -> clearCacheSmart()
                    2 -> clearHistorySmart()
                    3 -> clearAllDataSmart()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCookiesDialog() {
        val options = arrayOf(
            "📊 View All Cookies",
            "🛡️ Third-Party Cookies: ${if (prefs.blockThirdPartyCookies) "BLOCKED" else "ALLOWED"}",
            "🚫 Block Specific Sites",
            "✅ Allow Specific Sites",
            "🗑️ Clear All Cookies"
        )
        
        AlertDialog.Builder(this)
            .setTitle("🍪 Cookie Manager")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAllCookies()
                    1 -> toggleThirdPartyCookies()
                    2 -> showBlockSitesDialog()
                    3 -> showAllowSitesDialog()
                    4 -> clearAllCookies()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showAllCookies() {
        try {
            val cookieManager = CookieManager.getInstance()
            // Show cookie info dialog
            AlertDialog.Builder(this)
                .setTitle("📊 Cookie Information")
                .setMessage("""
                    LiteBrowser Cookie Management:
                    
                    • Session cookies are kept while app is open
                    • Third-party cookies can be blocked for privacy
                    • You can block specific websites
                    • All cookies are cleared when you select "Clear Data"
                    
                    Current Settings:
                    • Third-Party Cookies: ${if (prefs.blockThirdPartyCookies) "BLOCKED 🚫" else "ALLOWED ✅"}
                    • Blocked Sites: ${prefs.getBlockedSites().size} sites
                    • Allowed Sites: ${prefs.getAllowedSites().size} sites
                """.trimIndent())
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            showToast("Cannot access cookies")
        }
    }

    private fun toggleThirdPartyCookies() {
        prefs.blockThirdPartyCookies = !prefs.blockThirdPartyCookies
        
        // Reload all tabs to apply the change
        browserManager.getAllTabs().forEach { tab ->
            tab.webView?.let { wv ->
                try {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(wv, !prefs.blockThirdPartyCookies)
                } catch (e: Exception) {
                    // Ignore
                }
                if (!wv.url.isNullOrEmpty()) {
                    wv.reload()
                }
            }
        }
        
        showToast(if (prefs.blockThirdPartyCookies) "Third-party cookies BLOCKED" else "Third-party cookies ALLOWED")
    }

    private fun showBlockSitesDialog() {
        val blockableDomains = WebViewFactory.getBlockableDomains()
        val currentBlocked = prefs.getBlockedSites().toMutableSet()
        
        val items = blockableDomains.map { domain ->
            if (currentBlocked.contains(domain)) "🚫 $domain" else "⚪ $domain"
        }.toTypedArray()
        
        val selected = BooleanArray(blockableDomains.size) { currentBlocked.contains(blockableDomains[it]) }
        
        AlertDialog.Builder(this)
            .setTitle("🚫 Block Cookie Sites")
            .setMultiChoiceItems(items, selected) { _, which, isChecked ->
                if (isChecked) {
                    currentBlocked.add(blockableDomains[which])
                } else {
                    currentBlocked.remove(blockableDomains[which])
                }
            }
            .setPositiveButton("Save") { _, _ ->
                prefs.saveBlockedSites(currentBlocked)
                showToast("Blocked sites updated")
                // Reload to apply
                browserManager.getAllTabs().forEach { it.webView?.reload() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAllowSitesDialog() {
        val commonSites = listOf(
            "google.com",
            "facebook.com",
            "youtube.com",
            "twitter.com",
            "instagram.com",
            "reddit.com",
            "wikipedia.org",
            "amazon.com",
            "netflix.com",
            "linkedin.com"
        )
        
        val currentAllowed = prefs.getAllowedSites().toMutableSet()
        
        val items = commonSites.map { domain ->
            if (currentAllowed.contains(domain)) "✅ $domain" else "⚪ $domain"
        }.toTypedArray()
        
        val selected = BooleanArray(commonSites.size) { currentAllowed.contains(commonSites[it]) }
        
        AlertDialog.Builder(this)
            .setTitle("✅ Allow Cookie Sites")
            .setMultiChoiceItems(items, selected) { _, which, isChecked ->
                if (isChecked) {
                    currentAllowed.add(commonSites[which])
                } else {
                    currentAllowed.remove(commonSites[which])
                }
            }
            .setPositiveButton("Save") { _, _ ->
                prefs.saveAllowedSites(currentAllowed)
                showToast("Allowed sites updated")
                browserManager.getAllTabs().forEach { it.webView?.reload() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllCookies() {
        WebViewFactory.clearAllCookies()
        showToast("All cookies cleared")
    }

    private fun showHistoryDialog() {
        val browserDb = com.browser.app.utils.BrowserDatabase(this@MainActivity)
        val historyList = browserDb.getAllHistory()
        
        if (historyList.isEmpty()) {
            showToast("No history")
            return
        }
        
        val items = historyList.take(20).map { "${it.title}\n${it.url}" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("History (${historyList.size} items)")
            .setItems(items) { _, which ->
                val item = historyList[which]
                loadUrl(item.url)
            }
            .setPositiveButton("Clear All") { _, _ ->
                browserDb.clearAllHistory()
                showToast("History cleared")
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openDownloadsFolder() {
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(android.net.Uri.fromFile(downloadDir), "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Open Downloads app to view files")
        }
    }

    private fun clearCookiesSmart() {
        WebViewFactory.clearAllCookies()
        showToast("Cookies cleared")
    }

    private fun clearCacheSmart() {
        browserManager.getAllTabs().forEach { it.webView?.clearCache(true) }
        showToast("Cache cleared")
    }

    private fun clearHistorySmart() {
        browserManager.getAllTabs().forEach { it.webView?.clearHistory() }
        try {
            val browserDb = com.browser.app.utils.BrowserDatabase(this@MainActivity)
            browserDb.clearAllHistory()
        } catch (e: Exception) {
        }
        showToast("History cleared")
    }

    private fun clearAllDataSmart() {
        WebViewFactory.clearAllCookies()
        browserManager.getAllTabs().forEach { tab ->
            tab.webView?.let {
                it.clearCache(true)
                it.clearHistory()
                it.clearFormData()
            }
        }
        try {
            val browserDb = com.browser.app.utils.BrowserDatabase(this@MainActivity)
            browserDb.clearAllHistory()
        } catch (e: Exception) {
        }
        prefs.clearAllData()
        showToast("All data cleared")
        browserManager.getCurrentTab()?.webView?.reload()
    }

    // ========== SMART - Tab Persistence ==========
    private fun saveTabsSmart() {
        if (prefs.isIncognitoMode) return
        
        val tabs = browserManager.getAllTabs().map { tab ->
            BrowserPreferences.TabData(
                id = tab.id,
                url = tab.url,
                title = tab.title,
                isDesktopMode = tab.isDesktopMode,
                isSelected = tab.isSelected
            )
        }
        
        val currentId = browserManager.getCurrentTab()?.id ?: -1
        prefs.saveTabs(tabs, currentId)
    }

    private fun restoreTabsSmart(): Boolean {
        if (prefs.isIncognitoMode) return false
        
        val savedTabs = prefs.restoreTabs() ?: return false
        val savedCurrentTabId = prefs.getSavedCurrentTabId()
        
        webViewContainer.removeAllViews()
        browserManager.clearAllTabs()
        
        savedTabs.forEach { tabData ->
            val tab = BrowserTab(
                id = tabData.id,
                url = tabData.url,
                title = tabData.title,
                isDesktopMode = tabData.isDesktopMode,
                isSelected = tabData.isSelected,
                isIncognito = prefs.isIncognitoMode
            )
            
            val webView = WebViewFactory.createWebView(this, tab.isDesktopMode, tab.isIncognito).apply { tag = tab.id }
            
            webView.setDownloadListener { url, userAgent, _, _, _ ->
                val success = DownloadUtils.download(this, url, userAgent)
                Toast.makeText(this, if (success) "Download started" else "Download failed", Toast.LENGTH_SHORT).show()
            }
            
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
                    applyPageZoomJS(view, prefs.pageZoom)
                    
                    // Save to history
                    if (!url.isNullOrEmpty()) {
                        try {
                    val browserDb = com.browser.app.utils.BrowserDatabase(this@MainActivity)
                            browserDb.addHistory(url, view?.title ?: "")
                        } catch (e: Exception) {
                            // Ignore history errors
                        }
                    }
                    
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
        
        val currentTab = if (savedCurrentTabId >= 0) browserManager.getTab(savedCurrentTabId) else browserManager.getCurrentTab()
        currentTab?.let { 
            showTab(it.id) 
            if (it.url.isNotEmpty()) {
                it.webView?.loadUrl(it.url)
            }
        }
        
        updateMenuState()
        return true
    }

    // ========== Utilities ==========
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // ========== Lifecycle ==========
    override fun onPause() {
        super.onPause()
        browserManager.getCurrentTab()?.webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        browserManager.getCurrentTab()?.webView?.onResume()
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
        if (!prefs.isIncognitoMode) {
            saveTabsSmart()
        }
    }

    override fun onDestroy() {
        if (!prefs.isIncognitoMode) {
            saveTabsSmart()
        }
        browserManager.getAllTabs().forEach { it.webView?.destroy() }
        super.onDestroy()
    }
}
