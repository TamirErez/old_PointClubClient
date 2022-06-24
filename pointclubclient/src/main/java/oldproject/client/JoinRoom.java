package oldproject.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tamir.client.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinRoom extends AppCompatActivity {

    DataOutputStream out;
    private boolean isReady = false;
    public List<String> rooms;
    private static JoinRoom instance;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        instance = this;
        rooms = new ArrayList<>();
        out = MainActivity.out;
        setupList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isReady = false;
    }

    public static JoinRoom getInstance(){
        return instance;
    }

    public void setupList() {
        final ListView listView = findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, rooms);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String itemValue = (String) listView.getItemAtPosition(position);

                gotoRoom(itemValue.split("\\(")[0].trim());

            }
        });
        isReady = true;
    }

    public void gotoRoom(final String roomName){
        if(out == null) return;
        Thread temp = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.addMessage("sent: " + "entergame:" + roomName);
                try {
                    out.writeUTF("entergame:" + roomName);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        temp.start();
    }

    //expects input of type: <name>,<player count>
    public void addRoom(String input) {
        String[] split = input.split(",");
        String newRoom = split[0] + " (player count: " + split[1] + ")";
        rooms.add(newRoom);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
