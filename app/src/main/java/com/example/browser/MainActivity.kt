package com.example.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var mainContainer: LinearLayout
    private lateinit var bottomNav: LinearLayout
    private lateinit var tabOverlay: View
    private lateinit var tabContainer: LinearLayout
    private lateinit var urlBarContainer: RelativeLayout
    
    private var isDarkMode = false
    private var isDesktopMode = false
    private var currentZoom = 100

    private lateinit var btnBack: ImageView
    private lateinit var btnForward: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var btnTabs: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnRefresh: ImageView
    private lateinit var btnZoomIn: ImageView
    private lateinit var btnZoomOut: ImageView

    private var tabSliderShown = false
    private val tabs = mutableListOf<TabItem>()
    
    data class TabItem(var title: String, var url: String, var isActive: Boolean = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }

    @SuppressLint("SetJavaScriptEnabled", "InflateParams")
    private fun setupUI() {
        val rootLayout = FrameLayout(this)
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        tabs.add(TabItem("Google", "https://www.google.com", true))

        setupTopBar()
        setupWebView()
        setupBottomNav()

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = android.content.res.ColorStateList.valueOf(0xFF2196F3.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                6
            )
            visibility = View.GONE
        }
        mainContainer.addView(progressBar)

        tabOverlay = View(this).apply {
            setBackgroundColor(0xE6000000.toInt())
            visibility = View.GONE
            alpha = 0f
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener { hideTabSlider() }
        }

        setupTabContainer()

        rootLayout.addView(mainContainer)
        rootLayout.addView(tabOverlay)
        rootLayout.addView(tabContainer)

        setContentView(rootLayout)
        webView.loadUrl("https://www.google.com")
    }

    private fun setupTopBar() {
        urlBarContainer = RelativeLayout(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            setPadding(20, 24, 20, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        btnRefresh = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = RelativeLayout.LayoutParams(56, 56).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                marginEnd = 8
            }
            setOnClickListener { webView.reload() }
        }

        btnMenu = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_more)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = RelativeLayout.LayoutParams(56, 56).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }
            setOnClickListener { showModernMenu(it) }
        }

        urlBar = EditText(this).apply {
            hint = "Search or enter URL"
            setHintTextColor(0xFF999999.toInt())
            setBackgroundResource(android.R.drawable.edit_text)
            setPadding(32, 20, 32, 20)
            textSize = 15f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.ALIGN_PARENT_END)
                marginStart = 64
                marginEnd = 64
            }
            setOnEditorActionListener { _, _, _ ->
                loadUrl()
                true
            }
        }

        val homeIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_compass)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = RelativeLayout.LayoutParams(56, 56)
            setOnClickListener { 
                webView.loadUrl("https://www.google.com")
                updateActiveTab("Google", "https://www.google.com")
            }
        }

        urlBarContainer.addView(homeIcon)
        urlBarContainer.addView(urlBar)
        urlBarContainer.addView(btnRefresh)
        urlBarContainer.addView(btnMenu)
        mainContainer.addView(urlBarContainer)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.setSupportZoom(true)
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    progressBar.visibility = View.VISIBLE
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    urlBar.setText(url)
                    val title = view?.title ?: "Untitled"
                    updateActiveTab(title, url ?: "")
                    super.onPageFinished(view, url)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                }
            }
        }
        mainContainer.addView(webView)
    }

    private fun updateActiveTab(title: String, url: String) {
        tabs.forEach { it.isActive = false }
        if (tabs.isNotEmpty()) {
            tabs[0] = TabItem(title, url, true)
        }
    }

    private fun setupBottomNav() {
        bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            setPadding(16, 16, 16, 24)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        btnBack = createNavIcon(android.R.drawable.ic_media_previous, "Back") {
            if (webView.canGoBack()) webView.goBack()
        }

        btnForward = createNavIcon(android.R.drawable.ic_media_next, "Forward") {
            if (webView.canGoForward()) webView.goForward()
        }

        btnZoomOut = createNavIcon(android.R.drawable.ic_delete, "Zoom Out") {
            zoomOut()
        }

        btnZoomIn = createNavIcon(android.R.drawable.ic_input_add, "Zoom In") {
            zoomIn()
        }

        btnTabs = createNavIcon(android.R.drawable.ic_menu_add, "Tabs") {
            showTabSlider()
        }

        btnHome = createNavIcon(android.R.drawable.ic_menu_compass, "Home") {
            webView.loadUrl("https://www.google.com")
            updateActiveTab("Google", "https://www.google.com")
        }

        bottomNav.addView(btnBack)
        bottomNav.addView(btnForward)
        bottomNav.addView(btnZoomOut)
        bottomNav.addView(btnZoomIn)
        bottomNav.addView(btnTabs)
        bottomNav.addView(btnHome)

        mainContainer.addView(bottomNav)
    }

    private fun createNavIcon(icon: Int, contentDesc: String, onClick: () -> Unit): ImageView {
        return ImageView(this).apply {
            setImageResource(icon)
            setColorFilter(0xFF333333.toInt())
            layoutParams = LinearLayout.LayoutParams(0, 80, 1f).apply {
                gravity = Gravity.CENTER
            }
            setOnClickListener { onClick() }
            contentDescription = contentDesc
        }
    }

    private fun zoomIn() {
        currentZoom = (currentZoom + 25).coerceAtMost(300)
        webView.zoomIn()
        showMessage("Zoom: $currentZoom%")
    }

    private fun zoomOut() {
        currentZoom = (currentZoom - 25).coerceAtLeast(50)
        webView.zoomOut()
        showMessage("Zoom: $currentZoom%")
    }

    private fun setupTabContainer() {
        tabContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            visibility = View.GONE
            translationY = 1200f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
        }

        val tabHeader = LinearLayout(this).apply {
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(32, 32, 32, 32)
        }

        val tabCount = TextView(this).apply {
            text = "Tabs (${tabs.size})"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val newTabBtn = TextView(this).apply {
            text = "+ New"
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { addNewTab() }
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(56, 56)
            setOnClickListener { hideTabSlider() }
        }

        tabHeader.addView(tabCount)
        tabHeader.addView(newTabBtn)
        tabHeader.addView(closeBtn)

        val tabsScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val tabsGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        refreshTabViews(tabsGrid)

        tabsScroll.addView(tabsGrid)

        tabContainer.addView(tabHeader)
        tabContainer.addView(tabsScroll)
    }

    private fun refreshTabViews(tabsGrid: LinearLayout) {
        tabsGrid.removeAllViews()
        
        tabs.forEachIndexed { index, tab ->
            val tabCard = createTabCard(index, tab)
            tabsGrid.addView(tabCard)
        }
    }

    private fun createTabCard(index: Int, tab: TabItem): View {
        val card = CardView(this).apply {
            radius = 20f
            cardElevation = 6f
            setCardBackgroundColor(if (tab.isActive) 0xFFE3F2FD.toInt() else 0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                160
            ).apply {
                setMargins(12, 12, 12, 12)
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 16, 20, 16)
            gravity = Gravity.CENTER_VERTICAL
        }

        val favicon = View(this).apply {
            setBackgroundColor(if (tab.isActive) 0xFF2196F3.toInt() else 0xFF9E9E9E.toInt())
            layoutParams = LinearLayout.LayoutParams(48, 48)
        }

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(16, 0, 0, 0)
            }
        }

        val title = TextView(this).apply {
            text = tab.title
            textSize = 16f
            setTextColor(0xFF212121.toInt())
            maxLines = 1
        }

        val url = TextView(this).apply {
            text = tab.url
            textSize = 12f
            setTextColor(0xFF757575.toInt())
            maxLines = 1
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0xFF757575.toInt())
            layoutParams = LinearLayout.LayoutParams(40, 40)
            setOnClickListener {
                tabs.removeAt(index)
                if (tabs.isEmpty()) {
                    tabs.add(TabItem("Google", "https://www.google.com", true))
                }
                if (tab.isActive && tabs.isNotEmpty()) {
                    webView.loadUrl(tabs[0].url)
                }
                refreshTabContainer()
            }
        }

        info.addView(title)
        info.addView(url)
        content.addView(favicon)
        content.addView(info)
        content.addView(closeBtn)
        card.addView(content)

        card.setOnClickListener {
            tabs.forEach { it.isActive = false }
            tab.isActive = true
            webView.loadUrl(tab.url)
            hideTabSlider()
            refreshTabContainer()
        }

        return card
    }

    private fun refreshTabContainer() {
        tabContainer.removeAllViews()
        
        val tabHeader = LinearLayout(this).apply {
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(32, 32, 32, 32)
        }

        val tabCount = TextView(this).apply {
            text = "Tabs (${tabs.size})"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val newTabBtn = TextView(this).apply {
            text = "+ New"
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { addNewTab() }
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(56, 56)
            setOnClickListener { hideTabSlider() }
        }

        tabHeader.addView(tabCount)
        tabHeader.addView(newTabBtn)
        tabHeader.addView(closeBtn)

        val tabsScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val tabsGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        refreshTabViews(tabsGrid)
        tabsScroll.addView(tabsGrid)

        tabContainer.addView(tabHeader)
        tabContainer.addView(tabsScroll)
    }

    private fun addNewTab() {
        tabs.add(TabItem("New Tab", "https://www.google.com", true))
        webView.loadUrl("https://www.google.com")
        hideTabSlider()
        refreshTabContainer()
        showMessage("New tab created")
    }

    private fun showTabSlider() {
        tabSliderShown = true
        refreshTabContainer()
        tabOverlay.visibility = View.VISIBLE
        tabContainer.visibility = View.VISIBLE
        tabOverlay.animate().alpha(1f).setDuration(200).start()
        tabContainer.animate().translationY(0f).setDuration(300).start()
    }

    private fun hideTabSlider() {
        tabSliderShown = false
        tabOverlay.animate().alpha(0f).setDuration(200).withEndAction {
            tabOverlay.visibility = View.GONE
        }.start()
        tabContainer.animate().translationY(1200f).setDuration(300).withEndAction {
            tabContainer.visibility = View.GONE
        }.start()
    }

    @SuppressLint("InflateParams")
    private fun showModernMenu(anchor: View) {
        val menuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(8, 8, 8, 8)
        }

        val menuItems = listOf(
            Pair("Dark Mode") { toggleDarkMode() },
            Pair("Light Mode") { toggleLightMode() },
            Pair("Zoom In (+)") { zoomIn() },
            Pair("Zoom Out (-)") { zoomOut() },
            Pair("Reset Zoom") { resetZoom() },
            Pair("Desktop Mode: ${if (isDesktopMode) "ON" else "OFF"}") { toggleDesktopMode() },
            Pair("Clear History") { clearHistory() },
            Pair("Share Page") { sharePage() },
            Pair("About") { showAbout() }
        )

        menuItems.forEach { (title, action) ->
            val menuItem = TextView(this).apply {
                text = title
                textSize = 16f
                setTextColor(0xFF212121.toInt())
                setPadding(32, 24, 32, 24)
                setBackgroundColor(0x00000000.toInt())
            }
            menuItem.setOnClickListener {
                action()
            }
            menuLayout.addView(menuItem)
        }

        val popup = PopupWindow(menuLayout, 420, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 24f
            setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity, android.R.drawable.edit_text))
            showAtLocation(anchor, Gravity.TOP or Gravity.END, 0, anchor.top - 10)
        }
    }

    private fun toggleDarkMode() {
        isDarkMode = true
        applyTheme()
    }

    private fun toggleLightMode() {
        isDarkMode = false
        applyTheme()
    }

    private fun applyTheme() {
        val bgColor = if (isDarkMode) 0xFF121212.toInt() else 0xFFF5F5F5.toInt()
        val cardColor = if (isDarkMode) 0xFF1E1E1E.toInt() else 0xFFFFFFFF.toInt()
        val textColor = if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF212121.toInt()
        val hintColor = if (isDarkMode) 0xFF888888.toInt() else 0xFF999999.toInt()
        val iconColor = if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF333333.toInt()

        mainContainer.setBackgroundColor(bgColor)
        urlBarContainer.setBackgroundColor(cardColor)
        bottomNav.setBackgroundColor(cardColor)
        tabContainer.setBackgroundColor(cardColor)

        urlBar.setTextColor(textColor)
        urlBar.setHintTextColor(hintColor)

        btnBack.setColorFilter(iconColor)
        btnForward.setColorFilter(iconColor)
        btnHome.setColorFilter(iconColor)
        btnTabs.setColorFilter(iconColor)
        btnMenu.setColorFilter(iconColor)
        btnRefresh.setColorFilter(iconColor)
        btnZoomIn.setColorFilter(iconColor)
        btnZoomOut.setColorFilter(iconColor)

        showMessage(if (isDarkMode) "Dark Mode" else "Light Mode")
    }

    private fun resetZoom() {
        currentZoom = 100
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.reload()
        showMessage("Zoom reset to 100%")
    }

    private fun toggleDesktopMode() {
        isDesktopMode = !isDesktopMode
        
        if (isDesktopMode) {
            webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            showMessage("Desktop Mode ON")
        } else {
            webView.settings.userAgentString = null
            showMessage("Desktop Mode OFF")
        }
        
        webView.reload()
    }

    private fun clearHistory() {
        webView.clearHistory()
        webView.clearCache(true)
        showMessage("History cleared")
    }

    private fun sharePage() {
        try {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, urlBar.text.toString())
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            showMessage("Cannot share this page")
        }
    }

    private fun showAbout() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("LiteBrowser")
        dialog.setMessage("Version 2.0\n\nA lightweight browser built with Kotlin\n\nFeatures:\n- Tab Management\n- Dark/Light Mode\n- Zoom Controls\n- Desktop Mode\n- Share Function")
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun loadUrl() {
        var url = urlBar.text.toString().trim()
        if (url.isNotEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = if (url.contains(".")) {
                    "https://$url"
                } else {
                    "https://www.google.com/search?q=$url"
                }
            }
            webView.loadUrl(url)
            updateActiveTab("Loading...", url)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            tabSliderShown -> hideTabSlider()
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
