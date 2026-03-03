package com.browser.app.ui.components

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.browser.app.model.Tab
import com.browser.app.viewmodel.BrowserViewModel

@Composable
fun WebViewContainer(
    tab: Tab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Create WebView only once per tab and keep it in memory
    val webView = remember(tab.id) {
        tab.webView ?: viewModel.createWebView(context, tab)
    }

    // Use a FrameLayout to hold the WebView
    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Add WebView to container if not already added
                if (webView.parent == null) {
                    addView(webView, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                }
            }
        },
        update = { container ->
            // Ensure WebView is attached to this container
            if (webView.parent != container) {
                (webView.parent as? ViewGroup)?.removeView(webView)
                container.addView(webView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        },
        modifier = modifier
    )
}
