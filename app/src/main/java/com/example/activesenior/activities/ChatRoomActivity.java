package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.adapters.ChatRoomAdapter;
import com.example.activesenior.models.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView chatRoomRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRoomList = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUid;
    private TextView emptyChatRoomText;

    private ListenerRegistration chatRoomListener;
    private Button newChatbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        emptyChatRoomText = findViewById(R.id.emptyChatRoomText);
        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newChatbutton = findViewById(R.id.newChatButton);
        newChatbutton.setVisibility(View.GONE);
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, currentUid, chatRoom -> {
            String participantUid = currentUid.equals(chatRoom.getParticipant1Id()) ? chatRoom.getParticipant2Id() : chatRoom.getParticipant1Id();
            String participantName = currentUid.equals(chatRoom.getParticipant1Id()) ? chatRoom.getParticipant2Name() : chatRoom.getParticipant1Name();

            Intent intent = new Intent(ChatRoomActivity.this, ChatActivity.class);
            intent.putExtra("roomId", chatRoom.getRoomId());
            intent.putExtra("participantUid", participantUid);
            intent.putExtra("participantName", participantName);
            startActivity(intent);
        });

        chatRoomRecyclerView.setAdapter(chatRoomAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToChatRooms();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatRoomListener != null) {
            chatRoomListener.remove();
        }
    }

    private void listenToChatRooms() {
        chatRoomListener = db.collection("chat_rooms")
                .whereArrayContains("participants", currentUid)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "채팅방 실시간 로딩 오류", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        return;
                    }

                    chatRoomList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                        if (chatRoom == null) continue;

                        chatRoom.setRoomId(doc.getId());

                        if (chatRoom.getLastMessage() == null) {
                            chatRoom.setLastMessage("(메시지 없음)");
                        }

                        chatRoomList.add(chatRoom);
                    }

                    Collections.sort(chatRoomList, Comparator.comparing(
                            (ChatRoom room) -> room.getLastTimestamp() != null ? room.getLastTimestamp() : new Date()
                    ).reversed());

                    chatRoomAdapter.notifyDataSetChanged();

                    emptyChatRoomText.setVisibility(chatRoomList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}
