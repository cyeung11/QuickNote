package com.jkjk.quicknote.widget

import android.content.Context
import android.content.Intent
import android.support.v7.preference.PreferenceManager
import android.text.format.DateUtils
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.jkjk.quicknote.R
import com.jkjk.quicknote.listscreen.ItemListAdapter
import com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID
import com.jkjk.quicknote.taskeditscreen.Task
import com.jkjk.quicknote.taskeditscreen.TaskEditFragment.*
import java.util.*

class TaskListRemoteFactory internal constructor(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<Task> = listOf()
    private var widgetLayout: Int = 0
    private var noUrgencyLayout: Int = 0

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val byUrgencyByDefault = sharedPref.getBoolean(context.getString(R.string.change_default_sorting), false)
        val includeDoneTask = sharedPref.getBoolean(context.getString(R.string.task_widget_done), false)

        tasks = Task.getAllTask(context, byUrgencyByDefault, if (includeDoneTask) null else false)

        val widgetSize = sharedPref.getString(context.getString(R.string.font_size_widget), "m")
        when (widgetSize) {
            "s" -> {
                widgetLayout = R.layout.widget_task_s
                noUrgencyLayout = R.layout.widget_task_no_urgency_s
            }
            "m" -> {
                widgetLayout = R.layout.widget_task_m
                noUrgencyLayout = R.layout.widget_task_no_urgency_m
            }
            "l" -> {
                widgetLayout = R.layout.widget_task_l
                noUrgencyLayout = R.layout.widget_task_no_urgency_l
            }
            "xl" -> {
                widgetLayout = R.layout.widget_task_xl
                noUrgencyLayout = R.layout.widget_task_no_urgency_xl
            }
            else -> {
                widgetLayout = R.layout.widget_task_m
                noUrgencyLayout = R.layout.widget_task_no_urgency_m
            }
        }
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return tasks.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val remoteViews: RemoteViews
        if (i >= tasks.size) {
            remoteViews = RemoteViews(context.packageName, R.layout.list_widget_loading)
            remoteViews.setTextViewText(R.id.text, context.getString(R.string.error_loading))
            return remoteViews
        }

        val task = tasks.get(i)

        when (task.urgency) {
            2 -> {
                remoteViews = RemoteViews(context.packageName, widgetLayout)
                remoteViews.setTextColor(R.id.task_urgency, context.resources.getColor(R.color.colorPrimary))
                remoteViews.setTextViewText(R.id.task_urgency, context.getString(R.string.asap))
            }
            1 -> {
                remoteViews = RemoteViews(context.packageName, widgetLayout)
                remoteViews.setTextColor(R.id.task_urgency, context.resources.getColor(R.color.darkGrey))
                remoteViews.setTextViewText(R.id.task_urgency, context.getString(R.string.important))
            }
            0 -> remoteViews = RemoteViews(context.packageName, noUrgencyLayout)
            else -> {
                remoteViews = RemoteViews(context.packageName, R.layout.list_widget_loading)
                remoteViews.setTextViewText(R.id.text, context.getString(R.string.error_loading))
                return remoteViews
            }
        }

        val time = task.eventTime.timeInMillis
        remoteViews.setTextColor(R.id.item_date, context.resources.getColor(R.color.darkGrey))

        if (time != DATE_NOT_SET_INDICATOR) {
            if (DateUtils.isToday(time)) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = time

                //get the time to see if the time was set by user
                if (calendar.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                        && calendar.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && calendar.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && calendar.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR) {

                    remoteViews.setTextViewText(R.id.item_date, context.getString(R.string.today))

                } else
                    remoteViews.setTextViewText(R.id.item_date, DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME))

            } else if (ItemListAdapter.isTomorrow(time)) {
                remoteViews.setTextViewText(R.id.item_date, context.getString(R.string.tomorrow))
            } else {
                remoteViews.setTextViewText(R.id.item_date, DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE))
                if (Calendar.getInstance().timeInMillis > time) {
                    remoteViews.setTextColor(R.id.item_date, context.resources.getColor(R.color.alternative))
                }
            }
        } else {
            remoteViews.setTextViewText(R.id.item_date, "")
        }

        remoteViews.setTextViewText(R.id.item_title, task.title)

        val openTaskIntent = Intent()
        openTaskIntent.putExtra(EXTRA_ITEM_ID, task.id)
        remoteViews.setOnClickFillInIntent(R.id.container, openTaskIntent)
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.list_widget_loading)
    }

    override fun getViewTypeCount(): Int {
        // 2 each for 4 sizes and one for error layout
        return 9
    }

    override fun getItemId(i: Int): Long {
       return tasks.get(i).id ?: 0
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
