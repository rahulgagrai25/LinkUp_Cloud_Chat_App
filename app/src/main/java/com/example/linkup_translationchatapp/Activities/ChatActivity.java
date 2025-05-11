package com.example.linkup_translationchatapp.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.linkup_translationchatapp.Models.MessageModel;
import com.example.linkup_translationchatapp.R;
import com.example.linkup_translationchatapp.Adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messageList;

    private String currentUserId, otherUserId, otherUserName;
    private DatabaseReference senderRoomRef;
    private DatabaseReference receiverRoomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Views
        userNameTextView = findViewById(R.id.userNameTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        // Initialize Data
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("uid");
        otherUserName = getIntent().getStringExtra("name");

        userNameTextView.setText("Chat with " + otherUserName);

        String senderRoom = currentUserId + "_" + otherUserId;
        String receiverRoom = otherUserId + "_" + currentUserId;

        senderRoomRef = FirebaseDatabase.getInstance().getReference("Chats").child(senderRoom);
        receiverRoomRef = FirebaseDatabase.getInstance().getReference("Chats").child(receiverRoom);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Send Message
        sendMessageButton.setOnClickListener(v -> sendMessage());

        // Fetch Messages
        fetchMessages();
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) return;

        long timestamp = System.currentTimeMillis();
        String messageId = senderRoomRef.push().getKey();

        MessageModel message = new MessageModel(currentUserId, messageText, timestamp);

        // Save message in sender and receiver rooms
        if (messageId != null) {
            senderRoomRef.child(messageId).setValue(message)
                    .addOnSuccessListener(unused -> receiverRoomRef.child(messageId).setValue(message));
        }

        messageEditText.setText("");
    }

    private void fetchMessages() {
        senderRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel message = dataSnapshot.getValue(MessageModel.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
