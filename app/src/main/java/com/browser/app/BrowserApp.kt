package com.browser.app

import android.app.Application
import android.webkit.CookieManager

/**
 * Browser Application class
 * Handles app-wide initialization smartly
 */
class BrowserApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeBrowser()
    }

    /**
     * Initialize browser settings
     * Called once when app starts
     */
    private fun initializeBrowser() {
        // Initialize cookie manager for persistent sessions
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            // Accept third-party cookies for better compatibility
            try {
                setAcceptThirdPartyCookies(null, true)
            } catch (e: Exception) {
                // Some devices may not support this
            }
        }
    }
}
