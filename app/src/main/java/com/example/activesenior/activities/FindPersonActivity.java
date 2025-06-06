package com.example.activesenior.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activesenior.R;
import com.example.activesenior.adapters.MentorAdapter;
import com.example.activesenior.models.User;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class FindPersonActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String currentUserUid;
    private String targetRole; // "멘토" 또는 "멘티"

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private RecyclerView personRecyclerView;
    private MentorAdapter mentorAdapter;
    private List<User> userList = new ArrayList<>();

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location currentUserLocation;
    private boolean hasMovedToUserLocation = false;

    private int selectedRadius;
    private Circle radiusCircle;

    private TextView titleView;
    private Spinner radiusSpinner;

    private ImageButton reload;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final int REFRESH_INTERVAL_MS = 15_000; // 15초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_person);


        titleView = findViewById(R.id.findPersonTitle);
        radiusSpinner = findViewById(R.id.radiusSpinner);
        personRecyclerView = findViewById(R.id.personRecyclerView);

        reload = findViewById(R.id.reloadButton);

        reload.setOnClickListener(v -> {
            loadUsersAndMarkOnMap();
            Toast.makeText(this, "검색가능한 사용자 목록을 갱신합니다.", Toast.LENGTH_SHORT).show();
        });

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                currentUserLocation = locationResult.getLastLocation();
                if (googleMap != null && !hasMovedToUserLocation) {
                    LatLng userLatLng = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));
                    hasMovedToUserLocation = true;
                }
                calculateDistancesToUsers();
            }
        };

        setupRadiusSpinner(radiusSpinner);
        radiusSpinner.setSelection(1);

        personRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mentorAdapter = new MentorAdapter(userList, this::onUserSelected);
        personRecyclerView.setAdapter(mentorAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fetchMyRoleAndSetTarget();
    }

    private void fetchMyRoleAndSetTarget() {
        db.collection("users").document(currentUserUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String myRole = documentSnapshot.getString("role");
                        if (myRole != null) {
                            targetRole = myRole.equals("멘토") ? "멘티" : "멘토";
                            titleView.setText(targetRole + " 찾기");

                            checkLocationPermissionAndFetch(); // 위치 가져오기 후 → loadUsersAndMarkOnMap()이 호출됨
                        }
                    }
                });
    }

    private void setupRadiusSpinner(Spinner spinner) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float zoom = 13f;
                switch (position) {
                    case 1: selectedRadius = 500; zoom = 17f; break;
                    case 2: selectedRadius = 1000; zoom = 16f; break;
                    case 3: selectedRadius = 1500; zoom = 15f; break;
                    case 4: selectedRadius = 2000; zoom = 14f; break;
                    default: selectedRadius = 500; break;
                }

                if (googleMap != null && currentUserLocation != null) {
                    LatLng userLatLng = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoom));

                    if (radiusCircle != null) radiusCircle.remove();
                    radiusCircle = googleMap.addCircle(new CircleOptions()
                            .center(userLatLng).radius(selectedRadius)
                            .strokeColor(Color.BLUE).fillColor(0x304A90E2).strokeWidth(2f));
                }

                if (position > 0) calculateDistancesToUsers();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            refreshHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL_MS); // 🔁 시작
            Toast.makeText(this, "검색가능한 사용자 목록을 갱신합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        refreshHandler.removeCallbacks(autoRefreshRunnable); // 🛑 중지
    }

    private void calculateDistancesToUsers() {
        if (currentUserLocation == null || userList == null) return;

        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            GeoPoint loc = user.getLocation();
            if (loc != null) {
                float[] results = new float[1];
                Location.distanceBetween(currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                        loc.getLatitude(), loc.getLongitude(), results);
                user.setDistance(results[0]);
                if (results[0] <= selectedRadius) filteredList.add(user);
            }
        }

        filteredList.sort(Comparator.comparing(User::getDistance));
        mentorAdapter.setMentorList(filteredList);
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            fetchAndUploadLocation();
        }
    }

    private void fetchAndUploadLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                db.collection("users").document(currentUserUid).update("location", geoPoint);

                if (googleMap != null) {
                    LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
                }

                // 위치 가져온 후 사용자 목록 불러오기
                loadUsersAndMarkOnMap();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        // ❗ 사용자 역할 결정 후에 loadUsersAndMarkOnMap() 호출됨
    }

    private void loadUsersAndMarkOnMap() {
        db.collection("users")
                .whereEqualTo("role", targetRole)
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    userList.clear();
                    googleMap.clear();

                    for (DocumentSnapshot doc : querySnapshots) {
                        User user = doc.toObject(User.class);
                        user.setUid(doc.getId());
                        userList.add(user);

                        GeoPoint loc = doc.getGeoPoint("location");
                        if (loc != null && currentUserLocation != null) {
                            double distance = distanceBetween(
                                    currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                                    loc.getLatitude(), loc.getLongitude());

                            user.setDistance((float) distance);

                            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                            float hue = (distance <= selectedRadius) ?
                                    (targetRole.equals("멘토") ? BitmapDescriptorFactory.HUE_YELLOW : BitmapDescriptorFactory.HUE_CYAN) :
                                    BitmapDescriptorFactory.HUE_RED;

                            googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(user.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        }
                    }

                    calculateDistancesToUsers();

                    // ✅ 사용자 없으면 안내 메시지 표시
                    TextView emptyMessage = findViewById(R.id.emptyMessageTextView);
                    if (userList.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                    } else {
                        emptyMessage.setVisibility(View.GONE);
                    }

                    googleMap.setOnMarkerClickListener(marker -> {
                        String name = marker.getTitle();
                        for (User user : userList) {
                            if (user.getName().equals(name)) {
                                showDialog(user);
                                break;
                            }
                        }
                        return true;
                    });
                });
    }

    private void showDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle(targetRole + " 정보")
                .setMessage("이름: " + user.getName() + "\n거리: " + String.format("%.0f", user.getDistance()) + "m")
                .setPositiveButton("요청하기", (dialog, which) -> onUserSelected(user))
                .setNegativeButton("취소", null)
                .show();
    }

    private void onUserSelected(User user) {
        db.collection("users").document(currentUserUid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String currentUserName = doc.getString("name");

                    List<String> participants = Arrays.asList(currentUserUid, user.getUid());
                    Collections.sort(participants);

                    db.collection("chat_rooms")
                            .whereEqualTo("participants", participants)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    // ✅ 기존 채팅방이 있는 경우: matchedUserId / isAvailable 갱신
                                    DocumentSnapshot existingRoom = querySnapshot.getDocuments().get(0);

                                    WriteBatch batch = db.batch();
                                    DocumentReference myRef = db.collection("users").document(currentUserUid);
                                    DocumentReference userRef = db.collection("users").document(user.getUid());

                                    batch.update(myRef, "matchedUserId", user.getUid(), "isAvailable", false);
                                    batch.update(userRef, "matchedUserId", currentUserUid, "isAvailable", false);

                                    batch.commit().addOnSuccessListener(unused -> {
                                        Intent intent = new Intent(this, ChatActivity.class);
                                        intent.putExtra("roomId", existingRoom.getId());
                                        intent.putExtra("participantUid", user.getUid());
                                        intent.putExtra("participantName", user.getName());
                                        startActivity(intent);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(this, "사용자 상태 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                                } else {
                                    // 새 채팅방 생성 및 상태 업데이트는 기존대로 처리
                                    createNewChatRoom(currentUserUid, currentUserName, user);
                                }
                            });
                });
    }


    private void createNewChatRoom(String myUid, String myName, User user) {
        List<String> participants = Arrays.asList(myUid, user.getUid());
        Collections.sort(participants);

        Map<String, Object> chatRoom = new HashMap<>();
        chatRoom.put("participants", participants);
        chatRoom.put("participant1Id", myUid);
        chatRoom.put("participant2Id", user.getUid());
        chatRoom.put("participant1Name", myName);
        chatRoom.put("participant2Name", user.getName());
        chatRoom.put("lastMessage", "채팅이 시작되었습니다");
        chatRoom.put("lastTimestamp", new Date());

        WriteBatch batch = db.batch();

        DocumentReference myRef = db.collection("users").document(myUid);
        DocumentReference userRef = db.collection("users").document(user.getUid());

// 🔄 양쪽 사용자 모두 matchedUserId와 isAvailable 업데이트
        batch.update(myRef, "matchedUserId", user.getUid(), "isAvailable", false);
        batch.update(userRef, "matchedUserId", myUid, "isAvailable", false);

// 🔄 채팅방 생성
        db.collection("chat_rooms").add(chatRoom).addOnSuccessListener(docRef -> {
            // 채팅방 생성 성공 후 사용자 정보 업데이트 실행
            batch.commit().addOnSuccessListener(unused -> {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("roomId", docRef.getId());
                intent.putExtra("participantUid", user.getUid());
                intent.putExtra("participantName", user.getName());
                startActivity(intent);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "사용자 상태 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "채팅방 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndUploadLocation();
        }
    }

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadUsersAndMarkOnMap(); // 🔄 사용자 목록 새로고침
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS); // 다음 실행 예약
        }
    };
}
