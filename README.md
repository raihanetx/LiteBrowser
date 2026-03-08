# LiteBrowser

A lightweight Android browser with WebView.

## Features

- **Multiple Tabs** - Create, switch, and close tabs
- **Desktop Mode** - View desktop versions of websites
- **Pinch-to-Zoom** - Two-finger zoom like real browsers
- **DuckDuckGo Lite** - Fast, private search engine
- **Tab Slider** - Quick tab management

## Desktop Mode

Desktop Mode changes the **User Agent** string to trick websites into showing desktop versions.

### How it works:

1. **User Agent (UA)** - A string that tells websites "I'm Chrome on Windows" or "I'm Safari on iPhone"

2. **Desktop Mode UA:**
   ```
   Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
   ```

3. **Mobile Mode UA:**
   ```
   (default Android WebView UA)
   ```

### Implementation:

**BrowserTab.kt:**
```kotlin
data class BrowserTab(
    ...
    var isDesktopMode: Boolean = false  // Mode saved PER TAB
)
```

**WebViewFactory.kt:**
```kotlin
fun setDesktopMode(webView: WebView, enabled: Boolean) {
    webView.settings.userAgentString = if (enabled) DESKTOP_UA else ""
    webView.reload()  // Reload page with new UA
}
```

**MainActivity.kt:**
```kotlin
private fun toggleDesktopMode() {
    browserManager.getCurrentTab()?.let { tab ->
        tab.isDesktopMode = !tab.isDesktopMode  // Toggle mode
        tab.webView?.let { wv ->
            WebViewFactory.setDesktopMode(wv, tab.isDesktopMode)
        }
    }
}

private fun showTab(tabId: Int) {
    // Restore desktop mode when switching tabs
    browserManager.getCurrentTab()?.let { tab ->
        tab.webView?.let { wv ->
            WebViewFactory.setDesktopMode(wv, tab.isDesktopMode)
        }
    }
}
```

### Key Points:

1. **Per-Tab Setting** - Each tab remembers its own desktop/mobile mode
2. **Persistent** - Mode stays when switching between tabs
3. **Toggle** - Tap menu → Desktop Mode to turn on/off
4. **Page Reload** - Page reloads to apply new User Agent

## Pinch-to-Zoom

Uses Android WebView's built-in zoom:

```kotlin
webView.settings.apply {
    builtInZoomControls = true
    displayZoomControls = false  // Hide +/- buttons
    setSupportZoom(true)
}
```

## Build

```bash
./gradlew assembleDebug
```

## APK Location

`app/build/outputs/apk/debug/app-debug.apk`
