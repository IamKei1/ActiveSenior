package com.example.activesenior.activities;

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

public class CustomerServiceActivity extends AppCompatActivity {
    private EditText etCurrent, etNew, etConfirm;
    private Button btnChangePassword, btnDelete, btnSendSuggestion;
    private EditText etTitle, etContent;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 비밀번호 변경 초기화
        etCurrent   = findViewById(R.id.etCurrentPassword);
        etNew       = findViewById(R.id.etNewPassword);
        etConfirm   = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(v -> changePassword());

        // 회원탈퇴 초기화
        btnDelete = findViewById(R.id.btnDeleteAccount);
        btnDelete.setOnClickListener(v -> confirmAndDelete());

        // 건의사항 초기화
        etTitle   = findViewById(R.id.etSuggestionTitle);
        etContent = findViewById(R.id.etSuggestionContent);
        btnSendSuggestion = findViewById(R.id.btnSendSuggestion);
        btnSendSuggestion.setOnClickListener(v -> sendSuggestion());
    }

    private void changePassword() {
        String cur  = etCurrent.getText().toString();
        String nw   = etNew.getText().toString();
        String conf = etConfirm.getText().toString();

        if (TextUtils.isEmpty(cur) || TextUtils.isEmpty(nw) || TextUtils.isEmpty(conf)) {
            Toast.makeText(this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nw.equals(conf)) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), cur);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid ->
                        user.updatePassword(nw)
                                .addOnSuccessListener(a -> Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "변경 실패: "+e.getMessage(), Toast.LENGTH_SHORT).show())
                )
                .addOnFailureListener(e -> Toast.makeText(this, "현재 비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show());
    }

    private void confirmAndDelete() {
        new AlertDialog.Builder(this)
                .setTitle("회원탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> deleteAccount())
                .setNegativeButton("아니요", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        // Firestore 문서 삭제
        db.collection("users").document(uid).delete();
        // Auth 계정 삭제
        user.delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "탈퇴 완료", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "탈퇴 실패: "+e.getMessage(), Toast.LENGTH_SHORT).show());
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
                })
                .addOnFailureListener(e -> Toast.makeText(this, "전송 실패: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
