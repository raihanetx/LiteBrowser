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
import android.widget.TextView
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView

    var onUrlChange: ((String) -> Unit)? = null
    var onTitleChange: ((String) -> Unit)? = null

    private var initialUrl: String? = null
    private var desktopUserAgent: String = ""
    private var defaultUserAgent: String = ""

    companion object {
        fun newInstance(url: String): WebViewFragment {
            return WebViewFragment().apply {
                arguments = Bundle().apply { putString("url", url) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialUrl = arguments?.getString("url")
        defaultUserAgent = WebSettings.getDefaultUserAgent(requireContext())
        desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        webView = WebView(requireContext()).apply {
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
                // Enable text zoom for better readability
                textZoom = 100
            }

            // Store default user agent
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

        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialUrl?.let { webView.loadUrl(it) }
    }

    override fun onDestroyView() {
        try {
            webView.stopLoading()
            webView.settings.javaScriptEnabled = false
            webView.clearHistory()
            webView.clearCache(true)
            webView.loadUrl("about:blank")
            webView.onPause()
            webView.removeAllViews()
            webView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onUrlChange = null
        onTitleChange = null
        super.onDestroyView()
    }

    // Public methods for MainActivity to access
    fun getWebView(): WebView = webView

    fun loadUrl(url: String) {
        if (::webView.isInitialized) {
            webView.loadUrl(url)
        }
    }

    fun getUrl(): String? {
        return if (::webView.isInitialized) webView.url else null
    }

    fun canGoBack(): Boolean {
        return if (::webView.isInitialized) webView.canGoBack() else false
    }

    fun goBack() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        }
    }

    fun canGoForward(): Boolean {
        return if (::webView.isInitialized) webView.canGoForward() else false
    }

    fun goForward() {
        if (::webView.isInitialized && webView.canGoForward()) {
            webView.goForward()
        }
    }

    fun reload() {
        if (::webView.isInitialized) {
            webView.reload()
        }
    }

    fun zoomIn() {
        if (::webView.isInitialized) {
            // Try multiple zoom methods for better compatibility
            try {
                // Method 1: Use WebView zoomIn
                webView.zoomIn()
            } catch (e: Exception) {
                try {
                    // Method 2: Adjust text zoom
                    val currentZoom = webView.settings.textZoom
                    webView.settings.textZoom = minOf(currentZoom + 10, 200)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
    }

    fun zoomOut() {
        if (::webView.isInitialized) {
            try {
                // Method 1: Use WebView zoomOut
                webView.zoomOut()
            } catch (e: Exception) {
                try {
                    // Method 2: Adjust text zoom
                    val currentZoom = webView.settings.textZoom
                    webView.settings.textZoom = maxOf(currentZoom - 10, 50)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
    }

    fun getZoomLevel(): Float {
        return if (::webView.isInitialized) {
            try {
                webView.scale
            } catch (e: Exception) {
                webView.settings.textZoom / 100f
            }
        } else {
            1.0f
        }
    }

    fun setDesktopMode(enable: Boolean, userAgent: String) {
        if (::webView.isInitialized) {
            try {
                webView.settings.userAgentString = userAgent
                webView.settings.useWideViewPort = enable
                webView.settings.loadWithOverviewMode = !enable
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTitle(): String? {
        return if (::webView.isInitialized) webView.title else null
    }
}
