package com.example.browser

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var headerLayout: LinearLayout
    private lateinit var bottomNav: LinearLayout
    private lateinit var tabSlider: LinearLayout
    private lateinit var sliderOverlay: View

    private lateinit var btnHome: ImageButton
    private lateinit var btnTabs: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnHomeBottom: ImageButton

    private var isDarkMode = false
    private var isDesktopMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupMainLayout()
        webView.loadUrl("https://www.google.com")
    }

    private fun setupMainLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        sliderOverlay = View(this).apply {
            setBackgroundColor(0x80000000.toInt())
            visibility = View.GONE
            setOnClickListener { hideTabSlider() }
        }

        tabSlider = createTabSlider()
        tabSlider.visibility = View.GONE

        headerLayout = createHeader()
        
        webView = createWebView()
        
        bottomNav = createBottomNavigation()

        val contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        contentContainer.addView(sliderOverlay)
        contentContainer.addView(tabSlider)
        contentContainer.addView(webView)

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8
            )
            isVisible = false
        }

        mainLayout.addView(headerLayout)
        mainLayout.addView(progressBar)
        mainLayout.addView(contentContainer)
        mainLayout.addView(bottomNav)

        setContentView(mainLayout)
    }

    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            urlEditText = EditText(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
                hint = "Search or enter URL"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
                setPadding(24, 16, 24, 16)
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        loadUrl()
                        true
                    } else false
                }
            }

            btnHome = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_compass)
                layoutParams = LinearLayout.LayoutParams(120, 120)
                setOnClickListener { webView.loadUrl("https://www.google.com") }
            }

            btnTabs = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_add)
                layoutParams = LinearLayout.LayoutParams(120, 120)
                setOnClickListener { showTabSlider() }
            }

            btnMenu = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_more)
                layoutParams = LinearLayout.LayoutParams(120, 120)
                setOnClickListener { showMenu(it) }
            }

            addView(btnHome)
            addView(urlEditText)
            addView(btnTabs)
            addView(btnMenu)
        }
    }

    private fun createWebView(): WebView {
        return WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    urlEditText.setText(url)
                    super.onPageFinished(view, url)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                    progressBar.isVisible = newProgress < 100
                }
            }
        }
    }

    private fun createBottomNavigation(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            btnBack = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_media_previous)
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
                setOnClickListener {
                    if (webView.canGoBack()) webView.goBack()
                }
            }

            btnForward = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_media_next)
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
                setOnClickListener {
                    if (webView.canGoForward()) webView.goForward()
                }
            }

            btnHomeBottom = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_compass)
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
                setOnClickListener { webView.loadUrl("https://www.google.com") }
            }

            val menuButton = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_more)
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
                setOnClickListener { showMenu(it) }
            }

            val tabButton = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_add)
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
                setOnClickListener { showTabSlider() }
            }

            addView(btnBack)
            addView(btnForward)
            addView(btnHomeBottom)
            addView(menuButton)
            addView(tabButton)
        }
    }

    private fun createTabSlider(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFFEEEEEE.toInt())
        }.apply {
            val header = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0xFF2196F3.toInt())
                setPadding(32, 32, 32, 32)
            }

            val title = TextView(this@MainActivity).apply {
                text = "Tabs"
                textSize = 24f
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val closeBtn = ImageButton(this@MainActivity).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setBackgroundColor(0x00000000)
                setOnClickListener { hideTabSlider() }
            }

            header.addView(title)
            header.addView(closeBtn)

            val tabsContainer = ScrollView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }

            val tabsLinear = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }

            for (i in 1..5) {
                val tabView = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setBackgroundColor(0xFFFFFFFF.toInt())
                    setPadding(32, 32, 32, 32)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 16
                        bottomMargin = 16
                        leftMargin = 16
                        rightMargin = 16
                    }
                }

                val tabIcon = View(this@MainActivity).apply {
                    setBackgroundColor(0xFF2196F3.toInt())
                    layoutParams = LinearLayout.LayoutParams(80, 80)
                }

                val tabInfo = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        leftMargin = 24
                    }
                }

                val tabTitle = TextView(this@MainActivity).apply {
                    text = "Tab $i - Google"
                    textSize = 18f
                }

                val tabUrl = TextView(this@MainActivity).apply {
                    text = "https://www.google.com"
                    textSize = 14f
                    setTextColor(0xFF888888.toInt())
                }

                tabInfo.addView(tabTitle)
                tabInfo.addView(tabUrl)
                tabView.addView(tabIcon)
                tabView.addView(tabInfo)
                tabsLinear.addView(tabView)
            }

            tabsContainer.addView(tabsLinear)

            addView(header)
            addView(tabsContainer)
        }
    }

    private fun showTabSlider() {
        tabSlider.visibility = View.VISIBLE
        sliderOverlay.visibility = View.VISIBLE
    }

    private fun hideTabSlider() {
        tabSlider.visibility = View.GONE
        sliderOverlay.visibility = View.GONE
    }

    private fun showMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "Dark Mode")
        popup.menu.add(0, 2, 0, "Light Mode")
        popup.menu.add(0, 3, 0, "Zoom In")
        popup.menu.add(0, 4, 0, "Zoom Out")
        popup.menu.add(0, 5, 0, "Desktop Mode")
        popup.menu.add(0, 6, 0, "History")
        popup.menu.add(0, 7, 0, "Bookmarks")
        popup.menu.add(0, 8, 0, "Cookies Management")
        popup.menu.add(0, 9, 0, "New Tab")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { isDarkMode = true; applyTheme() }
                2 -> { isDarkMode = false; applyTheme() }
                3 -> webView.zoomIn()
                4 -> webView.zoomOut()
                5 -> { isDesktopMode = !isDesktopMode; applyDesktopMode() }
                6 -> showMessage("History - Coming soon")
                7 -> showMessage("Bookmarks - Coming soon")
                8 -> showMessage("Cookies Management - Coming soon")
                9 -> showMessage("New Tab - Coming soon")
            }
            true
        }
        popup.show()
    }

    private fun applyTheme() {
        val bgColor = if (isDarkMode) 0xFF121212.toInt() else 0xFFFFFFFF.toInt()
        val textColor = if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        
        headerLayout.setBackgroundColor(bgColor)
        bottomNav.setBackgroundColor(bgColor)
        
        urlEditText.setBackgroundColor(if (isDarkMode) 0xFF2C2C2C.toInt() else 0xFFFFFFFF.toInt())
        urlEditText.setTextColor(textColor)
    }

    private fun applyDesktopMode() {
        webView.settings.userAgentString = if (isDesktopMode) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        } else {
            ""
        }
        webView.reload()
    }

    private fun loadUrl() {
        var url = urlEditText.text.toString().trim()
        if (url.isNotEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://www.google.com/search?q=$url"
            }
            webView.loadUrl(url)
        }
    }

    private fun showMessage(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            tabSlider.visibility == View.VISIBLE -> hideTabSlider()
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
