package com.apress.proandroidmedia.ch1.fileprovidercameraintent

import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

class FileProviderCameraIntent : AppCompatActivity() {

    private var imageFileUri: Uri? = null
    private lateinit var picturePath: String

    // User interface elements, specified in res/layout/main.xml
    private lateinit var returnedImageView: ImageView
    private lateinit var takePictureButton: Button
    private lateinit var saveDataButton: Button
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view to be what is defined in teh res/layout/main.xml
        // file
        setContentView(R.layout.activity_main)

        // Get references to UI elements
        returnedImageView = findViewById(R.id.ReturnedImageView)
        takePictureButton = findViewById(R.id.TakePictureButton)
        saveDataButton = findViewById(R.id.SaveDataButton)
        titleTextView = findViewById(R.id.TitleTextView)
        descriptionTextView = findViewById(R.id.DescriptionTextView)
        titleEditText = findViewById(R.id.TitleEditText)
        descriptionEditText = findViewById(R.id.DescriptionEditText)

        // Set all except takePictureButton to not be visible initially
        // View.GONE is invisible and doesn't take up space in the layout
        returnedImageView.visibility = View.GONE
        saveDataButton.visibility = View.GONE
        titleTextView.visibility = View.GONE
        descriptionTextView.visibility = View.GONE
        titleEditText.visibility = View.GONE
        descriptionEditText.visibility = View.GONE

        // When the Take Picture Button is clicked
        takePictureButton.setOnClickListener(View.OnClickListener {
            // Add a new record without the bitmap
            // returns the URI of the new record
            picturePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + File.separator + "test.jpg"
            if (Build.VERSION.SDK_INT >= 24) {
                imageFileUri = FileProvider.getUriForFile(this@FileProviderCameraIntent, "com.apress.proandroidmedia.ch1.fileprovidercameraintent.FileProvider", File(picturePath))
            } else {
                imageFileUri = Uri.fromFile(File(picturePath))
            }

            // Start the Camera App
            val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri)
            startActivityForResult(i, CAMERA_RESULT)
        })

        saveDataButton.setOnClickListener(View.OnClickListener {
            // Update the MediaStore record with Title and Description
            val contentValues = ContentValues(3)
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, titleEditText.text.toString())
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, descriptionEditText.text.toString())
            imageFileUri?.let { uri -> contentResolver.update(uri, contentValues, null, null) }

            // Tell the user
            val bread = Toast.makeText(this@FileProviderCameraIntent, "Record Updated", Toast.LENGTH_SHORT)
            bread.show()

            // Go back to the initial state, set Take Picture Button Visible
            // hide other UI elements
            takePictureButton.visibility = View.VISIBLE

            returnedImageView.visibility = View.GONE
            saveDataButton.visibility = View.GONE
            titleTextView.visibility = View.GONE
            descriptionTextView.visibility = View.GONE
            titleEditText.visibility = View.GONE
            descriptionEditText.visibility = View.GONE
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            // The Camera App has returned

            // Hide the Take Picture Button
            takePictureButton.visibility = View.GONE

            // Show the other UI Elements
            returnedImageView.visibility = View.VISIBLE
            saveDataButton.visibility = View.VISIBLE
            titleTextView.visibility = View.VISIBLE
            descriptionTextView.visibility = View.VISIBLE
            titleEditText.visibility = View.VISIBLE
            descriptionEditText.visibility = View.VISIBLE

            // Scale the image
            val dw = 200;   // Make it at most 200 pixels wide
            val dh = 200;   // Make it at most 200 pixels tall

            // Load up the image's dimensions not the image itself
            val bmpFactoryOptions = BitmapFactory.Options()
            bmpFactoryOptions.inJustDecodeBounds = true
            imageFileUri?.let {
                var bmp = BitmapFactory.decodeStream(FileInputStream(picturePath), null, bmpFactoryOptions)

                val heightRatio = Math.ceil(bmpFactoryOptions.outHeight / dh.toDouble()).toInt()
                val widthRatio = Math.ceil(bmpFactoryOptions.outWidth / dw.toDouble()).toInt()

                Log.v(TAG, "$heightRatio")
                Log.v(TAG, "$widthRatio")

                // If both of the ratios are greater than 1,
                // one of the sides of the image is greater than the screen
                if (heightRatio > 1 && widthRatio > 1) {
                    if (heightRatio > widthRatio) {
                        // Height ratio is larger, scale according to it
                        bmpFactoryOptions.inSampleSize = heightRatio
                    } else {
                        // Width ratio is larger, scale according to it
                        bmpFactoryOptions.inSampleSize = widthRatio
                    }
                }

                // Decode it for real
                bmpFactoryOptions.inJustDecodeBounds = false
                bmp = BitmapFactory.decodeStream(FileInputStream(picturePath), null, bmpFactoryOptions)

                // Display it
                returnedImageView.setImageBitmap(bmp)
            }
        }
    }

    companion object {
        const val TAG = "MediaStoreCameraIntent"
        const val CAMERA_RESULT = 0
    }

}