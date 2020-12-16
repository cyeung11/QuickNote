package com.jkjk.quicknote.helper.backup

import android.content.Context
import com.jkjk.quicknote.noteeditscreen.Note
import com.jkjk.quicknote.taskeditscreen.Task


data class BackupModel(
        var notes: List<BackupNote>?,
        var tasks: List<BackupTask>?
) {

    fun saveToDb(context: Context) {
        notes?.forEach {
            it.toNote().saveAsNew(context)
        }
        tasks?.forEach{
            it.toTask().saveAsNew(context)
        }
    }

    companion object {
        fun fromDb(context: Context): BackupModel {
            val notes = Note.getAllNotes(context).map { BackupNote.fromNote(it) }
            val tasks = Task.getAllTask(context, false).map { BackupTask.fromTask(it) }

            return BackupModel(notes, tasks)
        }
    }

}