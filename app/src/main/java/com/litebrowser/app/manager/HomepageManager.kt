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
    <meta name="theme-color" content="#000000">
    <title>LitEBrowser</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #ffffff;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }
        
        .container {
            width: 100%;
            max-width: 500px;
        }
        
        .logo-section {
            text-align: center;
            margin-bottom: 40px;
        }
        
        .logo {
            width: 80px;
            height: 80px;
            background: #000000;
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
        }
        
        .logo-text {
            font-size: 40px;
            font-weight: bold;
            color: #ffffff;
        }
        
        .app-name {
            font-size: 28px;
            font-weight: 700;
            color: #000000;
            margin-bottom: 4px;
        }
        
        .tagline {
            font-size: 14px;
            color: #666666;
        }
        
        .search-box {
            background: #ffffff;
            border: 2px solid #000000;
            border-radius: 30px;
            display: flex;
            align-items: center;
            padding: 4px;
            margin-bottom: 40px;
        }
        
        .search-input {
            flex: 1;
            border: none;
            outline: none;
            font-size: 16px;
            padding: 12px 16px;
            background: transparent;
            color: #000000;
        }
        
        .search-input::placeholder {
            color: #999999;
        }
        
        .search-btn {
            background: #000000;
            color: #ffffff;
            border: none;
            padding: 12px 24px;
            border-radius: 26px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
        }
        
        .quick-links {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 16px;
        }
        
        .link-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-decoration: none;
            padding: 16px 8px;
            background: #f5f5f5;
            border-radius: 12px;
            transition: all 0.2s;
        }
        
        .link-item:hover {
            background: #000000;
        }
        
        .link-item:hover .link-icon,
        .link-item:hover .link-label {
            color: #ffffff;
        }
        
        .link-icon {
            font-size: 24px;
            margin-bottom: 8px;
            color: #000000;
        }
        
        .link-label {
            font-size: 12px;
            color: #333333;
            font-weight: 500;
        }
        
        @media (max-width: 400px) {
            .quick-links {
                grid-template-columns: repeat(3, 1fr);
                gap: 12px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo-section">
            <div class="logo">
                <span class="logo-text">E</span>
            </div>
            <h1 class="app-name">LitEBrowser</h1>
            <p class="tagline">Fast • Lightweight • Private</p>
        </div>
        
        <div class="search-box">
            <input 
                type="text" 
                class="search-input" 
                placeholder="Search or enter URL..." 
                id="searchInput"
                autocomplete="off"
            >
            <button class="search-btn" onclick="search()">Go</button>
        </div>
        
        <div class="quick-links">
            <a href="https://www.google.com" class="link-item">
                <span class="link-icon">G</span>
                <span class="link-label">Google</span>
            </a>
            <a href="https://www.youtube.com" class="link-item">
                <span class="link-icon">Y</span>
                <span class="link-label">YouTube</span>
            </a>
            <a href="https://www.facebook.com" class="link-item">
                <span class="link-icon">F</span>
                <span class="link-label">Facebook</span>
            </a>
            <a href="https://twitter.com" class="link-item">
                <span class="link-icon">T</span>
                <span class="link-label">Twitter</span>
            </a>
            <a href="https://www.instagram.com" class="link-item">
                <span class="link-icon">I</span>
                <span class="link-label">Instagram</span>
            </a>
            <a href="https://www.reddit.com" class="link-item">
                <span class="link-icon">R</span>
                <span class="link-label">Reddit</span>
            </a>
            <a href="https://github.com" class="link-item">
                <span class="link-icon">GH</span>
                <span class="link-label">GitHub</span>
            </a>
            <a href="https://www.wikipedia.org" class="link-item">
                <span class="link-icon">W</span>
                <span class="link-label">Wiki</span>
            </a>
        </div>
    </div>

    <script>
        function search() {
            const input = document.getElementById('searchInput').value.trim();
            if (input) {
                if (input.includes('.') && !input.includes(' ')) {
                    if (!input.startsWith('http')) input = 'https://' + input;
                    window.location.href = input;
                } else {
                    window.location.href = 'https://lite.duckduckgo.com/lite/?q=' + encodeURIComponent(input);
                }
            }
        }
        
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') search();
        });
    </script>
</body>
</html>
        """.trimIndent()
    }
}
