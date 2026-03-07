package com.example.browser

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsoluteLayout
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
    private lateinit var headerBar: LinearLayout
    
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
    
    data class TabItem(var title: String, var url: String, var isActive: Boolean = false, var faviconColor: Int = 0xFF2196F3.toInt())

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

        // Initial tab
        tabs.add(TabItem("Google", "https://www.google.com", true))

        setupHeaderBar()
        setupUrlBar()
        setupWebView()
        setupBottomNav()

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = android.content.res.ColorStateList.valueOf(0xFF2196F3.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                4
            )
            visibility = View.GONE
        }
        mainContainer.addView(progressBar)

        tabOverlay = View(this).apply {
            setBackgroundColor(0xB3000000.toInt())
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

    private fun setupHeaderBar() {
        headerBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(16, 20, 16, 20)
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Logo/Brand
        val logoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val appTitle = TextView(this).apply {
            text = "Lite"
            textSize = 20f
            setTextColor(0xFF2196F3.toInt())
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        }

        val appSubtitle = TextView(this).apply {
            text = "Browser"
            textSize = 10f
            setTextColor(0xFF9E9E9E.toInt())
        }

        logoContainer.addView(appTitle)
        logoContainer.addView(appSubtitle)

        // Tab counter
        val tabCounter = TextView(this).apply {
            text = "1"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(20, 8, 20, 8)
            gravity = Gravity.CENTER

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 20f
            drawable.setColor(0xFF2196F3.toInt())
            background = drawable
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 16, 0)
            }
            setOnClickListener { showTabSlider() }
        }

        // Menu button
        btnMenu = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_more)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = LinearLayout.LayoutParams(48, 48)
            setOnClickListener { showModernMenu(it) }
        }

        headerBar.addView(logoContainer)
        headerBar.addView(tabCounter)
        headerBar.addView(btnMenu)

        mainContainer.addView(headerBar)
    }

    private fun setupUrlBar() {
        urlBarContainer = RelativeLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            setPadding(16, 8, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Search container with rounded corners
        val searchContainer = RelativeLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                96
            )
            
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 50f
            drawable.setColor(0xFFF5F5F5.toInt())
            setBackgroundDrawable(drawable)
        }

        val searchIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_search)
            setColorFilter(0xFF9E9E9E.toInt())
            layoutParams = RelativeLayout.LayoutParams(48, 48).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginStart = 16
            }
        }

        urlBar = EditText(this).apply {
            hint = "Search or enter URL"
            setHintTextColor(0xFF9E9E9E.toInt())
            background = null
            textSize = 15f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.END_OF, searchIcon.id)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginStart = 8
                marginEnd = 80
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    loadUrl()
                    true
                } else false
            }
        }

        btnRefresh = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = RelativeLayout.LayoutParams(56, 56).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { webView.reload() }
        }

        searchContainer.addView(searchIcon)
        searchContainer.addView(urlBar)
        searchContainer.addView(btnRefresh)

        urlBarContainer.addView(searchContainer)
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
            settings.loadWithOverviewMode = true

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
            }
        }
        mainContainer.addView(webView)
    }

    private fun updateActiveTab(title: String, url: String) {
        tabs.forEach { it.isActive = false }
        if (tabs.isNotEmpty()) {
            val colors = listOf(0xFF2196F3.toInt(), 0xFFE91E63.toInt(), 0xFF4CAF50.toInt(), 
                              0xFFFF9800.toInt(), 0xFF9C27B0.toInt(), 0xFF00BCD4.toInt())
            val colorIndex = (0 until colors.size).random()
            tabs[0] = TabItem(title, url, true, colors[colorIndex])
        }
    }

    private fun setupBottomNav() {
        bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(8, 12, 8, 24)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            // Top shadow
            elevation = 8f
        }

        val navItems = listOf(
            Triple(android.R.drawable.ic_media_previous, "Back") { if (webView.canGoBack()) webView.goBack() },
            Triple(android.R.drawable.ic_media_next, "Forward") { if (webView.canGoForward()) webView.goForward() },
            Triple(android.R.drawable.ic_menu_compass, "Home") { 
                webView.loadUrl("https://www.google.com")
                updateActiveTab("Google", "https://www.google.com")
            },
            Triple(android.R.drawable.ic_menu_add, "Tabs") { showTabSlider() },
            Triple(android.R.drawable.ic_menu_more, "Menu") { showModernMenu(btnMenu) }
        )

        navItems.forEach { (icon, desc, action) ->
            val btn = ImageView(this).apply {
                setImageResource(icon)
                setColorFilter(0xFF616161.toInt())
                layoutParams = LinearLayout.LayoutParams(0, 80, 1f).apply {
                    gravity = Gravity.CENTER
                }
                setOnClickListener { action() }
                contentDescription = desc
            }
            bottomNav.addView(btn)
        }

        mainContainer.addView(bottomNav)
    }

    private fun setupTabContainer() {
        tabContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            visibility = View.GONE
            translationY = 1500f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
        }
        refreshTabContainer()
    }

    private fun refreshTabContainer() {
        tabContainer.removeAllViews()
        
        // Modern header
        val tabHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(24, 32, 24, 32)
            gravity = Gravity.CENTER_VERTICAL
        }

        val tabTitle = TextView(this).apply {
            text = "Tabs"
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tabCount = TextView(this).apply {
            text = "${tabs.size}"
            textSize = 16f
            setTextColor(0xCCFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 24, 0)
            }
        }

        val newTabBtn = TextView(this).apply {
            text = "+ New"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(24, 12, 24, 12)
            
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 24f
            drawable.setColor(0x33FFFFFF.toInt())
            background = drawable
            
            setOnClickListener { addNewTab() }
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(56, 56)
            setOnClickListener { hideTabSlider() }
        }

        tabHeader.addView(tabTitle)
        tabHeader.addView(tabCount)
        tabHeader.addView(newTabBtn)
        tabHeader.addView(closeBtn)

        // Tabs grid
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

        tabs.forEachIndexed { index, tab ->
            val tabCard = createTabCard(index, tab)
            tabsGrid.addView(tabCard)
        }

        tabsScroll.addView(tabsGrid)
        tabContainer.addView(tabHeader)
        tabContainer.addView(tabsScroll)
    }

    private fun createTabCard(index: Int, tab: TabItem): View {
        val card = CardView(this).apply {
            radius = 24f
            cardElevation = 4f
            setCardBackgroundColor(if (tab.isActive) Color.WHITE else Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                180
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            
            // Active indicator
            if (tab.isActive) {
                cardElevation = 8f
            }
        }

        val content = RelativeLayout(this).apply {
            setPadding(20, 16, 20, 16)
        }

        // Favicon with colored circle
        val favicon = View(this).apply {
            layoutParams = RelativeLayout.LayoutParams(56, 56).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(tab.faviconColor)
            setBackgroundDrawable(drawable)
        }

        // Tab info
        val infoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.END_OF, favicon.id)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginStart = 20
                marginEnd = 60
            }
        }

        val title = TextView(this).apply {
            text = tab.title
            textSize = 16f
            setTextColor(0xFF212121.toInt())
            maxLines = 1
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        }

        val url = TextView(this).apply {
            text = tab.url.replace("https://", "").replace("http://", "")
            textSize = 12f
            setTextColor(0xFF9E9E9E.toInt())
            maxLines = 1
        }

        infoContainer.addView(title)
        infoContainer.addView(url)

        // Close button
        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0xFFBDBDBD.toInt())
            layoutParams = RelativeLayout.LayoutParams(48, 48).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener {
                tabs.removeAt(index)
                if (tabs.isEmpty()) {
                    tabs.add(TabItem("Google", "https://www.google.com", true))
                }
                refreshTabContainer()
                showMessage("Tab closed")
            }
        }

        content.addView(favicon)
        content.addView(infoContainer)
        content.addView(closeBtn)
        card.addView(content)

        card.setOnClickListener {
            tabs.forEach { it.isActive = false }
            tab.isActive = true
            webView.loadUrl(tab.url)
            hideTabSlider()
        }

        return card
    }

    private fun addNewTab() {
        val colors = listOf(0xFF2196F3.toInt(), 0xFFE91E63.toInt(), 0xFF4CAF50.toInt(), 
                          0xFFFF9800.toInt(), 0xFF9C27B0.toInt(), 0xFF00BCD4.toInt())
        tabs.add(TabItem("New Tab", "https://www.google.com", true, colors[(tabs.size) % colors.size]))
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
        tabContainer.animate().translationY(1500f).setDuration(300).withEndAction {
            tabContainer.visibility = View.GONE
        }.start()
    }

    @SuppressLint("InflateParams")
    private fun showModernMenu(anchor: View) {
        val menuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
            layoutParams = LinearLayout.LayoutParams(380, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        // Menu header
        val menuHeader = TextView(this@MainActivity).apply {
            text = "Menu"
            textSize = 20f
            setTextColor(0xFF212121.toInt())
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            setPadding(24, 16, 24, 24)
        }
        menuLayout.addView(menuHeader)

        val menuItems = listOf(
            Triple("Dark Mode", "🌙") { toggleDarkMode() },
            Triple("Light Mode", "☀️") { toggleLightMode() },
            Triple("Zoom In", "🔍+") { zoomIn() },
            Triple("Zoom Out", "🔍-") { zoomOut() },
            Triple("Reset Zoom", "↺") { resetZoom() },
            Triple("Desktop Mode", "🖥️") { toggleDesktopMode() },
            Triple("Share", "📤") { sharePage() },
            Triple("Clear Data", "🗑️") { clearData() },
            Triple("About", "ℹ️") { showAbout() }
        )

        menuItems.forEach { (title, icon, action) ->
            val menuItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(24, 20, 24, 20)
            }

            val iconText = TextView(this).apply {
                text = icon
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 20, 0)
                }
            }

            val titleText = TextView(this).apply {
                text = title
                textSize = 15f
                setTextColor(0xFF424242.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            menuItem.addView(iconText)
            menuItem.addView(titleText)
            
            menuItem.setOnClickListener {
                action()
            }
            
            menuLayout.addView(menuItem)
        }

        val popup = PopupWindow(menuLayout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 16f
            setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity, android.R.drawable.edit_text))
            showAtLocation(anchor, Gravity.TOP or Gravity.END, 0, anchor.top)
            animationStyle = android.R.style.Animation_Dialog
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
        val cardColor = if (isDarkMode) 0xFF1E1E1E.toInt() else Color.WHITE
        val textColor = if (isDarkMode) Color.WHITE else 0xFF212121.toInt()
        val hintColor = if (isDarkMode) 0xFF888888.toInt() else 0xFF9E9E9E.toInt()
        val iconColor = if (isDarkMode) Color.WHITE else 0xFF616161.toInt()
        val searchBg = if (isDarkMode) 0xFF2C2C2C.toInt() else 0xFFF5F5F5.toInt()

        mainContainer.setBackgroundColor(bgColor)
        headerBar.setBackgroundColor(cardColor)
        urlBarContainer.setBackgroundColor(cardColor)
        bottomNav.setBackgroundColor(cardColor)
        tabContainer.setBackgroundColor(if (isDarkMode) 0xFF1E1E1E.toInt() else 0xFFF5F5F5.toInt())

        urlBar.setTextColor(textColor)
        urlBar.setHintTextColor(hintColor)

        for (i in 0 until bottomNav.childCount) {
            (bottomNav.getChildAt(i) as? ImageView)?.setColorFilter(iconColor)
        }

        showMessage(if (isDarkMode) "Dark Mode" else "Light Mode")
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

    private fun resetZoom() {
        currentZoom = 100
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.reload()
        showMessage("Zoom reset")
    }

    private fun toggleDesktopMode() {
        isDesktopMode = !isDesktopMode
        
        if (isDesktopMode) {
            webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            showMessage("Desktop Mode ON")
        } else {
            webView.settings.userAgentString = null
            webView.reload()
            showMessage("Desktop Mode OFF")
        }
    }

    private fun clearData() {
        webView.clearHistory()
        webView.clearCache(true)
        webView.clearFormData()
        showMessage("Data cleared")
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
            showMessage("Cannot share")
        }
    }

    private fun showAbout() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("LiteBrowser")
        dialog.setMessage("Version 2.0\n\nA modern lightweight browser\n\nFeatures:\n• Tab Management\n• Dark/Light Mode\n• Zoom Controls\n• Desktop Mode\n• Share Function")
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun loadUrl() {
        var url = urlBar.text.toString().trim()
        if (url.isNotEmpty()) {
            url = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (url.contains(".")) "https://$url" else "https://www.google.com/search?q=$url"
            } else url
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
