package com.litebrowser.pro

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    private var webViewInstance: WebView? = null

    var onUrlChange: ((String) -> Unit)? = null
    var onTitleChange: ((String) -> Unit)? = null

    private var initialUrl: String? = null
    private var defaultUserAgent: String = ""
    private var isDesktopMode: Boolean = false
    private var currentZoomLevel: Float = 1.0f

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String): WebViewFragment {
            return WebViewFragment().apply {
                arguments = Bundle().apply { putString(ARG_URL, url) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialUrl = arguments?.getString(ARG_URL)
        defaultUserAgent = WebSettings.getDefaultUserAgent(requireContext())
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (webViewInstance != null) {
            return webViewInstance!!
        }

        webViewInstance = WebView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                useWideViewPort = true
                loadWithOverviewMode = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                allowFileAccess = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                textZoom = 100
            }

            defaultUserAgent = settings.userAgentString

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { onUrlChange?.invoke(it) }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.title?.let { title ->
                        if (title.isNotEmpty()) {
                            onTitleChange?.invoke(title)
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    title?.let {
                        if (it.isNotEmpty()) {
                            onTitleChange?.invoke(it)
                        }
                    }
                }
            }
        }

        return webViewInstance!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Notify activity
        (activity as? MainActivity)?.onFragmentCreated(this)
        
        // Load URL
        if (initialUrl != null && webViewInstance?.url == null) {
            webViewInstance?.loadUrl(initialUrl!!)
        }
    }

    override fun onResume() {
        super.onResume()
        webViewInstance?.onResume()
    }

    override fun onPause() {
        super.onPause()
        webViewInstance?.onPause()
    }

    override fun onDestroy() {
        cleanupWebView()
        super.onDestroy()
    }

    private fun cleanupWebView() {
        try {
            webViewInstance?.apply {
                stopLoading()
                settings.javaScriptEnabled = false
                clearHistory()
                clearCache(true)
                loadUrl("about:blank")
                onPause()
                removeAllViews()
                destroy()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        webViewInstance = null
        onUrlChange = null
        onTitleChange = null
    }

    // Public API
    fun getWebView(): WebView? = webViewInstance

    fun loadUrl(url: String) {
        webViewInstance?.loadUrl(url)
    }

    fun getUrl(): String? = webViewInstance?.url

    fun canGoBack(): Boolean = webViewInstance?.canGoBack() ?: false

    fun goBack() {
        webViewInstance?.let { if (it.canGoBack()) it.goBack() }
    }

    fun canGoForward(): Boolean = webViewInstance?.canGoForward() ?: false

    fun goForward() {
        webViewInstance?.let { if (it.canGoForward()) it.goForward() }
    }

    fun reload() {
        webViewInstance?.reload()
    }

    fun zoomIn(): Boolean {
        return webViewInstance?.let { wv ->
            try {
                val currentZoom = wv.settings.textZoom
                if (currentZoom < 200) {
                    wv.settings.textZoom = minOf(currentZoom + 10, 200)
                    currentZoomLevel = wv.settings.textZoom / 100f
                    return true
                }
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } ?: false
    }

    fun zoomOut(): Boolean {
        return webViewInstance?.let { wv ->
            try {
                val currentZoom = wv.settings.textZoom
                if (currentZoom > 50) {
                    wv.settings.textZoom = maxOf(currentZoom - 10, 50)
                    currentZoomLevel = wv.settings.textZoom / 100f
                    return true
                }
                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } ?: false
    }

    fun getZoomLevel(): Float {
        return webViewInstance?.settings?.textZoom?.div(100f) ?: currentZoomLevel
    }

    fun setDesktopMode(enable: Boolean, userAgent: String) {
        webViewInstance?.let { wv ->
            try {
                isDesktopMode = enable
                wv.settings.userAgentString = userAgent
                wv.settings.useWideViewPort = enable
                wv.settings.loadWithOverviewMode = !enable
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isDesktopModeEnabled(): Boolean = isDesktopMode

    fun getTitle(): String? = webViewInstance?.title
}
