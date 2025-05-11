package com.example.linkup_translationchatapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.linkup_translationchatapp.Activities.LoginActivity;
import com.example.linkup_translationchatapp.Models.UserModel;
import com.example.linkup_translationchatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {

    EditText nameEdit;
    TextView emailText;
    Button updateButton, logoutButton, deleteButton;

    FirebaseAuth auth;
    FirebaseUser currentUser;
    DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameEdit = view.findViewById(R.id.nameEdit);
        emailText = view.findViewById(R.id.emailText);
        updateButton = view.findViewById(R.id.updateButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

            userRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    UserModel user = snapshot.getValue(UserModel.class);
                    nameEdit.setText(user.getName());
                    emailText.setText(user.getEmail());
                }
            });
        }

        updateButton.setOnClickListener(v -> {
            String newName = nameEdit.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                userRef.child("name").setValue(newName).addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Name updated", Toast.LENGTH_SHORT).show()
                );
            }
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        deleteButton.setOnClickListener(v -> {
            userRef.removeValue();
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }
}
