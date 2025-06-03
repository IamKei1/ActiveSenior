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
        Button menteeBtn = findViewById(R.id.findMenteeButton_manual);
        Button aiMentorBtn = findViewById(R.id.aiMentorButton_manual);
        Button manualBtn = findViewById(R.id.manualButton_manual);
        Button csBtn = findViewById(R.id.customerServiceButton_manual);

        dimOverlay.setVisibility(View.VISIBLE);

        // X 버튼 누르면 모두 닫힘
        closeBubbleButton.setOnClickListener(v -> hideBubble());

        // 버튼 클릭 시 설명창 표시
        mentorBtn.setOnClickListener(v -> showBubble("‘멘토 찾기’ 버튼은\n 나에게 맞는 멘토를 찾을 수 있습니다."));
        menteeBtn.setOnClickListener(v -> showBubble("‘멘티 찾기’ 버튼은\n 도움을 필요로 하는 멘티를 찾는 기능입니다."));
        aiMentorBtn.setOnClickListener(v -> showBubble("‘AI 멘토’ 버튼은\nAI 멘토에게 질문하여 조언을 받을 수 있습니다."));
        manualBtn.setOnClickListener(v -> showBubble("사용자 매뉴얼입니다. 버튼 설명을 볼 수 있습니다."));
        csBtn.setOnClickListener(v -> showBubble("회원정보 수정\n회원탈퇴\n건의사항을 이용할 수 있습니다."));
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
