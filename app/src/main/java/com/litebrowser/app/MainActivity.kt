package com.litebrowser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.litebrowser.app.ui.BrowserScreen
import com.litebrowser.app.ui.theme.BrowserTheme
import com.litebrowser.app.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BrowserTheme {
                BrowserScreen(viewModel = viewModel)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewModel.onLowMemory()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewModel.activeTab?.webView?.canGoBack() == true) {
            viewModel.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
