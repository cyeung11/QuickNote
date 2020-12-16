package com.jkjk.quicknote.taskeditscreen

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.google.android.gms.maps.model.LatLng
import com.jkjk.quicknote.MyApplication
import com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME
import com.jkjk.quicknote.helper.DatabaseHelper.dbColumn
import com.jkjk.quicknote.taskeditscreen.TaskEditFragment.*
import java.util.*
import kotlin.collections.ArrayList

class Task {
    var id: Long? = null
    var title = ""
    var content = ""
    var eventTime: Calendar = Calendar.getInstance().apply { timeInMillis = DATE_NOT_SET_INDICATOR }
    var urgency = 0
    var isDone = false
    var reminderTime: Calendar = Calendar.getInstance().apply { timeInMillis = 0 }
    var repeatTime = 0L
    var latLng: LatLng? = null
    var placeName: String? = null

    val isDateSet: Boolean
        get() = eventTime.timeInMillis != DATE_NOT_SET_INDICATOR

    val isTimeSet: Boolean
        get() = eventTime.get(Calendar.MILLISECOND) != TIME_NOT_SET_MILLISECOND_INDICATOR
                || eventTime.get(Calendar.SECOND) != TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                || eventTime.get(Calendar.MINUTE) != TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                || eventTime.get(Calendar.HOUR_OF_DAY) != TIME_NOT_SET_HOUR_INDICATOR

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
        eventTime.clear(Calendar.ZONE_OFFSET)
    }

    fun saveAsNew(context: Context): Long?{
        return save(context, null)
    }

    fun save(context: Context, taskId: Long? = null): Long? {
        var finalId = taskId
        val values = ContentValues()
        values.put("title", title)
        values.put("content", content)

        values.put("event_time", eventTime.timeInMillis)

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
        return finalId
    }

    companion object {

        private val urgentComparator : Comparator<Task>
            get() = Comparator<Task> { o1, o2 ->
                val firstCompare = -o1.urgency.compareTo(o2.urgency)
                if (firstCompare == 0) {
                    timeComparator.compare(o1, o2)
                } else
                    firstCompare
            }

        private val timeComparator : Comparator<Task>
            get() = Comparator<Task> { o1, o2 -> o1.eventTime.timeInMillis.compareTo(o2.eventTime.timeInMillis) }

        fun delete(context: Context, id: Long) {
            val database = (context.applicationContext as MyApplication).database
            database.delete(DATABASE_NAME, "_id='$id'", null)
        }

        @JvmOverloads
        fun getAllTask(context: Context, sortByUrgency: Boolean, isDone: Boolean? = null, queryResult: String? = null): ArrayList<Task> {
            val results = arrayListOf<Task>()
            val database = (context.applicationContext as MyApplication).database
            val taskCursor = database.query(DATABASE_NAME, arrayOf(dbColumn[0], dbColumn[1], dbColumn[2], dbColumn[3], dbColumn[6], dbColumn[7], dbColumn[8], dbColumn[9], dbColumn[10], dbColumn[11]),
                    "${ if (queryResult != null) {"_id in ($queryResult) AND "} else ""}${dbColumn[5]}= 1", null, null, null, null, null)
            if (taskCursor.moveToFirst()) {
                do {
                    createFromCursor(taskCursor)?.let {
                        results.add(it)
                    }
                } while (taskCursor.moveToNext())
            }
            taskCursor.close()
            if (sortByUrgency) {
                results.sortWith(urgentComparator)
            } else {
                results.sortWith(timeComparator)
            }
            return if (isDone == true) {
                ArrayList(results.filter { it.isDone })
            } else if (isDone == false) {
                ArrayList(results.filter { !it.isDone })
            } else {
                results
            }
        }

        fun getTask(context: Context, taskId: Long?): Task? {
            val database = (context.applicationContext as MyApplication).database
            val taskCursor = database.query(DATABASE_NAME, arrayOf(dbColumn[1], dbColumn[2], dbColumn[3], dbColumn[6], dbColumn[7], dbColumn[8], dbColumn[9], dbColumn[10], dbColumn[11]),
                    "${dbColumn[0]}= $taskId", null, null, null, null, null)
            var loadedTask: Task? = null
            if (taskCursor.moveToFirst()) {
                loadedTask = createFromCursor(taskCursor)
                loadedTask?.id = taskId
            }
            taskCursor.close()
            return loadedTask
        }

        // cursor is moved to correct position before calling this method
        private fun createFromCursor(cursor: Cursor): Task? {
            val names = cursor.columnNames
            val result = Task()

            val idIndex = names.indexOf(dbColumn[0])
            if (idIndex >= 0) {
                try {
                    result.id = cursor.getLong(idIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val titleIndex = names.indexOf(dbColumn[1])
            if (titleIndex >= 0) {
                try {
                    result.title = cursor.getString(titleIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val contentIndex = names.indexOf(dbColumn[2])
            if (contentIndex >= 0) {
                try {
                    result.content = cursor.getString(contentIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val eventTimeIndex = names.indexOf(dbColumn[3])
            if (eventTimeIndex >= 0) {
                try {
                    val eT = cursor.getLong(eventTimeIndex)
                    result.eventTime.timeInMillis = eT
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val urgencyIndex = names.indexOf(dbColumn[6])
            if (urgencyIndex >= 0) {
                try {
                    result.urgency = cursor.getInt(urgencyIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val doneIndex = names.indexOf(dbColumn[7])
            if (doneIndex >= 0) {
                try {
                    result.isDone = cursor.getInt(doneIndex) == 1
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val remindIndex = names.indexOf(dbColumn[8])
            if (remindIndex >= 0) {
                try {
                    val rT = cursor.getLong(remindIndex)
                    result.reminderTime.timeInMillis = rT
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val repeatIndex = names.indexOf(dbColumn[9])
            if (repeatIndex >= 0) {
                try {
                    result.repeatTime = cursor.getLong(repeatIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val latLngIndex = names.indexOf(dbColumn[10])
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

            val placeIndex = names.indexOf(dbColumn[11])
            if (placeIndex >= 0) {
                try {
                    result.placeName = cursor.getString(placeIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return result
        }
    }
}