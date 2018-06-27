package com.jkjk.quicknote.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NoteListWidgetRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NoteListRemoteFactory(getApplicationContext());
    }
}