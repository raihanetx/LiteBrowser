package com.browser.app.webview

import android.content.Context
import android.content.res.Configuration
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
     * Initialize cookie manager for persistent cookies
     * Call this once in Application class or MainActivity
     */
    fun initCookieManager() {
        val cookieManager = CookieManager.getInstance()
        // Enable cookies
        cookieManager.setAcceptCookie(true)
        // Accept third-party cookies
        cookieManager.setAcceptThirdPartyCookies(null, true)
    }

    /**
     * Enable/disable cookies for a specific WebView
     */
    fun setCookiesEnabled(webView: WebView?, enabled: Boolean) {
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = enabled
    }

    /**
     * Remove all cookies (for clear data)
     */
    fun clearAllCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    /**
     * Get all cookies as string
     */
    fun getCookies(): String {
        val cookieManager = CookieManager.getInstance()
        return cookieManager.getCookie("") ?: ""
    }

    fun getSystemFontScale(context: Context): Int {
        val scale = context.resources.configuration.fontScale
        return (scale * 100).toInt()
    }

    fun getDefaultTextZoom(context: Context, savedZoom: Int): Int {
        return if (savedZoom > 0) savedZoom else getSystemFontScale(context)
    }
    
    /**
     * Create WebView with standard settings
     * @param context Android context
     * @param isDesktopMode Whether to use desktop User-Agent
     * @param isIncognito Whether this is an incognito WebView (no cookies/cache)
     */
    fun createWebView(context: Context, isDesktopMode: Boolean = false, isIncognito: Boolean = false): WebView {
        val webView = WebView(context).apply {
            if (isIncognito) {
                // Use private context for incognito
                // Note: For true incognito, use WebView(context, null, WebView.ALWAYS_ALLOW_THIRD_PARTY_COOKIES)
            }
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = !isIncognito  // Disable DB in incognito
            cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
            
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            
            setSupportMultipleWindows(false)
            allowFileAccess = true
            allowContentAccess = true
            
            // Save form data in normal mode, not in incognito
            saveFormData = !isIncognito
            
            userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
        }
        
        // For incognito, use separate CookieManager
        if (isIncognito) {
            // Disable cookies for incognito
            CookieManager.getInstance().setAcceptCookie(false)
        }
        
        return webView
    }

    fun setDesktopMode(webView: WebView, enabled: Boolean) {
        webView.settings.userAgentString = if (enabled) DESKTOP_UA else MOBILE_UA
        webView.reload()
    }

    fun injectViewportFix(webView: WebView) {
        webView.evaluateJavascript(VIEWPORT_FIX_JS, null)
    }

    /**
     * Clear cache for a WebView
     */
    fun clearCache(webView: WebView?) {
        webView?.let {
            it.clearCache(true)
            it.clearFormData()
            it.clearHistory()
        }
    }

    /**
     * Clear all browser data (cookies, cache, history, form data)
     */
    fun clearAllBrowserData() {
        clearAllCookies()
        // Cache and history are cleared per WebView
    }
}
