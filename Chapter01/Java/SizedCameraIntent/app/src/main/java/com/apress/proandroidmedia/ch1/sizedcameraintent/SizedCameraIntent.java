package com.apress.proandroidmedia.ch1.sizedcameraintent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowMetrics;
import android.widget.ImageView;

import java.io.File;

public class SizedCameraIntent extends AppCompatActivity {

    private static final String TAG = "SizedCameraIntent";

    private static final int CAMERA_RESULT = 0;

    private ImageView imv;
    private String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myfavoritepicture.jpg";
        File imageFile = new File(imageFilePath);
        Uri imageFileUri = Uri.fromFile(imageFile);
        if (Build.VERSION.SDK_INT >= 24) {
            imageFileUri = FileProvider.getUriForFile(this, "com.apress.proandroidmedia.ch1.sizedcameraintent.FileProvider", imageFile);
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        startActivityForResult(i, CAMERA_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Get a reference to the ImageView
            imv = findViewById(R.id.ReturnedImageView);

            Display currentDisplay = getWindowManager().getDefaultDisplay();
            int dw = currentDisplay.getWidth();
            int dh = currentDisplay.getHeight();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics wm = getWindowManager().getCurrentWindowMetrics();
                dw = wm.getBounds().width();
                dh = wm.getBounds().height();
            }

            // Load up the image's dimensions not the image itself
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

            int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (double) dh);
            int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (double) dw);

            Log.d(TAG, "" + heightRatio);
            Log.d(TAG, "" + widthRatio);

            // If both of the ratios are greater than 1,
            // one of the sides of the image is greater than the screen
            if (heightRatio > 1 || widthRatio > 1) {
                bmpFactoryOptions.inSampleSize = Math.min(widthRatio, heightRatio);
            }

            // Decode it for real
            bmpFactoryOptions.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

            // Display it
            imv.setImageBitmap(bmp);
        }
    }
}