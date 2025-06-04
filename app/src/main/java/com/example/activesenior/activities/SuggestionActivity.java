package com.example.activesenior.activities;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SuggestionActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSendSuggestion;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etSuggestionTitle);
        etContent = findViewById(R.id.etSuggestionContent);
        btnSendSuggestion = findViewById(R.id.btnSendSuggestion);

        btnSendSuggestion.setOnClickListener(v -> sendSuggestion());
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }

    private void sendSuggestion() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("title", title);
        suggestion.put("content", content);
        suggestion.put("timestamp", FieldValue.serverTimestamp());

        db.collection("suggestions")
                .add(suggestion)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "건의사항이 전송되었습니다", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etContent.setText("");
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "전송 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

