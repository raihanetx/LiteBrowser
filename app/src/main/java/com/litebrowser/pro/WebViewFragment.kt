package com.litebrowser.pro

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    private var webView: WebView? = null

    var onUrlChange: ((String) -> Unit)? = null
    var onTitleChange: ((String) -> Unit)? = null

    private var initialUrl: String? = null
    private var defaultUserAgent: String = ""
    private var isDesktopMode: Boolean = false

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
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (webView != null) return webView!!

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
                    view?.title?.let { if (it.isNotEmpty()) onTitleChange?.invoke(it) }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
            }

            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    title?.let { if (it.isNotEmpty()) onTitleChange?.invoke(it) }
                }
            }
        }

        return webView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Notify activity
        (activity as? MainActivity)?.onFragmentCreated(this)
        
        // Load URL
        if (initialUrl != null && webView?.url == null) {
            webView?.loadUrl(initialUrl!!)
        }
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onDestroy() {
        try {
            webView?.apply {
                stopLoading()
                settings.javaScriptEnabled = false
                clearHistory()
                clearCache(true)
                loadUrl("about:blank")
                onPause()
                removeAllViews()
                destroy()
            }
        } catch (e: Exception) { e.printStackTrace() }
        webView = null
        onUrlChange = null
        onTitleChange = null
        super.onDestroy()
    }

    // Public API
    fun getWebView(): WebView? = webView

    fun loadUrl(url: String) { webView?.loadUrl(url) }

    fun getUrl(): String? = webView?.url

    fun canGoBack(): Boolean = webView?.canGoBack() ?: false

    fun goBack() { webView?.let { if (it.canGoBack()) it.goBack() } }

    fun reload() { webView?.reload() }

    fun zoomIn() {
        webView?.let { wv ->
            val current = wv.settings.textZoom
            if (current < 200) wv.settings.textZoom = minOf(current + 10, 200)
        }
    }

    fun zoomOut() {
        webView?.let { wv ->
            val current = wv.settings.textZoom
            if (current > 50) wv.settings.textZoom = maxOf(current - 10, 50)
        }
    }

    fun getZoomLevel(): Float = (webView?.settings?.textZoom ?: 100) / 100f

    fun setDesktopMode(enable: Boolean, userAgent: String) {
        webView?.settings?.userAgentString = userAgent
        webView?.settings?.useWideViewPort = enable
        webView?.settings?.loadWithOverviewMode = !enable
        isDesktopMode = enable
    }
}
