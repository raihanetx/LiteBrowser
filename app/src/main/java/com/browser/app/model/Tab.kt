package com.browser.app.model

import android.graphics.Bitmap

data class Tab(
    val id: Int,
    var url: String = "about:blank",
    var title: String = "New Tab",
    var favicon: Bitmap? = null
)
