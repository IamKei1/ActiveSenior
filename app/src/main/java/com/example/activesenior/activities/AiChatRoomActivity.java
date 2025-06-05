package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.adapters.ChatRoomAdapter;
import com.example.activesenior.models.ChatRoom;
import com.example.activesenior.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class AiChatRoomActivity extends AppCompatActivity {

    private RecyclerView chatRoomRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRoomList = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUid;
    private TextView emptyChatRoomText;
    private TextView chatRoomTitle;
    private TextView watermark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room); // ✅ 기존 layout 재사용

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatRoomTitle = findViewById(R.id.chatRoomTitle);
        chatRoomTitle.setText("AI 멘토 대화 기록");
        emptyChatRoomText = findViewById(R.id.emptyChatRoomText);
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());


        findViewById(R.id.newChatButton).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("ai_chat_rooms")
                    .document(uid)
                    .collection("messages")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit().addOnSuccessListener(unused -> {
                            // 🔄 삭제 후 AI 멘토 화면으로 이동
                            Intent intent = new Intent(AiChatRoomActivity.this, AiMentorActivity.class);
                            startActivity(intent);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "기존 메시지 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "메시지 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });



        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, currentUid, chatRoom -> {
            // ✅ AI 전용 → 항상 AiMentorActivity로 이동
            Intent intent = new Intent(AiChatRoomActivity.this, AiMentorActivity.class);
            startActivity(intent);
        });

        chatRoomRecyclerView.setAdapter(chatRoomAdapter);
        loadAiChatRoom();
    }

    private void loadAiChatRoom() {
        db.collection("ai_chat_rooms")
                .document(currentUid)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    chatRoomList.clear();

                    if (!querySnapshots.isEmpty()) {
                        DocumentSnapshot doc = querySnapshots.getDocuments().get(0);
                        ChatMessage lastMsg = doc.toObject(ChatMessage.class);

                        ChatRoom aiRoom = new ChatRoom();
                        aiRoom.setRoomId("ai_" + currentUid);
                        aiRoom.setParticipant1Id(currentUid);
                        aiRoom.setParticipant2Id("AI");
                        aiRoom.setParticipant1Name("나");
                        aiRoom.setParticipant2Name("AI 멘토");
                        aiRoom.setLastMessage(lastMsg.getMessage());
                        aiRoom.setLastTimestamp(lastMsg.getTimestamp());

                        chatRoomList.add(aiRoom);
                    }

                    chatRoomAdapter.notifyDataSetChanged();
                    emptyChatRoomText.setVisibility(chatRoomList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "AI 대화 내역 불러오기 실패", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
