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

public class ActivityNormal extends AppCompatActivity {
    private static final String TAG = "ActivityNormal";
    private GLSurfaceView mGLSurfaceView;
    private Renderer mRenderer;

    EditText editTextX1, editTextY1, editTextZ1,
            editTextX2, editTextY2, editTextZ2,
            editTextX3, editTextY3, editTextZ3,
            editTextNX1, editTextNY1, editTextNZ1,
            editTextNX2, editTextNY2, editTextNZ2,
            editTextNX3, editTextNY3, editTextNZ3,
            editTextX0, editTextY0, editTextZ0,
            editTextXd, editTextYd, editTextZd;

    Button buttonNorDraw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        mGLSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            Log.d(TAG, "Device supports OpenGL ES 2.0");
            mGLSurfaceView.setEGLContextClientVersion(2);
            mRenderer = new Renderer(this);
            mGLSurfaceView.setRenderer(mRenderer);

            editTextX1 = findViewById(R.id.editTextX1);
            editTextY1 = findViewById(R.id.editTextY1);
            editTextZ1 = findViewById(R.id.editTextZ1);
            editTextX2 = findViewById(R.id.editTextX2);
            editTextY2 = findViewById(R.id.editTextY2);
            editTextZ2 = findViewById(R.id.editTextZ2);
            editTextX3 = findViewById(R.id.editTextX3);
            editTextY3 = findViewById(R.id.editTextY3);
            editTextZ3 = findViewById(R.id.editTextZ3);

            editTextNX1 = findViewById(R.id.editTextNX1);
            editTextNY1 = findViewById(R.id.editTextNY1);
            editTextNZ1 = findViewById(R.id.editTextNZ1);
            editTextNX2 = findViewById(R.id.editTextNX2);
            editTextNY2 = findViewById(R.id.editTextNY2);
            editTextNZ2 = findViewById(R.id.editTextNZ2);
            editTextNX3 = findViewById(R.id.editTextNX3);
            editTextNY3 = findViewById(R.id.editTextNY3);
            editTextNZ3 = findViewById(R.id.editTextNZ3);

            editTextX0 = findViewById(R.id.editTextRayX0);
            editTextY0 = findViewById(R.id.editTextRayY0);
            editTextZ0 = findViewById(R.id.editTextRayZ0);
            editTextXd = findViewById(R.id.editTextRayXd);
            editTextYd = findViewById(R.id.editTextRayYd);
            editTextZd = findViewById(R.id.editTextRayZd);

            buttonNorDraw = findViewById(R.id.buttonNorDraw);

            buttonNorDraw.setOnClickListener(view -> {
                float x1 = Float.parseFloat(editTextX1.getText().toString());
                float y1 = Float.parseFloat(editTextY1.getText().toString());
                float z1 = Float.parseFloat(editTextZ1.getText().toString());
                float x2 = Float.parseFloat(editTextX2.getText().toString());
                float y2 = Float.parseFloat(editTextY2.getText().toString());
                float z2 = Float.parseFloat(editTextZ2.getText().toString());
                float x3 = Float.parseFloat(editTextX3.getText().toString());
                float y3 = Float.parseFloat(editTextY3.getText().toString());
                float z3 = Float.parseFloat(editTextZ3.getText().toString());

                float nx1 = Float.parseFloat(editTextNX1.getText().toString());
                float ny1 = Float.parseFloat(editTextNY1.getText().toString());
                float nz1 = Float.parseFloat(editTextNZ1.getText().toString());
                float nx2 = Float.parseFloat(editTextNX2.getText().toString());
                float ny2 = Float.parseFloat(editTextNY2.getText().toString());
                float nz2 = Float.parseFloat(editTextNZ2.getText().toString());
                float nx3 = Float.parseFloat(editTextNX3.getText().toString());
                float ny3 = Float.parseFloat(editTextNY3.getText().toString());
                float nz3 = Float.parseFloat(editTextNZ3.getText().toString());

                float x0 = Float.parseFloat(editTextX0.getText().toString());
                float y0 = Float.parseFloat(editTextY0.getText().toString());
                float z0 = Float.parseFloat(editTextZ0.getText().toString());
                float xd = Float.parseFloat(editTextXd.getText().toString());
                float yd = Float.parseFloat(editTextYd.getText().toString());
                float zd = Float.parseFloat(editTextZd.getText().toString());

                mRenderer.setNormalInput(
                        new float[]{x1, y1, z1}, new float[]{x2, y2, z2}, new float[]{x3, y3, z3},
                        new float[]{nx1, ny1, nz1}, new float[]{nx2, ny2, nz2}, new float[]{nx3, ny3, nz3},
                        new float[]{x0, y0, z0}, new float[]{xd, yd, zd});
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
