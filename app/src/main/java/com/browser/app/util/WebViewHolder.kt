package com.browser.app.util

import android.webkit.WebView

object WebViewHolder {
    private var currentWebView: WebView? = null

    fun setWebView(webView: WebView?) {
        currentWebView = webView
    }

    fun getWebView(): WebView? {
        return currentWebView
    }

    fun clearWebView() {
        currentWebView = null
    }
}
