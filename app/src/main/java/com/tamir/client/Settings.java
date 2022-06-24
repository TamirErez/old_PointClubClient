package com.tamir.client;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class Settings extends Activity {

    public static int diff = 0;
    private static String name = null;
    ProgressBar bar;
    TextView progressPerc;

    private static Settings instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        instance = this;
        bar = findViewById(R.id.progressBar);
        progressPerc = findViewById(R.id.prec);
        TextView temp = findViewById(R.id.currName);
        temp.setText(temp.getText() + MainActivity.name);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(name != null){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MainActivity.out.writeUTF("name:" + name);
                        MainActivity.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }

    }

    public static Settings getInstance(){
        return instance;
    }

    public void changeName(View view) {
        String temp = ((EditText) findViewById(R.id.setUsername)).getText().toString().trim();
        if(temp.length() == 0)
            return;
        MainActivity.name = temp;
        name = MainActivity.name;
        MainActivity.userInfo.setUsername(name);
        DB.writeObjectToFile(MainActivity.userInfo,MainActivity.userinfoPath);
        Toast.makeText(this,"Name changed to " + MainActivity.name, Toast.LENGTH_LONG).show();
    }

    public void getNewVer(View view){
        System.out.println("oh noes" + bar.getProgress());
        if(bar.getProgress() < 100 && bar.getProgress() > 0)
            return;
        System.out.println("here1");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("sending- request:update");
                    MainActivity.out.writeUTF("request:update");
                    MainActivity.out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void updateProgress(final int diff){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(diff);
                String per = diff + "%";
                progressPerc.setText(per);
            }
        });
    }

    public void gotoLog(View view) {
        Intent intent = new Intent(this, Log.class);
        startActivity(intent);
    }
}
