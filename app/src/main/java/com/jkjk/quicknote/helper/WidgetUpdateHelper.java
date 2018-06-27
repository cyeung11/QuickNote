package com.jkjk.quicknote.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;

public class WidgetUpdateHelper extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        updateTaskListWidget(context);
    }
}
