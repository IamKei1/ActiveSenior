package com.example.activesenior.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.adapters.ChatAdapter;
import com.example.activesenior.models.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText inputEditText;
    private Button sendTextButton, voiceButton;
    private TextView watermarkText;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private Date lastMessageDate = null;

    private FirebaseFirestore db;
    private String currentUserId, roomId;
    private String participant1Id, participant2Id, receiverUid;

    private static final int VOICE_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputEditText = findViewById(R.id.inputEditText);
        sendTextButton = findViewById(R.id.sendTextButton);
        voiceButton = findViewById(R.id.voiceButton);
        watermarkText = findViewById(R.id.chatWatermarkTextView);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        roomId = getIntent().getStringExtra("roomId");

        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sendTextButton.setOnClickListener(v -> {
            String msg = inputEditText.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
                inputEditText.setText("");

                if (receiverUid != null && !receiverUid.equals(currentUserId)) {
                    Log.d("FCM_TRACE", "메시지 전송 시도 -> receiverUid: " + receiverUid);
                    sendNotificationToUser(receiverUid, "새 메시지", msg);
                } else {
                    Log.w("FCM_TRACE", "수신자 UID가 비정상적이거나 자기 자신입니다");
                }
            }
        });

        voiceButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, VOICE_REQUEST_CODE);
            } else {
                startVoiceRecognition();
            }
        });

        loadRoomParticipantsAndListen();
    }

    private void loadRoomParticipantsAndListen() {
        db.collection("chat_rooms").document(roomId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    participant1Id = documentSnapshot.getString("participant1Id");
                    participant2Id = documentSnapshot.getString("participant2Id");

                    if (currentUserId.equals(participant1Id)) {
                        receiverUid = participant2Id;
                    } else {
                        receiverUid = participant1Id;
                    }

                    listenForMessages();
                })
                .addOnFailureListener(e -> Log.e("CHAT_INIT", "채팅방 정보 로딩 실패: " + e.getMessage()));
    }

    private void sendMessage(String msg) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", msg);
        messageData.put("senderId", currentUserId);
        messageData.put("timestamp", new Date());
        messageData.put("readBy", Arrays.asList(currentUserId));

        db.collection("chat_rooms").document(roomId)
                .collection("messages")
                .add(messageData);

        db.collection("chat_rooms").document(roomId)
                .update("lastMessage", msg, "lastTimestamp", new Date());
    }

    private void listenForMessages() {
        db.collection("chat_rooms").document(roomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    chatMessages.clear();
                    lastMessageDate = null;

                    for (DocumentSnapshot doc : snapshots) {
                        String message = doc.getString("message");
                        String senderId = doc.getString("senderId");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        Date date = ts != null ? ts.toDate() : new Date();

                        // 날짜 헤더 삽입
                        if (lastMessageDate == null || !isSameDay(lastMessageDate, date)) {
                            chatMessages.add(new ChatMessage("", date, true, senderId, currentUserId));
                            lastMessageDate = date;
                        }

                        // 메시지 삽입
                        chatMessages.add(new ChatMessage(message, date, false, senderId, currentUserId));

                        // 읽음 처리
                        List<String> readBy = (List<String>) doc.get("readBy");
                        if (readBy == null || !readBy.contains(currentUserId)) {
                            doc.getReference().update("readBy", FieldValue.arrayUnion(currentUserId));
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    watermarkText.setVisibility(chatMessages.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }


    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "음성으로 메시지를 입력하세요");

        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "음성 인식을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                inputEditText.setText(results.get(0));
            }
        }
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(d1);
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void sendNotificationToUser(String targetUid, String title, String body) {
        Map<String, Object> data = new HashMap<>();
        data.put("targetUid", targetUid);
        data.put("title", title);
        data.put("body", body);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("sendChatNotification")
                .call(data)
                .addOnSuccessListener(result -> Log.d("FCM", "알림 전송 성공"))
                .addOnFailureListener(e -> Log.e("FCM", "알림 전송 실패: " + e.getMessage()));
    }
}
