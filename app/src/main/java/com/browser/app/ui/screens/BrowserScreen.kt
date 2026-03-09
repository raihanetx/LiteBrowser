package com.browser.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.browser.app.ui.theme.BrowserTheme
import com.browser.app.ui.components.BrowserWebView
import com.browser.app.ui.components.TabManagerBottomSheet
import com.browser.app.ui.components.TopBar
import com.browser.app.util.WebViewHolder
import com.browser.app.viewmodel.BrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel()
) {
    val showTabManager by viewModel.showTabManager.collectAsState()
    val nightMode by viewModel.nightMode.collectAsState()

    BrowserTheme(darkTheme = nightMode) {
        BackHandler(enabled = !showTabManager) {
            WebViewHolder.getWebView()?.let { webView ->
                if (webView.canGoBack()) {
                    webView.goBack()
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(viewModel = viewModel)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BrowserWebView(viewModel = viewModel)
            }
        }

        if (showTabManager) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                TabManagerBottomSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.setShowTabManager(false) }
                )
            }
        }
    }
}
