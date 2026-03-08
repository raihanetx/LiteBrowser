package com.browser.app.download

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.webkit.WebView

/**
 * Download Manager for handling file downloads
 * Downloads files to phone's Downloads folder
 */
object DownloadManagerHelper {

    /**
     * Download a file from URL
     * @param context Android context
     * @param url The URL to download from
     * @param userAgent User-Agent string (optional)
     * @return Download ID (Long), -1 if failed
     */
    fun downloadFile(context: Context, url: String, userAgent: String? = null): Long {
        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            // Create download request
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                // Set description
                setDescription("Downloading file...")
                
                // Set notification visibility
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                
                // Set destination - Downloads folder
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    getFileNameFromUrl(url)
                )
                
                // Set user agent if provided
                userAgent?.let { addRequestHeader("User-Agent", it) }
                
                // Allow overwriting existing file
                setVisibleInDownloadsUi(true)
            }
            
            // Enqueue and return download ID
            downloadManager.enqueue(request)
            
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Extract filename from URL
     * If no filename found, generates one
     */
    fun getFileNameFromUrl(url: String): String {
        return try {
            val fileName = URLUtil.guessFileName(url, null, null)
            if (fileName.isNotEmpty()) fileName else "download_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "download_${System.currentTimeMillis()}"
        }
    }

    /**
     * Get download status
     */
    fun getDownloadStatus(context: Context, downloadId: Long): Int {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        
        return if (cursor.moveToFirst()) {
            val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            cursor.getInt(statusColumn)
        } else {
            -1
        }
    }

    /**
     * Get download URI (for opening the file)
     */
    fun getDownloadUri(context: Context, downloadId: Long): Uri? {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.getUriForDownloadedFile(downloadId)
    }

    /**
     * Open downloaded file
     */
    fun openDownloadedFile(context: Context, downloadId: Long): Boolean {
        return try {
            val uri = getDownloadUri(context, downloadId) ?: return false
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(context, downloadId))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get MIME type of downloaded file
     */
    fun getMimeType(context: Context, downloadId: Long): String? {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        
        return if (cursor.moveToFirst()) {
            val mimeTypeColumn = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
            cursor.getString(mimeTypeColumn)
        } else {
            null
        }
    }

    /**
     * Delete a download
     */
    fun deleteDownload(context: Context, downloadId: Long): Boolean {
        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(downloadId) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if download is complete
     */
    fun isDownloadComplete(context: Context, downloadId: Long): Boolean {
        val status = getDownloadStatus(context, downloadId)
        return status == DownloadManager.STATUS_SUCCESSFUL
    }
}

/**
 * Download status constants
 */
object DownloadStatus {
    const val PENDING = 1
    const val RUNNING = 2
    const val PAUSED = 4
    const val COMPLETED = 8
    const val FAILED = 16
    const val UNKNOWN = -1
}
