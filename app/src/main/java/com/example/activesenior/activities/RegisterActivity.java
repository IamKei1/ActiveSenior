package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText, emailEditText, passwordEditText, phoneEditText;
    private RadioGroup genderRadioGroup, roleRadioGroup;
    private RadioButton genderMaleRadio, genderFemaleRadio;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 요소 연결
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        genderRadioGroup = findViewById(R.id.radioGroup);
        genderMaleRadio = findViewById(R.id.rg_btn1);
        genderFemaleRadio = findViewById(R.id.rg_btn2);

        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> createUser());
    }

    private void createUser() {
        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // 성별 선택 처리
        String gender = "";
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rg_btn1) {
            gender = "남";
        } else if (selectedGenderId == R.id.rg_btn2) {
            gender = "여";
        }

        final String selectedGender = gender;

        // 역할 선택 처리
        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        String role = selectedRoleButton != null ? selectedRoleButton.getText().toString() : "";

        // 유효성 검사
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(role)) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Auth 회원가입
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Firestore에 사용자 정보 저장
                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", uid);
                            user.put("name", name);
                            user.put("gender", selectedGender);
                            user.put("age", age);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("role", role);

                            db.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "데이터 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e("RegisterActivity", "회원가입 실패", task.getException());
                        Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
