package com.example.activesenior.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView welcomeTextView;
    private TextView temperatureTextView;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase 인스턴스
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 레이아웃 연결
        welcomeTextView = findViewById(R.id.welcomeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        statusTextView = findViewById(R.id.statusTextView);

        // 사용자 정보 가져오기
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String role = documentSnapshot.getString("role");
                            String temp = "36.5℃"; // 추후 기능화 가능
                            String status = "대기중"; // 임시값

                            welcomeTextView.setText(name + "님 환영합니다");
                            temperatureTextView.setText("나의 온도 : " + temp);
                            statusTextView.setText("현재 멘토/멘티 활동\n" + status);
                        } else {
                            welcomeTextView.setText("사용자 정보를 찾을 수 없습니다.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        welcomeTextView.setText("정보 로딩 실패: " + e.getMessage());
                    });
        } else {
            welcomeTextView.setText("로그인 사용자 없음");
        }
    }
}
