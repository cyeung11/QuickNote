package com.jkjk.quicknote.Fragment;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.jkjk.quicknote.Adapter.NoteListAdapter;
import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.Widget.AppWidgetService;
import com.jkjk.quicknote.Widget.NoteWidget;

import java.util.Calendar;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditFragment extends Fragment {

    public static final String NOTE_ID = "noteId";
    public static final String DEFAULT_FRAGMENT_TAG = "NoteEditFragment";
    public final static String EXTRA_NOTE_ID = "extraNoteId";

    public static boolean hasNoteSave = false;

    long noteId;
    private EditText titleInFragment, contentInFragment;
    boolean newNote;
    FloatingActionButton done;
    // 0 stands for not starred, 1 starred
    private int isStarred = 0;


    public NoteEditFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_edit, container, false);
        titleInFragment = (EditText) view.findViewById(R.id.title_edit_fragment);
        contentInFragment = (EditText) view.findViewById(R.id.content_edit_fragment);

        if (savedInstanceState !=null) {
            // case when restoring from saved instance
            noteId = savedInstanceState.getLong(NOTE_ID, 0L);
            newNote = false;
        }else if (getArguments() != null) {

            // case when argument has data, either note ID from the note list activity or text from external intent
            noteId = getArguments().getLong(EXTRA_NOTE_ID, 999999999L);
            newNote = (noteId == 999999999L);

            // Read data from external intent
            if (newNote){
                contentInFragment.setText(getArguments().getString(Intent.EXTRA_TEXT));
            }

        } else {newNote = true;}


        //read data from database and attach them into the fragment
        if (!newNote) {
            try {
                Cursor tempNote = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "content", "starred"}, "_id='" + noteId +"'",
                      null, null, null, null, null);
                tempNote.moveToFirst();
                titleInFragment.setText(tempNote.getString(0));
                contentInFragment.setText(tempNote.getString(1));
                isStarred = tempNote.getInt(2);
                tempNote.close();
            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }


//        ImageButton shareButton = (ImageButton)view.findViewById(R.id.share);
//        shareButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.setType("text/plain");
//                shareIntent.putExtra(Intent.EXTRA_TEXT,titleInFragment.getText()+"\n\n"+contentInFragment.getText());
//                shareIntent.putExtra(Intent.EXTRA_TITLE,R.string.share+" "+titleInFragment.getText());
//                startActivity(shareIntent);
//            }
//        });

        final ImageButton showDropMenu = (ImageButton)view.findViewById(R.id.edit_show_drop_menu);
        showDropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu editDropMenu = new PopupMenu(view.getContext(), showDropMenu);
                editDropMenu.getMenuInflater().inflate(R.menu.edit_drop_menu, editDropMenu.getMenu());
                MenuItem starredButton = (MenuItem) editDropMenu.getMenu().findItem(R.id.edit_drop_menu_starred);
                if (isStarred == 0){
                    // not starred, set button to starred
                    starredButton.setTitle(R.string.starred);
                } else {
                    starredButton.setTitle(R.string.unstarred);
                }
                editDropMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit_drop_menu_share:
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,titleInFragment.getText()+"\n\n"+contentInFragment.getText());
                                shareIntent.putExtra(Intent.EXTRA_TITLE, titleInFragment.getText());
                                startActivity(shareIntent);
                                return true;

                            case R.id.edit_drop_menu_starred:
                                ContentValues values = new ContentValues();
                                if (newNote){
                                    //save new note in and star
                                    isStarred = 1;
                                    String noteTitle = titleInFragment.getText().toString().trim();
                                    if (noteTitle.length()<1){
                                        noteTitle = getActivity().getResources().getString(R.string.untitled);
                                    }
                                    values.put("title", noteTitle);
                                    values.put("content", contentInFragment.getText().toString());
                                    values.put("time", Long.toString(Calendar.getInstance().getTimeInMillis()));
                                    values.put("starred", isStarred);
                                    noteId = MyApplication.database.insert(DATABASE_NAME, "",values);
                                    newNote = false;
                                    Toast.makeText(getContext(), R.string.saved_starred_toast, Toast.LENGTH_SHORT).show();
                                } else if (isStarred == 1){
                                    // Unstarred
                                    isStarred = 0;
                                    values.put("starred", isStarred);
                                    MyApplication.database.update(DATABASE_NAME, values, "_id='" + noteId +"'", null);
                                    Toast.makeText(getContext(), R.string.unstarred_toast, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Star
                                    isStarred = 1;
                                    values.put("starred", isStarred);
                                    MyApplication.database.update(DATABASE_NAME, values, "_id='" + noteId +"'", null);
                                    Toast.makeText(getContext(), R.string.starred_toast, Toast.LENGTH_SHORT).show();
                                }
                                values.clear();
                                return true;

                            case R.id.edit_drop_menu_delete:
                                //Delete note
                                new AlertDialog.Builder(getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_edit)
                                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (!newNote) {
                                                    MyApplication.database.delete(DATABASE_NAME, "_id='" + noteId + "'", null);
                                                }
                                                    // No need to do saving
                                                    hasNoteSave = true;
                                                    Toast.makeText(getContext(), R.string.deleted_toast, Toast.LENGTH_SHORT).show();
                                                    getActivity().finish();
                                                }
                                            }
                                        )
                                        .setNegativeButton(R.string.cancel, null)
                                        .show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                editDropMenu.show();
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Define save function for the done button
        done = (FloatingActionButton)getActivity().findViewById(R.id.done_fab);
        done.setImageDrawable(getResources().getDrawable(R.drawable.sharp_done_24));
        done.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
                Toast.makeText(getContext(),R.string.saved, Toast.LENGTH_SHORT).show();
                updateAllWidget();
                hasNoteSave = true;
                getActivity().finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NOTE_ID, noteId);
    }



    public void saveNote(){
        ContentValues values = new ContentValues();
        String noteTitle = titleInFragment.getText().toString().trim();
        if (noteTitle.length()<1){
            noteTitle = getActivity().getResources().getString(R.string.untitled);
        }
        values.put("title", noteTitle);
        values.put("content", contentInFragment.getText().toString());
        values.put("time", Long.toString(Calendar.getInstance().getTimeInMillis()));
        values.put("starred", isStarred);
        if (!newNote) {
            MyApplication.database.update(DATABASE_NAME, values, "_id='" + noteId +"'", null);
        }else {
            noteId = MyApplication.database.insert(DATABASE_NAME, "",values);
        }
        values.clear();
        NoteListAdapter.updateCursor();
        hasNoteSave = true;
        newNote = false;
    }

    public void updateAllWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
        ComponentName name = new ComponentName(getActivity().getPackageName(), NoteWidget.class.getName());
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(name);
        Intent intent = new Intent(getContext(), AppWidgetService.class).putExtra(EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.error_text, Toast.LENGTH_SHORT).show();
            Log.e(getClass().getName(), "updating widget",e);
        }

    }


    public static NoteEditFragment newNoteEditFragmentInstance(long noteId){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_NOTE_ID, noteId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newNoteEditFragmentInstance(Intent intent){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        try {
            bundle.putString(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
        } catch (Exception e){
            Toast.makeText(fragment.getContext(),R.string.error_loading,Toast.LENGTH_SHORT).show();
            Log.e(fragment.getClass().getName(),"Loading from incoming intent",e);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newNoteEditFragmentInstance(){
        return new NoteEditFragment();
    }

}
