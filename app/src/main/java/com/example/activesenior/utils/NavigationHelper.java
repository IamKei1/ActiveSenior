package com.example.activesenior.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.example.activesenior.activities.HomeActivity;
import com.example.activesenior.activities.MainActivity;
import com.example.activesenior.dialogs.ConfirmDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class NavigationHelper {

    public static void setupConfirmNavigation(Context context, Button button, Class<?> targetActivity) {
        button.setOnClickListener(v -> {
            String buttonText = button.getText().toString();
            String message = "[" + buttonText + "] 로 이동하시겠습니까?";

            ConfirmDialog.show(context, message, () -> {
                Intent intent = new Intent(context, targetActivity);
                context.startActivity(intent);
            });
        });
    }

    public static void showConfirmLogout(Activity activity) {
        ConfirmDialog.show(activity, "로그아웃하시겠습니까?", () -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    // ✅ 활동 종료 확인 다이얼로그
    // ✅ 활동 종료 확인 다이얼로그
    public static void showConfirmEndActivity(Activity activity, String role) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;
        String currentUid = currentUser.getUid();

        // 🔄 현재 유저 문서 가져오기
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(currentUserDoc -> {
                    String matchedUserId = currentUserDoc.getString("matchedUserId");

                    if (matchedUserId == null) {
                        ConfirmDialog.show(activity, "매칭된 사용자가 없습니다.");
                        return;
                    }

                    db.collection("users").document(matchedUserId).get()
                            .addOnSuccessListener(matchedDoc -> {
                                String partnerName = matchedDoc.getString("name");
                                String partnerRole = matchedDoc.getString("role");

                                if (partnerName == null || partnerRole == null) {
                                    Toast.makeText(activity, "상대방 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String label = partnerRole.equals("멘토") ? "멘토" : "멘티";
                                String message = label + " " + partnerName + "님과의 활동을 종료하시겠습니까?";

                                ConfirmDialog.show(activity, message, () -> {
                                    DocumentReference currentRef = db.collection("users").document(currentUid);
                                    DocumentReference matchedRef = db.collection("users").document(matchedUserId);

                                    WriteBatch batch = db.batch();
                                    batch.update(currentRef, "matchedUserId", null);
                                    batch.update(matchedRef, "matchedUserId", null);

                                    // 🔄 멘토의 UID 결정
                                    String mentorUid = "멘토".equals(role) ? currentUid : matchedUserId;
                                    DocumentReference mentorRef = db.collection("users").document(mentorUid);

                                    // 🔄 멘토 포인트 증가
                                    mentorRef.get().addOnSuccessListener(mentorDoc -> {
                                        Long point = mentorDoc.getLong("point");
                                        long updated = (point != null ? point : 0) + 1000;
                                        batch.update(mentorRef, "point", updated);

                                        batch.commit().addOnSuccessListener(unused -> {
                                            ConfirmDialog.show(activity, "활동이 종료되었습니다.\n수고하셨습니다!", () -> {
                                                Intent intent = new Intent(activity, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                activity.startActivity(intent);
                                                activity.finish();
                                            });
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(activity, "활동 종료 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    });
                                });
                            });
                });
    }


    public static void showUserToggleHelp(Activity activity, String role) {
        String target = "멘토".equals(role) ? "멘티" : "멘토";
        String message = target + "가 나를 지도에서 찾을 수 있도록 공개 여부를 설정합니다.\n\n" +
                "• ON: " + target + "가 나를 찾을 수 있어요\n" +
                "• OFF: " + target + "가 나를 볼 수 없어요";

        ConfirmDialog.show(activity, message);
    }

}
