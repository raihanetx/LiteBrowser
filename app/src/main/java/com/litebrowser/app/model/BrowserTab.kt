package com.litebrowser.app.model

import android.os.Bundle
import android.webkit.WebView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class BrowserTab(
    val id: Long,
    var webView: WebView? = null,
    var title: String = "New Tab",
    var url: String = "",
    var isLoading: Boolean = false,
    var progress: Int = 0,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var isDesktopMode: Boolean = false,
    var zoomLevel: Int = 100,
    var savedState: Bundle? = null
)

data class Shortcut(
    val name: String,
    val url: String,
    val color: Color,
    val icon: ImageVector
)
