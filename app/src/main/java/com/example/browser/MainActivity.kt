package com.example.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.WebChromeClient
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
    private var isDarkMode = false
    private var isDesktopMode = false

    private lateinit var btnBack: ImageView
    private lateinit var btnForward: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var btnTabs: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnRefresh: ImageView

    private var tabSliderShown = false

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

        setupTopBar()
        setupWebView()
        setupBottomNav()

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
        val topBar = RelativeLayout(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            setPadding(24, 32, 24, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        urlBar = EditText(this).apply {
            hint = "Search or enter URL"
            setHintTextColor(0xFF999999.toInt())
            setBackgroundResource(android.R.drawable.edit_text)
            setPadding(32, 24, 32, 24)
            textSize = 16f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            setOnEditorActionListener { _, _, _ ->
                loadUrl()
                true
            }
        }

        btnRefresh = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setColorFilter(0xFF2196F3.toInt())
            layoutParams = RelativeLayout.LayoutParams(64, 64).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                marginEnd = 8
            }
            setOnClickListener { webView.reload() }
        }

        topBar.addView(urlBar)
        topBar.addView(btnRefresh)
        mainContainer.addView(topBar)
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

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    progressBar.visibility = View.VISIBLE
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    urlBar.setText(url)
                    super.onPageFinished(view, url)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                }
            }
        }

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = android.content.res.ColorStateList.valueOf(0xFF2196F3.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8
            )
            visibility = View.GONE
        }

        mainContainer.addView(webView)
    }

    private fun setupBottomNav() {
        bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            setPadding(16, 24, 16, 32)
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

        btnHome = createNavIcon(android.R.drawable.ic_menu_compass, "Home") {
            webView.loadUrl("https://www.google.com")
        }

        btnTabs = createNavIcon(android.R.drawable.ic_menu_add, "Tabs") {
            showTabSlider()
        }

        btnMenu = createNavIcon(android.R.drawable.ic_menu_more, "Menu") {
            showModernMenu(btnMenu)
        }

        bottomNav.addView(btnBack)
        bottomNav.addView(btnForward)
        bottomNav.addView(btnHome)
        bottomNav.addView(btnTabs)
        bottomNav.addView(btnMenu)

        mainContainer.addView(bottomNav)
    }

    private fun createNavIcon(icon: Int, contentDesc: String, onClick: () -> Unit): ImageView {
        return ImageView(this).apply {
            setImageResource(icon)
            setColorFilter(0xFF333333.toInt())
            layoutParams = LinearLayout.LayoutParams(0, 96, 1f).apply {
                gravity = Gravity.CENTER
            }
            setOnClickListener { onClick() }
            contentDescription = contentDesc
        }
    }

    private fun setupTabContainer() {
        tabContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            visibility = View.GONE
            translationY = 1000f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
        }

        val tabHeader = LinearLayout(this).apply {
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(48, 48, 48, 48)
        }

        val tabTitle = TextView(this).apply {
            text = "Tabs"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(64, 64)
            setOnClickListener { hideTabSlider() }
        }

        tabHeader.addView(tabTitle)
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
            setPadding(24, 24, 24, 24)
        }

        for (i in 1..5) {
            val tabCard = CardView(this).apply {
                radius = 24f
                cardElevation = 8f
                setCardBackgroundColor(0xFFF5F5F5.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    180
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
            }

            val tabContent = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 24, 32, 24)
                gravity = Gravity.CENTER_VERTICAL
            }

            val favicon = View(this).apply {
                setBackgroundColor(0xFF2196F3.toInt())
                layoutParams = LinearLayout.LayoutParams(64, 64)
            }

            val tabInfo = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(24, 0, 0, 0)
                }
            }

            val title = TextView(this).apply {
                text = "Google - Tab $i"
                textSize = 18f
                setTextColor(0xFF333333.toInt())
            }

            val url = TextView(this).apply {
                text = "https://www.google.com"
                textSize = 14f
                setTextColor(0xFF888888.toInt())
            }

            tabInfo.addView(title)
            tabInfo.addView(url)
            tabContent.addView(favicon)
            tabContent.addView(tabInfo)
            tabCard.addView(tabContent)

            tabCard.setOnClickListener {
                hideTabSlider()
                urlBar.setText("https://www.google.com")
                webView.loadUrl("https://www.google.com")
            }

            tabsGrid.addView(tabCard)
        }

        tabsScroll.addView(tabsGrid)

        val addTabBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF2196F3.toInt())
            setPadding(48, 32, 48, 48)
            gravity = Gravity.CENTER
        }

        val addTabText = TextView(this).apply {
            text = "+ New Tab"
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
        }

        addTabBtn.addView(addTabText)

        addTabBtn.setOnClickListener {
            hideTabSlider()
            showMessage("New tab created")
        }

        tabContainer.addView(tabHeader)
        tabContainer.addView(tabsScroll)
        tabContainer.addView(addTabBtn)
    }

    private fun showTabSlider() {
        tabSliderShown = true
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
        tabContainer.animate().translationY(1000f).setDuration(300).withEndAction {
            tabContainer.visibility = View.GONE
        }.start()
    }

    @SuppressLint("InflateParams")
    private fun showModernMenu(anchor: View) {
        val menuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(16, 8, 16, 8)
        }

        val menuItems = listOf(
            "Dark Mode" to { isDarkMode = !isDarkMode; applyTheme() },
            "Light Mode" to { isDarkMode = false; applyTheme() },
            "Zoom In" to { webView.zoomIn() },
            "Zoom Out" to { webView.zoomOut() },
            "Desktop Mode" to { toggleDesktopMode() },
            "History" to { showMessage("History - Coming soon") },
            "Bookmarks" to { showMessage("Bookmarks - Coming soon") },
            "Cookies" to { showMessage("Cookies - Coming soon") },
            "Settings" to { showMessage("Settings - Coming soon") },
            "About" to { showAboutDialog() }
        )

        menuItems.forEach { (title, action) ->
            val menuItem = TextView(this).apply {
                text = title
                textSize = 18f
                setTextColor(0xFF333333.toInt())
                setPadding(32, 28, 32, 28)
            }
            menuItem.setOnClickListener {
                action()
            }
            menuLayout.addView(menuItem)
        }

        val popup = PopupWindow(menuLayout, 450, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            elevation = 16f
            setBackgroundDrawable(ContextCompat.getDrawable(this@MainActivity, android.R.drawable.edit_text))
            showAtLocation(anchor, Gravity.TOP or Gravity.END, 0, anchor.top - 20)
        }
    }

    private fun applyTheme() {
        val bgColor = if (isDarkMode) 0xFF121212.toInt() else 0xFFF5F5F5.toInt()
        val cardColor = if (isDarkMode) 0xFF1E1E1E.toInt() else 0xFFFFFFFF.toInt()
        val textColor = if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF333333.toInt()

        mainContainer.setBackgroundColor(bgColor)
        bottomNav.setBackgroundColor(cardColor)
        tabContainer.setBackgroundColor(cardColor)

        val iconColor = if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF333333.toInt()
        btnBack.setColorFilter(iconColor)
        btnForward.setColorFilter(iconColor)
        btnHome.setColorFilter(iconColor)
        btnTabs.setColorFilter(iconColor)
        btnMenu.setColorFilter(iconColor)
        btnRefresh.setColorFilter(iconColor)

        urlBar.setTextColor(textColor)
        urlBar.setHintTextColor(if (isDarkMode) 0xFF888888.toInt() else 0xFF999999.toInt())

        showMessage(if (isDarkMode) "Dark Mode ON" else "Light Mode ON")
    }

    private fun toggleDesktopMode() {
        isDesktopMode = !isDesktopMode
        webView.settings.userAgentString = if (isDesktopMode) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        } else {
            ""
        }
        webView.reload()
        showMessage(if (isDesktopMode) "Desktop Mode ON" else "Desktop Mode OFF")
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("LiteBrowser")
        dialog.setMessage("Version 1.0.0\n\nA lightweight browser built with Kotlin and WebView")
        dialog.setPositiveButton("OK", null)
        dialog.show()
    }

    private fun loadUrl() {
        var url = urlBar.text.toString().trim()
        if (url.isNotEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (url.contains(".")) {
                    url = "https://$url"
                } else {
                    url = "https://www.google.com/search?q=$url"
                }
            }
            webView.loadUrl(url)
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
