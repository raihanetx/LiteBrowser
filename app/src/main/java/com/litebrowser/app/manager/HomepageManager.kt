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
            padding: 40px 20px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 40px;
        }
        
        .logo-container {
            width: 72px;
            height: 72px;
            background: #000000;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
            border: 2px solid #000000;
        }
        
        .logo-icon {
            font-size: 36px;
            color: white;
        }
        
        .app-name {
            font-size: 28px;
            font-weight: 700;
            color: #000000;
            margin-bottom: 4px;
            letter-spacing: -0.5px;
        }
        
        .tagline {
            font-size: 13px;
            color: #616161;
            font-weight: 400;
        }
        
        .search-section {
            width: 100%;
            max-width: 480px;
            margin-bottom: 30px;
        }
        
        .search-container {
            background: #ffffff;
            border-radius: 28px;
            border: 2px solid #000000;
            display: flex;
            align-items: center;
            padding: 4px;
        }
        
        .search-container:focus-within {
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
        }
        
        .search-icon {
            width: 44px;
            height: 44px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #000000;
            font-size: 18px;
        }
        
        .search-input {
            flex: 1;
            border: none;
            outline: none;
            font-size: 16px;
            padding: 12px 8px;
            background: transparent;
            color: #000000;
        }
        
        .search-input::placeholder {
            color: #9e9e9e;
        }
        
        .search-btn {
            background: #000000;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 22px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
        }
        
        .quick-links {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
            width: 100%;
            max-width: 400px;
        }
        
        .quick-link {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-decoration: none;
            padding: 16px 8px;
            background: #ffffff;
            border-radius: 12px;
            border: 2px solid #e0e0e0;
            transition: all 0.2s;
        }
        
        .quick-link:hover {
            border-color: #000000;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        
        .link-icon {
            width: 44px;
            height: 44px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            margin-bottom: 8px;
            background: #f5f5f5;
            border: 1px solid #e0e0e0;
        }
        
        .link-label {
            font-size: 11px;
            color: #424242;
            font-weight: 500;
        }
        
        @media (max-width: 380px) {
            .quick-links {
                grid-template-columns: repeat(3, 1fr);
            }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="logo-container">
            <div class="logo-icon">E</div>
        </div>
        <h1 class="app-name">LitEBrowser</h1>
        <p class="tagline">Fast • Lightweight • Private</p>
    </header>
    
    <section class="search-section">
        <div class="search-container">
            <span class="search-icon">&#128269;</span>
            <input 
                type="text" 
                class="search-input" 
                placeholder="Search or type URL..." 
                id="searchInput"
                autocomplete="off"
            >
            <button class="search-btn" onclick="performSearch()">Search</button>
        </div>
    </section>
    
    <nav class="quick-links">
        <a href="https://www.google.com" class="quick-link">
            <div class="link-icon">G</div>
            <span class="link-label">Google</span>
        </a>
        <a href="https://www.youtube.com" class="quick-link">
            <div class="link-icon">Y</div>
            <span class="link-label">YouTube</span>
        </a>
        <a href="https://www.facebook.com" class="quick-link">
            <div class="link-icon">F</div>
            <span class="link-label">Facebook</span>
        </a>
        <a href="https://twitter.com" class="quick-link">
            <div class="link-icon">T</div>
            <span class="link-label">Twitter</span>
        </a>
        <a href="https://www.instagram.com" class="quick-link">
            <div class="link-icon">I</div>
            <span class="link-label">Instagram</span>
        </a>
        <a href="https://www.reddit.com" class="quick-link">
            <div class="link-icon">R</div>
            <span class="link-label">Reddit</span>
        </a>
        <a href="https://github.com" class="quick-link">
            <div class="link-icon">Gi</div>
            <span class="link-label">GitHub</span>
        </a>
        <a href="https://www.wikipedia.org" class="quick-link">
            <div class="link-icon">W</div>
            <span class="link-label">Wiki</span>
        </a>
    </nav>

    <script>
        function performSearch() {
            const input = document.getElementById('searchInput').value.trim();
            if (input) {
                if (input.includes('.') && !input.includes(' ') && input.length > 3) {
                    if (!input.startsWith('http')) input = 'https://' + input;
                    window.location.href = input;
                } else {
                    window.location.href = 'https://lite.duckduckgo.com/lite/?q=' + encodeURIComponent(input);
                }
            }
        }
        
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') performSearch();
        });
    </script>
</body>
</html>
        """.trimIndent()
    }
}
