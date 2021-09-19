package com.apress.proandroidmedia.ch2.timelapsesnapshot;

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

public class TimelapseSnapShot extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback {

    private static final String TAG = "TimelapseSnapShot";

    private static final int SECONDS_BETWEEN_PHOTOS = 60;   // one minute

    private SurfaceView cameraView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private Button startStopButton;
    private TextView countdownTextView;
    private Handler timerUpdateHandler;

    private boolean timelapseRunning = false;
    private int currentTime = 0;
    private int rotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.CameraView);
        surfaceHolder = cameraView.getHolder();
        surfaceHolder.addCallback(this);

        countdownTextView = findViewById(R.id.CountDownTextView);
        startStopButton = findViewById(R.id.CountDownButton);
        startStopButton.setOnClickListener(this);
        timerUpdateHandler = new Handler();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.d(TAG, "surfaceCreated=>rotation: " + rotation);
            switch (rotation) {
                case Surface.ROTATION_0:
                    parameters.set("rotaion", 90);
                    parameters.setRotation(90);
                    camera.setDisplayOrientation(90);
                    break;

                case Surface.ROTATION_90:
                    parameters.set("rotaion", 0);
                    parameters.setRotation(0);
                    camera.setDisplayOrientation(0);
                    break;

                case Surface.ROTATION_180:
                    parameters.set("rotaion", 270);
                    parameters.setRotation(270);
                    camera.setDisplayOrientation(270);
                    break;

                case Surface.ROTATION_270:
                    parameters.set("rotaion", 180);
                    parameters.setRotation(180);
                    camera.setDisplayOrientation(180);
                    break;
            }
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
            Camera.Parameters parameters = camera.getParameters();
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.d(TAG, "surfaceCreated=>rotation: " + rotation);
            switch (rotation) {
                case Surface.ROTATION_0:
                    parameters.set("rotaion", 90);
                    parameters.setRotation(90);
                    camera.setDisplayOrientation(90);
                    break;

                case Surface.ROTATION_90:
                    parameters.set("rotaion", 0);
                    parameters.setRotation(0);
                    camera.setDisplayOrientation(0);
                    break;

                case Surface.ROTATION_180:
                    parameters.set("rotaion", 270);
                    parameters.setRotation(270);
                    camera.setDisplayOrientation(270);
                    break;

                case Surface.ROTATION_270:
                    parameters.set("rotaion", 180);
                    parameters.setRotation(180);
                    camera.setDisplayOrientation(180);
                    break;
            }
            camera.setParameters(parameters);
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
        if (!timelapseRunning) {
            startStopButton.setText("Stop");
            timelapseRunning = true;
            timerUpdateHandler.post(timerUpdateTask);
        } else {
            startStopButton.setText("Start");
            timelapseRunning = false;
            timerUpdateHandler.removeCallbacks(timerUpdateTask);
        }
    }

    private Runnable timerUpdateTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run=>time: " + currentTime);
            if (currentTime < SECONDS_BETWEEN_PHOTOS) {
                currentTime++;
            } else {
                if (camera != null) {
                    rotation = getWindowManager().getDefaultDisplay().getRotation();
                    Log.d(TAG, "surfaceCreated=>rotation: " + rotation);
                    switch (rotation) {
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
                    camera.takePicture(null, null, null, TimelapseSnapShot.this);
                }
                currentTime = 0;
            }

            timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
            countdownTextView.setText("" + currentTime);
        }
    };

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTake()...");
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

            Toast.makeText(this, "Saved JPEG!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "onPictureTaken=>File not found.", e);
        } catch (IOException e) {
            Log.e(TAG, "onPictureTaken=>Write data error.", e);
        }

        if (camera != null) {
            camera.startPreview();
        }
    }
}