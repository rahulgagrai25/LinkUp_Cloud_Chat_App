package com.example.linkup_translationchatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Spinner;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.Translation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {


    private Spinner languageSpinner;
    private Button downloadLanguageButton;
    private Translator translator;
    private String selectedLanguageCode;



    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;

    private MessageAdapter messageAdapter;
    private List<MessageModel> messageList;

    private DatabaseReference chatRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        languageSpinner = findViewById(R.id.languageSpinner);
        downloadLanguageButton = findViewById(R.id.downloadLanguageButton);

        downloadLanguageButton.setOnClickListener(view -> {
            String selectedLanguage = languageSpinner.getSelectedItem().toString();
            selectedLanguageCode = getLanguageCode(selectedLanguage);
            downloadLanguageModel(selectedLanguageCode);
        });


        recyclerView = findViewById(R.id.recyclerview);
        messageInput = findViewById(R.id.typemessage);
        sendButton = findViewById(R.id.send);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerView.setAdapter(messageAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        loadMessages(); // Load chat messages from Firebase

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }


    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = currentUser.getUid();
        String messageId = chatRef.push().getKey();

        // Fetch recipient's preferred language
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(senderId);
        userRef.child("preferredLanguage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String targetLanguage = snapshot.getValue(String.class);
                    translateAndSendMessage(text, senderId, messageId, targetLanguage);
                } else {
                    sendRawMessage(text, senderId, messageId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                sendRawMessage(text, senderId, messageId);
            }
        });
    }

    private void translateAndSendMessage(String text, String senderId, String messageId, String targetLanguage) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLanguage)
                .build();

        Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    translator.translate(text)
                            .addOnSuccessListener(translatedText -> {
                                MessageModel message = new MessageModel(translatedText, senderId, System.currentTimeMillis());
                                if (messageId != null) {
                                    chatRef.child(messageId).setValue(message);
                                    messageInput.setText("");
                                }
                            })
                            .addOnFailureListener(e -> sendRawMessage(text, senderId, messageId));
                })
                .addOnFailureListener(e -> sendRawMessage(text, senderId, messageId));
    }


    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel message = dataSnapshot.getValue(MessageModel.class);
                    if (message != null) {
                        translateMessageForDisplay(message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void translateMessageForDisplay(MessageModel message) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userRef.child("preferredLanguage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userPreferredLanguage = snapshot.getValue(String.class);

                    TranslatorOptions options = new TranslatorOptions.Builder()
                            .setSourceLanguage(TranslateLanguage.ENGLISH)
                            .setTargetLanguage(userPreferredLanguage)
                            .build();

                    Translator translator = Translation.getClient(options);

                    translator.translate(message.getMessage())  // FIXED: Using getMessage()
                            .addOnSuccessListener(translatedText -> {
                                message.setMessage(translatedText);  // FIXED: Using setMessage()
                                messageList.add(message);
                                messageAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(messageList.size() - 1);
                            })
                            .addOnFailureListener(e -> {
                                messageList.add(message);
                                messageAdapter.notifyDataSetChanged();
                            });
                } else {
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    // If translation fails, send the original message
    private void sendRawMessage(String text, String senderId, String messageId) {
        MessageModel message = new MessageModel(text, senderId, System.currentTimeMillis());
        if (messageId != null) {
            chatRef.child(messageId).setValue(message);
            messageInput.setText("");
        }
    }


    private String getLanguageCode(String language) {
        switch (language) {
            case "English": return TranslateLanguage.ENGLISH;
            case "Spanish": return TranslateLanguage.SPANISH;
            case "French": return TranslateLanguage.FRENCH;
            case "German": return TranslateLanguage.GERMAN;
            case "Hindi": return TranslateLanguage.HINDI;
            case "Chinese": return TranslateLanguage.CHINESE;
            default: return TranslateLanguage.ENGLISH;
        }
    }

    private void downloadLanguageModel(String langCode) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(langCode)
                .build();

        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Language downloaded!", Toast.LENGTH_SHORT).show();
                    savePreferredLanguage(langCode);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Download failed!", Toast.LENGTH_SHORT).show());
    }


    private void savePreferredLanguage(String langCode) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid());

        userRef.child("preferredLanguage").setValue(langCode);
    }


}
