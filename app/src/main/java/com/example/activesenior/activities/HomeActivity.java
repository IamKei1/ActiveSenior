package com.example.activesenior.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView welcomeTextView, temperatureTextView, statusTextView;
    private Button findMentorButton, findMenteeButton;
    private Button aiMentorButton, manualButton, customerServiceButton;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            Signature[] sigs = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.getApkContentsSigners();
            for (Signature sig : sigs) {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(sig.toByteArray());
                String sha1 = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("APP_SHA1", "Current Installed SHA-1: " + sha1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        manualButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ManualActivity.class);
            startActivity(intent);
        });

        // 멘토찾기
        findMentorButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FindMentorActivity.class);
            startActivity(intent);
        });


        manualButton.setOnClickListener(v -> {
            Log.d("HomeActivity", "Manual button clicked");
            Intent intent = new Intent(HomeActivity.this, ManualActivity.class);
            startActivity(intent);
        });



        // AI멘토에게 물어보기
        aiMentorButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AiMentorActivity.class);
            startActivity(intent);
        });

        customerServiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CustomerServiceActivity.class);
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
                                findMenteeButton.setVisibility(View.VISIBLE);
                            } else if ("멘티".equals(role)) {
                                findMentorButton.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        welcomeTextView.setText("정보 로딩 실패: " + e.getMessage());
                    });
        }
    }
}
