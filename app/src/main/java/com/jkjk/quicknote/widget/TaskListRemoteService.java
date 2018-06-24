package com.jkjk.quicknote.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class TaskListRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TaskListRemoteFactory(getApplicationContext());
    }
}
