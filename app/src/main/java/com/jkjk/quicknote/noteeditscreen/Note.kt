package com.jkjk.quicknote.noteeditscreen

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.jkjk.quicknote.MyApplication
import com.jkjk.quicknote.helper.DatabaseHelper
import java.util.*

/**
 *Created by chrisyeung on 1/2/2019.
 */

class Note{
    var id: Long? = null
    var title = ""
    var content = ""
    var isStarred = false
    var editTime: Calendar = Calendar.getInstance()

    fun saveAsNew(context: Context): Long{
        return save(context, null)
    }

    @JvmOverloads
    fun save(context: Context, noteId: Long? = null, updateEditTime: Boolean = true): Long {
        var finalId = noteId
        val values = ContentValues()
        values.put("title", title)
        values.put("content", content)
        if (updateEditTime || noteId == null) {
            values.put("event_time", Calendar.getInstance().timeInMillis)
        }
        values.put("starred", if (isStarred) 1 else 0)
        values.put("type", 0)

        val database = (context.applicationContext as MyApplication).database
        if (finalId != null) {
            database.update(DatabaseHelper.DATABASE_NAME, values, "_id='$noteId'", null)
        } else {
            finalId = database.insert(DatabaseHelper.DATABASE_NAME, "", values)
        }
        return finalId
    }

    companion object {


        fun delete(context: Context, id: Long) {
            val database = (context.applicationContext as MyApplication).database
            database.delete(DatabaseHelper.DATABASE_NAME, "_id='$id'", null)
        }

        @JvmOverloads
        fun getAllNotes(context: Context, isStarred: Boolean? = null, queryResult: String? = null): ArrayList<Note> {
            val results = arrayListOf<Note>()
            val database = (context.applicationContext as MyApplication).database
            val noteCursor = database.query(DatabaseHelper.DATABASE_NAME, arrayOf(DatabaseHelper.dbColumn[0], DatabaseHelper.dbColumn[1], DatabaseHelper.dbColumn[2], DatabaseHelper.dbColumn[3], DatabaseHelper.dbColumn[4]),
                    "${ if (queryResult != null) {"_id in ($queryResult) AND "} else ""}${if (isStarred == true)"starred = 1 AND " else if (isStarred == false)"starred = 0 AND " else ""}${DatabaseHelper.dbColumn[5]}= 0", null, null, null, "event_time DESC", null)
            if (noteCursor.moveToFirst()) {
                do {
                    Note.createFromCursor(noteCursor)?.let {
                        results.add(it)
                    }
                } while (noteCursor.moveToNext())
            }
            noteCursor.close()
            return results
        }

        fun getNote(context: Context, noteId: Long?): Note? {
            val database = (context.applicationContext as MyApplication).database
            val noteCursor = database.query(DatabaseHelper.DATABASE_NAME, arrayOf(DatabaseHelper.dbColumn[1], DatabaseHelper.dbColumn[2], DatabaseHelper.dbColumn[3], DatabaseHelper.dbColumn[4]),
                    "${DatabaseHelper.dbColumn[0]}= $noteId", null, null, null, null, null)
            var loadedNote: Note? = null
            if (noteCursor.moveToFirst()) {
                loadedNote = Note.createFromCursor(noteCursor)
                loadedNote?.id = noteId
            }
            noteCursor.close()
            return loadedNote
        }

        // cursor is moved to correct position before calling this method
        private fun createFromCursor(cursor: Cursor): Note? {
            val names = cursor.columnNames
            val result = Note()

            val idIndex = names.indexOf(DatabaseHelper.dbColumn[0])
            if (idIndex >= 0) {
                try {
                    result.id = cursor.getLong(idIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val titleIndex = names.indexOf(DatabaseHelper.dbColumn[1])
            if (titleIndex >= 0) {
                try {
                    result.title = cursor.getString(titleIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val contentIndex = names.indexOf(DatabaseHelper.dbColumn[2])
            if (contentIndex >= 0) {
                try {
                    result.content = cursor.getString(contentIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val eventTimeIndex = names.indexOf(DatabaseHelper.dbColumn[3])
            if (eventTimeIndex >= 0) {
                try {
                    val eT = cursor.getLong(eventTimeIndex)
                    result.editTime.timeInMillis = eT
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val starredIndex = names.indexOf(DatabaseHelper.dbColumn[4])
            if (starredIndex >= 0) {
                try {
                    result.isStarred = cursor.getInt(starredIndex) == 1
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return result
        }
    }
}