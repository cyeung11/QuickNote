package com.jkjk.quicknote.widget;

import android.app.IntentService;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class AppWidgetService extends IntentService {

    public static final int NOTE_WIDGET_REQUEST_CODE = 0;
    public static final int TASK_LIST_WIDGET_REQUEST_CODE = 1;
    public static final int NOTE_LIST_WIDGET_REQUEST_CODE = 2;
    public static final int NOTE_LIST_WIDGET_START_APP_REQUEST_CODE = 1399;
    public static final int TASK_LIST_WIDGET_START_APP_REQUEST_CODE = 2333;

    public AppWidgetService(){
        super("AppWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        AppWidgetJobService.handleIntent(this, intent);
    }

}

