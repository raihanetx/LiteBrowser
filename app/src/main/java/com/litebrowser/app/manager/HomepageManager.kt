package com.litebrowser.app.manager

object HomepageManager {
    
    const val SEARCH_ENGINE = "https://lite.duckduckgo.com/lite/?q="
    
    fun getHomepageHtml(): String {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
            background: #f8f9fa;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-start;
            padding-top: 80px;
        }
        
        .container {
            width: 100%;
            max-width: 500px;
            padding: 0 20px;
        }
        
        .logo-section {
            text-align: center;
            margin-bottom: 40px;
        }
        
        .logo-icon {
            width: 64px;
            height: 64px;
            background: linear-gradient(135deg, #1a73e8 0%, #4285f4 100%);
            border-radius: 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            margin-bottom: 12px;
            box-shadow: 0 4px 12px rgba(26, 115, 232, 0.3);
        }
        
        .app-name {
            font-size: 24px;
            font-weight: 600;
            color: #202124;
            letter-spacing: -0.5px;
        }
        
        .app-tagline {
            font-size: 13px;
            color: #5f6368;
            margin-top: 4px;
        }
        
        .search-section {
            margin-bottom: 30px;
        }
        
        .search-box {
            width: 100%;
            padding: 14px 20px;
            font-size: 16px;
            border: 1px solid #dadce0;
            border-radius: 24px;
            background: white;
            outline: none;
            transition: all 0.2s;
            box-shadow: 0 1px 6px rgba(32, 33, 36, 0.1);
        }
        
        .search-box:focus {
            border-color: #1a73e8;
            box-shadow: 0 1px 6px rgba(26, 115, 232, 0.2);
        }
        
        .search-box::placeholder {
            color: #9aa0a6;
        }
        
        .quick-links {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
        }
        
        .quick-link {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 12px 8px;
            background: white;
            border-radius: 12px;
            text-decoration: none;
            transition: all 0.2s;
            border: 1px solid #e8eaed;
        }
        
        .quick-link:hover {
            background: #f1f3f4;
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        
        .quick-link .icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            margin-bottom: 8px;
        }
        
        .quick-link.google .icon { background: #f1f3f4; }
        .quick-link.youtube .icon { background: #ffebee; }
        .quick-link.facebook .icon { background: #e3f2fd; }
        .quick-link.twitter .icon { background: #e0f7fa; }
        .quick-link.instagram .icon { background: #fce4ec; }
        .quick-link.reddit .icon { background: #fff3e0; }
        .quick-link.github .icon { background: #f5f5f5; }
        .quick-link.wikipedia .icon { background: #f3e5f5; }
        
        .quick-link .label {
            font-size: 11px;
            color: #5f6368;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo-section">
            <div class="logo-icon">🌐</div>
            <div class="app-name">LiteBrowser</div>
            <div class="app-tagline">Fast • Simple • Private</div>
        </div>
        
        <div class="search-section">
            <input type="text" class="search-box" placeholder="Search with DuckDuckGo Lite..." id="searchInput" autofocus>
        </div>
        
        <div class="quick-links">
            <a href="https://www.google.com" class="quick-link google">
                <span class="icon">🔍</span>
                <span class="label">Google</span>
            </a>
            <a href="https://www.youtube.com" class="quick-link youtube">
                <span class="icon">▶️</span>
                <span class="label">YouTube</span>
            </a>
            <a href="https://www.facebook.com" class="quick-link facebook">
                <span class="icon">f</span>
                <span class="label">Facebook</span>
            </a>
            <a href="https://twitter.com" class="quick-link twitter">
                <span class="icon">🐦</span>
                <span class="label">Twitter</span>
            </a>
            <a href="https://www.instagram.com" class="quick-link instagram">
                <span class="icon">📷</span>
                <span class="label">Instagram</span>
            </a>
            <a href="https://www.reddit.com" class="quick-link reddit">
                <span class="icon">🤖</span>
                <span class="label">Reddit</span>
            </a>
            <a href="https://github.com" class="quick-link github">
                <span class="icon">🐙</span>
                <span class="label">GitHub</span>
            </a>
            <a href="https://www.wikipedia.org" class="quick-link wikipedia">
                <span class="icon">📚</span>
                <span class="label">Wikipedia</span>
            </a>
        </div>
    </div>

    <script>
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                var input = this.value.trim();
                if (input) {
                    if (input.includes('.') && !input.includes(' ') && input.length > 3) {
                        if (!input.startsWith('http://') && !input.startsWith('https://')) {
                            input = 'https://' + input;
                        }
                        window.location.href = input;
                    } else {
                        window.location.href = 'https://lite.duckduckgo.com/lite/?q=' + encodeURIComponent(input);
                    }
                }
            }
        });
    </script>
</body>
</html>
        """.trimIndent()
    }
}
