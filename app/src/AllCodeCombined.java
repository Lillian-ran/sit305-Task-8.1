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
        addMessage("鉁?Welcome, " + username + "!", false);

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
package com.example.llama2chatbot;

import android.content.Context;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> messages;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        public ViewHolder(View view) {
            super(view);
            textViewMessage = view.findViewById(R.id.textViewMessage);
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMessage = messages.get(position);
        holder.textViewMessage.setText(chatMessage.message);

        LinearLayout messageContainer = (LinearLayout) holder.itemView;

        if (chatMessage.isUser) {
            messageContainer.setGravity(Gravity.END); // User messages are aligned to the right
            holder.textViewMessage.setBackgroundResource(R.drawable.bubble);
        } else {
            messageContainer.setGravity(Gravity.START); // AI message to the left
            holder.textViewMessage.setBackgroundResource(R.drawable.bubble);
        }
    }



    @Override
    public int getItemCount() {
        return messages.size();
    }
}

package com.example.llama2chatbot;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ChatApiService {

    @FormUrlEncoded
    @POST("/chat")
    Call<String> sendMessage(@Field("userMessage") String userMessage);
}
package com.example.llama2chatbot;

public class ChatMessage {
    public String message;
    public boolean isUser;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }
}
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
