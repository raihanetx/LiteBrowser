package com.litebrowser.app.manager

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.litebrowser.app.model.BrowserTab

class TabManager(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())

    var onPageStarted: (tabId: Long, url: String) -> Unit = { _, _ -> }
    var onPageFinished: (tabId: Long, url: String, title: String) -> Unit = { _, _, _ -> }
    var onProgressChanged: (tabId: Long, progress: Int) -> Unit = { _, _ -> }
    var onHistoryChanged: (tabId: Long, canBack: Boolean, canFwd: Boolean) -> Unit = { _, _, _ -> }
    var getTabZoomLevel: (tabId: Long) -> Int = { 100 }
    var getTabDesktopMode: (tabId: Long) -> Boolean = { false }

    fun createWebView(tab: BrowserTab): WebView {
        val wv = WebView(context)

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        applyUserAgentSettings(wv, tab.isDesktopMode)

        val tabId = tab.id

        wv.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                onPageStarted(tabId, url)
                onHistoryChanged(tabId, view.canGoBack(), view.canGoForward())
            }

            override fun onPageFinished(view: WebView, url: String) {
                val zoomLevel = getTabZoomLevel(tabId)
                
                applyZoomImmediate(view, zoomLevel)
                
                handler.postDelayed({
                    applyZoomImmediate(view, zoomLevel)
                }, 100)
                
                handler.postDelayed({
                    applyZoomImmediate(view, zoomLevel)
                }, 300)

                onPageFinished(tabId, url, view.title ?: url)
                onHistoryChanged(tabId, view.canGoBack(), view.canGoForward())
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                onProgressChanged(tabId, newProgress)
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                onPageFinished(tabId, view.url ?: "", title)
            }
        }

        return wv
    }

    fun pauseWebView(wv: WebView) {
        try {
            wv.onPause()
            wv.pauseTimers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resumeWebView(wv: WebView) {
        try {
            wv.resumeTimers()
            wv.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyZoomImmediate(wv: WebView?, zoomPercent: Int) {
        if (wv == null) return
        try {
            val scale = zoomPercent / 100.0
            wv.evaluateJavascript("document.documentElement.style.zoom = '$scale'", null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyUserAgentSettings(wv: WebView, desktopMode: Boolean) {
        wv.settings.userAgentString = if (desktopMode) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
        wv.settings.useWideViewPort = true
        wv.settings.loadWithOverviewMode = true
    }
}
