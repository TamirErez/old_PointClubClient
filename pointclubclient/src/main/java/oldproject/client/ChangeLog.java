package oldproject.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.tamir.client.R;

public class ChangeLog extends AppCompatActivity {

    private final String changeLog =
            "\nVersion 4:\n" +
                    "\t-fixed a crash on startup when server is down\n" +
                    "\t-fixed spaces as legal input for name and message\n" +
                    "\t-compressed apk before sending it\n" +
            "\nVersion 5:\n" +
                    "\t-fixed the compression\n"+
                    "\t-optimized the background threads\n" +
            "\nVersion 6:\n" +
                     "\t-the game now saves the username\n"+
            "\nVersion 7:\n" +
                    "\t-fixed a bug with the username where it would be mixed with other messages\n"+
                    "\t-fixed a game crashing bug where the app would crash when trying to send the username before connecting to the server\n"+
                    "\t-the server now saves the chat and sends to the user\n"+
            "\nVersion 8:\n" +
                    "\t-added rooms\n"+
                    "\t-user that sends the message won't receive it as well\n" +
                    "\t-fixed a bug where requesting update twice in a row would corrupt the update\n" +
            "\nVersion 9:\n" +
                    "\t-chat is now aligned left\n"+
                    "\t-added log to settings to see incoming and outgoing messages\n" +
            "\nVersion 10:\n" +
                    "\t-added current name to settings\n"+
                    "\t-fixed a bug where joining a game when server is down causes crash\n" +
                    "\t-made change log scrollable and organized a bit\n" +
                    "\t-changed the app logo\n"+
            "\nVersion 11:\n" +
                    "\t-added ability to send photos\n" +
            "\nVersion 12:\n" +
                    "\t-changed game UI\n" +
                    "\t-Improved communication versatility\n" +
                    "\t-fixed a bug where canceling taking a photo would crash the room\n"
            ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_log);

        TextView tv = findViewById(R.id.changeLog);

        tv.setText(changeLog);
    }
}
