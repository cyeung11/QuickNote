package com.jkjk.quicknote.helper.backup

import com.jkjk.quicknote.noteeditscreen.Note
import java.util.*

class BackupNote {

    var title = ""
    var content = ""
    var editTimeInMillis: Long? = null
    var isStarred = false

    fun toNote(): Note{
        return Note().also {
            it.title = title
            it.content = content
            it.editTime = Calendar.getInstance().apply {
                if (editTimeInMillis != null)
                timeInMillis = editTimeInMillis !!
            }
            it.isStarred = isStarred
        }
    }

    companion object {
        fun fromNote(note: Note): BackupNote {
            return BackupNote().apply {
                title = note.title
                content = note.content
                editTimeInMillis = note.editTime.timeInMillis
                isStarred = note.isStarred
            }
        }
    }
}