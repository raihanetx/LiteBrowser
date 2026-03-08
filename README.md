# LiteBrowser - Implementation Documentation

This document explains in detail how each feature of the LiteBrowser Android app was implemented.

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Zoom Feature Implementation](#zoom-feature-implementation)
3. [Desktop Mode Implementation](#desktop-mode-implementation)
4. [Tab Persistence Implementation](#tab-persistence-implementation)
5. [Tab Memory (No Reload on Switch)](#tab-memory-no-reload-on-switch)
6. [Key Files Explained](#key-files-explained)
7. [How to Modify Features](#how-to-modify-features)

---

## Project Structure

```
browser_app/
├── app/src/main/
│   ├── java/com/browser/app/
│   │   ├── ui/
│   │   │   └── MainActivity.kt       # Main browser activity
│   │   ├── model/
│   │   │   ├── BrowserManager.kt    # Tab management
│   │   │   └── BrowserTab.kt        # Tab data class
│   │   └── webview/
│   │       └── WebViewFactory.kt     # WebView creation factory
│   └── res/
│       └── layout/
│           └── activity_main.xml     # UI layout
├── build.gradle
└── AndroidManifest.xml
```

---

## Zoom Feature Implementation

### Overview
The zoom feature uses JavaScript injection to scale the entire page (not just text). This provides real browser-like zoom behavior.

### How It Works

1. **Zoom Slider in Menu**
   - Located in `activity_main.xml` inside the drawer menu
   - Range: 50% to 150%
   - Default: 100%

2. **Zoom Application (MainActivity.kt)**
   ```kotlin
   private fun applyPageZoomJS(webView: WebView?, percent: Int) {
       val scaleValue = percent.toFloat() / 100f
       
       val js = """
           (function() {
               // Set viewport meta for zoom
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
               
               // Also apply CSS transform for additional zoom effect
               document.body.style.zoom = '${(scaleValue * 100)}%';
               document.documentElement.style.zoom = '${(scaleValue * 100)}%';
           })();
       """.trimIndent()
       
       wv.evaluateJavascript(js, null)
   }
   ```

3. **How the Zoom Works**
   - **Viewport Meta Injection**: Modifies the viewport meta tag to set `initial-scale`, `maximum-scale`, and `minimum-scale` to the same value, locking the zoom level
   - **CSS Zoom**: Also applies `zoom` CSS property to `body` and `documentElement` for additional scaling
   - **JavaScript Execution**: The JavaScript is injected into the WebView using `evaluateJavascript()`

### Key Constants
```kotlin
private const val ZOOM_MIN = 50      // 50% minimum
private const val ZOOM_MAX = 150     // 150% maximum
private const val ZOOM_DEFAULT = 100 // 100% default
private const val KEY_PAGE_ZOOM = "page_zoom_percent" // SharedPreferences key
```

### When Zoom is Applied
1. **On SeekBar Change**: When user moves the slider
2. **On Page Finished**: After a new page loads
3. **On Tab Switch**: When switching between tabs

---

## Desktop Mode Implementation

### Overview
Desktop mode changes the User-Agent string to make websites think the browser is running on a desktop computer.

### User-Agent Strings

**Mobile (Default):**
```
Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36
```

**Desktop:**
```
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
```

### How Desktop Mode Works

1. **WebViewFactory.kt** - Stores the User-Agent strings:
   ```kotlin
   const val DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
   const val MOBILE_UA = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
   ```

2. **Creating WebView with Desktop Mode**:
   ```kotlin
   fun createWebView(context: Context, isDesktopMode: Boolean = false): WebView {
       // ... other settings ...
       webView.settings.userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
       return webView
   }
   ```

3. **Toggling Desktop Mode** (MainActivity.kt):
   ```kotlin
   private fun toggleDesktopMode() {
       // Toggle global preference
       val currentMode = prefs.getBoolean(KEY_DESKTOP_MODE, false)
       val newMode = !currentMode
       prefs.edit().putBoolean(KEY_DESKTOP_MODE, newMode).apply()
       
       // Apply to all tabs
       browserManager.getAllTabs().forEach { tab ->
           tab.isDesktopMode = newMode
           tab.webView?.let { wv ->
               wv.settings.userAgentString = if (newMode) {
                   WebViewFactory.DESKTOP_UA
               } else {
                   WebViewFactory.MOBILE_UA
               }
               // Reload to apply the new User-Agent
               if (!wv.url.isNullOrEmpty()) {
                   wv.reload()
               }
           }
       }
       
       saveTabs()
       updateMenuState()
   }
   ```

### Why Desktop Mode is Global
- Desktop mode is stored as a global preference (`KEY_DESKTOP_MODE`)
- When toggled, it applies to ALL tabs at once
- This ensures consistent behavior across the browser

---

## Tab Persistence Implementation

### Overview
Tabs are saved to SharedPreferences when the app goes to background or is closed. They are restored when the app reopens.

### What is Saved for Each Tab
```kotlin
data class BrowserTab(
    val id: Int,              // Unique tab identifier
    var title: String,        // Page title
    var url: String,          // Current URL
    var webView: WebView?,    // The WebView instance (not persisted, recreated)
    var isSelected: Boolean,  // Is this the active tab
    var isDesktopMode: Boolean // Desktop mode for this tab
)
```

### Saving Tabs (saveTabs function)

```kotlin
private fun saveTabs() {
    val tabsArray = JSONArray()
    var currentTabId = -1
    
    browserManager.getAllTabs().forEach { tab ->
        val tabObj = JSONObject().apply {
            put("id", tab.id)
            put("url", tab.url)
            put("title", tab.title)
            put("isDesktopMode", tab.isDesktopMode)
            put("isSelected", tab.isSelected)
        }
        tabsArray.put(tabObj)
        
        if (tab.isSelected) {
            currentTabId = tab.id
        }
    }
    
    prefs.edit()
        .putString(KEY_TABS, tabsArray.toString())
        .putInt(KEY_CURRENT_TAB, currentTabId)
        .apply()
}
```

### Restoring Tabs (restoreTabs function)

```kotlin
private fun restoreTabs(): Boolean {
    val savedTabsJson = prefs.getString(KEY_TABS, null) ?: return false
    // ... parse JSON and recreate tabs ...
    
    for (i in 0 until tabsArray.length()) {
        // Create BrowserTab from saved data
        val tab = BrowserTab(
            id = tabId,
            url = url,
            title = title,
            isDesktopMode = tabDesktopMode,
            isSelected = isSelected
        )
        
        // Recreate WebView
        val webView = WebViewFactory.createWebView(this, tabDesktopMode)
        tab.webView = webView
        
        // Add to container
        webViewContainer.addView(webView)
        browserManager.addTab(tab)
        
        // Load saved URL
        if (tab.url.isNotEmpty()) {
            tab.webView?.loadUrl(tab.url)
        }
    }
    
    // Show the previously selected tab
    showTab(savedCurrentTabId)
}
```

### When Tabs are Saved
- `onStop()` - When app goes to background
- `onDestroy()` - When app is closing
- After closing a tab
- After toggling desktop mode

---

## Tab Memory (No Reload on Switch)

### Overview
When switching between tabs, the browser simply shows/hides the existing WebViews instead of recreating them. This keeps the page state (scroll position, form data, etc.) intact.

### How It Works

```kotlin
private fun showTab(tabId: Int) {
    browserManager.setCurrentTab(tabId)
    tabSlider.isVisible = false
    
    // Show/hide WebViews - NO RECREATION
    for (i in 0 until webViewContainer.childCount) {
        webViewContainer.getChildAt(i).isVisible = 
            webViewContainer.getChildAt(i).tag == tabId
    }
    
    // Update URL bar
    browserManager.getCurrentTab()?.let { tab ->
        urlEditText.setText(tab.url)
    }
    
    updateMenuState()
    hideKeyboard()
}
```

### Why This Works
1. **All WebViews are always in memory** - Each tab has its own WebView added to the container
2. **Visibility control only** - `showTab()` just changes `isVisible` property
3. **No destruction on switch** - WebViews are only destroyed when the tab is closed
4. **State preserved** - Scroll position, form inputs, JavaScript state all preserved

### Trade-offs
- **Pros**: Instant switching, state preserved, better UX
- **Cons**: Higher memory usage (one WebView per tab)

---

## Key Files Explained

### 1. MainActivity.kt

**Purpose**: Main browser activity handling all user interactions

**Key Functions**:
| Function | Purpose |
|----------|---------|
| `createNewTab()` | Creates new tab with WebView |
| `showTab()` | Switches between tabs (no reload) |
| `closeTab()` | Closes a tab and cleans up |
| `loadUrl()` | Loads a URL in current tab |
| `applyPageZoomJS()` | Applies zoom using JavaScript |
| `toggleDesktopMode()` | Toggles desktop mode |
| `saveTabs()` | Saves tabs to SharedPreferences |
| `restoreTabs()` | Restores tabs from SharedPreferences |

### 2. WebViewFactory.kt

**Purpose**: Factory for creating and configuring WebViews

**Key Functions**:
| Function | Purpose |
|----------|---------|
| `createWebView()` | Creates configured WebView |
| `setDesktopMode()` | Sets User-Agent for desktop/mobile |
| `injectViewportFix()` | Injects JS to fix zoom restrictions |

### 3. BrowserManager.kt

**Purpose**: Manages the collection of tabs

**Key Functions**:
| Function | Purpose |
|----------|---------|
| `createNewTab()` | Creates and adds new tab |
| `getCurrentTab()` | Returns currently active tab |
| `getAllTabs()` | Returns all tabs |
| `setCurrentTab()` | Sets active tab |
| `closeTab()` | Removes tab |
| `addTab()` | Adds restored tab |
| `clearAllTabs()` | Removes all tabs |

### 4. BrowserTab.kt

**Purpose**: Data class representing a single tab

```kotlin
data class BrowserTab(
    val id: Int,              // Unique ID
    var title: String = "New Tab",
    var url: String = "",
    var webView: WebView? = null,
    var isSelected: Boolean = false,
    var isDesktopMode: Boolean = false
)
```

### 5. activity_main.xml

**Purpose**: XML layout for the main activity

**Key Views**:
| View ID | Purpose |
|---------|---------|
| `drawerLayout` | Main container with drawer |
| `topBar` | URL bar and navigation |
| `webViewContainer` | Holds all WebViews |
| `tabSlider` | Tab management panel |
| `drawerMenu` | Side menu with zoom slider |
| `zoomSeekBar` | Zoom slider (50-150%) |
| `menuDesktop` | Desktop mode toggle |

---

## How to Modify Features

### How to Change Zoom Range

1. **In MainActivity.kt**, modify these constants:
   ```kotlin
   private const val ZOOM_MIN = 50  // Change to 25 for 25%
   private const val ZOOM_MAX = 150 // Change to 200 for 200%
   ```

2. **In activity_main.xml**, update SeekBar attributes:
   ```xml
   <SeekBar
       android:id="@+id/zoomSeekBar"
       android:min="25"
       android:max="200"
       ... />
   ```

### How to Add New Desktop/Mobile User-Agent

1. **In WebViewFactory.kt**, modify the constants:
   ```kotlin
   const val DESKTOP_UA = "Your new desktop UA string"
   const val MOBILE_UA = "Your new mobile UA string"
   ```

### How to Change Tab Save Frequency

In **MainActivity.kt**, modify the `saveTabs()` calls:

```kotlin
// Currently saved in:
override fun onStop() {  // When app goes to background
    saveTabs()
}

override fun onDestroy() {  // When app closes
    saveTabs()
}
```

### How to Add New Tab Property

1. **In BrowserTab.kt**, add to data class:
   ```kotlin
   var isPrivateMode: Boolean = false
   ```

2. **In saveTabs()**, save the property:
   ```kotlin
   put("isPrivateMode", tab.isPrivateMode)
   ```

3. **In restoreTabs()**, restore the property:
   ```kotlin
   val isPrivateMode = tabObj.optBoolean("isPrivateMode", false)
   ```

### How to Disable Tab Persistence

To disable tab saving completely:

1. Remove `saveTabs()` calls from `onStop()` and `onDestroy()`
2. Clear saved data on app start:
   ```kotlin
   prefs.edit().remove(KEY_TABS).apply()
   ```

---

## SharedPreferences Keys

| Key | Type | Purpose |
|-----|------|---------|
| `saved_tabs` | String (JSON) | All tab data |
| `current_tab_id` | Int | Currently selected tab |
| `desktop_mode` | Boolean | Global desktop mode |
| `page_zoom_percent` | Int | Current zoom level |

---

## Common Issues and Solutions

### Issue: Zoom not working on some websites
**Solution**: Some websites forcefully override zoom. The viewport fix JavaScript helps, but not all sites can be zoomed.

### Issue: Desktop mode not persisting
**Solution**: Make sure `saveTabs()` is called after toggling. Check that `KEY_DESKTOP_MODE` preference is being saved.

### Issue: Tabs reload when switching
**Solution**: Ensure all WebViews remain in `webViewContainer` and only visibility is changed in `showTab()`.

### Issue: Memory usage high with many tabs
**Solution**: This is expected behavior. Each WebView uses memory. Consider adding a tab limit.

---

## Build and Run

```bash
# Build debug APK
./gradlew assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

---

## License

This project is open source and available on GitHub.
