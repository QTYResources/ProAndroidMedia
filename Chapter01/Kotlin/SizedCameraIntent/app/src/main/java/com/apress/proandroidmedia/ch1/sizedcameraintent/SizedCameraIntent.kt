package com.apress.proandroidmedia.ch1.sizedcameraintent

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.ceil
import kotlin.math.min

class SizedCameraIntent : AppCompatActivity() {

    private lateinit var imv: ImageView
    private lateinit var imageFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageFilePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "myfavoritepicture.jpg"
        val imageFile = File(imageFilePath)
        var imageFileUri = Uri.fromFile(imageFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageFileUri = FileProvider.getUriForFile(this, "com.apress.proandroidmedia.ch1.sizedcameraintent.FileProvider", imageFile)
        }

        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        i.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri)
        startActivityForResult(i, CAMERA_RESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            // Get a reference to the ImageView
            imv = findViewById(R.id.ReturnedImageView)

            var dw = windowManager.defaultDisplay.width
            var dh = windowManager.defaultDisplay.height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dw = windowManager.currentWindowMetrics.bounds.width()
                dh = windowManager.currentWindowMetrics.bounds.height()
            }

            // Load up the image's dimensions not the image itself
            val bmpFactoryOptions = BitmapFactory.Options()
            bmpFactoryOptions.inJustDecodeBounds = true
            var bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions)

            val heightRatio = ceil(bmpFactoryOptions.outHeight / dh.toDouble()).toInt()
            val widthRatio = ceil(bmpFactoryOptions.outWidth / dw.toDouble()).toInt()

            Log.v(TAG, "$heightRatio")
            Log.v(TAG, "$widthRatio")

            // If both of the ratios are greater than 1,
            // one of the sides of the image is greater than the screen
            if (heightRatio > 1 || widthRatio > 1) {
                bmpFactoryOptions.inSampleSize = min(widthRatio, heightRatio)
            }

            // Decode it for real
            bmpFactoryOptions.inJustDecodeBounds = false
            bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions)

            // Display it
            imv.setImageBitmap(bmp)
        }
    }

    companion object {
        const val TAG = "SizedCameraIntent"
        const val CAMERA_RESULT = 0
    }
}