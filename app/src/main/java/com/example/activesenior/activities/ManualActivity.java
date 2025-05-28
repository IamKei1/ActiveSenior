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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        bubbleTextView = findViewById(R.id.bubbleTextView);
        closeBubbleButton = findViewById(R.id.closeBubbleButton);

        Button mentorBtn = findViewById(R.id.findMentorButton_manual);
        Button menteeBtn = findViewById(R.id.findMenteeButton_manual);
        Button aiMentorBtn = findViewById(R.id.aiMentorButton_manual);
        Button manualBtn = findViewById(R.id.manualButton_manual);
        Button csBtn = findViewById(R.id.customerServiceButton_manual);

        closeBubbleButton.setOnClickListener(v -> {
            bubbleTextView.setVisibility(View.GONE);
            closeBubbleButton.setVisibility(View.GONE);
            findViewById(R.id.bubbleLayout).setVisibility(View.GONE);
        });

        mentorBtn.setOnClickListener(v -> showBubble("‘멘토 찾기’ 버튼은 나에게 맞는 멘토를 찾을 수 있습니다."));
        menteeBtn.setOnClickListener(v -> showBubble("‘멘티 찾기’ 버튼은 도움을 필요로 하는 멘티를 찾는 기능입니다."));
        aiMentorBtn.setOnClickListener(v -> showBubble("AI 멘토에게 질문하여 조언을 받을 수 있습니다."));
        manualBtn.setOnClickListener(v -> showBubble("이 화면은 사용자 매뉴얼입니다. 버튼 설명을 볼 수 있습니다."));
        csBtn.setOnClickListener(v -> showBubble("고객센터와의 문의를 연결하는 버튼입니다."));
    }

    private void showBubble(String message) {
        bubbleTextView.setText(message);
        bubbleTextView.setVisibility(View.VISIBLE);
        closeBubbleButton.setVisibility(View.VISIBLE);
        findViewById(R.id.bubbleLayout).setVisibility(View.VISIBLE);
    }
}
