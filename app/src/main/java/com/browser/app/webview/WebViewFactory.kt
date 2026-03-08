package com.browser.app.webview

import android.content.Context
import android.content.res.Configuration
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

    fun getSystemFontScale(context: Context): Int {
        val scale = context.resources.configuration.fontScale
        return (scale * 100).toInt()
    }

    fun getDefaultTextZoom(context: Context, savedZoom: Int): Int {
        return if (savedZoom > 0) savedZoom else getSystemFontScale(context)
    }
    
    fun createWebView(context: Context, isDesktopMode: Boolean = false): WebView {
        val webView = WebView(context).apply {
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            
            setSupportMultipleWindows(false)
            allowFileAccess = true
            allowContentAccess = true
            
            userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
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
}
