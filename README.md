# Browser - Lightweight Mobile Web Browser

A lightweight mobile web browser built with **Kotlin** and **Jetpack Compose**.

## Features

- **Multiple Tab Management** - Switch between tabs without losing page state
- **Zoom Controls** - Zoom in/out functionality
- **Desktop/Mobile Mode** - Toggle between desktop and mobile user agent
- **Navigation Controls** - Back/forward, reload
- **Progress Indicator** - Shows page loading progress
- **Persistent Tabs** - Tabs maintain their state when switching

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Web Engine**: Android WebView
- **Architecture**: MVVM with ViewModel

## Project Structure

```
app/src/main/java/com/browser/app/
├── BrowserApp.kt           # Application class
├── MainActivity.kt         # Main entry point
├── model/
│   └── Tab.kt             # Tab data model
├── viewmodel/
│   └── BrowserViewModel.kt # Business logic
├── ui/
│   ├── components/
│   │   ├── BrowserScreen.kt    # Main browser UI
│   │   ├── TabsOverview.kt     # Tab switcher
│   │   └── WebViewContainer.kt # WebView wrapper
│   └── theme/
│       ├── Theme.kt       # App theme
│       └── Type.kt        # Typography
```

## Build Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 34

### Build Steps

1. Open the project in Android Studio
2. Sync project with Gradle files
3. Run on an emulator or physical device

### Command Line Build

```bash
# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Usage

1. **Navigate**: Enter URL in the address bar and press Go
2. **New Tab**: Tap the + button in the bottom bar or tabs view
3. **Switch Tabs**: Tap the tabs button (with count badge) to see all tabs
4. **Zoom**: Use + and - buttons in the bottom bar
5. **Desktop Mode**: Toggle between phone and desktop icons in the bottom bar
6. **Back Navigation**: Use system back button or the back arrow

## Permissions

- `INTERNET` - Required for loading web pages
- `ACCESS_NETWORK_STATE` - For checking network connectivity

## License

MIT License
