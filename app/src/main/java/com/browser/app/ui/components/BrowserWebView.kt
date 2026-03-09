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
    val currentTab by viewModel.tabs.collectAsState()
    val currentIndex by viewModel.currentTabIndex.collectAsState()
    val nightMode by viewModel.nightMode.collectAsState()
    val desktopMode by viewModel.desktopMode.collectAsState()

    var webViewState by remember { mutableStateOf<WebView?>(null) }

    val currentUrl = if (currentIndex in currentTab.indices) currentTab[currentIndex].url else ""

    DisposableEffect(
        key1 = currentUrl,
        key2 = nightMode,
        key3 = desktopMode
    ) {
        webViewState?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            val userAgent = if (desktopMode) {
                settings.userAgentString.replace("Mobile", "")
            } else {
                settings.userAgentString
            }
            settings.userAgentString = userAgent

            webChromeClient = object : WebChromeClient() {
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

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    viewModel.setIsLoading(true)
                    viewModel.setProgress(0)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.setIsLoading(false)
                    viewModel.setProgress(100)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.let { url ->
                        view?.loadUrl(url.toString())
                    }
                    return true
                }
            }

            if (currentUrl.isNotEmpty() && currentUrl != "about:blank") {
                loadUrl(currentUrl)
            } else if (currentUrl == "about:blank") {
                loadDataWithBaseURL(
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
            }.also { webViewState = it }
        },
        update = { webView ->
            WebViewHolder.setWebView(webView)

            if (currentUrl.isNotEmpty() && currentUrl != webView.url && currentUrl != "about:blank") {
                webView.loadUrl(currentUrl)
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            WebViewHolder.clearWebView()
        }
    }
}
