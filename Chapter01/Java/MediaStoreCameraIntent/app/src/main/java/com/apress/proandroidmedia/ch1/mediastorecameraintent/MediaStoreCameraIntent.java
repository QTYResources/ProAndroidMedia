package com.apress.proandroidmedia.ch1.mediastorecameraintent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

public class MediaStoreCameraIntent extends AppCompatActivity {

    private static final String TAG = "MediaStoreCameraIntent";

    private static final int CAMERA_RESULT = 0;

    private Uri imageFileUri;

    // User interface elements, specified in res/layout/main.xml
    private ImageView returnedImageView;
    private Button takePictureButton;
    private Button saveDataButton;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private EditText titleEditText;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to be what is defined in the res/layout/main.xml
        // file
        setContentView(R.layout.activity_main);

        // Get references to UI elements
        returnedImageView = findViewById(R.id.ReturnedImageView);
        takePictureButton = findViewById(R.id.TakePictureButton);
        saveDataButton = findViewById(R.id.SaveDataButton);
        titleTextView = findViewById(R.id.TitleTextView);
        descriptionTextView = findViewById(R.id.DescriptionTextView);
        titleEditText = findViewById(R.id.TitleEditText);
        descriptionEditText = findViewById(R.id.DescriptionEditText);

        // Set all except takePictureButton to not be visible initially
        // View.GONE is invisible and doesn't take up space in the layout
        returnedImageView.setVisibility(View.GONE);
        saveDataButton.setVisibility(View.GONE);
        titleTextView.setVisibility(View.GONE);
        descriptionTextView.setVisibility(View.GONE);
        titleEditText.setVisibility(View.GONE);
        descriptionEditText.setVisibility(View.GONE);

        // When the Take Picture Button is clicked
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a new record without the bitmap
                // returns the URI of the new record
                imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues());

                // Start the Camera App
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                startActivityForResult(i, CAMERA_RESULT);
            }
        });

        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the MediaStore record with Title and Description
                ContentValues contentValues = new ContentValues(3);
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, titleEditText.getText().toString());
                contentValues.put(MediaStore.Images.Media.DESCRIPTION, descriptionEditText.getText().toString());
                getContentResolver().update(imageFileUri, contentValues, null, null);

                // Tell the user
                Toast.makeText(MediaStoreCameraIntent.this, "Record Updated", Toast.LENGTH_SHORT).show();

                // Go back to the initial state, set Take Picture Button Visible
                // hide other UI elements
                takePictureButton.setVisibility(View.VISIBLE);

                returnedImageView.setVisibility(View.GONE);
                saveDataButton.setVisibility(View.GONE);
                titleTextView.setVisibility(View.GONE);
                descriptionTextView.setVisibility(View.GONE);
                titleEditText.setVisibility(View.GONE);
                descriptionEditText.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // The Camera App has returned

            // Hide the Take Picture Button
            takePictureButton.setVisibility(View.GONE);

            returnedImageView.setVisibility(View.VISIBLE);
            saveDataButton.setVisibility(View.VISIBLE);
            titleTextView.setVisibility(View.VISIBLE);
            descriptionTextView.setVisibility(View.VISIBLE);
            titleEditText.setVisibility(View.VISIBLE);
            descriptionEditText.setVisibility(View.VISIBLE);

            // Scale the image
            int dw = 200;   // Make it at most 200 pixels wide
            int dh = 200;   // Make it at most 200 pixels tall

            try {
                // Load up the image's dimensions not the image itself
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri),
                        null, bmpFactoryOptions);

                int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (double) dh);
                int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (double) dw);

                Log.v(TAG, "" + heightRatio);
                Log.v(TAG, "" + widthRatio);

                // If both of the ratios are greater than 1,
                // one of the sides of the image is greater than the screen
                if (heightRatio > 1 && widthRatio > 1) {
                    if (heightRatio > widthRatio) {
                        // Height ratio is larger, scale according to it
                        bmpFactoryOptions.inSampleSize = heightRatio;
                    } else {
                        // Width ratio is larger, scale according to it
                        bmpFactoryOptions.inSampleSize = widthRatio;
                    }
                }

                // Decode it for real
                bmpFactoryOptions.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri),
                        null, bmpFactoryOptions);

                // Display it
                returnedImageView.setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "error: ", e);
            }
        }
    }
}