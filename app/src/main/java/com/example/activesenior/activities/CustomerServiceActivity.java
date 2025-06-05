package com.example.activesenior.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;
import com.example.activesenior.utils.NavigationHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerServiceActivity extends AppCompatActivity {

    private Button btnGoPointShop;
    private Button btnGoSuggestion;
    private Button btnGoChangePassword;
    private Button btnGoDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);

        btnGoPointShop = findViewById(R.id.btnGoPointShop);
        btnGoSuggestion = findViewById(R.id.btnGoSuggestion);
        btnGoChangePassword = findViewById(R.id.btnGoChangePassword);
        btnGoDeleteAccount = findViewById(R.id.btnGoDeleteAccount);

        NavigationHelper.setupConfirmNavigation(this,btnGoPointShop, PointShopActivity.class);
        NavigationHelper.setupConfirmNavigation(this,btnGoSuggestion, SuggestionActivity.class);
        NavigationHelper.setupConfirmNavigation(this,btnGoChangePassword, ChangePasswordActivity.class);
        NavigationHelper.setupConfirmNavigation(this,btnGoDeleteAccount, DeleteAccountActivity.class);


        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnLogOut).setOnClickListener(v -> {
            NavigationHelper.showConfirmLogout(this);
        });

    }
}

