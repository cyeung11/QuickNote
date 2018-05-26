package com.jkjk.quicknote.listscreen;


import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.editscreen.EditFragment;
import com.jkjk.quicknote.helper.SearchHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView notFoundTextView;
    ListAdapter listAdapter;
    private boolean showingStarred = false;
    private MenuItem showStarred, search, backUp, restore, setting;

    public static final  int BACK_UP_REQUEST_CODE = 5555;
    public static final  int RESTORE_REQUEST_CODE = 5556;


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        listAdapter = new ListAdapter(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        android.support.v7.widget.Toolbar listMenu;
        if (!listAdapter.inActionMode) {
            listMenu = getActivity().findViewById(R.id.list_menu);
            ((AppCompatActivity) getActivity()).setSupportActionBar(listMenu);
        }

        FloatingActionButton addNote =  getActivity().findViewById(R.id.add_note);
        addNote.setImageDrawable(getResources().getDrawable(R.drawable.sharp_add_24));
        addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listAdapter.inActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // delete note from  selectedItems
                                    ArrayList<Integer> mSelect = listAdapter.getSelected();
                                    Cursor tempNote = listAdapter.getCursor();
                                    for (int removedPosition : mSelect) {
                                        tempNote.moveToPosition(removedPosition);
                                        String removedId = tempNote.getString(0);
                                        MyApplication.database.delete(DATABASE_NAME, "_id='" + removedId+"'",null);
                                        listAdapter.notifyItemRemoved(removedPosition);
                                    }
                                    Toast.makeText(getContext(),R.string.deleted_toast,Toast.LENGTH_SHORT).show();
                                    listAdapter.updateCursor();
                                    listAdapter.mActionMode.finish();
                                    tempNote.close();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }else {
                    onNoteEdit();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        notFoundTextView = (TextView)view.findViewById(R.id.result_not_found);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listAdapter.updateCursor();
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.note_list_menu, menu);

        showStarred = (MenuItem) menu.findItem(R.id.show_starred);
        search = (MenuItem) menu.findItem(R.id.search);
        backUp = (MenuItem) menu.findItem(R.id.back_up);
        restore = (MenuItem) menu.findItem(R.id.restore);
        setting = (MenuItem) menu.findItem(R.id.setting);

        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent();
                intent.setClassName("com.jkjk.quicknote","com.jkjk.quicknote.Setting");
                startActivity(intent);
                return true;
            }
        });

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
                    recyclerView.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    listAdapter.updateCursorForSearch(result);
                    listAdapter.notifyDataSetChanged();
                }else if (!newText.equals("")){
                    //If search result is empty and the search input is not empty, show result not found
                    recyclerView.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    //after finish searching and user empty the search input, reset the view
                    recyclerView.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    listAdapter.updateCursor();
                    listAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                showStarred.setVisible(false);
                backUp.setVisible(false);
                restore.setVisible(false);
                showingStarred = false;
                showStarred.setIcon(R.drawable.sharp_star_border_24);
                listAdapter.updateCursor();
                listAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                showStarred.setVisible(true);
                backUp.setVisible(true);
                restore.setVisible(true);
                search.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW|MenuItem.SHOW_AS_ACTION_ALWAYS);
                showStarred.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                backUp.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                restore.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                return true;
            }
        });

        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!showingStarred){
                    // to show only starred notes
                    showingStarred = true;
                    showStarred.setIcon(R.drawable.baseline_star_24);
                    listAdapter.updateCursorForStarred();
                    listAdapter.notifyDataSetChanged();
                } else {
                    // to show all notes
                    showingStarred = false;
                    showStarred.setIcon(R.drawable.sharp_star_border_24);
                    listAdapter.updateCursor();
                    listAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        backUp.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(getContext()).setTitle(R.string.permission_required).setMessage(R.string.permission)
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    } else {
                        // request the permission
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                BACK_UP_REQUEST_CODE);
                    }
                } else {
                    // Permission has already been granted
                    selectBackUpLocation();
                }
                return true;
            }
        });

        restore.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                new AlertDialog.Builder(getContext()).setTitle(R.string.restore).setMessage(R.string.restore_confirm)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Check permission
                                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    // Permission is not granted
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        new AlertDialog.Builder(getContext()).setTitle(R.string.permission_required).setMessage(R.string.permission)
                                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        intent.setData(uri);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, null)
                                                .show();
                                    } else {
                                        // request the permission
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                RESTORE_REQUEST_CODE);
                                    }
                                } else {
                                    // Permission has already been granted
                                    selectRestoreLocation();
                                }
                            }
                        }).setNegativeButton(R.string.cancel, null).show();

                return true;
            }
        });
       }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Uri uri = null;
            if (intent != null) {
                uri = intent.getData();

                switch (requestCode){
                    case BACK_UP_REQUEST_CODE:
                        try {
                            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "w");
                            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

                            File dataPath = Environment.getDataDirectory();
                            String dbPath = "//data//"+getActivity().getPackageName()+"//databases//"+DATABASE_NAME+"_db";
                            File db = new File(dataPath, dbPath);
                            FileInputStream fileInputStream = new FileInputStream(db);

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fileInputStream.read(buffer))>0){
                                fileOutputStream.write(buffer, 0, length);
                            }

                            fileOutputStream.flush();
                            fileInputStream.close();
                            fileOutputStream.close();
                            pfd.close();

                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getActivity(),R.string.error_back_up,Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case RESTORE_REQUEST_CODE:
                        try {
                            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "r");
                            FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());

                            File dataPath = Environment.getDataDirectory();
                            String dbPath = "//data//"+getActivity().getPackageName()+"//databases//"+DATABASE_NAME+"_db";
                            File db = new File(dataPath, dbPath);
                            FileOutputStream fileOutputStream = new FileOutputStream(db);

                            byte[] buffer = new byte[1024];
                            int length;

                            while ((length = fileInputStream.read(buffer))>0){
                                fileOutputStream.write(buffer, 0, length);
                            }

                            fileOutputStream.flush();
                            fileInputStream.close();
                            fileOutputStream.close();
                            pfd.close();

                            //Restart app
                            Intent restart = new Intent();
                            restart.setClassName(getActivity().getPackageName(),getActivity().getLocalClassName());
                            restart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(restart);

                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getActivity(),R.string.error_back_up,Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState!= null){
            listAdapter.updateCursor();
            listAdapter.notifyDataSetChanged();
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStop() {
        if (listAdapter.mActionMode!=null) {
            listAdapter.mActionMode.finish();
        }

        // Clear all filter
        search.collapseActionView();
        showingStarred = false;
        showStarred.setIcon(R.drawable.sharp_star_border_24);
        super.onStop();
    }

    public void onNoteEdit(long noteId) {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.editscreen.Edit");
        startNoteActivity.putExtra(EditFragment.EXTRA_NOTE_ID, noteId);
        startActivity(startNoteActivity);
    }


    public void onNoteEdit() {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.editscreen.Edit");
        startActivity(startNoteActivity);
    }

    public void selectBackUpLocation(){
        //Define back up file name
        String backUpName = getString(R.string.back_up_name)+new SimpleDateFormat("yyMMddHHmmss").format(new Date())+"_db";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType("application/octet-stream");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Intent.EXTRA_TITLE, backUpName);
        startActivityForResult(intent, BACK_UP_REQUEST_CODE);
    }

    public void selectRestoreLocation(){
        //Define back up file name

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType("application/octet-stream");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, RESTORE_REQUEST_CODE);
    }

}
