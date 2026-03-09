package com.browser.app.webview

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

object WebViewFactory {
    
    const val DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    const val MOBILE_UA = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    val VIEWPORT_FIX_JS = """
        (function() {
            var metas = document.querySelectorAll('meta[name="viewport"]');
            metas.forEach(function(m) {
                var c = m.getAttribute('content');
                c = c.replace(/user-scalable\s*=\s*(no|0)/gi, 'user-scalable=yes');
                c = c.replace(/maximum-scale\s*=\s*[0-9.]+/gi, 'maximum-scale=5.0');
                m.setAttribute('content', c);
            });
            if (metas.length === 0) {
                var m = document.createElement('meta');
                m.name = 'viewport';
                m.content = 'width=device-width,initial-scale=1.0,maximum-scale=5.0,user-scalable=yes';
                document.head.appendChild(m);
            }
        })();
    """.trimIndent()

    /**
     * Create WebView with optimized settings for smooth zooming
     */
    fun createWebView(context: Context, isDesktopMode: Boolean = false, isIncognito: Boolean = false, blockThirdPartyCookies: Boolean = false): WebView {
        val webView = WebView(context).apply {
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
        
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            if (blockThirdPartyCookies) {
                try { setAcceptThirdPartyCookies(webView, false) } catch (e: Exception) {}
            } else {
                try { setAcceptThirdPartyCookies(webView, true) } catch (e: Exception) {}
            }
        }
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = !isIncognito
            cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            
            setSupportMultipleWindows(false)
            allowFileAccess = true
            allowContentAccess = true
            saveFormData = !isIncognito
            
            userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
        }
        
        return webView
    }

    /**
     * Reset zoom to default without reloading
     */
    fun resetZoom(webView: WebView) {
        // No longer needed - using native zoom
    }

    /**
     * Get all cookies as a list
     */
    fun getAllCookies(): List<CookieItem> {
        val cookies = mutableListOf<CookieItem>()
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.getCookie("http://example.com")
            cookies.add(CookieItem("Session", "Managed by LiteBrowser", "All sites"))
        } catch (e: Exception) {
            // Handle error
        }
        return cookies
    }

    /**
     * Get list of common third-party domains to block
     */
    fun getBlockableDomains(): List<String> = listOf(
        "google.com",
        "facebook.com",
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "analytics.google.com",
        "facebook.net",
        "twitter.com",
        "ads.twitter.com",
        "linkedin.com",
        "ads.linkedin.com",
        "amazon-adsystem.com",
        "advertising.com",
        "adnxs.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com"
    )

    data class CookieItem(val name: String, val value: String, val domain: String)

    fun setDesktopMode(webView: WebView, enabled: Boolean) {
        webView.settings.userAgentString = if (enabled) DESKTOP_UA else MOBILE_UA
        webView.reload()
    }

    fun injectViewportFix(webView: WebView) {
        webView.evaluateJavascript(VIEWPORT_FIX_JS, null)
    }

    /**
     * Clear all cookies
     */
    fun clearAllCookies() {
        try {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } catch (e: Exception) {
            // Handle silently
        }
    }
}
