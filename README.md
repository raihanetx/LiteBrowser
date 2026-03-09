# LiteBrowser - Lightweight Android Browser

A lightweight, feature-rich Android browser built with native WebView components.

## Features

- **Full Tab Management** - Multiple tabs with each having its own WebView; tabs stay alive when switching (no reload)
- **Desktop Mode Toggle** - Global switch between mobile and desktop user-agent
- **Zoom Controls** - Pinch-to-zoom + on-screen zoom buttons
- **Third-Party Cookie Blocking** - Global toggle to block third-party cookies
- **Per-Site Cookie Viewer/Deleter** - View and delete cookies for the current website
- **No History** - No custom history list (WebView's internal back/forward remains for navigation)
- **No Downloads** - File downloads are ignored by design

## Technical Stack

- **Language**: Kotlin
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34
- **Architecture**: Single Activity with ViewPager2 and Fragments

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Gradle 8.2
- Android SDK 34

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/lightweightbrowser/
│   │   ├── MainActivity.kt
│   │   ├── WebViewFragment.kt
│   │   └── TabAdapter.kt
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── fragment_webview.xml
│   │   │   └── tab_item.xml
│   │   ├── menu/
│   │   │   └── browser_menu.xml
│   │   └── values/
│   │       ├── strings.xml
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
└── build.gradle
```

## Usage

- **Navigate**: Enter URL in the address bar and tap "Go"
- **New Tab**: Tap the "+" button
- **Close Tab**: Tap the "X" on the tab
- **Back/Forward**: Use arrow buttons
- **Refresh**: Tap the refresh button
- **Zoom**: Use "+" and "-" buttons or pinch-to-zoom
- **Desktop Mode**: Open menu (⋮) → Desktop Mode
- **Cookie Management**: Open menu (⋮) → Manage Cookies for This Site

## Limitations

- Cookie deletion may not work for cookies with non-root paths or Secure flags
- No support for file downloads (by design)
- No SSL error handling (uses default WebView behavior)
- No tab titles (currently shows "Tab N")

## License

MIT License
