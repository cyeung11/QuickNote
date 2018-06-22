package com.jkjk.quicknote.widget;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.SearchHelper;

import static com.jkjk.quicknote.listscreen.ListFragment.REWARD_VIDEO_AD_ID;
import static com.jkjk.quicknote.listscreen.ListFragment.isAllowedToUse;


/**
 * The configuration screen for the {@link NoteWidget NoteWidget} AppWidget.
 */
public class NoteWidgetConfigureActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private WidgetAdapter widgetAdapter;
    private RecyclerView recyclerViewForWidget;
    private MenuItem showStarred, search;
    private TextView notFoundTextView;
    private boolean showingStarred;
//    private RewardedVideoAd mRewardedVideoAd;
    private RadioButton white;

    public NoteWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //TODO Ad Related
//        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
//        mRewardedVideoAd.setRewardedVideoAdListener(this);
//        loadRewardedVideoAd();

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

        android.support.v7.widget.Toolbar widgetMenu = findViewById(R.id.widget_config_menu);
        (NoteWidgetConfigureActivity.this).setSupportActionBar(widgetMenu);

        recyclerViewForWidget = findViewById(R.id.recycler_view_widget);
        recyclerViewForWidget.setHasFixedSize(true);
        recyclerViewForWidget.setLayoutManager(new LinearLayoutManager(this));
        widgetAdapter = new WidgetAdapter(this, mAppWidgetId);
        widgetAdapter.updateCursorForWidget();
        recyclerViewForWidget.setAdapter(widgetAdapter);

        notFoundTextView =  findViewById(R.id.widget_config_result_not_found);
        white = findViewById(R.id.color_white);
    }

    @Override
    protected void onResume() {
//        mRewardedVideoAd.resume(this);
        super.onResume();

        // TODO Delete to enable ads
        isAllowedToUse = true;
    }

    @Override
    protected void onStop() {
//        mRewardedVideoAd.pause(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_config_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.widget_config_search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        } else Toast.makeText(this, R.string.error_text, Toast.LENGTH_SHORT).show();
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
                    widgetAdapter.updateCursorForSearchForWidget(result);
                    widgetAdapter.notifyDataSetChanged();
                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    recyclerViewForWidget.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    //after finish searching and user empty the search input, reset the view
                    recyclerViewForWidget.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    widgetAdapter.updateCursorForWidget();
                    widgetAdapter.notifyDataSetChanged();
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
                showStarred.setIcon(R.drawable.sharp_star_border_24);
                widgetAdapter.updateCursorForWidget();
                widgetAdapter.notifyDataSetChanged();
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
        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!showingStarred){
                    // to show only starred notes
                    showingStarred = true;
                    showStarred.setIcon(R.drawable.baseline_star_24);
                    widgetAdapter.updateCursorForStarred();
                    widgetAdapter.notifyDataSetChanged();
                } else {
                    // to show all notes
                    showingStarred = false;
                    showStarred.setIcon(R.drawable.sharp_star_border_24);
                    widgetAdapter.updateCursorForWidget();
                    widgetAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        return true;
    }

    public void onColorSelected(View view) {
        if (isAllowedToUse) {
            widgetAdapter.onColorSelected(view);
        } else {
        new AlertDialog.Builder(this).setTitle(R.string.premium_function).setMessage(R.string.ads_prompt)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (mRewardedVideoAd.isLoaded()) {
//                            mRewardedVideoAd.show();
//                        } else {
//                            Toast.makeText(getBaseContext(),R.string.ads_wait,Toast.LENGTH_SHORT).show();
//                        }
                    }
                }).setNegativeButton(R.string.cancel, null).show();
        }
    }

    private void loadRewardedVideoAd() {
//        mRewardedVideoAd.loadAd(REWARD_VIDEO_AD_ID, new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
        white.setChecked(true);
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Toast.makeText(this, R.string.ads_complete, Toast.LENGTH_SHORT).show();
        isAllowedToUse = true;
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}

