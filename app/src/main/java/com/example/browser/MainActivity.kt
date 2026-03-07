package com.example.browser

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnGo: ImageButton
    private lateinit var mainLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFf0f0f0.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        btnBack = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_previous)
            layoutParams = LinearLayout.LayoutParams(96, 96)
            setOnClickListener {
                if (webView.canGoBack()) {
                    webView.goBack()
                }
            }
        }

        btnForward = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_next)
            layoutParams = LinearLayout.LayoutParams(96, 96)
            setOnClickListener {
                if (webView.canGoForward()) {
                    webView.goForward()
                }
            }
        }

        urlEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 96, 1f)
            hint = "Enter URL"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            setOnEditorActionListener { _, _, _ ->
                loadUrl()
                true
            }
        }

        btnRefresh = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            layoutParams = LinearLayout.LayoutParams(96, 96)
            setOnClickListener {
                webView.reload()
            }
        }

        btnGo = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_play)
            layoutParams = LinearLayout.LayoutParams(96, 96)
            setOnClickListener {
                loadUrl()
            }
        }

        toolbar.addView(btnBack)
        toolbar.addView(btnForward)
        toolbar.addView(urlEditText)
        toolbar.addView(btnRefresh)
        toolbar.addView(btnGo)

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                8
            )
        }

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    urlEditText.setText(url)
                    super.onPageFinished(view, url)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                    if (newProgress == 100) {
                        progressBar.visibility = View.GONE
                    } else {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }

            loadUrl("https://www.google.com")
        }

        mainLayout.addView(toolbar)
        mainLayout.addView(progressBar)
        mainLayout.addView(webView)

        setContentView(mainLayout)
    }

    private fun loadUrl() {
        var url = urlEditText.text.toString().trim()
        if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        if (url.isNotEmpty()) {
            webView.loadUrl(url)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
