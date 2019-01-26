package com.jkjk.quicknote.taskeditscreen

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.google.android.gms.maps.model.LatLng
import com.jkjk.quicknote.MyApplication
import com.jkjk.quicknote.helper.DatabaseHelper
import com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME
import com.jkjk.quicknote.taskeditscreen.TaskEditFragment.*
import java.util.*

class Task {
    var title: String = ""
    var content: String = ""
    var eventTime = Calendar.getInstance().apply { timeInMillis = DATE_NOT_SET_INDICATOR }
    var urgency: Int = 0
    var isDone: Boolean = false
    var reminderTime = Calendar.getInstance().apply { timeInMillis = 0 }
    var repeatTime = 0L
    var latLng: LatLng? = null
    var placeName: String? = null

    var hasSetTime = false

    val isDateSet: Boolean
        get() = eventTime.timeInMillis != DATE_NOT_SET_INDICATOR

    private val isTimeSet: Boolean
        get() = eventTime.get(Calendar.MILLISECOND) != TIME_NOT_SET_MILLISECOND_INDICATOR
                && eventTime.get(Calendar.SECOND) != TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                && eventTime.get(Calendar.MINUTE) != TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                && eventTime.get(Calendar.HOUR_OF_DAY) != TIME_NOT_SET_HOUR_INDICATOR

    val isReminderSet: Boolean
        get() = reminderTime.timeInMillis != 0L

    fun removeDate() {
        eventTime.timeInMillis = DATE_NOT_SET_INDICATOR
    }

    fun removeTime() {
        eventTime.set(Calendar.HOUR_OF_DAY, TIME_NOT_SET_HOUR_INDICATOR)
        eventTime.set(Calendar.MINUTE, TIME_NOT_SET_MINUTE_SECOND_INDICATOR)
        eventTime.set(Calendar.SECOND, TIME_NOT_SET_MINUTE_SECOND_INDICATOR)
        eventTime.set(Calendar.MILLISECOND, TIME_NOT_SET_MILLISECOND_INDICATOR)
    }

    @JvmOverloads
    fun save(context: Context, taskId: Long? = null): Long? {
        var finalId = taskId
        val values = ContentValues()
        values.put("title", title)
        values.put("content", content)

        if (hasSetTime) {
            values.put("event_time", eventTime.timeInMillis)
        } else {
            removeTime()
            values.put("event_time", eventTime.timeInMillis)
        }
        values.put("repeat_interval", repeatTime)
        values.put("type", 1)
        values.put("urgency", urgency)
        values.put("done", if (isDone) 1 else 0)
        values.put("reminder_time", reminderTime.timeInMillis)
        if (latLng != null) {
            values.put("lat_lng", latLng?.latitude.toString() + "," + latLng?.longitude.toString())
        } else {
            values.putNull("lat_lng")
        }
        values.put("place_name", placeName)

        val database = (context.applicationContext as MyApplication).database
        if (taskId != null) {
            database.update(DATABASE_NAME, values, "_id='$taskId'", null)
        } else {
            finalId = database.insert(DATABASE_NAME, "", values)
        }

        values.clear()
        return finalId
    }

    companion object {

        fun getTask(context: Context, taskId: Long?): Task? {
            val database = (context.applicationContext as MyApplication).database
            val taskCursor = database.query(DATABASE_NAME, arrayOf("title", "content", "event_time", "urgency", "done", "reminder_time", "repeat_interval", "lat_lng", "place_name"),
                    "_id= $taskId", null, null, null, null, null)
            val loadedTask = Task.createFromCursor(taskCursor)
            taskCursor.close()
            return loadedTask
        }

        private fun createFromCursor(cursor: Cursor): Task? {
            if (cursor.moveToFirst()) {

                val names = cursor.columnNames
                val result = Task()
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
                        result.eventTime.timeInMillis = eT
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val urgencyIndex = names.indexOf(DatabaseHelper.dbColumn[6])
                if (urgencyIndex >= 0) {
                    try {
                        result.urgency = cursor.getInt(urgencyIndex)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val doneIndex = names.indexOf(DatabaseHelper.dbColumn[7])
                if (doneIndex >= 0) {
                    try {
                        result.isDone = cursor.getInt(doneIndex) == 1
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val remindIndex = names.indexOf(DatabaseHelper.dbColumn[8])
                if (remindIndex >= 0) {
                    try {
                        val rT = cursor.getLong(remindIndex)
                        result.reminderTime.timeInMillis = rT
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val repeatIndex = names.indexOf(DatabaseHelper.dbColumn[9])
                if (repeatIndex >= 0) {
                    try {
                        result.repeatTime = cursor.getLong(repeatIndex)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val latLngIndex = names.indexOf(DatabaseHelper.dbColumn[10])
                if (latLngIndex >= 0) {
                    try {
                        val latLngString = cursor.getString(latLngIndex)
                        if (latLngString != null) {
                            val latLngValue = latLngString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            if (latLngValue.size == 2) {
                                result.latLng = LatLng(latLngValue[0].toDouble(), latLngValue[1].toDouble())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val placeIndex = names.indexOf(DatabaseHelper.dbColumn[11])
                if (placeIndex >= 0) {
                    try {
                        result.placeName = cursor.getString(placeIndex)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                result.hasSetTime = result.isTimeSet

                return result
            } else return null
        }
    }
}