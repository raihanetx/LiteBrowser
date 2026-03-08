# Zoom and Desktop Mode - Complete Implementation Guide

This document provides a detailed, step-by-step explanation of how the **Zoom Feature** and **Desktop Mode** were implemented in LiteBrowser. Each piece of code is explained with its purpose and how it works.

---

# TABLE OF CONTENTS

1. [Zoom Feature - Complete Guide](#zoom-feature---complete-guide)
   - [Concept](#concept)
   - [UI Layout (XML)](#ui-layout-xml)
   - [Zoom Constants](#zoom-constants)
   - [Zoom Application (JavaScript Injection)](#zoom-application-javascript-injection)
   - [Where Zoom is Applied](#where-zoom-is-applied)
   - [Complete Flow](#complete-flow)
   - [Code Summary](#code-summary)

2. [Desktop Mode - Complete Guide](#desktop-mode---complete-guide)
   - [Concept](#concept-1)
   - [User-Agent Strings](#user-agent-strings)
   - [Creating WebView with Desktop Mode](#creating-webview-with-desktop-mode)
   - [Toggling Desktop Mode](#toggling-desktop-mode)
   - [Applying to New Tabs](#applying-to-new-tabs)
   - [Persistence](#persistence)
   - [Complete Flow](#complete-flow-1)
   - [Code Summary](#code-summary-1)

---

# ZOOM FEATURE - COMPLETE GUIDE

## Concept

The zoom feature works by using **JavaScript injection** to modify the webpage's viewport meta tag and apply CSS zoom. This scales the **entire page** (images, text, layout, UI) - not just the text like simple font scaling.

**Why JavaScript?**
- Android's `setInitialScale()` only works when the page first loads
- Android's `setTextZoom()` only scales text, not images/layout
- JavaScript injection can dynamically change zoom at any time and works more reliably

---

## UI Layout (XML)

The zoom slider is placed inside the side menu (drawer). Here's the XML from `activity_main.xml`:

```xml
<!-- Zoom Slider in the Menu -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Zoom"
    android:textSize="14sp"
    android:textColor="#666666"
    android:paddingTop="16dp"
    android:paddingBottom="8dp" />

<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingHorizontal="8dp">

    <!-- Minimum zoom label (50%) -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="50%"
        android:textSize="12sp"
        android:textColor="#666666" />

    <!-- Zoom SeekBar - the main zoom control -->
    <SeekBar
        android:id="@+id/zoomSeekBar"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:min="50"
        android:max="150"
        android:progress="100"
        android:progressTint="#2196F3"
        android:thumbTint="#2196F3" />

    <!-- Maximum zoom label (150%) -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="150%"
        android:textSize="12sp"
        android:textColor="#666666" />

</LinearLayout>

<!-- Shows current zoom percentage -->
<TextView
    android:id="@+id/zoomPercentage"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="100%"
    android:textSize="14sp"
    android:textColor="#333333"
    android:gravity="center"
    android:paddingBottom="8dp" />
```

**Key Points:**
- `android:min="50"` = Minimum 50%
- `android:max="150"` = Maximum 150%
- `android:progress="100"` = Default 100%
- The SeekBar ID is `zoomSeekBar`
- The percentage display ID is `zoomPercentage`

---

## Zoom Constants

In `MainActivity.kt`, we define these constants:

```kotlin
companion object {
    private const val PREFS_NAME = "browser_prefs"
    private const val KEY_PAGE_ZOOM = "page_zoom_percent"
    private const val ZOOM_MIN = 50       // Minimum 50%
    private const val ZOOM_MAX = 150     // Maximum 150%
    private const val ZOOM_DEFAULT = 100 // Default 100%
}
```

| Constant | Value | Purpose |
|----------|-------|---------|
| `KEY_PAGE_ZOOM` | `"page_zoom_percent"` | SharedPreferences key to save zoom level |
| `ZOOM_MIN` | `50` | Minimum zoom percentage |
| `ZOOM_MAX` | `150` | Maximum zoom percentage |
| `ZOOM_DEFAULT` | `100` | Default zoom (100% = normal) |

---

## Zoom Application (JavaScript Injection)

This is the **core of the zoom feature**. The function `applyPageZoomJS()` injects JavaScript into the WebView to scale the entire page.

```kotlin
/**
 * Applies zoom to the WebView using JavaScript
 * This scales the ENTIRE page (layout, images, text, UI)
 * 
 * @param webView The WebView to apply zoom to
 * @param percent The zoom percentage (50-150)
 */
private fun applyPageZoomJS(webView: WebView?, percent: Int) {
    webView?.let { wv ->
        // Convert percentage to decimal (e.g., 100% = 1.0, 150% = 1.5)
        val scaleValue = percent.toFloat() / 100f
        
        // JavaScript code to inject into the webpage
        val js = """
            (function() {
                // STEP 1: Modify viewport meta tag
                var meta = document.querySelector('meta[name="viewport"]');
                var content = 'width=device-width, initial-scale=$scaleValue, maximum-scale=$scaleValue, minimum-scale=$scaleValue, user-scalable=yes';
                
                if (meta) {
                    // If viewport exists, update it
                    meta.setAttribute('content', content);
                } else {
                    // If no viewport, create one
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = content;
                    document.head.appendChild(meta);
                }
                
                // STEP 2: Apply CSS zoom to body and html elements
                // This provides additional zoom effect beyond viewport
                document.body.style.zoom = '${(scaleValue * 100)}%';
                document.documentElement.style.zoom = '${(scaleValue * 100)}%';
            })();
        """.trimIndent()
        
        // Execute the JavaScript in the WebView
        wv.evaluateJavascript(js, null)
    }
}
```

### How the JavaScript Works:

**Step 1: Viewport Meta Modification**
```javascript
var meta = document.querySelector('meta[name="viewport"]');
var content = 'width=device-width, initial-scale=1.5, maximum-scale=1.5, minimum-scale=1.5, user-scalable=yes';

if (meta) {
    meta.setAttribute('content', content);
} else {
    // Create new meta tag if doesn't exist
    meta = document.createElement('meta');
    meta.name = 'viewport';
    meta.content = content;
    document.head.appendChild(meta);
}
```

This modifies the viewport meta tag to:
- `initial-scale=1.5` = Start at 150% zoom
- `maximum-scale=1.5` = Don't allow zoom in beyond 150%
- `minimum-scale=1.5` = Don't allow zoom out beyond 150%
- `user-scalable=yes` = Allow user to zoom (we control the level)

**Step 2: CSS Zoom**
```javascript
document.body.style.zoom = '150%';
document.documentElement.style.zoom = '150%';
```

This applies CSS zoom to the entire page as a fallback and additional scaling method.

---

## Where Zoom is Applied

Zoom is applied in **3 places** to ensure it always works:

### 1. When User Moves the Slider (in setupListeners)

```kotlin
// In setupListeners() - when user drags the zoom slider
zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {  // Only if user is dragging, not programmatic changes
            val zoomPercent = progress.coerceIn(ZOOM_MIN, ZOOM_MAX)  // Ensure bounds
            applyPageZoom(zoomPercent)  // Apply the zoom
            zoomPercentage.text = "$zoomPercent%"  // Update display
        }
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
})
```

### 2. When Page Finishes Loading (in createNewTab's WebViewClient)

```kotlin
// In createNewTab() - WebViewClient.onPageFinished
override fun onPageFinished(view: WebView?, url: String?) {
    super.onPageFinished(view, url)
    
    // Apply saved zoom after page loads
    val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
    applyPageZoomJS(view, savedZoom)
}
```

### 3. When Switching Tabs (in showTab)

```kotlin
// In showTab() - when user switches to a different tab
private fun showTab(tabId: Int) {
    browserManager.setCurrentTab(tabId)
    tabSlider.isVisible = false
    
    // Show/hide WebViews (no reload)
    for (i in 0 until webViewContainer.childCount) {
        webViewContainer.getChildAt(i).isVisible = 
            webViewContainer.getChildAt(i).tag == tabId
    }
    
    browserManager.getCurrentTab()?.let { tab ->
        urlEditText.setText(tab.url)
        tab.webView?.let { wv ->
            // Apply zoom to the switched-to tab
            val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
            applyPageZoomJS(wv, savedZoom)
        }
    }
    
    updateMenuState()
    hideKeyboard()
}
```

### 4. Helper Function (applyPageZoom)

```kotlin
/**
 * Main function called when user changes zoom
 * Saves to preferences and applies to WebView
 */
private fun applyPageZoom(percent: Int) {
    // Ensure zoom is within bounds
    val zoomPercent = percent.coerceIn(ZOOM_MIN, ZOOM_MAX)
    
    // Save to SharedPreferences for persistence
    prefs.edit().putInt(KEY_PAGE_ZOOM, zoomPercent).apply()
    
    // Apply to current WebView
    browserManager.getCurrentTab()?.webView?.let { wv ->
        applyPageZoomJS(wv, zoomPercent)
    }
    
    // Update the percentage display
    zoomPercentage.text = "$zoomPercent%"
}
```

---

## Complete Flow

```
User drags slider
       │
       ▼
onProgressChanged() callback
       │
       ▼
applyPageZoom(percent)
       │
       ├──► Save to SharedPreferences
       │
       └──► applyPageZoomJS(webView, percent)
                    │
                    ▼
            Convert % to decimal
                    │
                    ▼
            Inject JavaScript
                    │
                    ├──► Modify viewport meta
                    │
                    └──► Apply CSS zoom
```

---

## Code Summary

**All zoom-related code in MainActivity.kt:**

```kotlin
// ===== CONSTANTS =====
private const val KEY_PAGE_ZOOM = "page_zoom_percent"
private const val ZOOM_MIN = 50
private const val ZOOM_MAX = 150
private const val ZOOM_DEFAULT = 100

// ===== ZOOM SLIDER SETUP (in setupListeners) =====
zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val zoomPercent = progress.coerceIn(ZOOM_MIN, ZOOM_MAX)
            applyPageZoom(zoomPercent)
            zoomPercentage.text = "$zoomPercent%"
        }
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
})

// ===== APPLY ZOOM FUNCTION =====
private fun applyPageZoom(percent: Int) {
    val zoomPercent = percent.coerceIn(ZOOM_MIN, ZOOM_MAX)
    prefs.edit().putInt(KEY_PAGE_ZOOM, zoomPercent).apply()
    
    browserManager.getCurrentTab()?.webView?.let { wv ->
        applyPageZoomJS(wv, zoomPercent)
    }
    
    zoomPercentage.text = "$zoomPercent%"
}

// ===== JAVASCRIPT ZOOM IMPLEMENTATION =====
private fun applyPageZoomJS(webView: WebView?, percent: Int) {
    webView?.let { wv ->
        val scaleValue = percent.toFloat() / 100f
        
        val js = """
            (function() {
                var meta = document.querySelector('meta[name="viewport"]');
                var content = 'width=device-width, initial-scale=$scaleValue, maximum-scale=$scaleValue, minimum-scale=$scaleValue, user-scalable=yes';
                
                if (meta) {
                    meta.setAttribute('content', content);
                } else {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = content;
                    document.head.appendChild(meta);
                }
                
                document.body.style.zoom = '${(scaleValue * 100)}%';
                document.documentElement.style.zoom = '${(scaleValue * 100)}%';
            })();
        """.trimIndent()
        
        wv.evaluateJavascript(js, null)
    }
}

// ===== MENU STATE UPDATE (shows current zoom) =====
private fun updateMenuState() {
    val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
    zoomSeekBar.progress = savedZoom
    zoomPercentage.text = "$savedZoom%"
}
```

---

# DESKTOP MODE - COMPLETE GUIDE

## Concept

Desktop Mode changes the **User-Agent string** that the browser sends to websites. This makes websites treat the browser as a desktop computer instead of a mobile device, often showing the desktop version of websites instead of the mobile version.

**What is User-Agent?**
When your browser connects to a website, it sends a User-Agent header that identifies the browser and device. Websites use this to decide whether to show mobile or desktop versions.

---

## User-Agent Strings

In `WebViewFactory.kt`, we define two User-Agent strings:

```kotlin
object WebViewFactory {
    
    // Desktop User-Agent - pretends to be Chrome on Windows
    const val DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    
    // Mobile User-Agent - identifies as Chrome on Android
    const val MOBILE_UA = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
}
```

| String | Use |
|--------|-----|
| `DESKTOP_UA` | When Desktop Mode is ON |
| `MOBILE_UA` | When Desktop Mode is OFF (default) |

---

## Creating WebView with Desktop Mode

When a new WebView is created, we pass the desktop mode preference:

```kotlin
/**
 * Creates a configured WebView
 * 
 * @param context Android context
 * @param isDesktopMode true for desktop UA, false for mobile UA
 * @return Configured WebView
 */
fun createWebView(context: Context, isDesktopMode: Boolean = false): WebView {
    val webView = WebView(context).apply {
        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
    }
    
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        cacheMode = WebSettings.LOAD_DEFAULT
        
        useWideViewPort = true
        loadWithOverviewMode = true
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
        
        setSupportMultipleWindows(false)
        allowFileAccess = true
        allowContentAccess = true
        
        // KEY LINE: Set User-Agent based on desktop mode
        userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
    }
    
    return webView
}
```

The key line is:
```kotlin
userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
```

---

## Toggling Desktop Mode

When the user clicks "Desktop Mode" in the menu, this function runs:

```kotlin
/**
 * Toggles desktop mode on/off
 * Applies to ALL tabs and saves preference
 */
private fun toggleDesktopMode() {
    // STEP 1: Get current mode and toggle it
    val currentMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
    val newMode = !currentMode
    
    // STEP 2: Save the new preference
    prefs.edit().putBoolean(KEY_DESKTOP_MODE, newMode).apply()
    
    // STEP 3: Apply to ALL open tabs
    browserManager.getAllTabs().forEach { tab ->
        // Update the tab's desktop mode flag
        tab.isDesktopMode = newMode
        
        // Update the WebView's User-Agent
        tab.webView?.let { wv ->
            wv.settings.userAgentString = if (newMode) {
                WebViewFactory.DESKTOP_UA
            } else {
                WebViewFactory.MOBILE_UA
            }
            
            // Reload the page to apply the new User-Agent
            // (websites only see the new UA after reload)
            if (!wv.url.isNullOrEmpty()) {
                wv.reload()
            }
        }
    }
    
    // STEP 4: Save tabs to preserve the setting
    saveTabs()
    
    // STEP 5: Update the menu UI
    updateMenuState()
    
    // STEP 6: Show confirmation toast
    showToast(if (newMode) "Desktop Mode ON" else "Desktop Mode OFF")
}
```

### Step-by-Step Explanation:

1. **Get current mode**: Read from SharedPreferences
2. **Toggle**: Flip the boolean (true → false, false → true)
3. **Save preference**: Store in SharedPreferences so it persists
4. **Apply to all tabs**: Loop through all tabs and update each WebView
5. **Reload pages**: Call `reload()` so websites see the new User-Agent
6. **Save tabs**: Save tab data including desktop mode setting
7. **Update UI**: Update the menu checkbox/text
8. **Toast**: Show user feedback

---

## Applying to New Tabs

When creating a new tab, we read the desktop mode preference:

```kotlin
@SuppressLint("SetJavaScriptEnabled")
private fun createNewTab(loadUrlNow: Boolean = true) {
    // Read the GLOBAL desktop mode preference
    val isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
    
    // Create tab with the current desktop mode setting
    val tab = browserManager.createNewTab().apply {
        this.isDesktopMode = isDesktopMode
        this.url = ""
        this.title = "New Tab"
    }
    
    // Create WebView with the correct User-Agent
    val webView = WebViewFactory.createWebView(this, isDesktopMode).apply { 
        tag = tab.id 
    }
    tab.webView = webView
    
    // ... rest of WebView setup ...
    
    showTab(tab.id)
    
    if (loadUrlNow) {
        loadUrl(getString(R.string.default_url))
    }
}
```

---

## Persistence

Desktop mode is saved in two ways:

### 1. As a Global Preference

```kotlin
// In toggleDesktopMode()
prefs.edit().putBoolean(KEY_DESKTOP_MODE, newMode).apply()

// Key: "desktop_mode"
// Value: true = desktop mode, false = mobile mode
```

### 2. With Each Tab (for individual tab settings)

```kotlin
// In saveTabs()
val tabObj = JSONObject().apply {
    put("id", tab.id)
    put("url", tab.url)
    put("title", tab.title)
    put("isDesktopMode", tab.isDesktopMode)  // Saved with each tab
    put("isSelected", tab.isSelected)
}
```

### Reading on App Start

```kotlin
// In restoreTabs()
val isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)

// When creating new tab
val isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
val webView = WebViewFactory.createWebView(this, isDesktopMode)
```

---

## Complete Flow

```
User clicks "Desktop Mode" menu item
              │
              ▼
toggleDesktopMode() called
              │
              ├──► Get current mode from prefs
              │
              ├──► Toggle (true → false)
              │
              ├──► Save to SharedPreferences
              │
              ├──► For EACH tab:
              │         │
              │         ├──► Update tab.isDesktopMode
              │         │
              │         ├──► Set userAgentString
              │         │
              │         └──► Reload page
              │
              ├──► Save tabs
              │
              ├──► Update menu UI
              │
              └──► Show toast

Result: All tabs reload with desktop User-Agent
```

---

## Code Summary

**WebViewFactory.kt - User-Agent constants:**

```kotlin
object WebViewFactory {
    const val DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    const val MOBILE_UA = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
}
```

**WebViewFactory.kt - Create WebView:**

```kotlin
fun createWebView(context: Context, isDesktopMode: Boolean = false): WebView {
    val webView = WebView(context)
    
    webView.settings.apply {
        // ... other settings ...
        userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
    }
    
    return webView
}
```

**MainActivity.kt - Toggle Desktop Mode:**

```kotlin
private fun toggleDesktopMode() {
    val currentMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
    val newMode = !currentMode
    prefs.edit().putBoolean(KEY_DESKTOP_MODE, newMode).apply()
    
    browserManager.getAllTabs().forEach { tab ->
        tab.isDesktopMode = newMode
        tab.webView?.let { wv ->
            wv.settings.userAgentString = if (newMode) {
                WebViewFactory.DESKTOP_UA
            } else {
                WebViewFactory.MOBILE_UA
            }
            if (!wv.url.isNullOrEmpty()) {
                wv.reload()
            }
        }
    }
    
    saveTabs()
    updateMenuState()
    showToast(if (newMode) "Desktop Mode ON" else "Desktop Mode OFF")
}
```

**MainActivity.kt - Create New Tab:**

```kotlin
private fun createNewTab(loadUrlNow: Boolean = true) {
    val isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
    
    val tab = browserManager.createNewTab().apply {
        this.isDesktopMode = isDesktopMode
    }
    
    val webView = WebViewFactory.createWebView(this, isDesktopMode)
    tab.webView = webView
    
    // ... rest of code ...
}
```

**MainActivity.kt - Update Menu State:**

```kotlin
private fun updateMenuState() {
    val isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
    
    // Show checkmark if desktop mode is on
    menuDesktop.text = if (isDesktopMode) "Desktop Mode ✓" else "Desktop Mode"
    
    // Also update zoom display
    val savedZoom = prefs.getInt(KEY_PAGE_ZOOM, ZOOM_DEFAULT)
    zoomSeekBar.progress = savedZoom
    zoomPercentage.text = "$savedZoom%"
}
```

---

# HOW TO MODIFY

## Change Zoom Range

**Step 1:** Update constants in MainActivity.kt:
```kotlin
private const val ZOOM_MIN = 25  // Change from 50 to 25
private const val ZOOM_MAX = 200 // Change from 150 to 200
```

**Step 2:** Update SeekBar in activity_main.xml:
```xml
<SeekBar
    android:id="@+id/zoomSeekBar"
    android:min="25"
    android:max="200"
    android:progress="100" />
```

## Change Desktop User-Agent

**Step 1:** Update the constant in WebViewFactory.kt:
```kotlin
const val DESKTOP_UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
```

---

# TESTING CHECKLIST

## Zoom Feature Test
- [ ] Open menu (three dots)
- [ ] Drag zoom slider left (should zoom out)
- [ ] Drag zoom slider right (should zoom in)
- [ ] Verify 50% makes page smaller
- [ ] Verify 150% makes page larger
- [ ] Navigate to a new website - zoom should persist
- [ ] Close app and reopen - zoom should persist
- [ ] Switch between tabs - zoom should apply to each tab

## Desktop Mode Test
- [ ] Click "Desktop Mode" in menu
- [ ] Verify toast shows "Desktop Mode ON"
- [ ] Verify checkmark appears next to "Desktop Mode"
- [ ] Navigate to a website (should show desktop version)
- [ ] Create new tab - should also be in desktop mode
- [ ] Turn off Desktop Mode - should reload pages in mobile mode
- [ ] Close app and reopen - desktop mode should persist
- [ ] Check multiple websites - all should show desktop versions

---

# END OF DOCUMENTATION
