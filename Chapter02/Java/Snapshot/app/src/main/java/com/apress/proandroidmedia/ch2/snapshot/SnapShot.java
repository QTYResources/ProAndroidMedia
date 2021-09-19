package com.apress.proandroidmedia.ch2.snapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class SnapShot extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback {

    private static final String TAG = "SnapShot";

    private SurfaceView cameraView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private int rotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.CameraView);
        surfaceHolder = cameraView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        cameraView.setFocusable(true);
        cameraView.setFocusableInTouchMode(true);
        cameraView.setClickable(true);

        cameraView.setOnClickListener(this);
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
            parameters.set("rotaion", rotation);
            parameters.setRotation(rotation);
            camera.setDisplayOrientation(rotation);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            // Effects are for Android Version 2.0 and higher
            List<String> colorEffects = parameters.getSupportedColorEffects();
            Iterator<String> cei = colorEffects.iterator();
            while (cei.hasNext()) {
                String currentEffect =cei.next();
                if (currentEffect.equals(Camera.Parameters.EFFECT_SOLARIZE)) {
                    parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                    break;
                }
            }
            // End Effects for Android Version 2.0 and higher

            camera.setParameters(parameters);
        } catch (IOException e) {
            Log.d(TAG, "surfaceCreated=>error: ", e);
            if (camera != null) {
                camera.release();
            }
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
        parameters.set("rotaion", rotation);
        parameters.setRotation(rotation);
        camera.setDisplayOrientation(rotation);
        camera.setParameters(parameters);
        camera.startPreview();
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
        if (camera != null) {
            rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.d(TAG, "onClick=>rotation: " + rotation);
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
            camera.takePicture(null, null, null, this);
        }
    }

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