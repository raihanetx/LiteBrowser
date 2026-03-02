package com.litebrowser.app.manager

object HomepageManager {
    
    const val SEARCH_ENGINE = "https://duckduckgo.com/?q="
    
    fun getHomepageHtml(): String {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 30px 15px;
        }
        
        .logo {
            font-size: 36px;
            margin-bottom: 5px;
        }
        
        .app-name {
            font-size: 22px;
            font-weight: bold;
            color: white;
            margin-bottom: 20px;
            text-shadow: 0 2px 4px rgba(0,0,0,0.2);
        }
        
        .search-container {
            width: 100%;
            max-width: 400px;
            margin-bottom: 25px;
        }
        
        .search-box {
            width: 100%;
            padding: 12px 18px;
            font-size: 15px;
            border: none;
            border-radius: 25px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
            outline: none;
        }
        
        .search-box:focus {
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
        }
        
        .quick-links {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            width: 100%;
            max-width: 350px;
        }
        
        .quick-link {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 12px 8px;
            background: rgba(255,255,255,0.2);
            border-radius: 12px;
            color: white;
            text-decoration: none;
            font-size: 11px;
            font-weight: 500;
            transition: all 0.3s ease;
            backdrop-filter: blur(10px);
        }
        
        .quick-link:hover {
            background: rgba(255,255,255,0.3);
            transform: scale(1.05);
        }
        
        .quick-link .icon {
            font-size: 20px;
            margin-bottom: 4px;
        }
    </style>
</head>
<body>
    <div class="logo">🌐</div>
    <div class="app-name">LiteBrowser</div>
    
    <div class="search-container">
        <input type="text" class="search-box" placeholder="Search or enter URL" id="searchInput" autofocus>
    </div>
    
    <div class="quick-links">
        <a href="https://www.google.com" class="quick-link">
            <span class="icon">🔍</span> Google
        </a>
        <a href="https://www.youtube.com" class="quick-link">
            <span class="icon">▶️</span> YouTube
        </a>
        <a href="https://www.facebook.com" class="quick-link">
            <span class="icon">📘</span> Facebook
        </a>
        <a href="https://twitter.com" class="quick-link">
            <span class="icon">🐦</span> Twitter
        </a>
        <a href="https://www.instagram.com" class="quick-link">
            <span class="icon">📷</span> Instagram
        </a>
        <a href="https://www.reddit.com" class="quick-link">
            <span class="icon">🤖</span> Reddit
        </a>
    </div>

    <script>
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                var input = this.value.trim();
                if (input) {
                    if (input.includes('.') && !input.includes(' ')) {
                        if (!input.startsWith('http://') && !input.startsWith('https://')) {
                            input = 'https://' + input;
                        }
                        window.location.href = input;
                    } else {
                        window.location.href = 'https://duckduckgo.com/?q=' + encodeURIComponent(input);
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
