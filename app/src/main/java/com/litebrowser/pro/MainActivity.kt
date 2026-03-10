package com.litebrowser.pro

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
    private val tabTitles = mutableListOf<String>()
    private lateinit var pagerAdapter: TabPagerAdapter

    // Fragment tracking for proper lifecycle management
    private val fragmentMap = mutableMapOf<Int, WebViewFragment>()

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
        tabTitles.add("Google")

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
                applySettingsToCurrentFragment()
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
        val position = viewPager.currentItem
        
        // First try to get from our tracked map
        fragmentMap[position]?.let { return it }
        
        // Fallback: try to find by tag (ViewPager2 uses "f{position}" format)
        val tag = "f$position"
        val fragment = supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment
        
        // If found, update our map
        if (fragment != null) {
            fragmentMap[position] = fragment
        }
        
        return fragment
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
        tabTitles.add("New Tab")
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
        val wasLastTab = position == tabUrls.size - 1

        // Remove from tracking map
        fragmentMap.remove(position)
        
        // Shift fragments in map for positions after the removed one
        val updatedMap = mutableMapOf<Int, WebViewFragment>()
        fragmentMap.forEach { (pos, fragment) ->
            if (pos > position) {
                updatedMap[pos - 1] = fragment
            } else {
                updatedMap[pos] = fragment
            }
        }
        fragmentMap.clear()
        fragmentMap.putAll(updatedMap)

        tabUrls.removeAt(position)
        tabTitles.removeAt(position)
        pagerAdapter.notifyItemRemoved(position)

        if (position < tabUrls.size) {
            pagerAdapter.notifyItemRangeChanged(position, tabUrls.size - position)
        }

        updateTabBadge()

        // Adjust current tab position
        if (wasSelected) {
            val newPosition = if (wasLastTab) tabUrls.size - 1 else position
            viewPager.setCurrentItem(newPosition, false)
            updateUrlFromCurrentTab()
        }
    }

    private fun selectTab(position: Int) {
        if (position in 0 until tabUrls.size) {
            viewPager.setCurrentItem(position, true)
            updateUrlFromCurrentTab()
        }
    }

    private fun updateTabBadge() {
        tabBadge.text = tabUrls.size.toString()
    }

    // ================== FRAGMENT CALLBACK ==================

    fun onFragmentReady(position: Int, fragment: WebViewFragment) {
        // Track the fragment
        fragmentMap[position] = fragment
        
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

        // Title change callback
        fragment.onTitleChange = { title ->
            if (position < tabTitles.size) {
                tabTitles[position] = title
            }
        }
    }

    private fun applySettingsToCurrentFragment() {
        val fragment = getCurrentWebViewFragment() ?: return
        applyDesktopMode(fragment, prefs.getBoolean(KEY_DESKTOP_MODE, false))
        applyThirdPartyCookieBlocking(fragment, prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true))
    }

    // ================== SETTINGS ==================

    private fun applyDesktopMode(fragment: WebViewFragment, enable: Boolean) {
        try {
            val ua = if (enable) {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else {
                defaultUserAgent
            }
            fragment.setDesktopMode(enable, ua)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyThirdPartyCookieBlocking(fragment: WebViewFragment, block: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                fragment.getWebView()?.let { webView ->
                    cookieManager.setAcceptThirdPartyCookies(webView, !block)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyDesktopModeToAll(enable: Boolean) {
        val ua = if (enable) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            defaultUserAgent
        }

        // Apply to tracked fragments
        for (i in tabUrls.indices) {
            val fragment = fragmentMap[i] 
                ?: (supportFragmentManager.findFragmentByTag("f$i") as? WebViewFragment)
            
            fragment?.setDesktopMode(enable, ua)
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

        refreshTabAdapter(recyclerView, dialog)

        addTabBtn.setOnClickListener {
            addTab()
            dialog.dismiss()
        }

        doneBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun refreshTabAdapter(recyclerView: RecyclerView, dialog: BottomSheetDialog) {
        val adapter = TabPreviewAdapter(
            tabUrls.toList(),
            tabTitles.toList(),
            viewPager.currentItem,
            onTabClick = { position ->
                selectTab(position)
                dialog.dismiss()
            },
            onCloseClick = { position ->
                closeTab(position)
                if (tabUrls.isNotEmpty()) {
                    refreshTabAdapter(recyclerView, dialog)
                } else {
                    dialog.dismiss()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showOptionsMenu() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        dialog.setContentView(view)

        // Zoom level text
        val zoomLevelText = view.findViewById<TextView>(R.id.zoomLevelText)
        updateZoomLevelText(zoomLevelText)

        // Zoom buttons
        view.findViewById<ImageButton>(R.id.zoomInBtn).setOnClickListener {
            val fragment = getCurrentWebViewFragment()
            if (fragment != null) {
                val success = fragment.zoomIn()
                updateZoomLevelText(zoomLevelText)
                Toast.makeText(this, if (success) "Zoomed in" else "Zoom limit reached", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No page loaded", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<ImageButton>(R.id.zoomOutBtn).setOnClickListener {
            val fragment = getCurrentWebViewFragment()
            if (fragment != null) {
                val success = fragment.zoomOut()
                updateZoomLevelText(zoomLevelText)
                Toast.makeText(this, if (success) "Zoomed out" else "Zoom limit reached", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No page loaded", Toast.LENGTH_SHORT).show()
            }
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
                if (enabled) "Desktop mode enabled - Reloading pages" else "Desktop mode disabled - Reloading pages",
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

    private fun updateZoomLevelText(textView: TextView) {
        val fragment = getCurrentWebViewFragment()
        if (fragment != null) {
            val scale = fragment.getZoomLevel()
            textView.text = "${(scale * 100).toInt()}%"
        } else {
            textView.text = "100%"
        }
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
            // Fragment is ready, notify activity
            fragment.postDelayed {
                onFragmentReady(position, fragment)
            }
            return fragment
        }
    }

    inner class TabPreviewAdapter(
        private val urls: List<String>,
        private val titles: List<String>,
        private val currentPosition: Int,
        private val onTabClick: (Int) -> Unit,
        private val onCloseClick: (Int) -> Unit
    ) : RecyclerView.Adapter<TabPreviewAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val closeBtn: ImageButton = view.findViewById(R.id.closeBtn)
            private val titleText: TextView? = view.findViewById(R.id.tabTitleText)
            private val container: View = view.findViewById(R.id.tabContainer)

            init {
                itemView.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onTabClick(adapterPosition)
                    }
                }
                closeBtn.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onCloseClick(adapterPosition)
                    }
                }
            }

            fun bind(url: String, title: String, isSelected: Boolean) {
                // Display title or URL shortened
                val displayTitle = if (title.isNotEmpty() && title != "about:blank") {
                    title
                } else {
                    try {
                        Uri.parse(url).host ?: "Page"
                    } catch (e: Exception) {
                        "Page"
                    }
                }
                titleText?.text = displayTitle.take(15)

                // Highlight selected tab
                container?.setBackgroundColor(
                    if (isSelected) Color.parseColor("#E3F2FD") else Color.parseColor("#FFFFFF")
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tab, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position < urls.size && position < titles.size) {
                holder.bind(urls[position], titles[position], position == currentPosition)
            }
        }

        override fun getItemCount(): Int = urls.size
    }
}

// Extension function to post with delay on Fragment
private fun Fragment.postDelayed(delayMillis: Long = 50, action: () -> Unit) {
    view?.postDelayed(action, delayMillis)
}
