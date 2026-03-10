package com.litebrowser.pro

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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

    private val TAG = "LiteBrowser"

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
        tabUrls.add("https://www.google.com")
        tabTitles.add("Google")

        pagerAdapter = TabPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = MAX_TABS

        updateTabBadge()
    }

    private fun setupClickListeners() {
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

        tabButton.setOnClickListener { showTabManager() }
        menuButton.setOnClickListener { showOptionsMenu() }

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

        tabUrls.removeAt(position)
        tabTitles.removeAt(position)
        pagerAdapter.notifyItemRemoved(position)

        if (position < tabUrls.size) {
            pagerAdapter.notifyItemRangeChanged(position, tabUrls.size - position)
        }

        updateTabBadge()

        if (wasSelected) {
            val newPosition = if (wasLastTab) tabUrls.size - 1 else position
            viewPager.setCurrentItem(newPosition, false)
            updateUrlFromCurrentTab()
        }
    }

    private fun updateTabBadge() {
        tabBadge.text = tabUrls.size.toString()
    }

    fun onFragmentCreated(fragment: WebViewFragment) {
        for (i in 0 until tabUrls.size) {
            val f = supportFragmentManager.findFragmentByTag("f$i") as? WebViewFragment
            if (f == fragment) {
                setupFragmentCallbacks(i, fragment)
                break
            }
        }
    }

    private fun setupFragmentCallbacks(position: Int, fragment: WebViewFragment) {
        applyDesktopMode(fragment, prefs.getBoolean(KEY_DESKTOP_MODE, false))
        applyThirdPartyCookieBlocking(fragment, prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true))

        fragment.onUrlChange = { url ->
            if (viewPager.currentItem == position) {
                urlEditText.setText(url)
            }
            if (position < tabUrls.size) {
                tabUrls[position] = url
            }
        }

        fragment.onTitleChange = { title ->
            if (position < tabTitles.size) {
                tabTitles[position] = title
            }
        }
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
        for (i in tabUrls.indices) {
            val fragment = supportFragmentManager.findFragmentByTag("f$i") as? WebViewFragment
            fragment?.let {
                applyDesktopMode(it, enable)
                it.reload()
            }
        }
    }

    // ================== BOTTOM SHEETS ==================

    @SuppressLint("InflateParams")
    private fun showTabManager() {
        Log.d(TAG, "showTabManager: ${tabUrls.size} tabs")
        
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_tabs, null)
        dialog.setContentView(view)

        val recyclerView: RecyclerView = view.findViewById(R.id.tabsRecyclerView)
        val addTabBtn: TextView = view.findViewById(R.id.addTabBtn)
        val doneBtn: TextView = view.findViewById(R.id.doneBtnTabs)

        // Simple adapter with item view click
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = TabAdapter(
            tabs = tabUrls.mapIndexed { index, url -> 
                TabItem(url, tabTitles.getOrNull(index) ?: "Tab ${index + 1}") 
            },
            selectedPosition = viewPager.currentItem,
            onTabSelected = { position ->
                Log.d(TAG, "Tab selected: $position")
                viewPager.setCurrentItem(position, true)
                dialog.dismiss()
            },
            onTabClosed = { position ->
                Log.d(TAG, "Tab closed: $position")
                closeTab(position)
                recyclerView.adapter?.notifyDataSetChanged()
                if (tabUrls.isEmpty()) dialog.dismiss()
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

        val zoomLevelText: TextView = view.findViewById(R.id.zoomLevelText)
        updateZoomLevelText(zoomLevelText)

        view.findViewById<ImageButton>(R.id.zoomInBtn).setOnClickListener {
            getCurrentWebViewFragment()?.let { fragment ->
                fragment.zoomIn()
                updateZoomLevelText(zoomLevelText)
            }
        }

        view.findViewById<ImageButton>(R.id.zoomOutBtn).setOnClickListener {
            getCurrentWebViewFragment()?.let { fragment ->
                fragment.zoomOut()
                updateZoomLevelText(zoomLevelText)
            }
        }

        view.findViewById<LinearLayout>(R.id.refreshBtn).setOnClickListener {
            getCurrentWebViewFragment()?.reload()
            animateLoading()
            dialog.dismiss()
        }

        val desktopToggle: SwitchCompat = view.findViewById(R.id.desktopToggle)
        desktopToggle.isChecked = prefs.getBoolean(KEY_DESKTOP_MODE, false)

        view.findViewById<LinearLayout>(R.id.desktopModeRow).setOnClickListener {
            desktopToggle.toggle()
            val enabled = desktopToggle.isChecked
            prefs.edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply()
            applyDesktopModeToAll(enabled)
            Toast.makeText(this, if (enabled) "Desktop mode enabled" else "Desktop mode disabled", Toast.LENGTH_SHORT).show()
        }

        val trackingToggle: SwitchCompat = view.findViewById(R.id.trackingToggle)
        trackingToggle.isChecked = prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true)

        view.findViewById<LinearLayout>(R.id.trackingProtectionRow).setOnClickListener {
            trackingToggle.toggle()
            prefs.edit().putBoolean(KEY_BLOCK_THIRD_PARTY, trackingToggle.isChecked).apply()
        }

        view.findViewById<LinearLayout>(R.id.cookieManageBtn).setOnClickListener {
            dialog.dismiss()
            showCookieManager()
        }

        view.findViewById<TextView>(R.id.closeMenuBtn).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun updateZoomLevelText(textView: TextView) {
        val scale = getCurrentWebViewFragment()?.getZoomLevel() ?: 1.0f
        textView.text = "${(scale * 100).toInt()}%"
    }

    private fun showCookieManager() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_cookies, null)
        dialog.setContentView(view)

        val blockToggle: SwitchCompat = view.findViewById(R.id.blockThirdPartyToggle)
        blockToggle.isChecked = prefs.getBoolean(KEY_BLOCK_THIRD_PARTY, true)

        view.findViewById<LinearLayout>(R.id.blockThirdPartyRow).setOnClickListener {
            blockToggle.toggle()
            prefs.edit().putBoolean(KEY_BLOCK_THIRD_PARTY, blockToggle.isChecked).apply()
        }

        view.findViewById<LinearLayout>(R.id.clearCookiesBtn).setOnClickListener {
            getCurrentWebViewFragment()?.getUrl()?.let { url ->
                Uri.parse(url).host?.let { domain ->
                    clearCookiesForDomain(domain)
                    Toast.makeText(this, "Cookies cleared for $domain", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            dialog.dismiss()
            showOptionsMenu()
        }

        dialog.show()
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
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

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
        override fun createFragment(position: Int): Fragment = WebViewFragment.newInstance(tabUrls[position])
    }

    // Data class for tab
    data class TabItem(val url: String, val title: String)

    // Simple Tab Adapter
    inner class TabAdapter(
        private val tabs: List<TabItem>,
        private val selectedPosition: Int,
        private val onTabSelected: (Int) -> Unit,
        private val onTabClosed: (Int) -> Unit
    ) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

        inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titleText: TextView = itemView.findViewById(R.id.tabTitleText)
            val closeBtn: ImageView = itemView.findViewById(R.id.closeBtn)
            val selectedIndicator: View = itemView.findViewById(R.id.selectedIndicator)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tab, parent, false)
            return TabViewHolder(view)
        }

        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
            val tab = tabs[position]
            
            // Set title
            holder.titleText.text = tab.title.take(12)
            
            // Highlight selected
            holder.selectedIndicator.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
            holder.itemView.setBackgroundColor(
                if (position == selectedPosition) Color.parseColor("#E3F2FD") else Color.WHITE
            )

            // Click on entire item to select tab
            holder.itemView.setOnClickListener {
                Log.d(TAG, "ItemView clicked at position: $position")
                onTabSelected(position)
            }

            // Click on close button
            holder.closeBtn.setOnClickListener {
                Log.d(TAG, "CloseBtn clicked at position: $position")
                onTabClosed(position)
            }
        }

        override fun getItemCount(): Int = tabs.size
    }
}
