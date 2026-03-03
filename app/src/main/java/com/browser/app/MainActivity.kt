package com.browser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.browser.app.ui.components.BrowserScreen
import com.browser.app.ui.theme.BrowserTheme
import com.browser.app.viewmodel.BrowserViewModel
import com.browser.app.viewmodel.BrowserViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BrowserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create ViewModel with factory to provide Context
        val factory = BrowserViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[BrowserViewModel::class.java]

        setContent {
            BrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserScreen(viewModel = viewModel)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentTab = viewModel.getCurrentTab()
        if (currentTab?.canGoBack == true) {
            viewModel.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
