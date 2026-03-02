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

    var onPageStarted:     (tabId: String, url: String) -> Unit                          = { _, _ -> }
    var onPageFinished:    (tabId: String, url: String, title: String) -> Unit           = { _, _, _ -> }
    var onProgressChanged: (tabId: String, progress: Int) -> Unit                        = { _, _ -> }
    var onHistoryChanged:  (tabId: String, canBack: Boolean, canFwd: Boolean) -> Unit    = { _, _, _ -> }
    var getTabZoomLevel:   (tabId: String) -> Int = { 100 }

    fun createWebView(tab: BrowserTab): WebView {
        val wv = WebView(context)

        wv.settings.apply {
            javaScriptEnabled                 = true
            domStorageEnabled                 = true
            databaseEnabled                   = true
            allowFileAccess                   = true
            loadWithOverviewMode              = true
            useWideViewPort                   = true
            setSupportZoom(false)
            builtInZoomControls               = false
            displayZoomControls               = false
            mediaPlaybackRequiresUserGesture  = false
            mixedContentMode                  = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode                         = WebSettings.LOAD_DEFAULT
        }

        applyUserAgent(wv, tab.isDesktopMode)

        wv.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                onPageStarted(tab.id, url)
                onHistoryChanged(tab.id, view.canGoBack(), view.canGoForward())
            }

            override fun onPageFinished(view: WebView, url: String) {
                val zoomLevel = getTabZoomLevel(tab.id)
                applyZoom(view, zoomLevel)
                
                handler.postDelayed({
                    applyZoom(view, zoomLevel)
                }, 100)

                onPageFinished(tab.id, url, view.title ?: url)
                onHistoryChanged(tab.id, view.canGoBack(), view.canGoForward())
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
                onProgressChanged(tab.id, newProgress)
            }
            override fun onReceivedTitle(view: WebView, title: String) {
                onPageFinished(tab.id, view.url ?: "", title)
            }
        }

        return wv
    }

    fun pauseWebView(wv: WebView) {
        wv.onPause()
        wv.pauseTimers()
    }

    fun resumeWebView(wv: WebView) {
        wv.resumeTimers()
        wv.onResume()
    }

    fun applyZoom(wv: WebView, zoomPercent: Int) {
        val scale = zoomPercent / 100.0
        wv.evaluateJavascript(
            "document.documentElement.style.zoom = '$scale'",
            null
        )
    }

    fun applyUserAgent(wv: WebView, desktopMode: Boolean, reload: Boolean = false) {
        wv.settings.userAgentString = if (desktopMode) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        } else {
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }
        wv.settings.useWideViewPort = true
        wv.settings.loadWithOverviewMode = true
        
        if (reload) {
            wv.clearCache(true)
            wv.reload()
        }
    }
}
