package com.jkjk.quicknote

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.jkjk.quicknote.helper.DatabaseHelper
import com.jkjk.quicknote.helper.NotificationHelper
import com.jkjk.quicknote.noteeditscreen.NoteEditFragment

class MyApplication : MultiDexApplication() {

    lateinit var database: SQLiteDatabase

    override fun onCreate() {
        super.onCreate()
        val helper = DatabaseHelper(this, "note_db", null, DatabaseHelper.CURRENT_DB_VER)
        database = helper.writableDatabase

        // if pinned notification tool bar is enable, start it
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPref.getBoolean(getString(R.string.notification_pin), false)) {
            val toolBarIntent = Intent(this, NotificationHelper::class.java)
            toolBarIntent.action = NotificationHelper.ACTION_TOOL_BAR
            val toolbarPendingIntent = PendingIntent.getBroadcast(this, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            try {
                toolbarPendingIntent.send()
            } catch (e: CanceledException) {
                e.printStackTrace()
            }
        }
        val idPref = getSharedPreferences(PINNED_NOTIFICATION_IDS, Context.MODE_PRIVATE)
        val pinnedItem = idPref.all
        for (key in pinnedItem.keys) {
            val intent = Intent(this, NotificationHelper::class.java)
            intent.action = NotificationHelper.ACTION_PIN_ITEM
            intent.putExtra(NoteEditFragment.EXTRA_ITEM_ID, pinnedItem[key] as Long?)
            val pinNotificationPI = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            try {
                pinNotificationPI.send()
            } catch (e: CanceledException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val PINNED_NOTIFICATION_IDS = "pinned_notification_ids"
    }
}