package com.apress.proandroidmedia.ch1.cameraintent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

public class CameraIntent extends AppCompatActivity {

    private static final int CAMERA_RESULT = 0;

    private ImageView imv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, CAMERA_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bmp = (Bitmap) extras.get("data");
            imv = findViewById(R.id.ReturnedImageView);
            imv.setImageBitmap(bmp);
        }
    }
}