package com.jkjk.quicknote.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.jkjk.quicknote.R;

public class LauncherShortcuts extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            setupShortcut();
            finish();
        }
    }

    private void setupShortcut() {
        // set up the shortcut intent.
        Intent shortcutIntent = new Intent();
        shortcutIntent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.editscreen.Edit").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Then, set up the container intent (the response to the caller)
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.new_note));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this,  R.mipmap.ic_launcher);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        // Now, return the result to the launcher
        setResult(RESULT_OK, intent);
    }

}
