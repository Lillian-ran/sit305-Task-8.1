package com.example.llama2chatbot;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private ChatApiService apiService;
    private String username = "User";  // Default username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // get username
        if (getIntent() != null && getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        }

        editTextMessage = findViewById(R.id.editTextMessage);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        Button buttonSend = findViewById(R.id.buttonSend);

        // LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);  // message showed from bottom
        recyclerViewChat.setLayoutManager(layoutManager);

        // initiate adapter and bound
        chatAdapter = new ChatAdapter(this, messageList); // context
        recyclerViewChat.setAdapter(chatAdapter);

        // Retrofit set
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiService = retrofit.create(ChatApiService.class);

        // welcome message
        addMessage("✨ Welcome, " + username + "!", false);

        buttonSend.setOnClickListener(v -> {
            String userMessage = editTextMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addMessage(userMessage, true);
                sendMessageToServer(userMessage);
                editTextMessage.setText("");
            }
        });
    }

    private void addMessage(String message, boolean isUser) {
        messageList.add(new ChatMessage(message, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessageToServer(String userMessage) {
        Call<String> call = apiService.sendMessage(userMessage);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addMessage(response.body().trim(), false);
                } else {
                    addMessage("Failed to get response. Try again.", false);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                addMessage("Network error: " + t.getMessage(), false);
            }
        });
    }
}
