package com.jkjk.quicknote.Fragment;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jkjk.quicknote.Adapter.NoteListAdapter;
import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.Service.AppWidgetService;
import com.jkjk.quicknote.Widget.NoteWidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.inActionMode;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.mActionMode;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    RecyclerView recyclerView;
    NoteListAdapter noteListAdapter;
    FloatingActionButton addNote;
    android.support.v7.widget.Toolbar noteListMenu;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //TODO
//        setHasOptionsMenu(true);
        noteListAdapter = new NoteListAdapter(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if (!inActionMode) {
//            noteListMenu = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.note_list_menu);
//            ((AppCompatActivity) getActivity()).setSupportActionBar(noteListMenu);
//        }

        if (savedInstanceState!=null){
            noteListAdapter.notifyDataSetChanged();
        }

        addNote = (FloatingActionButton)getActivity().findViewById(R.id.add_note);
        addNote.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white));
        addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //TODO delete note  selectedItems
                                    int[] mSelect = noteListAdapter.getSelected();
                                    Cursor tempNote = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"_id", "time"}, null, null, null
                                            , null, "time DESC");
                                    for (int removedPosition : mSelect) {
                                        tempNote.moveToPosition(removedPosition);
                                        String removedId = tempNote.getString(0);
                                        MyApplication.getDatabase().delete(DATABASE_NAME, "_id='" + removedId+"'",null);
                                        NoteListAdapter.updateCursor();
                                        mActionMode.finish();
                                        noteListAdapter.notifyItemRemoved(removedPosition);
                                    }
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
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(noteListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }


    //TODO
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //call its fragment notifyitemchange method to update list
        if (data!=null) {
                noteListAdapter.notifyDataSetChanged();
                updateAllWidget();
            }
    }


    @Override
    public void onStop() {
        if (mActionMode!=null) {
            mActionMode.finish();
        }
        super.onStop();
    }

    public void onNoteEdit(long noteId) {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startNoteActivity.putExtra(NoteEditFragment.EXTRA_NOTE_ID, noteId);
        startActivityForResult(startNoteActivity, 1);
    }


    public void onNoteEdit() {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startActivityForResult(startNoteActivity, 1);
    }

    private void updateAllWidget(){
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

}
