package com.jkjk.quicknote.widget;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.SearchHelper;


public class NoteWidgetConfigureActivity extends AppCompatActivity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private NoteWidgetAdapter noteWidgetAdapter;
    private RecyclerView recyclerViewForWidget;
    private MenuItem showStarred, search;
    private TextView notFoundTextView;
    private boolean showingStarred;
    private SQLiteDatabase database;

    public NoteWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        database = ((MyApplication)getApplication()).database;

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

        androidx.appcompat.widget.Toolbar widgetMenu = findViewById(R.id.widget_config_menu);
        setSupportActionBar(widgetMenu);

        recyclerViewForWidget = findViewById(R.id.recycler_view_widget);
        recyclerViewForWidget.setHasFixedSize(true);
        recyclerViewForWidget.setLayoutManager(new LinearLayoutManager(this));
        noteWidgetAdapter = new NoteWidgetAdapter(this, mAppWidgetId);
        noteWidgetAdapter.updateCursor();
        recyclerViewForWidget.setAdapter(noteWidgetAdapter);

        notFoundTextView =  findViewById(R.id.widget_config_result_not_found);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.widget_config_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.widget_config_search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        } else Toast.makeText(this, R.string.error_text, Toast.LENGTH_SHORT).show();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String result = SearchHelper.searchResult(database, newText);

                //If search result is not empty, update the cursor to show result
                if (!result.equals("")) {
                    recyclerViewForWidget.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    noteWidgetAdapter.updateCursorForSearch(result);
                    noteWidgetAdapter.notifyDataSetChanged();
                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    recyclerViewForWidget.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    //after finish searching and user empty the search input, reset the view
                    recyclerViewForWidget.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    noteWidgetAdapter.updateCursor();
                    noteWidgetAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        search = menu.findItem(R.id.widget_config_search);
        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                showStarred.setVisible(false);
                showingStarred = false;
                showStarred.setIcon(R.drawable.sharp_outlined_flag_24);
                noteWidgetAdapter.updateCursor();
                noteWidgetAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                showStarred.setVisible(true);
                search.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW|MenuItem.SHOW_AS_ACTION_ALWAYS);
                showStarred.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            }
        });

        showStarred = menu.findItem(R.id.widget_show_starred);
        showStarred.setIcon(R.drawable.sharp_outlined_flag_24);
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (showingStarred){
                    // to toggle and show all notes
                    showingStarred = false;
                    showStarred.setIcon(R.drawable.sharp_outlined_flag_24);
                    noteWidgetAdapter.updateCursor();
                } else {
                    // to toggle and show only starred notes
                    showingStarred = true;
                    showStarred.setIcon(R.drawable.sharp_flag_24);
                    noteWidgetAdapter.updateCursorForStarred();
                }
                noteWidgetAdapter.notifyDataSetChanged();
                return true;
            }
        });
        return true;
    }

    public void onColorSelected(View view) {
        noteWidgetAdapter.onColorSelected(view);
    }
}

