package com.example.activesenior.activities;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;

public class ManualActivity extends AppCompatActivity {

    private TextView bubbleTextView;
    private ImageButton closeBubbleButton;
    private View dimOverlay;
    private View bubbleLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        // 말풍선 구성요소 찾기
        bubbleTextView = findViewById(R.id.bubbleTextView);
        closeBubbleButton = findViewById(R.id.closeBubbleButton);
        dimOverlay = findViewById(R.id.dimOverlay);
        bubbleLayout = findViewById(R.id.bubbleLayout);

        // 버튼 연결
        Button mentorBtn = findViewById(R.id.findMentorButton_manual);
        Button aiMentorBtn = findViewById(R.id.aiMentorButton_manual);
        Button manualBtn = findViewById(R.id.manualButton_manual);
        Button csBtn = findViewById(R.id.customerServiceButton_manual);
        Button ahBtn = findViewById(R.id.approveHelpButton_manual);
        Button ocbtn = findViewById(R.id.openChatButton_manual);

        dimOverlay.setVisibility(View.VISIBLE);

        // X 버튼 누르면 모두 닫힘
        closeBubbleButton.setOnClickListener(v -> hideBubble());

        // 버튼 클릭 시 설명창 표시
        mentorBtn.setOnClickListener(v -> showBubble("‘멘토/멘티 찾기’ 버튼은\n 나에게 맞는 멘토를 찾을 수 있습니다."));
        aiMentorBtn.setOnClickListener(v -> showBubble("‘AI 멘토’ 버튼은\nAI 멘토에게 질문하여 조언을 받을 수 있습니다."));
        manualBtn.setOnClickListener(v -> showBubble("사용자 매뉴얼입니다. 버튼 설명을 볼 수 있습니다."));
        csBtn.setOnClickListener(v -> showBubble("포인트 샵 | 건의사항 | 비밀번호 변경 | 로그아웃 | 회원탈퇴를 이용할 수 있습니다."));
        ahBtn.setOnClickListener(v -> showBubble("멘토-멘티와 매칭후  활동이 끝났을 때 활동종료 하는 버튼입니다."));
        ocbtn.setOnClickListener(v -> showBubble("멘토-멘티와 대화를 서로 주고받는 버튼입니다."));

    }

    private void showBubble(String message) {
        bubbleTextView.setText(message);
        dimOverlay.setVisibility(View.VISIBLE);
        bubbleLayout.setVisibility(View.VISIBLE);
        closeBubbleButton.setVisibility(View.VISIBLE);
    }

    private void hideBubble() {
        dimOverlay.setVisibility(View.GONE);
        bubbleLayout.setVisibility(View.GONE);
        closeBubbleButton.setVisibility(View.GONE);
    }
}
