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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.activesenior.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;
import java.util.List;

import android.os.Vibrator;
import android.os.VibratorManager;
import android.os.VibrationEffect;

import com.example.activesenior.dialogs.ConfirmDialog;
import com.example.activesenior.utils.NavigationHelper;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView welcomeTextView, pointTextView, statusTextView;
    private Button findPersonButton, openChatButton, approveButton;
    private Button aiMentorButton, manualButton, customerServiceButton;
    private ImageButton helpButton;


    private Switch userToggleSwitch;
    private Handler handler = new Handler();
    private Toast currentToast;
    private long backPressedTime = 0;
    private Toast backToast;

    private LinearLayout infoBoxLayout;
    private ValueAnimator gradientAnimator;

    private ShimmerFrameLayout homeShimmerLayout;
    private ScrollView homeScrollView;


    private String role;

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
        pointTextView = findViewById(R.id.pointTextView);
        statusTextView = findViewById(R.id.statusTextView);

        findPersonButton = findViewById(R.id.findPersonButton);
        aiMentorButton = findViewById(R.id.aiMentorButton);
        openChatButton = findViewById(R.id.openChatButton);
        manualButton = findViewById(R.id.manualButton);
        customerServiceButton = findViewById(R.id.customerServiceButton);
        approveButton = findViewById(R.id.approveButton);
        helpButton = findViewById(R.id.helpButton);

        homeShimmerLayout = findViewById(R.id.homeShimmerLayout);
        homeScrollView = findViewById(R.id.homeScrollView);

        approveButton.setOnClickListener(v -> {
            NavigationHelper.showConfirmEndActivity(this, role); // "멘토" 또는 "멘티"
        });

        helpButton.setOnClickListener(v -> {
            NavigationHelper.showUserToggleHelp(this, role);
        });


        userToggleSwitch = findViewById(R.id.userToggleSwitch);
        infoBoxLayout = findViewById(R.id.infoBoxLayout);
        infoBoxLayout.setBackgroundResource(R.drawable.rounded_blue_box);


        NavigationHelper.setupConfirmNavigation(this, manualButton, ManualActivity.class);
        NavigationHelper.setupConfirmNavigation(this, findPersonButton, FindPersonActivity.class);
        NavigationHelper.setupConfirmNavigation(this, openChatButton, ChatRoomActivity.class);
        NavigationHelper.setupConfirmNavigation(this, aiMentorButton, AiChatRoomActivity.class);
        NavigationHelper.setupConfirmNavigation(this, customerServiceButton, CustomerServiceActivity.class);

        homeShimmerLayout.startShimmer();
        homeShimmerLayout.setVisibility(View.VISIBLE);
        homeScrollView.setVisibility(View.GONE);



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
                                                role = doc.getString("role");
                                                Boolean isAvailable = doc.getBoolean("isAvailable");

                                                welcomeTextView.setText(name + "님 환영합니다");

                                                if ("멘토".equals(role)) {
                                                    Long pointLong = doc.getLong("point");
                                                    int point = pointLong != null ? pointLong.intValue() : 0;

                                                    // 🔹 badges 배열에서 첫 번째 값 가져오기
                                                    List<String> badges = (List<String>) doc.get("badges");
                                                    String badgeText = "";
                                                    if (badges != null && !badges.isEmpty()) {
                                                        badgeText = badges.get(0) + " "; // 🌱 새싹멘토
                                                    }

                                                    // UI 반영
                                                    pointTextView.setVisibility(View.VISIBLE);
                                                    pointTextView.setText("나의 포인트 " + point + "P");
                                                    openChatButton.setText("💬 멘티와 대화하기");

                                                    // ✅ 뱃지를 이름 앞에 붙이기
                                                    welcomeTextView.setText(badgeText + name + "님\n환영합니다");
                                                } else {
                                                    pointTextView.setVisibility(View.GONE);
                                                    approveButton.setText("✔️ 멘토의 도움 받았어요");

                                                    // 멘토가 아닐 경우, 뱃지 없이 기본 이름만 출력
                                                    welcomeTextView.setText(name + "님 환영합니다");
                                                }

                                                if (role != null) {
                                                    findPersonButton.setText(role.equals("멘토") ? "\uD83D\uDD0D 멘티 찾기" : "\uD83D\uDD0D 멘토 찾기");
                                                    initializeUserToggle(role, isAvailable, uid);
                                                }
                                                homeShimmerLayout.stopShimmer();
                                                homeShimmerLayout.setVisibility(View.GONE);
                                                homeScrollView.setVisibility(View.VISIBLE);
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
        String target = role.equals("멘토") ? "멘티" : "멘토"; // 내가 공개하는 대상
        boolean active = Boolean.TRUE.equals(isAvailable);

        // ✅ 1. 초기 UI 적용 (리스너 등록 전에 먼저)
        userToggleSwitch.setChecked(active);
        userToggleSwitch.setText(target + (active ? "가 나를 찾을 수 있어요" : "가 나를 찾을 수 없어요"));
        statusTextView.setText("현재 상태: " + (active ? "활동 중" : "대기중"));
        if (active) startGradientAnimation();
        else stopGradientAnimation();

        // ✅ 2. 리스너 등록
        userToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users").document(uid)
                    .update("isAvailable", isChecked)
                    .addOnSuccessListener(unused -> {
                        // UI 업데이트
                        userToggleSwitch.setText(target + (isChecked ? "가 나를 찾을 수 있어요" : "가 나를 찾을 수 없어요"));
                        statusTextView.setText("현재 상태: " + (isChecked ? "활동 중" : "대기중"));
                        if (isChecked) startGradientAnimation();
                        else stopGradientAnimation();

                        // 알림 메시지
                        if (currentToast != null) currentToast.cancel();
                        String message = isChecked ? "검색이 가능해졌습니다" : "검색이 중단되었습니다";
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
