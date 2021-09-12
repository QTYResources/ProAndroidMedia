package com.apress.proandroidmedia.ch1.mediastoregallery;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MediaStoreGallery extends AppCompatActivity {

    private static final String TAG = "MediaStoreGallery";

    private static final int DISPLAYWIDTH = 200;
    private static final int DISPLAYHEIGHT = 200;

    private TextView titleTextView;
    private ImageButton imageButton;

    private Cursor cursor;
    private Bitmap bmp;
    private String imageFilePath;
    private int fileColumn;
    private int titleColumn;
    private int displayColumn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleTextView = findViewById(R.id.TitleTextView);
        imageButton = findViewById(R.id.ImageButton);

        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME };
        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);

        fileColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
        displayColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

        if (cursor.moveToFirst()) {
            // titleTextView.setText(cursor.getString(titleColumn));
            titleTextView.setText(cursor.getString(displayColumn));

            imageFilePath = cursor.getString(fileColumn);
            bmp = getBitmap(imageFilePath);

            // Display it
            imageButton.setImageBitmap(bmp);
        }

        imageButton.setOnClickListener(v -> {
            if (cursor.moveToNext()) {
                // titleTextView.setText(cursor.getString(titleColumn));
                titleTextView.setText(cursor.getString(displayColumn));

                imageFilePath = cursor.getString(fileColumn);
                bmp = getBitmap(imageFilePath);

                // Display it
                imageButton.setImageBitmap(bmp);
            }
        });
    }

    private Bitmap getBitmap(String imageFilePath) {
        // Load up the image's dimensions not the image itself
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (double) DISPLAYHEIGHT);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (double) DISPLAYWIDTH);

        Log.v(TAG, "" + heightRatio);
        Log.v(TAG, "" + widthRatio);

        // If both of the ratios are greater than 1, one of the sides of
        // the image is greater than the screen
        if (heightRatio > 1 || widthRatio > 1) {
            bmpFactoryOptions.inSampleSize = Math.max(widthRatio, heightRatio);
        }

        // Decode it for real
        bmpFactoryOptions.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

        return bmp;
    }
}