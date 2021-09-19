package com.apress.proandroidmedia.ch2.timersnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class TimerSnapShot extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback {

    private static final String TAG = "TimerSnapShot";

    private SurfaceView cameraView;
    private Button startButton;
    private TextView countdownTextView;

    private Camera camera;
    private Handler timerUpdateHandler = new Handler();

    private int rotation = 0;
    private int currentTime = 10;
    private boolean timerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.CameraView);
        cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraView.getHolder().addCallback(this);

        countdownTextView = findViewById(R.id.CountDownTextView);
        startButton = findViewById(R.id.CountDownButton);
        startButton.setOnClickListener(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(parameters);
        } catch (IOException e) {
            Log.e(TAG, "surfaceCreated=>error: ", e);
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            switch (getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    rotation = 90;
                    break;

                case Surface.ROTATION_90:
                    rotation = 0;
                    break;

                case Surface.ROTATION_180:
                    rotation = 270;
                    break;

                case Surface.ROTATION_270:
                    rotation = 180;
                    break;
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.set("rotation", rotation);
            parameters.setRotation(rotation);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(rotation);
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (!timerRunning) {
            timerRunning = true;
            timerUpdateHandler.post(timerUpdateTask);
        }
    }

    private Runnable timerUpdateTask = new Runnable() {
        @Override
        public void run() {
            if (currentTime > 1) {
                currentTime--;
                timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
            } else {
                if (camera != null) {
                    switch (getWindowManager().getDefaultDisplay().getRotation()) {
                        case Surface.ROTATION_0:
                            rotation = 90;
                            break;

                        case Surface.ROTATION_90:
                            rotation = 0;
                            break;

                        case Surface.ROTATION_180:
                            rotation = 270;
                            break;

                        case Surface.ROTATION_270:
                            rotation = 180;
                            break;
                    }
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.set("rotation", rotation);
                    parameters.setRotation(rotation);
                    camera.setParameters(parameters);
                    camera.setDisplayOrientation(rotation);
                    camera.takePicture(null, null, null, TimerSnapShot.this);
                }
                timerRunning = false;
                currentTime = 10;
            }

            countdownTextView.setText("" + currentTime);
        }
    };

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Uri imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

        try {
            OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(rotation);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS);
            imageFileOS.flush();
            imageFileOS.close();
            Toast.makeText(this, "Save JPEG!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "onPictureTaken=>File not found.", e);
        } catch (IOException e) {
            Log.e(TAG, "onPictureTaken=>Write file error.", e);
        }

        if (camera != null) {
            camera.startPreview();
        }
    }
}