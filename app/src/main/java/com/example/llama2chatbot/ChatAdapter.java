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

