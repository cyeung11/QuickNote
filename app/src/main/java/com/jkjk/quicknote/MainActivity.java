package com.jkjk.quicknote;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class MainActivity extends AppCompatActivity {

    EditText note;
    public final static String ACTION_UPDATE_NOTE = "actionUpdateNote";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        note = (EditText) findViewById(R.id.note);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private void Save() {
        //Saving everything in the EditText into noteSaved with OutputStreamWriter
        try {
            OutputStreamWriter writer = new OutputStreamWriter(openFileOutput("noteSaved", Context.MODE_PRIVATE));
            writer.write(note.getText().toString());
            writer.flush();
            writer.close();
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            Log.e("main","error",e);
        }

        Intent intent = new Intent(getBaseContext(),AppWidgetService.class);
        intent.putExtra(ACTION_UPDATE_NOTE,true);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        try { pendingIntent.send();
        } catch (Exception e){
            Toast.makeText(this, R.string.error_text, Toast.LENGTH_SHORT).show();
            Log.e("main","error",e);
        }
    }

    private void Load(){
        //Determine whether saved file exists before loading it into the activity
        try {
            if ((getFileStreamPath("noteSaved").canRead())) {
                InputStreamReader in = new InputStreamReader(openFileInput("noteSaved"));
                BufferedReader reader = new BufferedReader(in);
                StringBuilder stringBuilder = new StringBuilder();
                String readLine;
                while ((readLine = reader.readLine()) != null) {
                    stringBuilder.append(readLine).append("\n");
                }
                reader.close();
                if (stringBuilder.length() != 0) {
                    stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length() + 1);
                }
                String mNote = stringBuilder.toString();
                note.setText(mNote);
            }
        }catch(Exception e) {
            Toast.makeText(this, R.string.error_loading, Toast.LENGTH_SHORT).show();
            Log.e("main","error",e);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //By pressing the done button on the action bar, call finish and end the application
        switch (item.getItemId()) {
            case R.id.done:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onStop() {
        //Run the save method every time the activity is stopped
        super.onStop();
        Save();


    }

        @Override
    public void onResume() {
        //Load the saved note every time the activity is resumed
            super.onResume();
            Load();
        }

}
