package com.browser.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.browser.app.util.WebViewHolder
import com.browser.app.viewmodel.BrowserViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    viewModel: BrowserViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsState()
    val currentIndex by viewModel.currentTabIndex.collectAsState()
    val desktopMode by viewModel.desktopMode.collectAsState()
    val currentTab = if (currentIndex in tabs.indices) tabs[currentIndex] else null
    val currentUrl = currentTab?.url ?: ""

    var webView by remember { mutableStateOf<WebView?>(null) }
    var originalUserAgent by remember { mutableStateOf<String?>(null) }

    DisposableEffect(
        key1 = webView,
        key2 = currentUrl,
        key3 = desktopMode
    ) {
        val wv = webView ?: return@DisposableEffect onDispose { }

        // Configure settings
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.settings.loadWithOverviewMode = true
        wv.settings.useWideViewPort = true
        wv.settings.setSupportZoom(true)
        wv.settings.builtInZoomControls = true
        wv.settings.displayZoomControls = false

        // Remember original UA
        if (originalUserAgent == null) {
            originalUserAgent = wv.settings.userAgentString
        }

        // Apply desktop mode UA
        val baseUA = originalUserAgent ?: wv.settings.userAgentString
        val targetUA = if (desktopMode) baseUA.replace("Mobile", "") else baseUA
        if (wv.settings.userAgentString != targetUA) {
            wv.settings.userAgentString = targetUA
        }

        // Set clients
        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                viewModel.setProgress(newProgress)
                viewModel.setIsLoading(newProgress < 100)
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                icon?.let { viewModel.updateCurrentTabFavicon(it) }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                title?.let { viewModel.updateCurrentTabTitle(it) }
            }
        }

        wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                viewModel.setIsLoading(true)
                viewModel.setProgress(0)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                viewModel.setIsLoading(false)
                viewModel.setProgress(100)
                url?.let { viewModel.updateCurrentTabUrl(it) }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.let { view?.loadUrl(it.toString()) }
                return true
            }
        }

        // Load or reload as needed
        if (currentUrl.isNotEmpty() && currentUrl != "about:blank") {
            if (wv.url != currentUrl) {
                wv.loadUrl(currentUrl)
            } else if (wv.settings.userAgentString != targetUA) {
                // UA changed but URL same -> reload
                wv.reload()
            }
        } else if (currentUrl == "about:blank") {
            // Ensure blank page if needed
            if (wv.url == null || wv.url != "about:blank") {
                wv.loadDataWithBaseURL(
                    null,
                    "<html><body style='margin:0;display:flex;justify-content:center;align-items:center;height:100vh;background:#ffffff;color:#000000;font-family:sans-serif;font-size:18px;'>New Tab</body></html>",
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }

        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }.also { webView = it }
        },
        update = { wv ->
            WebViewHolder.setWebView(wv)
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            WebViewHolder.clearWebView()
        }
    }
}
