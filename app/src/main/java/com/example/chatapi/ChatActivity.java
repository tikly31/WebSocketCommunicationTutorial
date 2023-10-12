package com.example.chatapi;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
//import java.sql.SQLOutput;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity implements TextWatcher {

    private String name;
    private WebSocket webSocket;
    String SERVER_PATH = "ws://192.168.0.124:3000";
    private EditText messageEdit;
    private View sendBtn,pickImgBtn;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

    private ActivityResultLauncher<Intent> imagePickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    try {
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                Uri imageUri = item.getUri();

                                try {
                                    InputStream is = getContentResolver().openInputStream(imageUri);
                                    if (is != null) {
                                        Bitmap image = BitmapFactory.decodeStream(is);
                                        sendImage(image);
                                    } else {
                                        System.out.println("not inputted image");
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    // Handle the exception
                                }
                            }
                        } else {
                            // Handle single image selection without ClipData
                            Uri imageUri = data.getData();
                            // Proceed with the image processing here
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        name= getIntent().getStringExtra("name").trim();
        initiateSocketConnection();

    }

    private void initiateSocketConnection(){
        OkHttpClient client=new OkHttpClient();
        Request request =new Request.Builder().url(SERVER_PATH).build();
        webSocket=client.newWebSocket(request,new SocketListener());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        String string = s.toString().trim();
        if(string.isEmpty()){
            resetMessageEdit();
        }else {
            sendBtn.setVisibility(View.VISIBLE);
            pickImgBtn.setVisibility(View.INVISIBLE);

        }
    }

    private void resetMessageEdit() {
        messageEdit.removeTextChangedListener(this);
        messageEdit.setText("");
        sendBtn.setVisibility(View.INVISIBLE);
        pickImgBtn.setVisibility(View.VISIBLE);

        messageEdit.addTextChangedListener(this);
    }


    private class SocketListener extends WebSocketListener{

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);
            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this,
                        "Socket Connection Successful",
                        Toast.LENGTH_SHORT).show();
                initializeView();
            });
        }

        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(() ->{
                try{
                    JSONObject jsonObject=new JSONObject(text);
                    jsonObject.put("isSent",false);

                    messageAdapter.addItem(jsonObject);

                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);

                }catch(JSONException e){
                    e.printStackTrace();
                }
            });
        }
    }

    private void initializeView(){
        messageEdit= findViewById(R.id.messageEdit);
        if (messageEdit != null) {
            messageEdit.addTextChangedListener(this);
        }
        sendBtn=findViewById(R.id.sendBtn);
        pickImgBtn=findViewById(R.id.pickImgBtn);

        recyclerView=findViewById(R.id.recyclerView);

        messageAdapter=new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageEdit.addTextChangedListener(this);

        sendBtn.setOnClickListener(v ->{
            JSONObject jsonObject=new JSONObject();
            try{
                jsonObject.put("name",name);
                jsonObject.put("message",messageEdit.getText().toString());

                webSocket.send(jsonObject.toString());

                jsonObject.put("isSent",true);
                messageAdapter.addItem(jsonObject);

                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

                resetMessageEdit();
            }catch(JSONException e){
                e.printStackTrace();
            }
        });

        pickImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            // Use the ActivityResultLauncher to start the image picker activity
            imagePickerLauncher.launch(intent);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int IMAGE_REQUEST_ID = 1;
        if (requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK) {
            if (data != null) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri imageUri = item.getUri();

                        try {
                            InputStream is = getContentResolver().openInputStream(imageUri);
                            if (is != null) {
                                Bitmap image = BitmapFactory.decodeStream(is);
                                sendImage(image);
                            } else {
                                System.out.println("Other");
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            // Handle the exception
                        }
                    }
                } else {
                    // Handle single image selection without ClipData
                    Uri imageUri = data.getData();
                    // Proceed with the image processing here
                }
            }
        }
    }

    private void sendImage(Bitmap image) {
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
        String base64String= Base64.encodeToString(outputStream.toByteArray(),
                Base64.DEFAULT);

        JSONObject jsonObject=new JSONObject();

        try{
            jsonObject.put("name",name);
            jsonObject.put("image",base64String);

            webSocket.send(jsonObject.toString());

            jsonObject.put("isSent",true);

            messageAdapter.addItem(jsonObject);

        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}