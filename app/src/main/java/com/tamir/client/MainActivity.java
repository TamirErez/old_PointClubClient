package com.tamir.client;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.InflaterInputStream;


public class MainActivity extends AppCompatActivity {

    public static final String hostIP = "85.64.202.25";
    public static final int port = 31415;
    public static final String FileName = "client.apk";
    private static Socket socket = null;
    public static DataOutputStream out;
    public static DataInputStream in;
    public static String name = "Anon";
    Thread checkVersion;
    public static final int version = 12;
    public static final String channel_id = "default";
    public static UserInfo userInfo;
    public static final int RC_SIGN_IN = 42;
    public static boolean isConnectedToServer = false;

    public static final File userinfoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES), "userinfo");
    public static final String userinfoPath = userinfoFile.getPath();

    @Override
    protected void onResume() {
        super.onResume();
        Thread temp = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.addMessage("sent: " + "leavegame");
                    out.writeUTF("leavegame");
                    out.flush();
                }
                catch (Exception e){
                    //I don't know what im doing
                    System.out.println("Exception occurred: " + e.getMessage());
                }
            }
        });
        temp.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();


        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int getAccountsPermission =ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS);

        // If do not grant write external storage permission.

        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (getAccountsPermission != PackageManager.PERMISSION_GRANTED) {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, 1);
        }

        /*String s = getUserGoogleName();
        System.out.println("google username: "  + s);*/

        Thread listen = initConnection();

        checkVersion = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.addMessage("sent: " + "version:" + version);
                try {
                    out.writeUTF("version:" + version);
                    out.flush();
                }
                catch (Exception e){
                    System.out.println("Failed to send version");
                }
            }
        });
        listen.start();
        if (in != null)
            checkVersion.start();

        Thread sendUserinfo = new Thread(new Runnable() {
            @Override
            public void run() {
                if (userinfoFile.exists()) {
                    while (in == null) ;
                    userInfo = DB.readObjectFromFile(userinfoPath);
                    if (userInfo != null) {
                        name = userInfo.getusername();
                        Log.addMessage("sent: " + "name:" + name);
                        try {
                            out.writeUTF("name:" + name);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("failed to send name");
                        }
                    }
                } else {
                    userInfo = new UserInfo("Anon");
                    DB.writeObjectToFile(userInfo, userinfoPath);
                }
            }
        });
        sendUserinfo.start();
    }

    @NonNull
    private Thread initConnection() {
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Initialization section:
                // Try to open a socket on port 25
                // Try to open input and output streams
                try {
                    socket = new Socket(hostIP, port);
                    out = new DataOutputStream(socket.getOutputStream());
                    in = new DataInputStream(socket.getInputStream());
                    turnGreen();
                    isConnectedToServer = true;
                } catch (UnknownHostException e) {
                    System.err.println("Don't know about host: hostname");
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O for the connection to: hostname");
                }
            }
        });
        t.start();
        try {
            t.join(1000);
        } catch (Exception e) {
            Toast.makeText(this, "Server took too long to respond", Toast.LENGTH_LONG).show();
        }


        return new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    try {
                        String input = in.readUTF().toLowerCase().trim();
                        Log.addMessage("received: " + input);
                        handleInput(input);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("disconnected from server, trying to reconnect");
                        turnRed();
                        isConnectedToServer = false;
                        t.run();
                        try {
                            Thread.sleep(200);
                            t.join();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void turnRed() {
        if (Boom.getInstance() != null)
            Boom.getInstance().turnRed();
        ImageView view = findViewById(R.id.serverConnectivity);
        view.setImageResource(android.R.drawable.ic_notification_overlay);
    }

    public void turnGreen() {
        Boom boom = Boom.getInstance();
        if (boom != null)
            boom.turnGreen();
        System.out.println("reconnected to server");
        ImageView view = findViewById(R.id.serverConnectivity);
        view.setImageResource(android.R.drawable.presence_online);
    }

    public void handleInput(String input) {
        String[] split = input.split(":");
        System.out.println("got input: " + input);
        input = joinString(split, 1, ":");
        switch (split[0]) {
            case "gamemessage":
                if (Boom.getInstance() != null)
                    Boom.getInstance().addMessage(input);
                break;
            case "gamephoto":
                if (Boom.getInstance() != null)
                    Boom.getInstance().handlePhoto(input,socket);
                break;
            case "entergame":
                Intent intent = new Intent(this, Boom.class);
                intent.putExtra("name", split[1]);
                startActivity(intent);

                break;
            case "creategame":
                Intent intent2 = new Intent(this, Boom.class);
                intent2.putExtra("name", split[1]);
                startActivity(intent2);

                break;
            case "update":
                getUpdate(input);
                break;
            case "version":
                if (split[1].equals("false"))
                    showNotification("A newer version is available", "Version update");
                break;
            case "chat":
                Boom.getInstance().addMessage(input);
                break;
            case "room":
                JoinRoom.getInstance().addRoom(input);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void getUpdate(String input) {
        try {

            // Get the directory for the user's public pictures directory.
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), FileName);
            try {
                Files.delete(Paths.get(outputFile.getPath()));
                Files.createFile(outputFile.toPath());
            } catch (Exception e) {
                System.out.println("file created");
            }
            int size = Integer.parseInt(input);
            /*byte[] filyBytes = new byte[size];
            InputStream is = socket.getInputStream();
            //File yourFile = new File(filePath);
            //FileOutputStream fos = new FileOutputStream(yourFile, false);
            // BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = is.read(filyBytes, 0, filyBytes.length);
            int current = bytesRead;

            do {
                bytesRead =
                        is.read(filyBytes, current, (filyBytes.length - current));
                Settings.getInstance().updateProgress(10 + 75 * current / size);
                if (bytesRead >= 0) current += bytesRead;
            } while (current < size);

            Settings.getInstance().updateProgress(10 + 75 * current / size);
            */
            int totalSize = decompressAndWrite(size, outputFile);
            Settings.getInstance().updateProgress(100);

            //bos.write(filyBytes, 0, originalSize);
            // bos.flush();
            System.out.println("File " + "client.apk"
                    + " downloaded (" + totalSize + " bytes read)");

            showToast("Download finished");
            if (socket.isClosed())
                initConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void joinGame(final View view) {
        Thread temp = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Log.addMessage("sent: " + "request:roominfo");
                    out.writeUTF("request:roominfo");
                    out.flush();
                }
                catch (Exception e){
                    System.err.println("Couldn't join game with: " + e.getMessage());
                    Log.addMessage("Couldn't join game with: " + e.getMessage());
                }
            }
        });
        temp.start();
        if(!isConnectedToServer)
            showToast("Couldn't reach server");
        Intent intent = new Intent(this, JoinRoom.class);
        startActivity(intent);
/*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    checkVersion.run();
                    String msg = "entergame:0";
                    out.println(msg);
                    out.flush();
                    System.out.println("said to server:" + msg + "\n");
                } catch (Exception e) {
                    System.err.println("Couldn't talk to server :(");
                }
            }
        });
        t.start();
        try {
            t.join(1000);
        } catch (Exception e) {
            Toast.makeText(this, "Server took too long to respond", Toast.LENGTH_LONG).show();
        }*/
    }

    public void createGame(View view) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    checkVersion.run();
                    String msg = "creategame:boom,7";
                    Log.addMessage("sent: " + msg);
                    out.writeUTF(msg);
                    out.flush();
                    System.out.println("said to server:" + msg + "\n");
                } catch (Exception e) {
                    System.err.println("Couldn't talk to server :(");
                }
            }
        });
        t.start();
        try {
            t.join(1000);
        } catch (Exception e) {
            Toast.makeText(this, "Server took too long to respond", Toast.LENGTH_LONG).show();
        }

    }

    public String joinString(String[] split, int startIndex, String del) {
        String output = "";
        for (int i = startIndex; i < split.length; i++) {
            if (i > startIndex)
                output += del + split[i];
            else
                output += split[i];
        }
        return output;
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showNotification(String text, String title) {
        showNotification(text, title, 0);
    }

    public void showNotification(String text, String title, int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(id, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void gotoSettings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void gotoChangeLog(View view) {
        Intent intent = new Intent(this, ChangeLog.class);
        startActivity(intent);
    }

    public int decompressAndWrite(int size, File outputFile) {
        try {
            InflaterInputStream iis = new InflaterInputStream(socket.getInputStream());
            FileOutputStream fout = new FileOutputStream(outputFile);
            int counter = 0;
            int ch;
            while ((ch = iis.read()) != -1) {
                fout.write((byte) ch);
                counter++;
                Settings.getInstance().updateProgress(100 * counter / size);
                //System.out.println("counter:" + counter + " diff:" + 100*counter/size);
            }
            iis.close();
            fout.close();
            return counter;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getUserGoogleName(){

        GoogleSignInAccount _account = GoogleSignIn.getLastSignedInAccount(this);

        if (_account != null) {
            return _account.getEmail();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        assert manager != null;
        Account[] list = manager.getAccounts();
        for(Account account: list)
        {
            if(account.type.equalsIgnoreCase("com.google"))
            {
                return account.name;
            }
        }
        return "";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                showToast("you are" + account.getDisplayName());
            }
            else{
                showToast("sign in failed?");
            }
        } catch (ApiException e) {
            System.out.println("FAILED CODE: " + e.getStatusCode());
            e.printStackTrace();
        }
    }
}
