package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView welcomeTextView, temperatureTextView, statusTextView;
    private Button findMentorButton, findMenteeButton;
    private Button aiMentorButton, manualButton, customerServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase 인스턴스
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 연결
        welcomeTextView = findViewById(R.id.welcomeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        statusTextView = findViewById(R.id.statusTextView);

        findMentorButton = findViewById(R.id.findMentorButton);
        findMenteeButton = findViewById(R.id.findMenteeButton);
        aiMentorButton = findViewById(R.id.aiMentorButton);
        manualButton = findViewById(R.id.manualButton);
        customerServiceButton = findViewById(R.id.customerServiceButton);

        // 버튼 초기 숨김
        findMentorButton.setVisibility(View.GONE);
        findMenteeButton.setVisibility(View.GONE);

        aiMentorButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AiMentorActivity.class);
            startActivity(intent);
        });

        // 사용자 정보 로드
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            String role = doc.getString("role");
                            String temp = "36.5℃"; // 추후 로직 반영 가능
                            String status = "대기중"; // 추후 로직 반영 가능

                            welcomeTextView.setText(name + "님 환영합니다");
                            temperatureTextView.setText("나의 온도 : " + temp);
                            statusTextView.setText(role + " 활동 " + status);

                            // 역할에 따라 버튼 표시
                            if ("멘토".equals(role)) {
                                findMentorButton.setVisibility(View.VISIBLE);
                            } else if ("멘티".equals(role)) {
                                findMenteeButton.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        welcomeTextView.setText("정보 로딩 실패: " + e.getMessage());
                    });
        }
    }
}
