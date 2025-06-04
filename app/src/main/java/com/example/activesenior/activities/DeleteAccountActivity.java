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

public class DeleteAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etDeleteEmail);
        etPassword = findViewById(R.id.etDeletePassword);

        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> confirmAndDelete());
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }

    private void confirmAndDelete() {
        new AlertDialog.Builder(this)
                .setTitle("회원탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?\n이메일과 비밀번호를 확인합니다.")
                .setPositiveButton("예", (dialog, which) -> verifyAndDelete())
                .setNegativeButton("아니요", null)
                .show();
    }

    private void verifyAndDelete() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(authResult -> {
                    // 인증 성공 → 삭제 진행
                    deleteAccount(user.getUid());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "인증 실패: 이메일 또는 비밀번호가 올바르지 않습니다", Toast.LENGTH_SHORT).show());
    }

    private void deleteAccount(String uid) {
        db.collection("users").document(uid).delete(); // DB 문서 삭제

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        user.delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "탈퇴 완료", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "계정 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

