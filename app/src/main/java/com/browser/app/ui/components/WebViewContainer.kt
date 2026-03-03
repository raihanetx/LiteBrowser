package com.browser.app.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.browser.app.model.Tab
import com.browser.app.viewmodel.BrowserViewModel

@Composable
fun WebViewContainer(
    tab: Tab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            viewModel.createWebView(context, tab)
        },
        update = { webView ->
            // WebView is managed by ViewModel
        },
        modifier = modifier
    )
}
