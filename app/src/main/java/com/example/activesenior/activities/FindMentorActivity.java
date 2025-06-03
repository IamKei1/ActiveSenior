package com.example.activesenior.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindMentorActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String currentUserUid;
    private RecyclerView mentorRecyclerView;
    private MentorAdapter mentorAdapter;
    private List<User> mentorList = new ArrayList<>();

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location currentUserLocation;
    private boolean hasMovedToUserLocation = false;

    private int selectedRadius;

    private Circle radiusCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_mentor);
        setupRadiusSpinner(); // 스피너 리스너 설정
        Spinner radiusSpinner = findViewById(R.id.radiusSpinner);
        radiusSpinner.setSelection(1); // "0.5km 이내"를 기본값으로 선택

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // 위치 요청 설정
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();


// 위치 업데이트 콜백
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                currentUserLocation = locationResult.getLastLocation();

                // 지도 중심 이동
                if (googleMap != null && !hasMovedToUserLocation) {
                    LatLng userLatLng = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));
                    hasMovedToUserLocation = true; // 이후에는 지도 중심을 변경하지 않음
                }

                // 거리 계산
                calculateDistancesToMentors();
            }
        };


        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();




        // 지도 Fragment 설정
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // RecyclerView 설정
        mentorRecyclerView = findViewById(R.id.mentorRecyclerView);
        mentorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mentorAdapter = new MentorAdapter(mentorList, this::onMentorSelected);
        mentorRecyclerView.setAdapter(mentorAdapter);

        checkLocationPermissionAndFetch();






    }


    private void setupRadiusSpinner() {
        Spinner radiusSpinner = findViewById(R.id.radiusSpinner);
        float zoomLevel = 13f;


        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float zoomLevel = 13f;


                switch (position) {
                    case 0:
                        selectedRadius = 500;

                        break;
                    case 1:
                        selectedRadius = 500;
                        zoomLevel = 17f;
                        break;
                    case 2:
                        selectedRadius = 1000;
                        zoomLevel = 16f;
                        break;
                    case 3:
                        selectedRadius = 1500;
                        zoomLevel = 15f;
                        break;
                    case 4:
                        selectedRadius = 2000;
                        zoomLevel = 14f;
                        break;
                    case 5:
                        selectedRadius = 2000;
                        zoomLevel = 13f;
                        break;
                    default:
                        selectedRadius = 500;
                        zoomLevel = 13f;
                        break;
                }

                if (googleMap != null && currentUserLocation != null) {
                    LatLng userLatLng = new LatLng(
                            currentUserLocation.getLatitude(),
                            currentUserLocation.getLongitude()
                    );

                    // 지도 이동
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel));

                    // 기존 원 제거
                    if (radiusCircle != null) {
                        radiusCircle.remove();
                    }

                    // 새로운 원 그리기
                    radiusCircle = googleMap.addCircle(new CircleOptions()
                            .center(userLatLng)
                            .radius(selectedRadius) // meter 단위
                            .strokeColor(Color.BLUE)
                            .fillColor(0x304A90E2) // 반투명 파란색
                            .strokeWidth(2f));
                }


                if (position > 0) {
                    calculateDistancesToMentors();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void calculateDistancesToMentors() {
        if (currentUserLocation == null || mentorList == null) return;

        List<User> filteredList = new ArrayList<>();

        for (User mentor : mentorList) {
            GeoPoint loc = mentor.getLocation();
            if (loc != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                        currentUserLocation.getLatitude(), currentUserLocation.getLongitude(),
                        loc.getLatitude(), loc.getLongitude(),
                        results
                );
                mentor.setDistance(results[0]);

                if (results[0] <= selectedRadius) {
                    filteredList.add(mentor);
                }
            }
        }

        Collections.sort(filteredList, Comparator.comparing(User::getDistance));
        mentorAdapter.setMentorList(filteredList); // 어댑터에 필터링된 리스트 반영
    }

    private double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }






    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            fetchAndUploadLocation();
        }
    }

    private void fetchAndUploadLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                db.collection("users").document(currentUserUid).update("location", geoPoint);

                if (googleMap != null) {
                    LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // 위치 권한이 있는 경우 현재 위치 표시
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);  // 🔵 블루 닷 표시
        }

        loadMentorsAndMarkOnMap();
    }


    private void loadMentorsAndMarkOnMap() {
        db.collection("users")
                .whereEqualTo("role", "멘토")
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    mentorList.clear();
                    googleMap.clear(); // 기존 마커 및 원 제거

                    // 🔵 1. 반경 원 그리기
                    if (currentUserLocation != null) {
                        LatLng center = new LatLng(
                                currentUserLocation.getLatitude(),
                                currentUserLocation.getLongitude()
                        );

                        if (radiusCircle != null) radiusCircle.remove();

                        radiusCircle = googleMap.addCircle(new CircleOptions()
                                .center(center)
                                .radius(selectedRadius)
                                .strokeColor(Color.parseColor("#4A90E2"))
                                .fillColor(0x304A90E2)
                                .strokeWidth(2f));
                    }

                    // 🟡 2. 멘토 마커 추가
                    int visibleMentorCount = 0;
                    for (DocumentSnapshot doc : querySnapshots) {
                        User mentor = doc.toObject(User.class);
                        mentor.setUid(doc.getId());
                        mentorList.add(mentor);

                        GeoPoint loc = doc.getGeoPoint("location");
                        if (loc != null && currentUserLocation != null) {
                            double distance = distanceBetween(
                                    currentUserLocation.getLatitude(),
                                    currentUserLocation.getLongitude(),
                                    loc.getLatitude(),
                                    loc.getLongitude()
                            );
                            mentor.setDistance((float) distance);

                            LatLng mentorLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                            float hue = (distance <= selectedRadius) ?
                                    BitmapDescriptorFactory.HUE_YELLOW :
                                    BitmapDescriptorFactory.HUE_RED;

                            if (distance <= selectedRadius) visibleMentorCount++;

                            googleMap.addMarker(new MarkerOptions()
                                    .position(mentorLatLng)
                                    .title(mentor.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                        }
                    }

                    // 🟢 3. 내 위치에 반경 내 멘토 수 마커 표시
                    if (currentUserLocation != null && visibleMentorCount > 0) {
                        LatLng center = new LatLng(
                                currentUserLocation.getLatitude(),
                                currentUserLocation.getLongitude()
                        );

                        googleMap.addMarker(new MarkerOptions()
                                .position(center)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title(visibleMentorCount + "명의 멘토가 반경 내에 있습니다."));
                    }

                    // 💬 4. 마커 클릭 시 다이얼로그 표시
                    googleMap.setOnMarkerClickListener(marker -> {
                        String name = marker.getTitle();

                        // 내 위치 마커는 무시
                        if (!name.contains("멘토") && !name.contains("명의")) return true;

                        final User[] selectedMentor = {null};
                        for (User mentor : mentorList) {
                            if (mentor.getName().equals(name)) {
                                selectedMentor[0] = mentor;
                                break;
                            }
                        }

                        if (selectedMentor[0] != null) {
                            new AlertDialog.Builder(this)
                                    .setTitle("멘토 정보")
                                    .setMessage("이름: " + selectedMentor[0].getName() +
                                            "\n거리: " + String.format("%.0f", selectedMentor[0].getDistance()) + "m")
                                    .setPositiveButton("요청하기", (dialog, which) -> {
                                        onMentorSelected(selectedMentor[0]);
                                    })
                                    .setNegativeButton("취소", null)
                                    .show();
                        }

                        return true;
                    });

                    // 🔄 5. 리스트 정렬 및 UI 반영
                    calculateDistancesToMentors();
                });
    }







    private void onMentorSelected(User mentor) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUserUid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String currentUserName = doc.getString("name");

                    // 매칭 정보 업데이트
                    db.collection("users").document(currentUserUid)
                            .update("matchedUserId", mentor.getUid());
                    db.collection("users").document(mentor.getUid())
                            .update("matchedUserId", currentUserUid, "isAvailable", false);

                    // ChatRoom 문서 생성용 데이터
                    Map<String, Object> chatRoom = new HashMap<>();
                    chatRoom.put("participants", Arrays.asList(currentUserUid, mentor.getUid()));
                    chatRoom.put("participant1Id", currentUserUid);
                    chatRoom.put("participant2Id", mentor.getUid());
                    chatRoom.put("participant1Name", currentUserName);
                    chatRoom.put("participant2Name", mentor.getName());
                    chatRoom.put("lastMessage", "채팅이 시작되었습니다");
                    chatRoom.put("lastTimestamp", new Date());

                    db.collection("chat_rooms").add(chatRoom)
                            .addOnSuccessListener(docRef -> {
                                Intent intent = new Intent(FindMentorActivity.this, ChatActivity.class);
                                intent.putExtra("roomId", docRef.getId());
                                intent.putExtra("participantUid", mentor.getUid());
                                intent.putExtra("participantName", mentor.getName());
                                startActivity(intent);
                            });
                });
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
}
