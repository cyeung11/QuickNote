package com.jkjk.quicknote.Widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jkjk.quicknote.Adapter.WidgetAdapter;
import com.jkjk.quicknote.R;

/**
 * The configuration screen for the {@link NoteWidget NoteWidget} AppWidget.
 */
public class NoteWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    WidgetAdapter widgetAdapter;
    RecyclerView recyclerViewForWidget;

    public NoteWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        setContentView(R.layout.note_widget_configure);

        recyclerViewForWidget = (RecyclerView)findViewById(R.id.recycler_view_widget);
        recyclerViewForWidget.setHasFixedSize(true);
        recyclerViewForWidget.setLayoutManager(new LinearLayoutManager(this));
        widgetAdapter = new WidgetAdapter(this, mAppWidgetId);
        recyclerViewForWidget.setAdapter(widgetAdapter);

    }


    public void onColorSelected(View view) {
        widgetAdapter.onColorSelected(view);
    }
}

