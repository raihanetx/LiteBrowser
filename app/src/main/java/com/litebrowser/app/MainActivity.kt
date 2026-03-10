package com.litebrowser.app

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    // UI
    private lateinit var urlEditText: EditText
    private lateinit var tabButton: FrameLayout
    private lateinit var tabBadge: TextView
    private lateinit var menuButton: ImageButton
    private lateinit var loadingProgress: View
    private lateinit var viewPager: ViewPager2

    // Data
    private val tabUrls = mutableListOf<String>()
    private lateinit var pagerAdapter: TabPagerAdapter

    // Settings
    private lateinit var prefs: SharedPreferences
    private lateinit var cookieManager: CookieManager
    private var defaultUserAgent: String = ""

    companion object {
        private const val MAX_TABS = 10
        private const val PREFS_NAME = "browser_prefs"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_BLOCK_THIRD_PARTY = "block_third_party"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        defaultUserAgent = WebSettings.getDefaultUserAgent(this)

        bindViews()
        setupViewPager()
        setupClickListeners()
    }

    private fun bindViews() {
        urlEditText = findViewById(R.id.urlEditText)
        tabButton = findViewById(R.id.tabButton)
        tabBadge = findViewById(R.id.tabBadge)
        menuButton = findViewById(R.id.menuButton)
        loadingProgress = findViewById(R.id.loadingProgress)
        viewPager = findViewById(R.id.viewPager)
    }

    private fun setupViewPager() {
        // Initialize with one tab
        tabUrls.add("https://www.google.com")

        pagerAdapter = TabPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = MAX_TABS

        updateTabBadge()
    }

    private fun setupClickListeners() {
        // URL bar - navigate on Enter
        urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                val input = urlEditText.text.toString().trim()
                if (input.isNotEmpty()) {
                    navigateTo(input)
                }
                true
            } else {
                false
            }
        }

        // Tab button - show tab manager
        tabButton.setOnClickListener {
            showTabManager()
        }

        // Menu button - show options
        menuButton.setOnClickListener {
            showOptionsMenu()
        }

        // Update URL when swiping tabs
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUrlFromCurrentTab()
            }
        })
    }

    // ================== NAVIGATION ==================

    private fun navigateTo(input: String) {
        val url = formatUrl(input)
        getCurrentWebViewFragment()?.loadUrl(url)
        animateLoading()
    }

    private fun formatUrl(input: String): String {
        return when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${Uri.encode(input)}"
        }
    }

    private fun getCurrentWebViewFragment(): WebViewFragment? {
        return supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? WebViewFragment
    }

    private fun updateUrlFromCurrentTab() {
        getCurrentWebViewFragment()?.let { fragment ->
            fragment.getUrl()?.let { urlEditText.setText(it) }
        }
    }

    private fun animateLoading() {
        loadingProgress.visibility = View.VISIBLE
        val anim = ValueAnimator.ofInt(0, viewPager.width)
        anim.duration = 800
        anim.interpolator = DecelerateInterpolator()
        anim.addUpdateListener { animation ->
            val params = loadingProgress.layoutParams
            params.width = animation.animatedValue as Int
            loadingProgress.layoutParams = params
        }
        anim.start()
        loadingProgress.postDelayed({
            loadingProgress.visibility = View.GONE
            val params = loadingProgress.layoutParams
            params.width = 0
            loadingProgress.layoutParams = params
        }, 1000)
    }

    // ================== TAB MANAGEMENT ==================

    private fun addTab(url: String = "https://www.google.com") {
        if (tabUrls.size >= MAX_TABS) {
            Toast.makeText(this, "Maximum $MAX_TABS tabs allowed", Toast.LENGTH_SHORT).show()
            return
        }
        tabUrls.add(url)
        pagerAdapter.notifyItemInserted(tabUrls.size - 1)
        viewPager.setCurrentItem(tabUrls.size - 1, true)
        updateTabBadge()
    }

    private fun closeTab(position: Int) {
        if (tabUrls.size <= 1) {
            Toast.makeText(this, "Cannot close the last tab", Toast.LENGTH_SHORT).show()
            return
        }

        val wasSelected = viewPager.currentItem == position
        tabUrls.removeAt(position)
        pagerAdapter.notifyItemRemoved(position)
        
        if (position < tabUrls.size) {
            pagerAdapter.notifyItemRangeChanged(position, tabUrls.size - position)
        }

        updateTabBadge()

        if (wasSelected && position >= tabUrls.size) {
            viewPager.setCurrentItem(tabUrls.size - 1, false)
        }
    }

    private fun updateTabBadge() {
        tabBadge.text = tabUrls.size.toString()
    }

    // ================== FRAGMENT CALLBACK ==================

    fun onFragmentReady(position: Int, fragment: WebViewFragment) {
        // Apply current settings
        applyDesktopMode(fragment, prefs.getBoolean(KEY_DESKTOP_MODE, false))
        applyThirdPartyCookieBlocking(fragment, prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true))

        // URL change callback
        fragment.onUrlChange = { url ->
            if (viewPager.currentItem == position) {
                urlEditText.setText(url)
            }
            // Update stored URL
            if (position < tabUrls.size) {
                tabUrls[position] = url
            }
        }
    }

    // ================== SETTINGS ==================

    private fun applyDesktopMode(fragment: WebViewFragment, enable: Boolean) {
        val ua = if (enable) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            defaultUserAgent
        }
        try {
            fragment.webView.settings.userAgentString = ua
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyThirdPartyCookieBlocking(fragment: WebViewFragment, block: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                cookieManager.setAcceptThirdPartyCookies(fragment.webView, !block)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyDesktopModeToAll(enable: Boolean) {
        for (i in tabUrls.indices) {
            val fragment = supportFragmentManager.findFragmentByTag("f$i") as? WebViewFragment
            fragment?.let {
                applyDesktopMode(it, enable)
                it.reload()
            }
        }
    }

    // ================== BOTTOM SHEETS ==================

    private fun showTabManager() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_tabs, null)
        dialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.tabsRecyclerView)
        val addTabBtn = view.findViewById<TextView>(R.id.addTabBtn)
        val doneBtn = view.findViewById<TextView>(R.id.doneBtnTabs)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = TabPreviewAdapter(tabUrls, 
            onTabClick = { position ->
                viewPager.setCurrentItem(position, true)
                updateUrlFromCurrentTab()
                dialog.dismiss()
            },
            onCloseClick = { position ->
                closeTab(position)
                recyclerView.adapter?.notifyDataSetChanged()
            }
        )

        addTabBtn.setOnClickListener {
            addTab()
            dialog.dismiss()
        }

        doneBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showOptionsMenu() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        dialog.setContentView(view)

        // Zoom buttons
        view.findViewById<ImageButton>(R.id.zoomInBtn).setOnClickListener {
            getCurrentWebViewFragment()?.zoomIn()
        }

        view.findViewById<ImageButton>(R.id.zoomOutBtn).setOnClickListener {
            getCurrentWebViewFragment()?.zoomOut()
        }

        // Refresh
        view.findViewById<LinearLayout>(R.id.refreshBtn).setOnClickListener {
            getCurrentWebViewFragment()?.reload()
            animateLoading()
            dialog.dismiss()
        }

        // Desktop mode toggle
        val desktopToggle = view.findViewById<SwitchCompat>(R.id.desktopToggle)
        desktopToggle.isChecked = prefs.getBoolean(KEY_DESKTOP_MODE, false)
        
        view.findViewById<LinearLayout>(R.id.desktopModeRow).setOnClickListener {
            desktopToggle.toggle()
            val enabled = desktopToggle.isChecked
            prefs.edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply()
            applyDesktopModeToAll(enabled)
            Toast.makeText(this, 
                if (enabled) "Desktop mode enabled" else "Desktop mode disabled", 
                Toast.LENGTH_SHORT
            ).show()
        }

        // Tracking protection toggle
        val trackingToggle = view.findViewById<SwitchCompat>(R.id.trackingToggle)
        trackingToggle.isChecked = prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true)

        view.findViewById<LinearLayout>(R.id.trackingProtectionRow).setOnClickListener {
            trackingToggle.toggle()
            val enabled = trackingToggle.isChecked
            prefs.edit().putBoolean(KEY_BLOCK_THIRD_PARTY, enabled).apply()
            Toast.makeText(this, 
                if (enabled) "Tracking blocked" else "Tracking allowed", 
                Toast.LENGTH_SHORT
            ).show()
        }

        // Cookie management
        view.findViewById<LinearLayout>(R.id.cookieManageBtn).setOnClickListener {
            dialog.dismiss()
            showCookieManager()
        }

        // Close button
        view.findViewById<TextView>(R.id.closeMenuBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCookieManager() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_cookies, null)
        dialog.setContentView(view)

        val blockToggle = view.findViewById<SwitchCompat>(R.id.blockThirdPartyToggle)
        blockToggle.isChecked = prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true)

        view.findViewById<LinearLayout>(R.id.blockThirdPartyRow).setOnClickListener {
            blockToggle.toggle()
            prefs.edit().putBoolean(KEY_BLOCK_THIRD_PARTY, blockToggle.isChecked).apply()
        }

        view.findViewById<LinearLayout>(R.id.clearCookiesBtn).setOnClickListener {
            clearCurrentSiteCookies()
        }

        view.findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            dialog.dismiss()
            showOptionsMenu()
        }

        dialog.show()
    }

    private fun clearCurrentSiteCookies() {
        val url = getCurrentWebViewFragment()?.getUrl()
        if (url != null) {
            val domain = Uri.parse(url).host
            if (domain != null) {
                clearCookiesForDomain(domain)
                Toast.makeText(this, "Cookies cleared for $domain", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No page loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearCookiesForDomain(domain: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val cookies = cookieManager.getCookie("https://$domain")
                cookies?.split(";")?.forEach { cookie ->
                    val name = cookie.trim().substringBefore("=", "")
                    if (name.isNotEmpty()) {
                        cookieManager.setCookie("https://$domain", "$name=; Max-Age=0; Path=/")
                    }
                }
                cookieManager.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ================== BACK BUTTON ==================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = getCurrentWebViewFragment()
        if (fragment != null && fragment.canGoBack()) {
            fragment.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    // ================== ADAPTERS ==================

    inner class TabPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = tabUrls.size

        override fun createFragment(position: Int): Fragment {
            val fragment = WebViewFragment.newInstance(tabUrls[position])
            // Post to ensure fragment is created first
            viewPager.post {
                onFragmentReady(position, fragment)
            }
            return fragment
        }
    }

    inner class TabPreviewAdapter(
        private val urls: List<String>,
        private val onTabClick: (Int) -> Unit,
        private val onCloseClick: (Int) -> Unit
    ) : RecyclerView.Adapter<TabPreviewAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val closeBtn: View = view.findViewById(R.id.closeBtn)

            init {
                itemView.setOnClickListener {
                    onTabClick(adapterPosition)
                }
                closeBtn.setOnClickListener {
                    onCloseClick(adapterPosition)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tab, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Tab preview binding
        }

        override fun getItemCount(): Int = urls.size
    }
}
