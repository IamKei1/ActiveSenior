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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private String currentUid, roomId;

    private final int VOICE_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputEditText = findViewById(R.id.inputEditText);
        sendTextButton = findViewById(R.id.sendTextButton);
        voiceButton = findViewById(R.id.voiceButton);
        watermarkText = findViewById(R.id.chatWatermarkTextView);

        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        roomId = getIntent().getStringExtra("roomId");

        sendTextButton.setOnClickListener(v -> {
            String msg = inputEditText.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
                inputEditText.setText("");

                String receiverUid = getIntent().getStringExtra("participantUid");
                String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Log.d("CHATROOM_DEBUG", "🔥 participantUid: " + receiverUid);
                Log.d("CHATROOM_DEBUG", "🙋 내 UID: " + senderUid);


                if (receiverUid != null && !receiverUid.equals(senderUid)) {
                    Log.d("FCM_TRACE", "멘티가 메시지 전송 시도");
                    sendNotificationToUser(receiverUid, "새 메시지", msg);
                } else {
                    Log.w("FCM_TRACE", "수신자가 자기 자신으로 판단됨. 알림 전송 안 함");
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

        listenForMessages();
    }

    private void sendMessage(String msg) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", msg);
        messageData.put("senderId", currentUid);
        messageData.put("timestamp", new Date());
        messageData.put("readBy", Arrays.asList(currentUid));

        db.collection("chat_rooms")
                .document(roomId)
                .collection("messages")
                .add(messageData);

        db.collection("chat_rooms")
                .document(roomId)
                .update("lastMessage", msg, "lastTimestamp", new Date());
    }

    private void listenForMessages() {
        db.collection("chat_rooms")
                .document(roomId)
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
                        boolean isUser = senderId.equals(currentUid);

                        if (lastMessageDate == null || !isSameDay(lastMessageDate, date)) {
                            chatMessages.add(new ChatMessage("", false, date, true));
                            lastMessageDate = date;
                        }

                        chatMessages.add(new ChatMessage(message, isUser, date, false));

                        List<String> readBy = (List<String>) doc.get("readBy");
                        if (readBy == null || !readBy.contains(currentUid)) {
                            doc.getReference().update("readBy", FieldValue.arrayUnion(currentUid));
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

                    watermarkText.setVisibility(chatMessages.size() > 0 ? View.GONE : View.VISIBLE);
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
                String recognizedText = results.get(0);
                inputEditText.setText(recognizedText);
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
        Log.d("FCM_DEBUG", "sendNotificationToUser() 호출됨");
        Log.d("FCM_DEBUG", "Sender: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d("FCM_DEBUG", "Receiver: " + targetUid);
        Log.d("FCM_DEBUG", "Title: " + title);
        Log.d("FCM_DEBUG", "Body: " + body);

        Map<String, Object> data = new HashMap<>();
        data.put("targetUid", targetUid);

        data.put("title", title);
        data.put("body", body);
        Log.d("FCM_DEBUG", "멘티 -> 멘토 알림 시도, targetUid = " + targetUid);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("sendChatNotification")
                .call(data)
                .addOnSuccessListener(result -> Log.d("FCM", "알림 전송 성공"))
                .addOnFailureListener(e -> Log.e("FCM", "알림 전송 실패: " + e.getMessage()));
    }
}
