package com.example.opengldemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityTriangle extends AppCompatActivity {
    private static final String TAG = "ActivityTriangle";
    private GLSurfaceView mGLSurfaceView;
    private Renderer mRenderer;

    EditText editTextX1, editTextY1, editTextZ1,
            editTextX2, editTextY2, editTextZ2,
            editTextX3, editTextY3, editTextZ3,
            editTextX0, editTextY0, editTextZ0,
            editTextXd, editTextYd, editTextZd;

    Button buttonTriDraw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);
        mGLSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            Log.d(TAG, "Device supports OpenGL ES 2.0");
            mGLSurfaceView.setEGLContextClientVersion(2);
            mRenderer = new Renderer(this);
            mGLSurfaceView.setRenderer(mRenderer);

            editTextX1 = findViewById(R.id.editText1);
            editTextY1 = findViewById(R.id.editText2);
            editTextZ1 = findViewById(R.id.editText3);
            editTextX2 = findViewById(R.id.editText4);
            editTextY2 = findViewById(R.id.editText5);
            editTextZ2 = findViewById(R.id.editText6);
            editTextX3 = findViewById(R.id.editText7);
            editTextY3 = findViewById(R.id.editText8);
            editTextZ3 = findViewById(R.id.editText9);
            editTextX0 = findViewById(R.id.editText10);
            editTextY0 = findViewById(R.id.editText11);
            editTextZ0 = findViewById(R.id.editText12);
            editTextXd = findViewById(R.id.editText13);
            editTextYd = findViewById(R.id.editText14);
            editTextZd = findViewById(R.id.editText15);

            buttonTriDraw = findViewById(R.id.buttonTriDraw);

            buttonTriDraw.setOnClickListener(view -> {
                float x1 = Float.parseFloat(editTextX1.getText().toString());
                float y1 = Float.parseFloat(editTextY1.getText().toString());
                float z1 = Float.parseFloat(editTextZ1.getText().toString());
                float x2 = Float.parseFloat(editTextX2.getText().toString());
                float y2 = Float.parseFloat(editTextY2.getText().toString());
                float z2 = Float.parseFloat(editTextZ2.getText().toString());
                float x3 = Float.parseFloat(editTextX3.getText().toString());
                float y3 = Float.parseFloat(editTextY3.getText().toString());
                float z3 = Float.parseFloat(editTextZ3.getText().toString());
                float x0 = Float.parseFloat(editTextX0.getText().toString());
                float y0 = Float.parseFloat(editTextY0.getText().toString());
                float z0 = Float.parseFloat(editTextZ0.getText().toString());
                float xd = Float.parseFloat(editTextXd.getText().toString());
                float yd = Float.parseFloat(editTextYd.getText().toString());
                float zd = Float.parseFloat(editTextZd.getText().toString());

                mRenderer.setTriangleInput(new float[]{x1, y1, z1}, new float[]{x2, y2, z2}, new float[]{x3, y3, z3},
                        new float[]{x0, y0, z0}, new float[]{xd, yd, zd});
                setContentView(mGLSurfaceView);
            });
        } else {
            Log.d(TAG, "Device does not support OpenGL ES 2.0");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}