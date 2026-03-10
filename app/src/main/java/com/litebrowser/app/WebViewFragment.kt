package com.litebrowser.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    lateinit var webView: WebView
        private set

    var onUrlChange: ((String) -> Unit)? = null
    var onTitleChange: ((String) -> Unit)? = null
    var onProgress: ((Int) -> Unit)? = null

    private var initialUrl: String? = null
    private var position: Int = 0

    companion object {
        fun newInstance(url: String, position: Int): WebViewFragment {
            return WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putInt("position", position)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialUrl = arguments?.getString("url")
        position = arguments?.getInt("position", 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)
        webView = view.findViewById(R.id.webView)
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
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportMultipleWindows(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { onUrlChange?.invoke(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.title?.let { onTitleChange?.invoke(it) }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgress?.invoke(newProgress)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialUrl?.let { webView.loadUrl(it) }
        (activity as? MainActivity)?.onFragmentReady(position, this)
    }

    override fun onDestroyView() {
        webView.destroy()
        onUrlChange = null
        onTitleChange = null
        onProgress = null
        super.onDestroyView()
    }

    fun loadUrl(url: String) { webView.loadUrl(url) }
    fun canGoBack(): Boolean = webView.canGoBack()
    fun goBack() { webView.goBack() }
    fun canGoForward(): Boolean = webView.canGoForward()
    fun goForward() { webView.goForward() }
    fun reload() { webView.reload() }
    fun zoomIn() { webView.zoomIn() }
    fun zoomOut() { webView.zoomOut() }
    fun getUrl(): String? = webView.url
}
