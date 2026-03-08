package com.browser.app.model

import android.webkit.WebView

data class BrowserTab(
    val id: Int,
    var title: String = "New Tab",
    var url: String = "",
    var webView: WebView? = null,
    var isSelected: Boolean = false,
    var zoomLevel: Float = 1.0f,
    var isDesktopMode: Boolean = false
)
