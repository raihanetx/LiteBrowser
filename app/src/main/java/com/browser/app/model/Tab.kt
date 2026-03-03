package com.browser.app.model

import android.webkit.WebView
import java.util.UUID

data class Tab(
    val id: String = UUID.randomUUID().toString(),
    var url: String = "",
    var title: String = "New Tab",
    var favicon: android.graphics.Bitmap? = null,
    var isLoading: Boolean = false,
    var progress: Int = 0,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var desktopMode: Boolean = false,
    var zoomLevel: Float = 1.0f
) {
    @Transient
    var webView: WebView? = null
}
