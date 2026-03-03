package com.browser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.browser.app.ui.components.BrowserScreen
import com.browser.app.ui.theme.BrowserTheme
import com.browser.app.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: BrowserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
    
    override fun onBackPressed() {
        val currentTab = viewModel.getCurrentTab()
        if (currentTab?.canGoBack == true) {
            viewModel.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
