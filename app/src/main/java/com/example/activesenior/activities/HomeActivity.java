package com.example.activesenior.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;

import android.os.Vibrator;
import android.os.VibratorManager;
import android.os.VibrationEffect;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView welcomeTextView, temperatureTextView, statusTextView;
    private Button findPersonButton;
    private Button aiMentorButton, manualButton, customerServiceButton;
    private Button openChatButton;

    private Switch userToggleSwitch;
    private Handler handler = new Handler();
    private Toast currentToast;
    private long backPressedTime = 0;
    private Toast backToast;

    private LinearLayout infoBoxLayout;
    private ValueAnimator gradientAnimator;

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        statusTextView = findViewById(R.id.statusTextView);

        findPersonButton = findViewById(R.id.findPersonButton);
        aiMentorButton = findViewById(R.id.aiMentorButton);
        openChatButton = findViewById(R.id.openChatButton);
        manualButton = findViewById(R.id.manualButton);
        customerServiceButton = findViewById(R.id.customerServiceButton);




        userToggleSwitch = findViewById(R.id.userToggleSwitch);
        infoBoxLayout = findViewById(R.id.infoBoxLayout);
        infoBoxLayout.setBackgroundResource(R.drawable.rounded_blue_box);

        manualButton.setOnClickListener(v -> startActivity(new Intent(this, ManualActivity.class)));
        findPersonButton.setOnClickListener(v -> startActivity(new Intent(this, FindPersonActivity.class)));
        openChatButton.setOnClickListener(v -> startActivity(new Intent(this, ChatRoomActivity.class)));
        aiMentorButton.setOnClickListener(v -> startActivity(new Intent(this, AiChatRoomActivity.class)));
        customerServiceButton.setOnClickListener(v -> startActivity(new Intent(this, CustomerServiceActivity.class)));



        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> {
                        Log.d("FCM_CHECK", "현재 기기 토큰: " + token);
                        db.collection("users").document(uid)
                                .update("fcmToken", token)
                                .addOnSuccessListener(unused -> {
                                    db.collection("users").document(uid).get()
                                            .addOnSuccessListener(doc -> {
                                                if (!doc.exists()) return;

                                                String name = doc.getString("name");
                                                String role = doc.getString("role");
                                                Boolean isAvailable = doc.getBoolean("isAvailable");
                                                String temp = "36.5℃";

                                                welcomeTextView.setText(name + "님 환영합니다");
                                                temperatureTextView.setText("나의 온도 : " + temp);

                                                if (role != null) {
                                                    findPersonButton.setText(role.equals("멘토") ? "멘티 찾기" : "멘토 찾기");
                                                    initializeUserToggle(role, isAvailable, uid);
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

    private void initializeUserToggle(String role, Boolean isAvailable, String uid) {
        String label = role.equals("멘토") ? "멘토" : "멘티";
        boolean active = Boolean.TRUE.equals(isAvailable);
        userToggleSwitch.setChecked(active);
        userToggleSwitch.setText(label + " 활동 " + (active ? "ON" : "OFF"));
        statusTextView.setText("현재 " + label + " 활동\n [" + (active ? "진행중" : "대기중") + "]");

        if (active) startGradientAnimation();
        else stopGradientAnimation();

        userToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users").document(uid)
                    .update("isAvailable", isChecked)
                    .addOnSuccessListener(unused -> {
                        userToggleSwitch.setText(label + " 활동 " + (isChecked ? "ON" : "OFF"));
                        statusTextView.setText("현재 " + label + " 활동\n [" + (isChecked ? "진행중" : "대기중") + "]");

                        if (isChecked) startGradientAnimation();
                        else stopGradientAnimation();

                        if (currentToast != null) currentToast.cancel();
                        String message = isChecked ? label + " 활동을 시작합니다" : label + " 활동을 종료합니다";
                        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                        currentToast.show();

                        vibrate();

                        handler.postDelayed(() -> {
                            if (currentToast != null) currentToast.cancel();
                        }, 3000);
                    });
        });
    }

    private void startGradientAnimation() {
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.rounded_gradient_box).mutate();
        infoBoxLayout.setBackground(drawable);

        int colorStart = ContextCompat.getColor(this, R.color.light_blue);
        int colorEnd = ContextCompat.getColor(this, R.color.purple_200);

        gradientAnimator = ValueAnimator.ofFloat(0, 1);
        gradientAnimator.setDuration(1500);
        gradientAnimator.setRepeatCount(ValueAnimator.INFINITE);
        gradientAnimator.setRepeatMode(ValueAnimator.REVERSE);

        gradientAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int blended = (Integer) new ArgbEvaluator().evaluate(fraction, colorStart, colorEnd);
            drawable.setColors(new int[]{blended, colorStart});
        });

        gradientAnimator.start();
    }

    private void stopGradientAnimation() {
        if (gradientAnimator != null && gradientAnimator.isRunning()) {
            gradientAnimator.cancel();
        }
        infoBoxLayout.setBackgroundResource(R.drawable.rounded_blue_box);
    }

    private void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
            Vibrator vibrator = vibratorManager.getDefaultVibrator();
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(200);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            if (backToast != null) backToast.cancel(); // 토스트 제거
            super.onBackPressed(); // 앱 종료
        } else {
            backPressedTime = System.currentTimeMillis();
            backToast = Toast.makeText(this, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT);
            backToast.show();
        }
    }
}
