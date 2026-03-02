package com.litebrowser.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.litebrowser.app.model.BrowserTab

@Composable
fun WebViewContainer(
    tab: BrowserTab,
    modifier: Modifier = Modifier,
) {
    val webView = tab.webView ?: return

    AndroidView(
        factory = { webView },
        modifier = modifier,
        update = { },
    )
}
