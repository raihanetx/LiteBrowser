package com.browser.app.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "browser_settings"
        private const val KEY_TEXT_ZOOM = "text_zoom"
        private const val KEY_DEFAULT_PAGE_ZOOM = "default_page_zoom"
        private const val KEY_DOMAIN_ZOOM_PREFIX = "domain_zoom_"
        private const val DEFAULT_TEXT_ZOOM = 100
        private const val DEFAULT_PAGE_ZOOM = 100
        private const val MIN_TEXT_ZOOM = 80
        private const val MAX_TEXT_ZOOM = 200
    }

    // Text Zoom (80% - 200%)
    fun getTextZoom(): Int {
        return prefs.getInt(KEY_TEXT_ZOOM, DEFAULT_TEXT_ZOOM)
    }

    fun setTextZoom(zoom: Int) {
        prefs.edit().putInt(KEY_TEXT_ZOOM, zoom.coerceIn(MIN_TEXT_ZOOM, MAX_TEXT_ZOOM)).apply()
    }

    // Default Page Zoom for new pages
    fun getDefaultPageZoom(): Int {
        return prefs.getInt(KEY_DEFAULT_PAGE_ZOOM, DEFAULT_PAGE_ZOOM)
    }

    fun setDefaultPageZoom(zoom: Int) {
        prefs.edit().putInt(KEY_DEFAULT_PAGE_ZOOM, zoom.coerceIn(50, 300)).apply()
    }

    // Per-Domain Zoom Memory
    fun getDomainZoom(domain: String): Int {
        return prefs.getInt(KEY_DOMAIN_ZOOM_PREFIX + domain, getDefaultPageZoom())
    }

    fun setDomainZoom(domain: String, zoom: Int) {
        prefs.edit().putInt(KEY_DOMAIN_ZOOM_PREFIX + domain, zoom).apply()
    }

    fun clearDomainZoom(domain: String) {
        prefs.edit().remove(KEY_DOMAIN_ZOOM_PREFIX + domain).apply()
    }

    // Extract domain from URL
    fun extractDomain(url: String): String {
        return try {
            val cleanUrl = url.replace("^(https?://)".toRegex(), "")
                .replace("^(www\\.)".toRegex(), "")
            cleanUrl.split("/")[0]
                .split(":")[0]
        } catch (e: Exception) {
            url
        }
    }
}
