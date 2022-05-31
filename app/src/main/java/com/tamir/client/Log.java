package com.tamir.client;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Log extends AppCompatActivity {

    private static String log = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        TextView tv = findViewById(R.id.logContents);
        tv.setText(log);
    }

    public static void addMessage(final String newMessage){
       log = log + "\n" + newMessage;
    }
}
