package com.browser.app.download

import android.content.Context
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/**
 * Custom WebViewClient that handles file downloads
 */
class DownloadWebViewClient(
    private val context: Context,
    private val onDownloadRequested: (url: String, userAgent: String?) -> Unit = { url, ua ->
        // Default: use our download manager
        val downloadId = DownloadManagerHelper.downloadFile(context, url, ua)
        if (downloadId > 0) {
            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        url?.let {
            // Check if it's a download link
            if (isDownloadUrl(it)) {
                // Get user agent
                val userAgent = view?.settings?.userAgentString
                onDownloadRequested(it, userAgent)
                return true
            }
        }
        return false
    }

    /**
     * Check if URL is a download link
     */
    private fun isDownloadUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains(".pdf") ||
                lowerUrl.contains(".doc") ||
                lowerUrl.contains(".docx") ||
                lowerUrl.contains(".xls") ||
                lowerUrl.contains(".xlsx") ||
                lowerUrl.contains(".ppt") ||
                lowerUrl.contains(".pptx") ||
                lowerUrl.contains(".zip") ||
                lowerUrl.contains(".rar") ||
                lowerUrl.contains(".tar") ||
                lowerUrl.contains(".gz") ||
                lowerUrl.contains(".apk") ||
                lowerUrl.contains(".mp3") ||
                lowerUrl.contains(".mp4") ||
                lowerUrl.contains(".avi") ||
                lowerUrl.contains(".mkv") ||
                lowerUrl.contains(".jpg") ||
                lowerUrl.contains(".jpeg") ||
                lowerUrl.contains(".png") ||
                lowerUrl.contains(".gif") ||
                lowerUrl.contains(".bmp") ||
                lowerUrl.contains(".svg") ||
                lowerUrl.contains(".webp") ||
                lowerUrl.contains(".exe") ||
                lowerUrl.contains(".dmg") ||
                lowerUrl.contains(".iso") ||
                lowerUrl.contains(".txt") ||
                lowerUrl.contains(".rtf") ||
                lowerUrl.contains(".csv") ||
                lowerUrl.contains(".json") ||
                lowerUrl.contains(".xml") ||
                lowerUrl.contains(".html") ||
                lowerUrl.contains(".css") ||
                lowerUrl.contains(".js") ||
                lowerUrl.contains(".java") ||
                lowerUrl.contains(".kt") ||
                lowerUrl.contains(".swift") ||
                lowerUrl.contains(".py") ||
                lowerUrl.contains(".c") ||
                lowerUrl.contains(".cpp") ||
                lowerUrl.contains(".h") ||
                lowerUrl.contains(".sh") ||
                lowerUrl.contains(".bat") ||
                lowerUrl.contains(".psd") ||
                lowerUrl.contains(".ai") ||
                lowerUrl.contains(".eps") ||
                lowerUrl.contains(".raw") ||
                lowerUrl.contains(".cr2") ||
                lowerUrl.contains(".nef") ||
                lowerUrl.contains(".dng") ||
                lowerUrl.contains(".m4a") ||
                lowerUrl.contains(".wav") ||
                lowerUrl.contains(".flac") ||
                lowerUrl.contains(".ogg") ||
                lowerUrl.contains(".wma") ||
                lowerUrl.contains(".aac") ||
                lowerUrl.contains(".3gp") ||
                lowerUrl.contains(".mov") ||
                lowerUrl.contains(".wmv") ||
                lowerUrl.contains(".flv") ||
                lowerUrl.contains(".webm") ||
                lowerUrl.contains(".m4v") ||
                lowerUrl.contains(".3g2") ||
                lowerUrl.contains(".mpg") ||
                lowerUrl.contains(".mpeg") ||
                lowerUrl.contains(".7z")
    }
}

/**
 * Simple download click listener for WebView
 */
class SimpleDownloadListener(
    private val context: Context
) : android.webkit.DownloadListener {
    
    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        // Start download
        val downloadId = DownloadManagerHelper.downloadFile(context, url, userAgent)
        
        if (downloadId > 0) {
            Toast.makeText(
                context,
                "Downloading: ${DownloadManagerHelper.getFileNameFromUrl(url)}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show()
        }
    }
}
