package oldproject.client;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tamir.client.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Boom extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    DataOutputStream out;
    DataInputStream in;
    //TextView tv;
    LinearLayout tv;
    ImageView sc;
    public String name;
    String currentPhotoPath;


    private static Boom instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
        }
        instance = this;
        /*if (socket == null)
            finish();*/
        out = MainActivity.out;
        in = MainActivity.in;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        tv = findViewById(R.id.chatViewLayout);
        //tv = findViewById(R.id.chatView);
        //tv.setMovementMethod(new ScrollingMovementMethod());
        sc = findViewById(R.id.serverConnectivity);
        final EditText et = findViewById(R.id.enterMessage);

        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    sendMessage(v);
                    et.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    public static Boom getInstance() {
        return instance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.addMessage("sent: " + "leavegame");
                try {
                    out.writeUTF("leavegame");
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void turnGreen() {
        if (sc != null)
            sc.setImageResource(android.R.drawable.presence_online);
    }

    public void turnRed() {
        if (sc != null)
            sc.setImageResource(android.R.drawable.ic_notification_overlay);
    }

    public void sendMessage(View view) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String msg = ((EditText) findViewById(R.id.enterMessage)).getText().toString().trim();
                if (msg.length() == 0)
                    return;
                msg = MainActivity.name + ":" + msg;
                addMessage(msg);
                Log.addMessage("sent: " + "gamemessage:" + name + "," + msg);
                try {
                    out.writeUTF("gamemessage:" + name + "," + msg);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
    }

    public void addMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView newText = new TextView(getApplicationContext());
                newText.setText(msg);
                newText.setTextColor(Color.BLACK);
                newText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                tv.addView(newText);
                /*String newStr = tv.getText() + "\n" + msg;
                tv.setText(newStr);*/
            }
        });
    }

    public void addPhoto() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                addPhotoToChat(bitmap);
            }
        });
    }

    public void addPhoto(final byte[] imgBytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                addPhotoToChat(bitmap);
            }
        });
    }

    private void addPhotoToChat(Bitmap bitmap) {
        ImageView iv = new ImageView(getApplicationContext());
        iv.setImageBitmap(bitmap);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(800, 800);
        iv.setLayoutParams(layoutParams);
        tv.addView(iv);
    }

    public void takePic(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.google.android.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "Failed to use camera", Toast.LENGTH_LONG).show();
        }
        //TODO: move to call of this func
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File image = new File(currentPhotoPath);
        System.out.println("pic end with size " + image.length());
        if(image.length() == 0){
            image.delete();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            sendPhoto();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void sendPhoto() {
/*        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
*/
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        final byte[] imgBytes = stream.toByteArray();
        /*for (int i = 0; i < imgBytes.length; i++) {
            imgBytes[i] = (byte)(i%255);
        }*/

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String msg = imgBytes.length + "";
                    Log.addMessage("sent: " + "gamephoto:" + name + "," + msg);
                    Log.addMessage("sent image file: " + currentPhotoPath + " of size " + imgBytes.length);
                    out.writeUTF("gamephoto:" + name + "," + msg);
                    out.flush();
                    out.write(imgBytes);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Failed to send image" + e.getMessage());
                }
            }
        });
        t.start();


        /*ImageView imgTest = (ImageView) findViewById(R.id.serverConnectivity3);
        imgTest.setImageBitmap(bitmap);*/
        addPhoto();
    }

    //Expects gamephoto:<size>
    public void handlePhoto(String input, Socket socket) {
        try {
            Log.addMessage("received: " + input);
            String[] split = input.split(",");
            int size = Integer.parseInt(split[0]);
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            //InputStream in = socket.getInputStream();
            byte[] imgBytes = new byte[size];
            int read = 0, totalGot = 0;
            while (read > -1 && totalGot < size) {
                read = in.read(imgBytes, totalGot, size - totalGot);
                if (read > -1)
                    totalGot += read;
                System.out.println("read " + read + " totalGot " + totalGot);
            }

            Log.addMessage("Received image file of size " +totalGot);
            System.out.println("Expected " + size + " bytes, and got " + totalGot + " bytes");
            addPhoto(imgBytes);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to read image from server" + e.getMessage());

        }
    }
}
