package com.example.activesenior.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.adapters.ChatAdapter;
import com.example.activesenior.models.ChatMessage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.*;

public class AiMentorActivity extends AppCompatActivity {

    private static final int VOICE_REQUEST_CODE = 1001;
    private static final int LOCATION_REQUEST_CODE = 101;

    private EditText inputEditText;
    private Button sendTextButton, voiceButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseFunctions functions = FirebaseFunctions.getInstance();
    private Date lastMessageDate = null;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_mentor);

        inputEditText = findViewById(R.id.inputEditText);
        sendTextButton = findViewById(R.id.sendTextButton);
        voiceButton = findViewById(R.id.voiceButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermissionAndFetch();

        sendTextButton.setOnClickListener(v -> {
            String input = inputEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                addMessage(input, true);
                processMessage(input);
                inputEditText.setText("");
            }
        });

        voiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "질문을 말씀해주세요");
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                inputEditText.setText(recognizedText);
                addMessage(recognizedText, true);
                processMessage(recognizedText);
            }
        }
    }

    private void addMessage(String message, boolean isUser) {
        Date now = new Date();
        if (lastMessageDate == null || !isSameDay(lastMessageDate, now)) {
            chatMessages.add(new ChatMessage("", false, now, true));
            lastMessageDate = now;
        }

        chatMessages.add(new ChatMessage(message, isUser, now, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(d1);
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void processMessage(String question) {
        if (containsLocationKeyword(question)) {
            sendToFindNearbyPlace(question);
        } else {
            sendToChatGPT(question);
        }
    }

    private boolean containsLocationKeyword(String message) {
        return message.contains("근처") || message.contains("찾아줘") || message.contains("어디") || message.contains("가는 법");
    }

    private void sendToChatGPT(String question) {
        addMessage("AI 응답을 불러오는 중...", false);
        Map<String, Object> data = new HashMap<>();
        data.put("message", question);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("chatWithAI")
                .call(data)
                .addOnSuccessListener(result -> {
                    String reply = (String) ((Map<String, Object>) result.getData()).get("reply");
                    removeLoadingAndAdd(reply);
                })
                .addOnFailureListener(e -> removeLoadingAndAdd("AI 응답 실패: " + e.getMessage()));
    }

    private void sendToFindNearbyPlace(String question) {
        addMessage("근처 장소를 검색 중입니다...", false);

        Map<String, Object> data = new HashMap<>();
        data.put("lat", currentLat);
        data.put("lng", currentLng);
        data.put("query", question);

        functions.getHttpsCallable("findNearbyPlace")
                .call(data)
                .addOnSuccessListener(result -> {
                    List<Map<String, Object>> places = (List<Map<String, Object>>) ((Map<String, Object>) result.getData()).get("results");

                    if (places.isEmpty()) {
                        removeLoadingAndAdd("근처에 해당 장소를 찾을 수 없습니다.");
                        return;
                    }

                    // 가장 첫 장소 이름은 추출하지만 사용 안 함
                    String reply = "근처에 " + places.size() + "개의 장소가 검색되었습니다.\n지도에서 확인해보세요!";
                    removeLoadingAndAdd(reply);

                    // 🔽 자동 지도 연결: 질문 키워드 기반
                    openMapWithFallback(question, currentLat, currentLng);
                })
                .addOnFailureListener(e -> removeLoadingAndAdd("장소 검색 실패: " + e.getMessage()));
    }



    private void openMapWithFallback(String keyword, double lat, double lng) {
        String placeKeyword = extractPlaceKeyword(keyword);
        if (placeKeyword == null) {
            Toast.makeText(this, "장소 키워드를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 네이버 지도 인텐트
        Uri naverUri = Uri.parse("nmap://search?query=" + placeKeyword + "&lat=" + lat + "&lng=" + lng + "&appname=com.example.activesenior");
        Intent naverIntent = new Intent(Intent.ACTION_VIEW, naverUri);
        naverIntent.setPackage("com.nhn.android.nmap");

        if (naverIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(naverIntent); // ✅ 네이버 지도 실행
        } else {
            // 2. 구글 지도 인텐트
            Uri googleUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + placeKeyword);
            Intent googleIntent = new Intent(Intent.ACTION_VIEW, googleUri);
            googleIntent.setPackage("com.google.android.apps.maps");

            if (googleIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(googleIntent); // ✅ 구글 지도 실행
            } else {
                // 3. 웹 브라우저 fallback
                String webUrl = "https://map.naver.com/v5/search/" + placeKeyword;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                startActivity(browserIntent); // ✅ 웹으로 연결
            }
        }
    }

    private String extractPlaceKeyword(String query) {
        String[] keywords = {
                "카페", "약국", "병원", "식당", "편의점", "은행", "주차장", "미용실", "헬스장", "도서관",
                "지하철역", "지하철", "버스정류장", "마트", "관공서", "동사무소", "주민센터"
        };

        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                return keyword;
            }
        }

        return null;  // 키워드가 없을 경우 null
    }











    private void removeLoadingAndAdd(String message) {
        chatMessages.remove(chatMessages.size() - 1);
        chatAdapter.notifyItemRemoved(chatMessages.size());
        addMessage(message, false);
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                }

            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissionAndFetch();
        }
    }
}
