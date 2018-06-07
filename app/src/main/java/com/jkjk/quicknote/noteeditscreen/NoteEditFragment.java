package com.jkjk.quicknote.noteeditscreen;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.widget.AppWidgetService;
import com.jkjk.quicknote.widget.NoteWidget;

import java.util.Calendar;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;



public class NoteEditFragment extends Fragment {

    private static final String NOTE_ID = "noteId";
    public final static String EXTRA_NOTE_ID = "extraNoteId";
    public final static long DEFAULT_NOTE_ID = 999999999L;

    boolean hasNoteSave = false;

    private long noteId;
    private EditText titleInFragment, contentInFragment;
    private boolean newNote;
    private String title, content;

    // 0 stands for not starred, 1 starred
    private int isStarred = 0;


    public NoteEditFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain correspond value from preferences to show appropriate size for the view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String editViewSize = sharedPref.getString(getResources().getString(R.string.font_size_editing_screen),"m");
        int editViewInt;
        switch (editViewSize){
            case ("s"):
                editViewInt = R.layout.fragment_note_edit_s;
                break;
            case ("m"):
                editViewInt = R.layout.fragment_note_edit_m;
                break;
            case ("l"):
                editViewInt = R.layout.fragment_note_edit_l;
                break;
            case ("xl"):
                editViewInt = R.layout.fragment_note_edit_xl;
                break;
            default:
                editViewInt = R.layout.fragment_note_edit_m;
        }


        // Inflate the layout for this fragment
        View view = inflater.inflate(editViewInt, container, false);

        titleInFragment = view.findViewById(R.id.title_edit_fragment);
        contentInFragment = view.findViewById(R.id.content_edit_fragment);

        if (savedInstanceState !=null) {
            // case when restoring from saved instance
            noteId = savedInstanceState.getLong(NOTE_ID, 0L);
            newNote = false;
        }else if (getArguments() != null) {

            // case when argument has data, either note ID from the note list activity or text from external intent
            noteId = getArguments().getLong(EXTRA_NOTE_ID, DEFAULT_NOTE_ID);
            newNote = (noteId == DEFAULT_NOTE_ID);

            // Read data from external intent
            if (newNote){
                contentInFragment.setText(getArguments().getString(Intent.EXTRA_TEXT));
            }

        } else {newNote = true;}


        //read data from database and attach them into the fragment
        if (!newNote) {
            try {
                Cursor noteCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "content", "starred"}, "_id='" + noteId +"'",
                      null, null, null, null, null);
                noteCursor.moveToFirst();
                titleInFragment.setText(noteCursor.getString(0));
                contentInFragment.setText(noteCursor.getString(1));
                isStarred = noteCursor.getInt(2);
                noteCursor.close();
            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }

        final ImageButton showDropMenu = view.findViewById(R.id.edit_show_drop_menu);
        showDropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu editDropMenu = new PopupMenu(view.getContext(), showDropMenu);
                editDropMenu.inflate(R.menu.note_edit_drop_menu);
                MenuItem starredButton = editDropMenu.getMenu().findItem(R.id.edit_drop_menu_starred);
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
                                        noteTitle = getActivity().getResources().getString(R.string.untitled_note);
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
                                                    Toast.makeText(getContext(), R.string.note_deleted_toast, Toast.LENGTH_SHORT).show();
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
        FloatingActionButton done = getActivity().findViewById(R.id.done_fab);
        done.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.sharp_done_24));
        done.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
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

    @Override
    public void onResume() {
        super.onResume();
        title = titleInFragment.getText().toString();
        content = contentInFragment.getText().toString();
    }

    @Override
    public void onPause() {
        if (checkModified() && !hasNoteSave) {
            //when user quit the app without choosing save or discard, save the note
            saveNote();
            updateAllWidget();
        }
        // then reset it to not saved for the case when user come back
        hasNoteSave = false;
        super.onPause();
    }

    public void saveNote(){
        ContentValues values = new ContentValues();
        String noteTitle = titleInFragment.getText().toString().trim();
        if (noteTitle.length()<1){
            noteTitle = getString(R.string.untitled_note);
        }
        values.put("title", noteTitle);
        values.put("content", contentInFragment.getText().toString());
        values.put("time", Long.toString(Calendar.getInstance().getTimeInMillis()));
        values.put("starred", isStarred);
        values.put("type", 0);
        values.put("urgency", 0);
        values.put("done", 0);
        if (!newNote) {
            MyApplication.database.update(DATABASE_NAME, values, "_id='" + noteId +"'", null);
        }else {
            noteId = MyApplication.database.insert(DATABASE_NAME, "",values);
        }
        values.clear();
        hasNoteSave = true;
        newNote = false;
        Toast.makeText(getActivity(), R.string.saved_note, Toast.LENGTH_SHORT).show();
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

    boolean checkModified(){
        if (newNote){
            return !titleInFragment.getText().toString().trim().equals("")
                    || !contentInFragment.getText().toString().trim().equals("");
        } else {
            return !title.equals(titleInFragment.getText().toString())
                    || !content.equals(contentInFragment.getText().toString());
        }
    }


    public static NoteEditFragment newEditFragmentInstance(long noteId){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_NOTE_ID, noteId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newEditFragmentInstance(Intent intent){
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

    public static NoteEditFragment newEditFragmentInstance(){
        return new NoteEditFragment();
    }

}
