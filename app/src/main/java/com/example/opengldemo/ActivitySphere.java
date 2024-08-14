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

public class ActivitySphere extends AppCompatActivity {
    private static final String TAG = "ActivitySphere";
    private GLSurfaceView mGLSurfaceView;
    private Renderer mRenderer;

    EditText editTextR,
            editTextXSph, editTextYSph, editTextZSph,
            editTextX0, editTextY0, editTextZ0,
            editTextXd, editTextYd, editTextZd;

    Button buttonSphDraw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphere);
        mGLSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            Log.d(TAG, "Device supports OpenGL ES 2.0");
            mGLSurfaceView.setEGLContextClientVersion(2);
            mRenderer = new Renderer(this);
            mGLSurfaceView.setRenderer(mRenderer);

            editTextR = findViewById(R.id.editTextR);
            editTextXSph = findViewById(R.id.editTextXSph);
            editTextYSph = findViewById(R.id.editTextYSph);
            editTextZSph = findViewById(R.id.editTextZSph);
            editTextX0 = findViewById(R.id.editTextRayX0);
            editTextY0 = findViewById(R.id.editTextY0);
            editTextZ0 = findViewById(R.id.editTextZ0);
            editTextXd = findViewById(R.id.editTextXd);
            editTextYd = findViewById(R.id.editTextYd);
            editTextZd = findViewById(R.id.editTextZd);

            buttonSphDraw = findViewById(R.id.buttonSphDraw);

            buttonSphDraw.setOnClickListener(view -> {
                float r = Float.parseFloat(editTextR.getText().toString());
                float xSph = Float.parseFloat(editTextXSph.getText().toString());
                float ySph = Float.parseFloat(editTextYSph.getText().toString());
                float zSph = Float.parseFloat(editTextZSph.getText().toString());
                float x0 = Float.parseFloat(editTextX0.getText().toString());
                float y0 = Float.parseFloat(editTextY0.getText().toString());
                float z0 = Float.parseFloat(editTextZ0.getText().toString());
                float xd = Float.parseFloat(editTextXd.getText().toString());
                float yd = Float.parseFloat(editTextYd.getText().toString());
                float zd = Float.parseFloat(editTextZd.getText().toString());

                mRenderer.setSphereInput(r, new float[]{xSph, ySph, zSph}, new float[]{x0, y0, z0}, new float[]{xd, yd, zd});
                setContentView(mGLSurfaceView);
            });
        } else {
            Log.e(TAG, "Device does not support OpenGL ES 2.0");
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