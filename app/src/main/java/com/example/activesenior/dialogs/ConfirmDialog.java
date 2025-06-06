package com.example.activesenior.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.activesenior.R;

public class ConfirmDialog {

    public interface ConfirmListener {
        void onConfirm();
    }

    public static void show(Context context, String message, ConfirmListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);

        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        Button confirmBtn = dialog.findViewById(R.id.buttonConfirm);
        Button cancelBtn = dialog.findViewById(R.id.buttonCancel);

        messageView.setText(message);

        confirmBtn.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onConfirm();
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.setCancelable(false); // 팝업 바깥 터치 방지
        dialog.show();
    }

    public static void show(Context context, String message) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);

        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        Button confirmBtn = dialog.findViewById(R.id.buttonConfirm);
        Button cancelBtn = dialog.findViewById(R.id.buttonCancel);
        confirmBtn.setText("확인");

        messageView.setText(message);
        cancelBtn.setVisibility(View.GONE); // ⛔ 취소 버튼 숨기기

        confirmBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.setCancelable(false);
        dialog.show();
    }
}
