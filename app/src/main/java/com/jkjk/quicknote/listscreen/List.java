package com.jkjk.quicknote.listscreen;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.jkjk.quicknote.R;


public class List extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

    }

}
