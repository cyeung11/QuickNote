package com.jkjk.quicknote.noteeditscreen;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.actions.NoteIntents;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;
import com.jkjk.quicknote.helper.NotificationHelper;

import static android.content.Context.MODE_PRIVATE;
import static com.jkjk.quicknote.MyApplication.PINNED_NOTIFICATION_IDS;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_PIN_ITEM;
import static com.jkjk.quicknote.helper.NotificationHelper.PIN_ITEM_NOTIFICATION_ID;
import static com.jkjk.quicknote.widget.NoteListWidget.updateNoteListWidget;
import static com.jkjk.quicknote.widget.NoteWidget.updateNoteWidget;


public class NoteEditFragment extends Fragment {

    private Context context;
    public final static String EXTRA_ITEM_ID = "extraItemId";
    private static final String NOTE_ID = "noteId";
    public final static long DEFAULT_NOTE_ID = 999999999L;
    boolean hasNoteSave = false;
    private long noteId;
    private EditText titleInFragment, contentInFragment;
    private boolean newNote;
    private float doneButtonYPosition;

    private Note note = new Note();

    public NoteEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain correspond value from preferences to show appropriate size for the view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
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

        if (savedInstanceState != null) {
            // case when restoring from saved instance
            noteId = savedInstanceState.getLong(NOTE_ID, 0L);
            newNote = false;
        } else if (getArguments() != null) {

            // case when argument has data, either note ID from the note list activity or text from external intent
            noteId = getArguments().getLong(EXTRA_ITEM_ID, DEFAULT_NOTE_ID);
            newNote = (noteId == DEFAULT_NOTE_ID);

            // Read data from external intent
            if (newNote){
                String title = getArguments().getString(Intent.EXTRA_TITLE, "");
                if (title.isEmpty()) {
                    title = getArguments().getString(NoteIntents.EXTRA_NAME, "");
                }
                note.setTitle(title);
                note.setContent(getArguments().getString(Intent.EXTRA_TEXT, ""));
                titleInFragment.setText(note.getTitle());
                contentInFragment.setText(note.getContent());
            }

        } else {newNote = true;}


        //read data from database and attach them into the fragment
        if (!newNote) {

            note = Note.Companion.getNote(context, noteId);
            if (note != null) {
                titleInFragment.setText(note.getTitle());
                contentInFragment.setText(note.getContent());
            } else {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                hasNoteSave = true;
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return view;
            }
        }


        final ImageButton showDropMenu = view.findViewById(R.id.edit_show_drop_menu);

        showDropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu editDropMenu = new PopupMenu(context, showDropMenu);
                editDropMenu.inflate(R.menu.note_edit_drop_menu);

                SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                MenuItem pinToNotification = editDropMenu.getMenu().findItem(R.id.edit_drop_menu_pin);
                if (!newNote && idPref.getLong(Long.toString(noteId), 999999L)!=999999L){
                    pinToNotification.setTitle(R.string.notification_unpin);
                } else pinToNotification.setTitle(R.string.notification_pin);

                pinToNotification.setVisible(true);


                MenuItem starredButton = editDropMenu.getMenu().findItem(R.id.edit_drop_menu_starred);
                if (!note.isStarred()){
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
                                shareIntent.putExtra(Intent.EXTRA_TEXT, (titleInFragment.getText() + "\n\n" + contentInFragment.getText()).trim());
                                shareIntent.putExtra(Intent.EXTRA_TITLE, titleInFragment.getText().toString());
                                shareIntent.putExtra(NoteIntents.EXTRA_NAME, titleInFragment.getText().toString());
                                Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share));
                                startActivity(chooser);
                                return true;

                            case R.id.edit_drop_menu_starred:
                                if (newNote){
                                    //save new note in and star
                                    note.setStarred(true);
                                    saveNote();
                                    hasNoteSave = false;
                                    Toast.makeText(context, R.string.saved_starred_toast, Toast.LENGTH_SHORT).show();
                                } else if (note.isStarred()){
                                    // Unstarred
                                    note.setStarred(false);
                                    saveNote();
                                    hasNoteSave = false;
                                    Toast.makeText(context, R.string.unstarred_toast, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Star
                                    note.setStarred(true);
                                    saveNote();
                                    hasNoteSave = false;
                                    Toast.makeText(context, R.string.starred_toast, Toast.LENGTH_SHORT).show();
                                }
                                return true;

                            case R.id.edit_drop_menu_delete:
                                //Delete note
                                new AlertDialog.Builder(context).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_edit)
                                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (!newNote) {
                                                            Note.Companion.delete(context, noteId);
                                                            updateNoteListWidget(context.getApplicationContext());

                                                            AlarmHelper.cancelReminder(context.getApplicationContext(), noteId);

                                                            SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                                                            if (idPref.getLong(Long.toString(noteId), 999999L)!=999999L) {
                                                                idPref.edit().remove(Long.toString(noteId)).apply();
                                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                                if (notificationManager != null) {
                                                                    notificationManager.cancel((int)noteId*PIN_ITEM_NOTIFICATION_ID);
                                                                }
                                                            }
                                                        }
                                                        // No need to do saving
                                                        hasNoteSave = true;
                                                        Toast.makeText(context, R.string.note_deleted_toast, Toast.LENGTH_SHORT).show();
                                                        if (getActivity() != null) {
                                                            getActivity().finish();
                                                        }
                                                    }
                                                }
                                        )
                                        .setNegativeButton(R.string.cancel, null)
                                        .show();
                                return true;

                            case R.id.edit_drop_menu_pin:
                                // if it is a new note, save the note and then pin to notification
                                if (newNote) {
                                    saveNote();
                                    hasNoteSave = false;
                                    Toast.makeText(getActivity(), R.string.saved_note, Toast.LENGTH_SHORT).show();
                                }

                                SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);

                                if (idPref.getLong(Long.toString(noteId), 999999L)==999999L) {
                                    pinNoteToNotification();
                                } else {
                                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (notificationManager!=null){
                                        // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
                                        notificationManager.cancel((int)noteId*PIN_ITEM_NOTIFICATION_ID);
                                        idPref.edit().remove(Long.toString(noteId)).apply();
                                    }
                                }
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
        if (getActivity() != null) {
            final FloatingActionButton done = getActivity().findViewById(R.id.save_fab);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                done.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sharp_save_24));
            } else {
                done.setImageResource(R.drawable.sharp_save_24);
            }
            done.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveNote();
                    Toast.makeText(getActivity(), R.string.saved_note, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });

            doneButtonYPosition = done.getY();
            done.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    float y = done.getY();
                    if (y < doneButtonYPosition){
                        if (contentInFragment.hasFocus()) {
                            done.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (done.getVisibility() != View.VISIBLE) done.setVisibility(View.VISIBLE);
                    }
                    doneButtonYPosition = y;
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NOTE_ID, noteId);
    }

    @Override
    public void onResume() {
        super.onResume();
        //  reset it to not saved when user come back
        hasNoteSave = false;
        note.setTitle(titleInFragment.getText().toString());
        note.setContent(contentInFragment.getText().toString());
    }

    @Override
    public void onPause() {
        if (!hasNoteSave && checkModified()) {
            //when user quit the app without choosing save or discard, save the note
            saveNote();
            Toast.makeText(getActivity(), R.string.saved_note, Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        context = null;
        super.onDetach();
    }

    public void saveNote(){
        String title = titleInFragment.getText().toString().trim();
        if (title.isEmpty()) {
            title = getString(R.string.untitled_note);
        }
        note.setTitle(title);
        note.setContent(contentInFragment.getText().toString());

        if (!newNote) {
            note.save(context, noteId);
            SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
            if (idPref.getLong(Long.toString(noteId), 999999L) != 999999L) {
                pinNoteToNotification();
            }

//            // to update pinned notification if there is any, api 23 up exclusive
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if(isItemAnActiveNotification()){
//                    pinNoteToNotification();
//                }
//            }
        }else {
            noteId = note.saveAsNew(context);
        }

        hasNoteSave = true;
        newNote = false;
        updateNoteWidget(context);
        updateNoteListWidget(context);
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private boolean isItemAnActiveNotification(){
//        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (notificationManager != null) {
//            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
//            ArrayList<Integer> notificationId = new ArrayList<>();
//            for (StatusBarNotification activeNotification : activeNotifications) {
//                notificationId.add(activeNotification.getId());
//            }
//            // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
//            return notificationId.contains((int) noteId * PIN_ITEM_NOTIFICATION_ID);
//        } else Toast.makeText(getContext(), R.string.error_text, Toast.LENGTH_SHORT).show();
//        return false;
//    }

    private void pinNoteToNotification(){
        Intent intent = new Intent(context, NotificationHelper.class);
        intent.setAction(ACTION_PIN_ITEM);
        intent.putExtra(EXTRA_ITEM_ID, noteId);
        PendingIntent pinNotificationPI = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pinNotificationPI.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    boolean checkModified(){
        if (newNote){
            return !titleInFragment.getText().toString().isEmpty()
                    || !contentInFragment.getText().toString().isEmpty();
        } else {
            return !note.getTitle().equals(titleInFragment.getText().toString())
                    || !note.getContent().equals(contentInFragment.getText().toString());
        }
    }


    public static NoteEditFragment newEditFragmentInstance(long noteId){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ITEM_ID, noteId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newEditFragmentInstance(Intent intent){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        try {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                bundle.putString(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
            }
            if (intent.hasExtra(Intent.EXTRA_TITLE)){
                bundle.putString(Intent.EXTRA_TITLE, intent.getStringExtra(Intent.EXTRA_TITLE));
            } else if (intent.hasExtra(NoteIntents.EXTRA_NAME)){
                bundle.putString(Intent.EXTRA_TITLE, intent.getStringExtra(NoteIntents.EXTRA_NAME));
            }
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
