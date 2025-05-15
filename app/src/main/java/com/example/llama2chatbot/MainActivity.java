package com.example.llama2chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username);
        goButton = findViewById(R.id.goButton);

        goButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(MainActivity.this, "Please input username.", Toast.LENGTH_SHORT).show();
            } else {
                // Login successfully
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            }
        });
    }
}
