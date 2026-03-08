package com.browser.app.utils

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Smart Preferences Manager
 * Handles all browser preferences efficiently
 */
class BrowserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "browser_prefs"
        
        // Keys
        private const val KEY_TABS = "saved_tabs"
        private const val KEY_CURRENT_TAB = "current_tab_id"
        private const val KEY_DESKTOP_MODE = "desktop_mode"
        private const val KEY_PAGE_ZOOM = "page_zoom_percent"
        private const val KEY_INCOGNITO_MODE = "incognito_mode"
        
        // Cookie Settings
        private const val KEY_BLOCK_THIRD_PARTY_COOKIES = "block_third_party_cookies"
        private const val KEY_BLOCKED_SITES = "blocked_cookie_sites"
        private const val KEY_ALLOWED_SITES = "allowed_cookie_sites"
        
        // Defaults
        private const val ZOOM_DEFAULT = 100
    }

    // ========== Desktop Mode ==========
    var isDesktopMode: Boolean
        get() = prefs.getBoolean(KEY_DESKTOP_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DESKTOP_MODE, value).apply()

    // ========== Incognito Mode ==========
    var isIncognitoMode: Boolean
        get() = prefs.getBoolean(KEY_INCOGNITO_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_INCOGNITO_MODE, value).apply()

    // ========== Page Zoom ==========
    var pageZoom: Int
        get() = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
        set(value) = prefs.edit().putInt(KEY_PAGE_ZOOM, value).apply()

    // ========== Cookie Settings ==========
    var blockThirdPartyCookies: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_THIRD_PARTY_COOKIES, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_THIRD_PARTY_COOKIES, value).apply()

    // ========== Blocked/Allowed Sites ==========
    fun getBlockedSites(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_SITES, emptySet()) ?: emptySet()
    }

    fun saveBlockedSites(sites: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_SITES, sites).apply()
    }

    fun getAllowedSites(): Set<String> {
        return prefs.getStringSet(KEY_ALLOWED_SITES, emptySet()) ?: emptySet()
    }

    fun saveAllowedSites(sites: Set<String>) {
        prefs.edit().putStringSet(KEY_ALLOWED_SITES, sites).apply()
    }

    // ========== Tab Management ==========
    
    /**
     * Save tabs to preferences
     * Only saves if NOT in incognito mode
     */
    fun saveTabs(tabs: List<TabData>, currentTabId: Int) {
        if (isIncognitoMode) return  // Don't save in incognito

        val tabsArray = JSONArray()
        tabs.forEach { tab ->
            val tabObj = JSONObject().apply {
                put("id", tab.id)
                put("url", tab.url)
                put("title", tab.title)
                put("isDesktopMode", tab.isDesktopMode)
                put("isSelected", tab.isSelected)
            }
            tabsArray.put(tabObj)
        }

        prefs.edit()
            .putString(KEY_TABS, tabsArray.toString())
            .putInt(KEY_CURRENT_TAB, currentTabId)
            .apply()
    }

    /**
     * Restore tabs from preferences
     * Returns null if incognito mode or no saved tabs
     */
    fun restoreTabs(): List<TabData>? {
        if (isIncognitoMode) return null

        val savedJson = prefs.getString(KEY_TABS, null) ?: return null
        
        return try {
            val tabsArray = JSONArray(savedJson)
            if (tabsArray.length() == 0) return null

            val tabs = mutableListOf<TabData>()
            for (i in 0 until tabsArray.length()) {
                val obj = tabsArray.getJSONObject(i)
                tabs.add(TabData(
                    id = obj.getInt("id"),
                    url = obj.optString("url", ""),
                    title = obj.optString("title", "New Tab"),
                    isDesktopMode = obj.optBoolean("isDesktopMode", false),
                    isSelected = obj.optBoolean("isSelected", false)
                ))
            }
            tabs
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get saved current tab ID
     */
    fun getSavedCurrentTabId(): Int = prefs.getInt(KEY_CURRENT_TAB, 0)

    /**
     * Clear all saved data (for clear data feature)
     */
    fun clearAllData() {
        prefs.edit()
            .remove(KEY_TABS)
            .remove(KEY_CURRENT_TAB)
            .apply()
    }

    /**
     * Data class for tab information
     */
    data class TabData(
        val id: Int,
        val url: String = "",
        val title: String = "New Tab",
        val isDesktopMode: Boolean = false,
        val isSelected: Boolean = false
    )
}
