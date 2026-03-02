package com.litebrowser.app.manager

object HomepageManager {
    
    const val SEARCH_ENGINE = "https://lite.duckduckgo.com/lite/?q="
    
    fun getHomepageHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>LiteBrowser</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 40px 20px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 50px;
        }
        
        .logo-container {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, #4285f4 0%, #34a853 100%);
            border-radius: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            box-shadow: 0 8px 32px rgba(66, 133, 244, 0.3);
        }
        
        .logo-icon {
            font-size: 40px;
        }
        
        .app-name {
            font-size: 28px;
            font-weight: 700;
            color: #202124;
            margin-bottom: 6px;
        }
        
        .tagline {
            font-size: 14px;
            color: #5f6368;
            font-weight: 400;
        }
        
        .search-section {
            width: 100%;
            max-width: 480px;
            margin-bottom: 40px;
        }
        
        .search-container {
            position: relative;
            background: white;
            border-radius: 28px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            padding: 6px;
            display: flex;
            align-items: center;
        }
        
        .search-icon {
            width: 44px;
            height: 44px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #5f6368;
            font-size: 18px;
            flex-shrink: 0;
        }
        
        .search-input {
            flex: 1;
            border: none;
            outline: none;
            font-size: 16px;
            padding: 12px 8px;
            background: transparent;
            color: #202124;
        }
        
        .search-input::placeholder {
            color: #9aa0a6;
        }
        
        .search-btn {
            background: #1a73e8;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 22px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.2s;
        }
        
        .search-btn:hover {
            background: #1557b0;
        }
        
        .quick-links {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 16px;
            width: 100%;
            max-width: 400px;
        }
        
        .quick-link {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-decoration: none;
            padding: 16px 8px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            transition: all 0.2s ease;
        }
        
        .quick-link:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 24px rgba(0,0,0,0.12);
        }
        
        .quick-link:active {
            transform: translateY(-2px);
        }
        
        .link-icon {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            margin-bottom: 8px;
        }
        
        .link-label {
            font-size: 12px;
            color: #5f6368;
            font-weight: 500;
        }
        
        @media (max-width: 400px) {
            .quick-links {
                grid-template-columns: repeat(3, 1fr);
                gap: 12px;
            }
            
            .quick-link {
                padding: 12px 6px;
            }
            
            .link-icon {
                width: 40px;
                height: 40px;
                font-size: 20px;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="logo-container">
            <div class="logo-icon">🌐</div>
        </div>
        <h1 class="app-name">LiteBrowser</h1>
        <p class="tagline">Fast • Lightweight • Private</p>
    </div>
    
    <div class="search-section">
        <div class="search-container">
            <span class="search-icon">🔍</span>
            <input 
                type="text" 
                class="search-input" 
                placeholder="Search with DuckDuckGo Lite..." 
                id="searchInput"
                autocomplete="off"
            >
            <button class="search-btn" onclick="performSearch()">Search</button>
        </div>
    </div>
    
    <div class="quick-links">
        <a href="https://www.google.com" class="quick-link">
            <div class="link-icon" style="background: #f1f3f4;">🔍</div>
            <span class="link-label">Google</span>
        </a>
        <a href="https://www.youtube.com" class="quick-link">
            <div class="link-icon" style="background: #ffebee; color: #c62828;">▶️</div>
            <span class="link-label">YouTube</span>
        </a>
        <a href="https://www.facebook.com" class="quick-link">
            <div class="link-icon" style="background: #e3f2fd; color: #1565c0;">f</div>
            <span class="link-label">Facebook</span>
        </a>
        <a href="https://twitter.com" class="quick-link">
            <div class="link-icon" style="background: #e0f7fa; color: #00838f;">🐦</div>
            <span class="link-label">Twitter</span>
        </a>
        <a href="https://www.instagram.com" class="quick-link">
            <div class="link-icon" style="background: #fce4ec; color: #c2185b;">📷</div>
            <span class="link-label">Instagram</span>
        </a>
        <a href="https://www.reddit.com" class="quick-link">
            <div class="link-icon" style="background: #fff3e0; color: #e65100;">🤖</div>
            <span class="link-label">Reddit</span>
        </a>
        <a href="https://github.com" class="quick-link">
            <div class="link-icon" style="background: #f5f5f5; color: #212121;">🐙</div>
            <span class="link-label">GitHub</span>
        </a>
        <a href="https://www.wikipedia.org" class="quick-link">
            <div class="link-icon" style="background: #f3e5f5; color: #7b1fa2;">📚</div>
            <span class="link-label">Wikipedia</span>
        </a>
    </div>

    <script>
        function performSearch() {
            var input = document.getElementById('searchInput').value.trim();
            if (input) {
                navigateTo(input);
            }
        }
        
        function navigateTo(input) {
            if (input.includes('.') && !input.includes(' ') && input.length > 3) {
                if (!input.startsWith('http://') && !input.startsWith('https://')) {
                    input = 'https://' + input;
                }
                window.location.href = input;
            } else {
                window.location.href = 'https://lite.duckduckgo.com/lite/?q=' + encodeURIComponent(input);
            }
        }
        
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
        
        document.getElementById('searchInput').focus();
    </script>
</body>
</html>
        """.trimIndent()
    }
}
