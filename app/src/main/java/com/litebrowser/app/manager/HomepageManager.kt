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
    <meta name="theme-color" content="#4285f4">
    <title>LiteBrowser</title>
    <style>
        :root {
            --primary: #1a73e8;
            --primary-dark: #1557b0;
            --background: #f8f9fa;
            --card: #ffffff;
            --text: #202124;
            --text-secondary: #5f6368;
            --border: #dadce0;
            --shadow: rgba(0,0,0,0.1);
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: var(--background);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 20px;
            animation: fadeIn 0.5s ease;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        /* Header Section */
        .header {
            text-align: center;
            margin-bottom: 30px;
            animation: slideDown 0.6s ease;
        }
        
        @keyframes slideDown {
            from { opacity: 0; transform: translateY(-30px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .logo-container {
            width: 72px;
            height: 72px;
            background: linear-gradient(135deg, #4285f4 0%, #34a853 50%, #fbbc05 100%);
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
            box-shadow: 0 8px 32px rgba(66, 133, 244, 0.3);
            animation: pulse 2s infinite;
        }
        
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }
        
        .logo-icon {
            font-size: 36px;
            filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
        }
        
        .app-name {
            font-size: 26px;
            font-weight: 700;
            color: var(--text);
            margin-bottom: 4px;
            letter-spacing: -0.5px;
        }
        
        .tagline {
            font-size: 13px;
            color: var(--text-secondary);
            font-weight: 400;
        }
        
        /* Search Section */
        .search-section {
            width: 100%;
            max-width: 520px;
            margin-bottom: 24px;
            position: relative;
        }
        
        .search-container {
            background: var(--card);
            border-radius: 28px;
            box-shadow: 0 4px 20px var(--shadow);
            display: flex;
            align-items: center;
            padding: 4px;
            transition: all 0.3s ease;
            border: 2px solid transparent;
        }
        
        .search-container:focus-within {
            box-shadow: 0 8px 32px rgba(26, 115, 232, 0.15);
            border-color: rgba(26, 115, 232, 0.3);
            transform: translateY(-2px);
        }
        
        .search-icon {
            width: 44px;
            height: 44px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--text-secondary);
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
            color: var(--text);
            font-family: inherit;
        }
        
        .search-input::placeholder {
            color: #9aa0a6;
        }
        
        .voice-btn, .search-btn {
            width: 44px;
            height: 44px;
            border: none;
            background: transparent;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            transition: all 0.2s;
        }
        
        .voice-btn:hover, .search-btn:hover {
            background: rgba(0,0,0,0.05);
            transform: scale(1.1);
        }
        
        .search-btn {
            background: var(--primary);
            color: white;
            margin-left: 4px;
        }
        
        .search-btn:hover {
            background: var(--primary-dark);
        }
        
        /* Quick Actions */
        .quick-actions {
            display: flex;
            gap: 12px;
            margin-bottom: 24px;
            flex-wrap: wrap;
            justify-content: center;
        }
        
        .action-chip {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 20px;
            font-size: 13px;
            color: var(--text-secondary);
            text-decoration: none;
            transition: all 0.2s;
            cursor: pointer;
        }
        
        .action-chip:hover {
            background: var(--primary);
            color: white;
            border-color: var(--primary);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(26, 115, 232, 0.3);
        }
        
        .action-chip .icon {
            font-size: 14px;
        }
        
        /* Quick Links Grid */
        .quick-links-title {
            font-size: 12px;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 12px;
            width: 100%;
            max-width: 400px;
            text-align: left;
            padding-left: 8px;
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
            background: var(--card);
            border-radius: 16px;
            box-shadow: 0 2px 8px var(--shadow);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }
        
        .quick-link::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.4) 100%);
            opacity: 0;
            transition: opacity 0.3s;
        }
        
        .quick-link:hover {
            transform: translateY(-4px) scale(1.02);
            box-shadow: 0 8px 24px rgba(0,0,0,0.15);
        }
        
        .quick-link:hover::before {
            opacity: 1;
        }
        
        .quick-link:active {
            transform: translateY(-2px) scale(0.98);
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
            transition: transform 0.3s;
        }
        
        .quick-link:hover .link-icon {
            transform: scale(1.1);
        }
        
        .link-label {
            font-size: 11px;
            color: var(--text-secondary);
            font-weight: 500;
            transition: color 0.2s;
        }
        
        .quick-link:hover .link-label {
            color: var(--text);
        }
        
        /* Footer */
        .footer {
            margin-top: auto;
            padding-top: 30px;
            text-align: center;
        }
        
        .footer-text {
            font-size: 11px;
            color: var(--text-secondary);
        }
        
        .footer-links {
            display: flex;
            gap: 16px;
            justify-content: center;
            margin-top: 8px;
        }
        
        .footer-links a {
            font-size: 11px;
            color: var(--primary);
            text-decoration: none;
        }
        
        .footer-links a:hover {
            text-decoration: underline;
        }
        
        /* Responsive */
        @media (max-width: 380px) {
            .quick-links {
                grid-template-columns: repeat(3, 1fr);
                gap: 10px;
            }
            
            .quick-link {
                padding: 12px 6px;
            }
            
            .link-icon {
                width: 38px;
                height: 38px;
                font-size: 18px;
            }
            
            .app-name {
                font-size: 22px;
            }
        }
        
        /* Loading Animation */
        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 2px solid rgba(255,255,255,.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 1s ease-in-out infinite;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="logo-container">
            <div class="logo-icon">🌐</div>
        </div>
        <h1 class="app-name">LiteBrowser</h1>
        <p class="tagline">Fast • Lightweight • Private</p>
    </header>
    
    <section class="search-section">
        <div class="search-container">
            <span class="search-icon">🔍</span>
            <input 
                type="text" 
                class="search-input" 
                placeholder="Search or type URL..." 
                id="searchInput"
                autocomplete="off"
                autocorrect="off"
                autocapitalize="off"
            >
            <button class="search-btn" onclick="performSearch()" title="Search">→</button>
        </div>
    </section>
    
    <div class="quick-actions">
        <a href="https://lite.duckduckgo.com/lite/" class="action-chip">
            <span class="icon">🔍</span> DuckDuckGo
        </a>
        <a href="https://www.google.com" class="action-chip">
            <span class="icon">G</span> Google
        </a>
        <a href="https://www.bing.com" class="action-chip">
            <span class="icon">B</span> Bing
        </a>
        <a href="https://www.wikipedia.org" class="action-chip">
            <span class="icon">W</span> Wikipedia
        </a>
    </div>
    
    <div class="quick-links-title">Quick Links</div>
    
    <nav class="quick-links">
        <a href="https://www.youtube.com" class="quick-link">
            <div class="link-icon" style="background: #ff0000; color: white;">▶️</div>
            <span class="link-label">YouTube</span>
        </a>
        <a href="https://www.facebook.com" class="quick-link">
            <div class="link-icon" style="background: #1877f2; color: white;">f</div>
            <span class="link-label">Facebook</span>
        </a>
        <a href="https://twitter.com" class="quick-link">
            <div class="link-icon" style="background: #1da1f2; color: white;">🐦</div>
            <span class="link-label">Twitter</span>
        </a>
        <a href="https://www.instagram.com" class="quick-link">
            <div class="link-icon" style="background: linear-gradient(45deg, #f09433, #e6683c, #dc2743, #cc2366, #bc1888); color: white;">📷</div>
            <span class="link-label">Instagram</span>
        </a>
        <a href="https://www.reddit.com" class="quick-link">
            <div class="link-icon" style="background: #ff4500; color: white;">🤖</div>
            <span class="link-label">Reddit</span>
        </a>
        <a href="https://github.com" class="quick-link">
            <div class="link-icon" style="background: #24292e; color: white;">🐙</div>
            <span class="link-label">GitHub</span>
        </a>
        <a href="https://www.tiktok.com" class="quick-link">
            <div class="link-icon" style="background: #000000; color: white;">🎵</div>
            <span class="link-label">TikTok</span>
        </a>
        <a href="https://www.linkedin.com" class="quick-link">
            <div class="link-icon" style="background: #0a66c2; color: white;">💼</div>
            <span class="link-label">LinkedIn</span>
        </a>
    </nav>
    
    <footer class="footer">
        <p class="footer-text">© 2024 LiteBrowser - Private & Fast</p>
        <div class="footer-links">
            <a href="https://lite.duckduckgo.com/lite/">DuckDuckGo</a>
            <a href="https://www.google.com">Google</a>
            <a href="https://www.bing.com">Bing</a>
        </div>
    </footer>

    <script>
        const searchInput = document.getElementById('searchInput');
        
        function performSearch() {
            const input = searchInput.value.trim();
            if (input) {
                navigateTo(input);
            }
        }
        
        function navigateTo(input) {
            // Check if it's a URL
            const urlPattern = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/;
            const isUrl = urlPattern.test(input) || (input.includes('.') && !input.includes(' ') && input.length > 3);
            
            if (isUrl && !input.startsWith('http')) {
                input = 'https://' + input;
            }
            
            if (input.startsWith('http')) {
                window.location.href = input;
            } else {
                // Search with DuckDuckGo Lite
                window.location.href = 'https://lite.duckduckgo.com/lite/?q=' + encodeURIComponent(input);
            }
        }
        
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
        
        // Focus search on load with animation
        window.addEventListener('load', () => {
            setTimeout(() => {
                searchInput.focus();
            }, 300);
        });
        
        // Add ripple effect to quick links
        document.querySelectorAll('.quick-link').forEach(link => {
            link.addEventListener('click', function(e) {
                const rect = this.getBoundingClientRect();
                const xPos = e.clientX - rect.left;
                const yPos = e.clientY - rect.top;
                
                const ripple = document.createElement('span');
                ripple.style.cssText = `
                    position: absolute;
                    background: rgba(255,255,255,0.5);
                    border-radius: 50%;
                    width: 20px;
                    height: 20px;
                    left: ${'$'}{xPos}px;
                    top: ${'$'}{yPos}px;
                    animation: ripple 0.6s ease-out;
                    pointer-events: none;
                `;
                
                this.appendChild(ripple);
                setTimeout(() => ripple.remove(), 600);
            });
        });
    </script>
</body>
</html>
        """.trimIndent()
    }
}
