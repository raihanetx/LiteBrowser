package com.litebrowser.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    private var _webView: WebView? = null
    val webView: WebView get() = _webView!!

    // Callbacks set by activity
    var onUrlChange: ((String) -> Unit)? = null
    var onViewReady: ((WebViewFragment) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        _webView = view.findViewById(R.id.webView)
        setupWebView()
        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { onUrlChange?.invoke(it) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load initial URL from arguments
        arguments?.getString("url")?.let { url ->
            webView.loadUrl(url)
        }
        // Notify activity that fragment is ready
        onViewReady?.invoke(this)
    }

    override fun onDestroyView() {
        webView.destroy()
        _webView = null
        onUrlChange = null
        onViewReady = null
        super.onDestroyView()
    }

    // Public methods for activity to control this WebView
    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun canGoBack(): Boolean = webView.canGoBack()
    fun goBack() = webView.goBack()
    fun canGoForward(): Boolean = webView.canGoForward()
    fun goForward() = webView.goForward()
    fun reload() = webView.reload()
    fun zoomIn() = webView.zoomIn()
    fun zoomOut() = webView.zoomOut()
    fun getUrl(): String? = webView.url
}
