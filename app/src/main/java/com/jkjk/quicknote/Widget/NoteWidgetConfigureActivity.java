package com.jkjk.quicknote.Widget;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.jkjk.quicknote.Adapter.WidgetAdapter;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.SearchHelper;


/**
 * The configuration screen for the {@link NoteWidget NoteWidget} AppWidget.
 */
public class NoteWidgetConfigureActivity extends AppCompatActivity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    WidgetAdapter widgetAdapter;
    RecyclerView recyclerViewForWidget;
    android.support.v7.widget.Toolbar widgetMenu;
    SearchView searchView;
    TextView notFoundTextView;

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

        widgetMenu = (android.support.v7.widget.Toolbar)findViewById(R.id.widget_config_menu);
        ((AppCompatActivity) NoteWidgetConfigureActivity.this).setSupportActionBar(widgetMenu);

        recyclerViewForWidget = (RecyclerView)findViewById(R.id.recycler_view_widget);
        recyclerViewForWidget.setHasFixedSize(true);
        recyclerViewForWidget.setLayoutManager(new LinearLayoutManager(this));
        widgetAdapter = new WidgetAdapter(this, mAppWidgetId);
        WidgetAdapter.updateCursorForWidget();
        recyclerViewForWidget.setAdapter(widgetAdapter);

        notFoundTextView = (TextView) findViewById(R.id.widget_config_result_not_found);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = (MenuInflater) getMenuInflater();
        inflater.inflate(R.menu.widget_config_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.widget_config_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            SearchHelper searchHelper = new SearchHelper();

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String result = searchHelper.searchResult(newText);

                //If search result is not empty, update the cursor to show result
                if (!result.equals("")) {
                    recyclerViewForWidget.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    WidgetAdapter.updateCursorForSearchForWidget(result);
                    widgetAdapter.notifyDataSetChanged();
                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    recyclerViewForWidget.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    //after finish searching and user empty the search input, reset the view
                    recyclerViewForWidget.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    WidgetAdapter.updateCursorForWidget();
                    widgetAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
        return true;
    }

    public void onColorSelected(View view) {
        widgetAdapter.onColorSelected(view);
    }
}

