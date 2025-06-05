package com.example.activesenior.activities;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.activesenior.R;

public class PointShopActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_shop);

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }
}
