package com.litebrowser.app

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var urlEditText: EditText
    private lateinit var tabButton: FrameLayout
    private lateinit var tabBadge: TextView
    private lateinit var menuButton: ImageButton
    private lateinit var loadingProgress: View
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

        prefs = getSharedPreferences("browser_prefs", Context.MODE_PRIVATE)
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        defaultUserAgent = WebSettings.getDefaultUserAgent(this)

        // Restore or create tabs
        if (savedInstanceState != null) {
            val savedUrls = savedInstanceState.getStringArrayList(SAVED_TABS)
            savedUrls?.forEach { tabUrls.add(it) }
        }
        if (tabUrls.isEmpty()) {
            tabUrls.add("https://www.google.com")
        }

        adapter = TabAdapter(this, tabUrls)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = MAX_TABS

        updateTabBadge()
        setupListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(SAVED_TABS, ArrayList(tabUrls))
    }

    private fun bindViews() {
        urlEditText = findViewById(R.id.urlEditText)
        tabButton = findViewById(R.id.tabButton)
        tabBadge = findViewById(R.id.tabBadge)
        menuButton = findViewById(R.id.menuButton)
        loadingProgress = findViewById(R.id.loadingProgress)
        viewPager = findViewById(R.id.viewPager)
    }

    private fun setupListeners() {
        urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                val input = urlEditText.text.toString().trim()
                if (input.isNotEmpty()) {
                    getCurrentFragment()?.loadUrl(formatUrl(input))
                    simulateLoading()
                }
                true
            } else {
                false
            }
        }

        tabButton.setOnClickListener { showTabsBottomSheet() }
        menuButton.setOnClickListener { showMenuBottomSheet() }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUrlForCurrentTab()
            }
        })
    }

    private fun getCurrentFragment(): WebViewFragment? {
        val tag = "f${viewPager.currentItem}"
        return supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment
    }

    private fun formatUrl(input: String): String {
        return if (!input.startsWith("http://") && !input.startsWith("https://")) {
            "https://$input"
        } else input
    }

    private fun addNewTab(url: String = "https://www.google.com", select: Boolean = true) {
        if (tabUrls.size >= MAX_TABS) {
            Toast.makeText(this, "Maximum $MAX_TABS tabs", Toast.LENGTH_SHORT).show()
            return
        }
        tabUrls.add(url)
        adapter.notifyItemInserted(tabUrls.size - 1)
        updateTabBadge()
        if (select) {
            viewPager.setCurrentItem(tabUrls.size - 1, true)
        }
    }

    private fun closeTab(position: Int) {
        if (tabUrls.size <= 1) {
            Toast.makeText(this, "Cannot close last tab", Toast.LENGTH_SHORT).show()
            return
        }
        tabUrls.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, tabUrls.size)
        updateTabBadge()
    }

    private fun updateTabBadge() {
        tabBadge.text = tabUrls.size.toString()
    }

    private fun updateUrlForCurrentTab() {
        getCurrentFragment()?.getUrl()?.let { urlEditText.setText(it) }
    }

    fun onFragmentReady(position: Int, fragment: WebViewFragment) {
        applyDesktopMode(fragment, prefs.getBoolean("desktop_mode", false))
        applyThirdPartyBlocking(fragment, prefs.getBoolean("block_third_party", true))

        fragment.onUrlChange = { newUrl ->
            if (viewPager.currentItem == position) {
                urlEditText.setText(newUrl)
            }
        }
    }

    private fun applyDesktopMode(fragment: WebViewFragment, enable: Boolean) {
        val ua = if (enable) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
        } else {
            defaultUserAgent
        }
        fragment.webView.settings.userAgentString = ua
    }

    private fun applyThirdPartyBlocking(fragment: WebViewFragment, block: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(fragment.webView, !block)
        }
    }

    private fun applyDesktopModeToAll(enable: Boolean) {
        for (i in tabUrls.indices) {
            val tag = "f$i"
            (supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment)?.let {
                applyDesktopMode(it, enable)
                it.webView.reload()
            }
        }
    }

    private fun applyThirdPartyBlockingToAll(block: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        for (i in tabUrls.indices) {
            val tag = "f$i"
            (supportFragmentManager.findFragmentByTag(tag) as? WebViewFragment)?.let {
                applyThirdPartyBlocking(it, block)
            }
        }
    }

    private fun simulateLoading() {
        loadingProgress.visibility = View.VISIBLE
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 800
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val lp = loadingProgress.layoutParams
            lp.width = (animation.animatedValue as Int * viewPager.width / 100)
            loadingProgress.layoutParams = lp
        }
        animator.start()
    }

    private fun showTabsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_tabs, null)
        dialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.tabsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
                return object : RecyclerView.ViewHolder(v) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder.itemView.setOnClickListener {
                    viewPager.setCurrentItem(position, true)
                    dialog.dismiss()
                    updateUrlForCurrentTab()
                }
                holder.itemView.findViewById<View>(R.id.closeBtn).setOnClickListener {
                    closeTab(position)
                    notifyDataSetChanged()
                }
            }

            override fun getItemCount() = tabUrls.size
        }

        view.findViewById<TextView>(R.id.addTabBtn).setOnClickListener {
            addNewTab()
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.doneBtnTabs).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showMenuBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        dialog.setContentView(view)

        view.findViewById<ImageButton>(R.id.zoomInBtn).setOnClickListener {
            getCurrentFragment()?.zoomIn()
        }
        view.findViewById<ImageButton>(R.id.zoomOutBtn).setOnClickListener {
            getCurrentFragment()?.zoomOut()
        }

        view.findViewById<LinearLayout>(R.id.refreshBtn).setOnClickListener {
            getCurrentFragment()?.reload()
            simulateLoading()
            dialog.dismiss()
        }

        val desktopToggle = view.findViewById<SwitchCompat>(R.id.desktopToggle)
        desktopToggle.isChecked = prefs.getBoolean("desktop_mode", false)
        view.findViewById<LinearLayout>(R.id.desktopModeRow).setOnClickListener {
            desktopToggle.toggle()
            prefs.edit().putBoolean("desktop_mode", desktopToggle.isChecked).apply()
            applyDesktopModeToAll(desktopToggle.isChecked)
        }

        val trackingToggle = view.findViewById<SwitchCompat>(R.id.trackingToggle)
        trackingToggle.isChecked = prefs.getBoolean("block_third_party", true)
        view.findViewById<LinearLayout>(R.id.trackingProtectionRow).setOnClickListener {
            trackingToggle.toggle()
            prefs.edit().putBoolean("block_third_party", trackingToggle.isChecked).apply()
            applyThirdPartyBlockingToAll(trackingToggle.isChecked)
        }

        view.findViewById<LinearLayout>(R.id.cookieManageBtn).setOnClickListener {
            dialog.dismiss()
            showCookiesBottomSheet()
        }

        view.findViewById<TextView>(R.id.closeMenuBtn).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showCookiesBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_cookies, null)
        dialog.setContentView(view)

        val blockToggle = view.findViewById<SwitchCompat>(R.id.blockThirdPartyToggle)
        blockToggle.isChecked = prefs.getBoolean("block_third_party", true)
        view.findViewById<LinearLayout>(R.id.blockThirdPartyRow).setOnClickListener {
            blockToggle.toggle()
            prefs.edit().putBoolean("block_third_party", blockToggle.isChecked).apply()
            applyThirdPartyBlockingToAll(!blockToggle.isChecked)
        }

        view.findViewById<LinearLayout>(R.id.clearCookiesBtn).setOnClickListener {
            val fragment = getCurrentFragment()
            val url = fragment?.webView?.url
            if (url != null) {
                val domain = Uri.parse(url).host
                domain?.let { clearCookies(it) }
                Toast.makeText(this, "Cookies cleared for $domain", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            dialog.dismiss()
            showMenuBottomSheet()
        }

        dialog.show()
    }

    private fun clearCookies(domain: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cookies = cookieManager.getCookie("https://$domain")
            if (!cookies.isNullOrEmpty()) {
                cookies.split(";").forEach { cookie ->
                    val name = cookie.trim().substringBefore("=")
                    val expired = "$name=; Max-Age=0; Path=/; Domain=$domain"
                    cookieManager.setCookie("https://$domain", expired)
                    cookieManager.setCookie("https://$domain", "$expired; Domain=.$domain")
                }
                cookieManager.flush()
            }
        }
    }

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
