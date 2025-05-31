package com.example.activesenior.activities;


// 안드로이드 UI 관련 클래스 import
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.text.TextUtils;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

// Firebase 인증 관련 클래스 import
import com.example.activesenior.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // 이메일, 비밀번호 입력창과 로그인 버튼 선언
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;  // 회원가입 버튼 추가

    // Firebase 인증 인스턴스
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // activity_main.xml과 연결

        // Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // XML에 있는 EditText와 Button을 Java 코드와 연결
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);  // 회원가입 버튼 연결

        // 로그인 버튼 클릭 이벤트 설정
        loginButton.setOnClickListener(v -> loginUser());

        // 회원가입 버튼 클릭 이벤트 설정
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class); // RegisterActivity로 이동
            startActivity(intent);
        });
    }

    // 로그인 기능 구현 함수
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // 이메일이나 비밀번호가 비었는지 확인
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase 로그인 시도
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공 시 메시지 출력 및 다음 화면으로 이동 (임시로 MainActivity 재실행)
                        Toast.makeText(MainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class)); // 로그인 후 HomeActivity로 이동
                        finish(); // 현재 화면 종료
                    } else {
                        // 로그인 실패 시 메시지 출력
                        String errorMsg = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "로그인 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("LoginError", errorMsg);
                    }
                });
    }
}

