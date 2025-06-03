package com.example.activesenior.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.widget.Toast;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView welcomeTextView, temperatureTextView, statusTextView;
    private Button findMentorButton, findMenteeButton;
    private Button aiMentorButton, manualButton, customerServiceButton;
    private Button openChatButton;

    private Switch mentorToggleSwitch;
    private Handler handler = new Handler();
    private Toast currentToast;

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

        // 알림 권한 요청 (Android 13 이상 대상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1002
                );
            }
        }





        // Switch 연결
        mentorToggleSwitch = findViewById(R.id.mentorToggleSwitch);
        mentorToggleSwitch.setVisibility(View.GONE); // 기본 숨김



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
        openChatButton = findViewById(R.id.openChatButton);
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
            Intent intent = new Intent(HomeActivity.this, ManualActivity.class);
            startActivity(intent);
        });


        openChatButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ChatRoomActivity.class);
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

            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> {
                        Log.d("FCM_CHECK", "현재 기기 토큰: " + token);
                        // 🔐 FCM 토큰 저장
                        db.collection("users").document(uid)
                                .update("fcmToken", token)
                                .addOnSuccessListener(unused -> {
                                    Log.d("FCM", "🔐 로그인 후 토큰 저장 완료");

                                    // ✅ 사용자 정보 불러오기
                                    db.collection("users").document(uid).get()
                                            .addOnSuccessListener(doc -> {
                                                if (!doc.exists()) return;

                                                String name = doc.getString("name");
                                                String role = doc.getString("role");
                                                Boolean isAvailable = doc.getBoolean("isAvailable");
                                                String temp = "36.5℃";

                                                welcomeTextView.setText(name + "님 환영합니다");
                                                temperatureTextView.setText("나의 온도 : " + temp);

                                                switch (role) {
                                                    case "멘티":
                                                        findMentorButton.setVisibility(View.VISIBLE);
                                                        mentorToggleSwitch.setVisibility(View.GONE);
                                                        break;

                                                    case "멘토":
                                                        findMenteeButton.setVisibility(View.VISIBLE);
                                                        mentorToggleSwitch.setVisibility(View.VISIBLE);

                                                        if (Boolean.TRUE.equals(isAvailable)) {
                                                            mentorToggleSwitch.setChecked(true);
                                                            mentorToggleSwitch.setText("멘토 활동 ON");
                                                        }

                                                        mentorToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                                            db.collection("users").document(uid)
                                                                    .update("isAvailable", isChecked)
                                                                    .addOnSuccessListener(unused2 -> {
                                                                        mentorToggleSwitch.setText(role + " 활동 " + (isChecked ? "ON" : "OFF"));
                                                                        statusTextView.setText("현재 " + role + " 활동\n [" + (isChecked ? "진행중" : "대기중") + "]");

                                                                        if (isChecked) {
                                                                            if (currentToast != null) currentToast.cancel();
                                                                            currentToast = Toast.makeText(this, "멘토 활동을 시작합니다", Toast.LENGTH_SHORT);
                                                                            currentToast.show();
                                                                            handler.postDelayed(() -> {
                                                                                if (currentToast != null) currentToast.cancel();
                                                                            }, 3000);
                                                                        }
                                                                    });
                                                        });
                                                        break;
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                welcomeTextView.setText("정보 로딩 실패: " + e.getMessage());
                                                Log.e("Firestore", "사용자 정보 로드 실패", e);
                                            });
                                })
                                .addOnFailureListener(e -> Log.e("FCM", "❌ 토큰 저장 실패: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> Log.e("FCM", "❌ 토큰 가져오기 실패: " + e.getMessage()));
        }

    }
    // 🔔 알림 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "알림 권한 허용됨");
            } else {
                Log.w("Permission", "알림 권한 거부됨");
                Toast.makeText(this, "알림 권한이 거부되어 메시지 알림이 표시되지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
