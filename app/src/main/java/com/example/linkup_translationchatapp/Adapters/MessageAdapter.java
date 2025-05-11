package com.example.linkup_translationchatapp.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup_translationchatapp.Models.MessageModel;
import com.example.linkup_translationchatapp.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<MessageModel> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<MessageModel> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        String formattedTime = DateFormat.format("hh:mm a", message.getTimestamp()).toString();

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).messageTextView.setText(message.getMessageText());
            ((SentViewHolder) holder).timestampTextView.setText(formattedTime);
        } else {
            ((ReceivedViewHolder) holder).messageTextView.setText(message.getMessageText());
            ((ReceivedViewHolder) holder).timestampTextView.setText(formattedTime);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
