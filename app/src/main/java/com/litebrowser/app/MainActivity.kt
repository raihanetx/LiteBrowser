package com.litebrowser.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var urlEditText: EditText
    private lateinit var goButton: Button
    private lateinit var backButton: Button
    private lateinit var forwardButton: Button
    private lateinit var refreshButton: Button
    private lateinit var zoomInButton: Button
    private lateinit var zoomOutButton: Button
    private lateinit var menuButton: Button
    private lateinit var newTabButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    // Adapter & Tab URLs
    private lateinit var adapter: TabAdapter
    private val tabUrls = mutableListOf<String>()

    // Settings
    private lateinit var prefs: SharedPreferences
    private lateinit var cookieManager: CookieManager
    private lateinit var defaultUserAgent: String

    // Constants
    private val MAX_TABS = 10
    private val SAVED_TABS = "saved_tabs"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        // Initialize SharedPreferences
        prefs = getSharedPreferences("browser_prefs", Context.MODE_PRIVATE)

        // Setup adapter with URL list
        adapter = TabAdapter(this, tabUrls)
        viewPager.adapter = adapter

        // Keep enough offscreen pages to prevent fragment destruction
        viewPager.offscreenPageLimit = MAX_TABS

        // Link TabLayout with ViewPager2 using custom tab views
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabView = LayoutInflater.from(this).inflate(R.layout.tab_item, null)
            val tabText = tabView.findViewById<TextView>(R.id.tabText)
            val closeButton = tabView.findViewById<ImageView>(R.id.closeButton)

            tabText.text = "Tab ${position + 1}"
            closeButton.setOnClickListener {
                closeTab(position)
            }
            tab.customView = tabView
        }.attach()

        // CookieManager – global instance
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true) // accept first-party cookies globally

        // Save default user agent for later toggling
        defaultUserAgent = WebSettings.getDefaultUserAgent(this)

        // Restore tabs if activity is recreated (e.g., after rotation)
        if (savedInstanceState != null) {
            val savedUrls = savedInstanceState.getStringArrayList(SAVED_TABS)
            savedUrls?.forEach { url ->
                addNewTab(url, select = false)
            }
            // If no saved tabs, add default
            if (tabUrls.isEmpty()) {
                addNewTab()
            }
        } else {
            // First launch: add default tab
            addNewTab()
        }

        // Setup click listeners
        setupListeners()

        // Update URL bar when tab changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUrlForCurrentTab()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current tab URLs to restore after recreation
        outState.putStringArrayList(SAVED_TABS, ArrayList(tabUrls))
    }

    private fun bindViews() {
        urlEditText = findViewById(R.id.urlEditText)
        goButton = findViewById(R.id.goButton)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        refreshButton = findViewById(R.id.refreshButton)
        zoomInButton = findViewById(R.id.zoomInButton)
        zoomOutButton = findViewById(R.id.zoomOutButton)
        menuButton = findViewById(R.id.menuButton)
        newTabButton = findViewById(R.id.newTabButton)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
    }

    private fun setupListeners() {
        goButton.setOnClickListener {
            val input = urlEditText.text.toString().trim()
            if (input.isNotEmpty()) {
                getCurrentFragment()?.loadUrl(formatUrl(input))
            }
        }

        backButton.setOnClickListener {
            getCurrentFragment()?.takeIf { it.canGoBack() }?.goBack()
        }

        forwardButton.setOnClickListener {
            getCurrentFragment()?.takeIf { it.canGoForward() }?.goForward()
        }

        refreshButton.setOnClickListener {
            getCurrentFragment()?.reload()
        }

        zoomInButton.setOnClickListener {
            getCurrentFragment()?.zoomIn()
        }

        zoomOutButton.setOnClickListener {
            getCurrentFragment()?.zoomOut()
        }

        menuButton.setOnClickListener {
            showMenuPopup()
        }

        newTabButton.setOnClickListener {
            addNewTab()
        }
    }

    // Helper to get currently visible fragment using ViewPager2's tag convention
    private fun getCurrentFragment(): WebViewFragment? {
        val tag = "f${viewPager.currentItem}"
        return supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment
    }

    // Prepend https:// if no scheme
    private fun formatUrl(input: String): String {
        return if (!input.startsWith("http://") && !input.startsWith("https://")) {
            "https://$input"
        } else input
    }

    // Add a new tab with optional initial URL
    private fun addNewTab(url: String = "https://www.google.com", select: Boolean = true) {
        if (tabUrls.size >= MAX_TABS) {
            Toast.makeText(this, "Maximum $MAX_TABS tabs allowed", Toast.LENGTH_SHORT).show()
            return
        }
        adapter.addTab(url)
        if (select) {
            viewPager.setCurrentItem(tabUrls.size - 1, true)
        }
    }

    // Close a tab at given position
    private fun closeTab(position: Int) {
        if (tabUrls.size <= 1) {
            Toast.makeText(this, "Cannot close the last tab", Toast.LENGTH_SHORT).show()
            return
        }
        adapter.removeTab(position)
    }

    // Update URL bar to reflect current tab's URL
    private fun updateUrlForCurrentTab() {
        getCurrentFragment()?.getUrl()?.let {
            urlEditText.setText(it)
        }
    }

    // Called by WebViewFragment when its view is ready
    fun onFragmentViewReady(position: Int, fragment: WebViewFragment) {
        // Apply current global settings
        applyDesktopModeToFragment(fragment, prefs.getBoolean("desktop_mode", false))
        applyThirdPartyBlockingToFragment(fragment, prefs.getBoolean("block_third_party", false))

        // Set URL change callback
        fragment.onUrlChange = { newUrl ->
            if (viewPager.currentItem == position) {
                urlEditText.setText(newUrl)
            }
        }
    }

    // Apply desktop mode to a single fragment
    private fun applyDesktopModeToFragment(fragment: WebViewFragment, enable: Boolean) {
        val ua = if (enable) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            defaultUserAgent
        }
        fragment.webView.settings.userAgentString = ua
        fragment.webView.reload() // reload to apply immediately
    }

    // Apply third-party cookie blocking to a single fragment
    private fun applyThirdPartyBlockingToFragment(fragment: WebViewFragment, block: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(fragment.webView, !block)
        }
    }

    // Apply desktop mode to all existing fragments
    private fun applyDesktopModeToAllTabs(enable: Boolean) {
        for (i in tabUrls.indices) {
            val tag = "f$i"
            (supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment)?.let {
                applyDesktopModeToFragment(it, enable)
            }
        }
    }

    // Apply third-party cookie blocking to all existing fragments
    private fun applyThirdPartyBlockingToAllTabs(block: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        for (i in tabUrls.indices) {
            val tag = "f$i"
            (supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment)?.let {
                applyThirdPartyBlockingToFragment(it, block)
            }
        }
    }

    // ------------------ Menu & Cookie Dialogs ------------------

    private fun showMenuPopup() {
        val popup = PopupMenu(this, menuButton)
        popup.menuInflater.inflate(R.menu.browser_menu, popup.menu)

        popup.menu.findItem(R.id.action_desktop_mode).isChecked = prefs.getBoolean("desktop_mode", false)
        popup.menu.findItem(R.id.action_block_third_party).isChecked = prefs.getBoolean("block_third_party", false)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_desktop_mode -> {
                    val newState = !item.isChecked
                    item.isChecked = newState
                    prefs.edit().putBoolean("desktop_mode", newState).apply()
                    applyDesktopModeToAllTabs(newState)
                    true
                }
                R.id.action_block_third_party -> {
                    val newState = !item.isChecked
                    item.isChecked = newState
                    prefs.edit().putBoolean("block_third_party", newState).apply()
                    applyThirdPartyBlockingToAllTabs(newState)
                    true
                }
                R.id.action_manage_cookies -> {
                    showCookieManagerDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showCookieManagerDialog() {
        val fragment = getCurrentFragment() ?: return
        val webView = fragment.webView
        val currentUrl = webView.url ?: return
        val domain = Uri.parse(currentUrl).host ?: return

        val cookies = cookieManager.getCookie(currentUrl)
        val cookieList = if (cookies.isNullOrEmpty()) {
            emptyList()
        } else {
            cookies.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        }

        AlertDialog.Builder(this)
            .setTitle("Cookies for $domain")
            .apply {
                if (cookieList.isEmpty()) {
                    setMessage("No cookies found for this site.")
                    setPositiveButton("OK", null)
                } else {
                    val items = cookieList.toTypedArray()
                    setItems(items) { _, which ->
                        val selected = items[which]
                        confirmDeleteCookie(domain, selected)
                    }
                    setNeutralButton("Delete All") { _, _ ->
                        deleteAllCookiesForDomain(domain)
                    }
                    setNegativeButton("Cancel", null)
                }
            }
            .show()
    }

    private fun confirmDeleteCookie(domain: String, cookieString: String) {
        val name = cookieString.substringBefore('=')
        AlertDialog.Builder(this)
            .setTitle("Delete Cookie")
            .setMessage("Delete cookie '$name'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCookie(domain, name)
                showCookieManagerDialog() // refresh
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes a cookie by setting an expired version.
     * Tries multiple domain variants (exact and .domain) and both http/https.
     */
    private fun deleteCookie(domain: String, cookieName: String) {
        val expiredBase = "$cookieName=; Max-Age=0; Path=/"
        // Try exact domain
        cookieManager.setCookie("https://$domain", "$expiredBase; Domain=$domain")
        cookieManager.setCookie("http://$domain", "$expiredBase; Domain=$domain")
        // Try domain with leading dot (covers subdomains)
        cookieManager.setCookie("https://$domain", "$expiredBase; Domain=.$domain")
        cookieManager.setCookie("http://$domain", "$expiredBase; Domain=.$domain")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush()
        }
    }

    private fun deleteAllCookiesForDomain(domain: String) {
        val fragment = getCurrentFragment() ?: return
        val currentUrl = fragment.webView.url ?: return
        val cookies = cookieManager.getCookie(currentUrl)
        if (!cookies.isNullOrEmpty()) {
            cookies.split(";").map { it.trim() }.filter { it.isNotEmpty() }.forEach { cookie ->
                val name = cookie.substringBefore('=')
                deleteCookie(domain, name)
            }
        }
        showCookieManagerDialog()
    }

    // Handle back button: first try WebView back, else super
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = getCurrentFragment()
        if (fragment != null && fragment.canGoBack()) {
            fragment.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
