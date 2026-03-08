package com.browser.app.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil

/**
 * Smart Download Utilities
 * Handles file downloads cleanly
 */
object DownloadUtils {

    /**
     * Start a file download
     * @return true if download started successfully
     */
    fun download(context: Context, url: String, userAgent: String? = null): Boolean {
        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setDescription("Downloading ${getFileName(url)}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFileName(url))
                userAgent?.let { addRequestHeader("User-Agent", it) }
            }
            
            downloadManager.enqueue(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get filename from URL
     */
    fun getFileName(url: String): String {
        return try {
            URLUtil.guessFileName(url, null, null).ifEmpty { "download_${System.currentTimeMillis()}" }
        } catch (e: Exception) {
            "download_${System.currentTimeMillis()}"
        }
    }

    /**
     * Open downloaded file
     */
    fun openFile(context: Context, downloadId: Long): Boolean {
        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = downloadManager.getUriForDownloadedFile(downloadId) ?: return false
            
            // Try to get MIME type
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            var mimeType = "application/octet-stream"
            
            if (cursor.moveToFirst()) {
                val mimeTypeColumn = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
                if (mimeTypeColumn >= 0) {
                    cursor.getString(mimeTypeColumn)?.let { mimeType = it }
                }
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
