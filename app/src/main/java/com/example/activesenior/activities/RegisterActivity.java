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

import java.text.SimpleDateFormat;
import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, birthYearEditText, birthMonthEditText, birthDayEditText;
    private EditText emailEditText, passwordEditText, passwordConfirmEditText, phoneEditText;
    private RadioGroup genderRadioGroup, roleRadioGroup;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 연결
        nameEditText = findViewById(R.id.nameEditText);
        birthYearEditText = findViewById(R.id.birthYearEditText);
        birthMonthEditText = findViewById(R.id.birthMonthEditText);
        birthDayEditText = findViewById(R.id.birthDayEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordConfirmEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        genderRadioGroup = findViewById(R.id.radioGroup);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> createUser());
    }

    private void createUser() {
        String name = nameEditText.getText().toString().trim();
        String year = birthYearEditText.getText().toString().trim();
        String month = birthMonthEditText.getText().toString().trim();
        String day = birthDayEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordConfirm = passwordConfirmEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // 성별
        String gender;
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rg_btn1) {
            gender = "남";
        } else if (selectedGenderId == R.id.rg_btn2) {
            gender = "여";
        } else {
            Toast.makeText(this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 역할
        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        String role = selectedRoleButton != null ? selectedRoleButton.getText().toString() : "";

        // 생년월일 유효성 확인
        if (TextUtils.isEmpty(year) || TextUtils.isEmpty(month) || TextUtils.isEmpty(day)) {
            Toast.makeText(this, "생년월일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String birthDate = String.format("%s%02d%02d", year, Integer.parseInt(month), Integer.parseInt(day));

        Calendar birthCal = Calendar.getInstance();
        try {
            birthCal.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
        } catch (Exception e) {
            Toast.makeText(this, "생년월일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 필수 항목 유효성 검사
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(passwordConfirm) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(role)) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = calculateAge(birthCal);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String registerDay = simpleDateFormat.format(calendar.getTime());


        // Firebase Auth 회원가입
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", uid);
                            user.put("name", name);
                            user.put("gender", gender);
                            user.put("birthDate", birthDate);
                            user.put("age", age);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("role", role);
                            user.put("password", password);
                            user.put("isAvailable", false);
                            user.put("registerDate", registerDay);


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

    private int calculateAge(Calendar birthCal) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}
