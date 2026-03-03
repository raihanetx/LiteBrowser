package com.browser.app.ui.components

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.browser.app.viewmodel.BrowserViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewManager(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab = viewModel.getCurrentTab()

    // Create a container that holds all WebViews
    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { container ->
            // Ensure all tab WebViews are created and attached
            viewModel.tabs.forEachIndexed { index, tab ->
                val webView = tab.webView ?: viewModel.createWebView(context, tab).also {
                    tab.webView = it
                }

                // Add to container if not already added
                if (webView.parent == null) {
                    container.addView(webView, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                }

                // Show only current tab, hide others
                val isCurrentTab = index == viewModel.currentTabIndex.value
                webView.visibility = if (isCurrentTab) View.VISIBLE else View.GONE

                // Apply desktop mode scripts when tab becomes visible
                if (isCurrentTab && tab.desktopMode) {
                    viewModel.injectDesktopModeScripts(webView)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
