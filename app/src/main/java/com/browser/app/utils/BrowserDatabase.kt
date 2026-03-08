package com.browser.app.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BrowserDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "browser_history.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_HISTORY = "history"
        private const val COL_ID = "id"
        private const val COL_URL = "url"
        private const val COL_TITLE = "title"
        private const val COL_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_HISTORY ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_URL TEXT, $COL_TITLE TEXT, $COL_TIMESTAMP INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun addHistory(url: String, title: String) {
        val db = writableDatabase
        val timestamp = System.currentTimeMillis()
        db.execSQL("INSERT INTO $TABLE_HISTORY ($COL_URL, $COL_TITLE, $COL_TIMESTAMP) VALUES (?, ?, ?)", arrayOf(url, title, timestamp))
    }

    fun getAllHistory(): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HISTORY ORDER BY $COL_TIMESTAMP DESC", null)
        while (cursor.moveToNext()) {
            list.add(HistoryItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3)))
        }
        cursor.close()
        return list
    }

    fun clearAllHistory() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_HISTORY")
    }

    data class HistoryItem(val id: Int, val url: String, val title: String, val timestamp: Long)
}
