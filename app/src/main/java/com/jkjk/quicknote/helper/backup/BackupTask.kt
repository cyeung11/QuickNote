package com.jkjk.quicknote.helper.backup

import com.google.android.gms.maps.model.LatLng
import com.jkjk.quicknote.taskeditscreen.Task
import com.jkjk.quicknote.taskeditscreen.TaskEditFragment.DATE_NOT_SET_INDICATOR
import java.util.*

class BackupTask {
    var title = ""
    var content = ""
    var eventTimeInMillis = DATE_NOT_SET_INDICATOR
    var urgency = 0
    var isDone = false
    var reminderTimeInMillis = 0L
    var repeatTime = 0L
    var lat: Double? = null
    var lng: Double? = null
    var placeName: String? = null

    fun toTask(): Task{
        return Task().also {
            it.title = title
            it.content = content
            it.eventTime = Calendar.getInstance().apply { timeInMillis = eventTimeInMillis }
            it.urgency = urgency
            it.isDone = isDone
            it.reminderTime = Calendar.getInstance().apply { timeInMillis = reminderTimeInMillis }
            it.repeatTime = repeatTime
            it.placeName = placeName
            if (lat != null && lng != null) {
                it.latLng = LatLng(lat!!, lng!!)
            }
        }
    }

    companion object {
        fun fromTask(task: Task): BackupTask {
            return BackupTask().apply {
                title = task.title
                content = task.content
                eventTimeInMillis = task.eventTime.timeInMillis
                urgency = task.urgency
                isDone = task.isDone
                reminderTimeInMillis = task.reminderTime.timeInMillis
                repeatTime = task.repeatTime
                lat = task.latLng?.latitude
                lng = task.latLng?.longitude
                placeName = task.placeName
            }
        }
    }
}