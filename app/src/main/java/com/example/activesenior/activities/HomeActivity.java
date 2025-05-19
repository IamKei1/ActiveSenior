package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private Button viewMentorListButton;
    private Button logoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);  // 예쁜 레이아웃 연결

        // Firebase Auth 초기화
        mAuth = FirebaseAuth.getInstance();

        // 버튼 연결
        viewMentorListButton = findViewById(R.id.viewMentorListButton);
        logoutButton = findViewById(R.id.logoutButton);

        // 멘토 리스트 보기 버튼 클릭 이벤트
        viewMentorListButton.setOnClickListener(v -> {
            // 나중에 MentorListActivity가 생기면 연결
            Toast.makeText(this, "멘토 리스트로 이동할 예정이에요!", Toast.LENGTH_SHORT).show();
            // 예시: startActivity(new Intent(HomeActivity.this, MentorListActivity.class));
        });

        // 로그아웃 버튼 클릭 이벤트
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut(); // Firebase에서 로그아웃
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, MainActivity.class)); // 로그인 화면으로 이동
            finish(); // 현재 화면 종료
        });
    }
}
