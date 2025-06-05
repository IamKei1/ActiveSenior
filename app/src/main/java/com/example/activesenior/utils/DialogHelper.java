package com.example.activesenior.utils;

import android.content.Context;
import com.example.activesenior.dialogs.ConfirmDialog;

public class DialogHelper {

    public static void showConfirmDialog(Context context, String message, Runnable onConfirm) {
        ConfirmDialog.show(context, message, onConfirm::run);
    }
}
