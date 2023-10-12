package com.example.chatapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check and request STORAGE permissions if needed for media access
        requestStoragePermission();

        EditText editText = findViewById(R.id.editText);

        findViewById(R.id.enterBtn).setOnClickListener(v -> {
            String name = editText.getText().toString().trim(); // Trim any leading/trailing spaces

            if (!name.isEmpty()) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        }
    }
}
