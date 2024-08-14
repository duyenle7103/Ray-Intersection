package com.example.opengldemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button buttonTriangle, buttonSphere, buttonNormal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        buttonTriangle = findViewById(R.id.buttonTriangle);
        buttonSphere = findViewById(R.id.buttonSphere);
        buttonNormal = findViewById(R.id.buttonNormal);

        buttonTriangle.setOnClickListener(view -> {
            Log.d(TAG, "buttonTriangle clicked");
            Intent intent = new Intent(MainActivity.this, ActivityTriangle.class);
            startActivity(intent);
        });

        buttonSphere.setOnClickListener(view -> {
            Log.d(TAG, "buttonSphere clicked");
            Intent intent = new Intent(MainActivity.this, ActivitySphere.class);
            startActivity(intent);
        });

        buttonNormal.setOnClickListener(view -> {
            Log.d(TAG, "buttonNormal clicked");
            Intent intent = new Intent(MainActivity.this, ActivityNormal.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}