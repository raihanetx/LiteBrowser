package com.browser.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BrowserViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowserViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
