package com.jkjk.quicknote.Fragment;



import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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

    public static boolean hasNoteSave;

    long noteId;
    private EditText titleInFragment, contentInFragment;
    boolean newNote;
    FloatingActionButton done;
    //initial value not starred
    private int isStarred = 0;


    public NoteEditFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_edit, container, false);
        hasNoteSave = false;

        if (savedInstanceState !=null) {
            noteId = savedInstanceState.getLong(NOTE_ID, 0L);
            newNote = false;
        }else if (getArguments() != null) {
            noteId = getArguments().getLong(EXTRA_NOTE_ID, 0L);
            newNote = false;
        } else {newNote = true;}

        titleInFragment = (EditText) view.findViewById(R.id.title_edit_fragment);
        contentInFragment = (EditText) view.findViewById(R.id.content_edit_fragment);
        ImageButton shareButton = (ImageButton)view.findViewById(R.id.share);

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

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,titleInFragment.getText()+"\n\n"+contentInFragment.getText());
                shareIntent.putExtra(Intent.EXTRA_TITLE,R.string.share+" "+titleInFragment.getText());
                startActivity(shareIntent);
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
            Log.e(getClass().getName(), "error",e);
        }

    }


    public static NoteEditFragment newNoteEditFragmentInstance(long noteId){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_NOTE_ID, noteId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newNoteEditFragmentInstance(){
        return new NoteEditFragment();
    }

}
