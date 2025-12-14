package com.uj.echocrisismain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userText.setText(message.getText());
        } else {
            ((BotViewHolder) holder).botText.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userText;
        UserViewHolder(View itemView) {
            super(itemView);
            userText = itemView.findViewById(R.id.userText);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botText;
        BotViewHolder(View itemView) {
            super(itemView);
            botText = itemView.findViewById(R.id.botText);
        }
    }
}
