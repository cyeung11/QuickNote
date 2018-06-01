package com.jkjk.quicknote.taskeditscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.TaskListFragment;

import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;

public class TaskEdit extends AppCompatActivity {

    private TaskEditFragment taskEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        long taskId;
        final String fragmentTag = "taskEditFragmentTag";

        if (savedInstanceState == null) {
            //Case when the activity is newly created

            if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
                //Case when activity is called by note list activity after user selected existing note
                taskId = getIntent().getLongExtra(EXTRA_NOTE_ID, 0L);
                taskEditFragment = TaskEditFragment.newEditFragmentInstance(taskId);

            } else {
                //Case when opening a new note
                taskEditFragment = TaskEditFragment.newEditFragmentInstance();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.task_container, taskEditFragment,fragmentTag).commit();

        }else {
            //Case when restoring from saved instance, rotate etc.
            taskEditFragment = (TaskEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
    }

    @Override
    public void onBackPressed() {
        //confirm to discard dialog, and ask the note list activity to update its view
        new AlertDialog.Builder(this).setTitle(R.string.discard_title).setMessage(R.string.confirm_discard)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!taskEditFragment.hasTaskSave){
                            //NoteEdit no need to save. so override hasNoteSave to true to avoid saving on onStop
                            taskEditFragment.hasTaskSave = true;
                        }
                        TaskEdit.super.onBackPressed();
                    }
                })
                .setNeutralButton(R.string.save_instead, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Save instantly instead of onStop so that when the note list's onResume get called, the note is already update
                        taskEditFragment.saveTask();
                        Toast.makeText(getBaseContext(),R.string.saved, Toast.LENGTH_SHORT).show();
                        taskEditFragment.hasTaskSave = true;
                        TaskEdit.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
