package com.example.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
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

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var mainContainer: LinearLayout
    private lateinit var bottomNav: LinearLayout
    private lateinit var tabOverlay: View
    private lateinit var tabContainer: LinearLayout
    private lateinit var urlSearchContainer: RelativeLayout
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

    private var tabSliderShown = false
    private val tabs = mutableListOf<TabItem>()
    
    data class TabItem(
        var title: String, 
        var url: String, 
        var isActive: Boolean = false, 
        var faviconColor: Int = Color.parseColor("#2196F3")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }

    @SuppressLint("SetJavaScriptEnabled", "InflateParams")
    private fun setupUI() {
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.parseColor("#F5F5F5"))
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        tabs.add(TabItem("Google", "https://www.google.com", true, Color.parseColor("#2196F3")))

        setupHeaderBar()
        setupSearchBar()
        setupWebView()
        setupBottomNav()

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                3
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
            setPadding(20, 16, 20, 12)
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val logoText = TextView(this).apply {
            text = "Lite"
            textSize = 22f
            setTextColor(Color.parseColor("#2196F3"))
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        }

        val tabCounter = TextView(this).apply {
            text = "1"
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(16, 6, 16, 6)
            gravity = Gravity.CENTER

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 12f
            drawable.setColor(Color.parseColor("#2196F3"))
            background = drawable
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 8, 0)
            }
            setOnClickListener { showTabSlider() }
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }

        btnMenu = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_more)
            setColorFilter(Color.parseColor("#757575"))
            layoutParams = LinearLayout.LayoutParams(44, 44)
            setOnClickListener { showModernMenu(it) }
        }

        headerBar.addView(logoText)
        headerBar.addView(tabCounter)
        headerBar.addView(spacer)
        headerBar.addView(btnMenu)

        mainContainer.addView(headerBar)
    }

    private fun setupSearchBar() {
        urlSearchContainer = RelativeLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            setPadding(16, 8, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val searchBox = RelativeLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                88
            )
            
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 44f
            drawable.setColor(Color.parseColor("#F5F5F5"))
            setBackgroundDrawable(drawable)
        }

        val searchIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_search)
            setColorFilter(Color.parseColor("#9E9E9E"))
            layoutParams = RelativeLayout.LayoutParams(44, 44).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginStart = 12
            }
        }

        urlBar = EditText(this).apply {
            hint = "Search or enter URL"
            setHintTextColor(Color.parseColor("#9E9E9E"))
            background = null
            textSize = 15f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.END_OF, searchIcon.id)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginStart = 4
                marginEnd = 56
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
            setColorFilter(Color.parseColor("#2196F3"))
            layoutParams = RelativeLayout.LayoutParams(48, 48).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginEnd = 4
            }
            setOnClickListener { webView.reload() }
        }

        searchBox.addView(searchIcon)
        searchBox.addView(urlBar)
        searchBox.addView(btnRefresh)

        urlSearchContainer.addView(searchBox)
        mainContainer.addView(urlSearchContainer)
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
            settings.allowFileAccess = true
            settings.allowContentAccess = true

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
            val colors = listOf(
                Color.parseColor("#2196F3"), Color.parseColor("#E91E63"), 
                Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0"), Color.parseColor("#00BCD4")
            )
            tabs[0] = TabItem(title, url, true, colors[kotlin.math.abs(url.hashCode()) % colors.size])
        }
    }

    private fun setupBottomNav() {
        bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(12, 8, 12, 16)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
                setColorFilter(Color.parseColor("#616161"))
                layoutParams = LinearLayout.LayoutParams(0, 72, 1f).apply {
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
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            visibility = View.GONE
            translationY = 2000f
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
        
        val tabHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#2196F3"))
            setPadding(24, 28, 24, 28)
            gravity = Gravity.CENTER_VERTICAL
        }

        val tabTitle = TextView(this).apply {
            text = "Tabs"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val newTabBtn = TextView(this).apply {
            text = "+ New"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(20, 10, 20, 10)

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 20f
            drawable.setColor(0x33FFFFFF)
            background = drawable
            
            setOnClickListener { addNewTab() }
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(48, 48)
            setOnClickListener { hideTabSlider() }
        }

        tabHeader.addView(tabTitle)
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
            setPadding(12, 12, 12, 12)
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
            radius = 20f
            cardElevation = if (tab.isActive) 8f else 3f
            setCardBackgroundColor(if (tab.isActive) Color.WHITE else Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                160
            ).apply {
                setMargins(4, 6, 4, 6)
            }
        }

        val content = RelativeLayout(this).apply {
            setPadding(16, 12, 16, 12)
        }

        val favicon = View(this).apply {
            layoutParams = RelativeLayout.LayoutParams(48, 48).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(tab.faviconColor)
            setBackgroundDrawable(drawable)
        }

        val infoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.END_OF, favicon.id)
                addRule(RelativeLayout.CENTER_VERTICAL)
                marginStart = 16
                marginEnd = 48
            }
        }

        val title = TextView(this).apply {
            text = tab.title
            textSize = 15f
            setTextColor(Color.parseColor("#212121"))
            maxLines = 1
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        }

        val url = TextView(this).apply {
            text = tab.url.replace("https://", "").replace("http://", "").take(40)
            textSize = 12f
            setTextColor(Color.parseColor("#757575"))
            maxLines = 1
        }

        infoContainer.addView(title)
        infoContainer.addView(url)

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.parseColor("#BDBDBD"))
            layoutParams = RelativeLayout.LayoutParams(40, 40).apply {
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
        val colors = listOf(
            Color.parseColor("#2196F3"), Color.parseColor("#E91E63"), 
            Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"),
            Color.parseColor("#9C27B0"), Color.parseColor("#00BCD4")
        )
        tabs.add(TabItem("New Tab", "https://www.google.com", true, colors[tabs.size % colors.size]))
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
        tabContainer.animate().translationY(2000f).setDuration(300).withEndAction {
            tabContainer.visibility = View.GONE
        }.start()
    }

    @SuppressLint("InflateParams")
    private fun showModernMenu(anchor: View) {
        val menuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, 8, 0, 8)
        }

        val menuHeader = TextView(this).apply {
            text = "Settings"
            textSize = 18f
            setTextColor(Color.parseColor("#212121"))
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            setPadding(24, 16, 24, 16)
        }
        menuLayout.addView(menuHeader)

        val menuItems = listOf(
            Triple("Dark Mode", "🌙") { toggleDarkMode() },
            Triple("Light Mode", "☀️") { toggleLightMode() },
            Triple("Zoom In", "🔍") { webView.zoomIn(); showMessage("Zoom: ${++currentZoom}%") },
            Triple("Zoom Out", "🔍") { webView.zoomOut(); showMessage("Zoom: ${--currentZoom}%") },
            Triple("Reset Zoom", "↺") { resetZoom() },
            Triple("Desktop Mode", "🖥️") { toggleDesktopMode() },
            Triple("Share Page", "📤") { sharePage() },
            Triple("Clear Data", "🗑️") { clearData() },
            Triple("About", "ℹ️") { showAbout() }
        )

        menuItems.forEach { (title, icon, action) ->
            val menuItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(20, 16, 20, 16)
            }

            val iconText = TextView(this).apply {
                text = icon
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 16, 0)
                }
            }

            val titleText = TextView(this).apply {
                text = title
                textSize = 14f
                setTextColor(Color.parseColor("#424242"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            menuItem.addView(iconText)
            menuItem.addView(titleText)
            menuItem.setOnClickListener { action() }
            
            menuLayout.addView(menuItem)
        }

        val popup = PopupWindow(menuLayout, 360, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 16f
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
            })
            showAtLocation(anchor, Gravity.TOP or Gravity.END, 0, anchor.top + 20)
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
        val bgColor = if (isDarkMode) Color.parseColor("#121212") else Color.parseColor("#F5F5F5")
        val cardColor = if (isDarkMode) Color.parseColor("#1E1E1E") else Color.WHITE
        val textColor = if (isDarkMode) Color.WHITE else Color.parseColor("#212121")
        val hintColor = if (isDarkMode) Color.parseColor("#888888") else Color.parseColor("#9E9E9E")
        val iconColor = if (isDarkMode) Color.WHITE else Color.parseColor("#616161")
        val searchBg = if (isDarkMode) Color.parseColor("#2C2C2C") else Color.parseColor("#F5F5F5")

        mainContainer.setBackgroundColor(bgColor)
        headerBar.setBackgroundColor(cardColor)
        urlSearchContainer.setBackgroundColor(cardColor)
        bottomNav.setBackgroundColor(cardColor)
        tabContainer.setBackgroundColor(if (isDarkMode) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F5"))

        urlBar.setTextColor(textColor)
        urlBar.setHintTextColor(hintColor)

        for (i in 0 until bottomNav.childCount) {
            (bottomNav.getChildAt(i) as? ImageView)?.setColorFilter(iconColor)
        }

        btnMenu.setColorFilter(iconColor)

        showMessage(if (isDarkMode) "Dark Mode" else "Light Mode")
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
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, urlBar.text.toString())
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            showMessage("Cannot share")
        }
    }

    private fun showAbout() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("LiteBrowser")
        dialog.setMessage("Version 2.0\n\nA modern lightweight browser\n\nBuilt with Kotlin & WebView")
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
