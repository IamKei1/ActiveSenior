package com.example.activesenior.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import com.example.activesenior.activities.MainActivity;
import com.example.activesenior.dialogs.ConfirmDialog;
import com.google.firebase.auth.FirebaseAuth;

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
}
