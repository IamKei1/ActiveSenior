package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrent, etNew, etConfirm;
    private Button btnChangePassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etCurrent = findViewById(R.id.etCurrentPassword);
        etNew = findViewById(R.id.etNewPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(v -> changePassword());
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }

    private void changePassword() {
        String cur = etCurrent.getText().toString();
        String nw = etNew.getText().toString();
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
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "인증된 사용자가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), cur);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid ->
                        user.updatePassword(nw)
                                .addOnSuccessListener(a -> {
                                    updatePasswordInDatabase(user.getUid(), nw);
                                    Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "현재 비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show());
    }

    private void updatePasswordInDatabase(String uid, String newPassword) {
        db.collection("users").document(uid)
                .update("password", newPassword)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                    finish();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "DB 비밀번호 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
