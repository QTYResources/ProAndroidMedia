package com.apress.proandroidmedia.ch1.mediastoregallery

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

class MediaStoreGallery : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var imageButton: ImageButton

    private var cursor: Cursor? = null
    private var bmp: Bitmap? = null
    private var imageFilePath: String? = null
    private var fileColumn: Int = 0
    private var titleColumn: Int = 0
    private var displayColumn: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleTextView = findViewById(R.id.TitleTextView)
        imageButton = findViewById(R.id.ImageButton)

        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME)
        cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, null)

        cursor?.let {
            fileColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            titleColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
            displayColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            if (it.moveToFirst()) {
                // titleTextView.setText(cursor.getString(titleColumn));
                titleTextView.setText(it.getString(displayColumn))

                imageFilePath = it.getString(fileColumn)
                bmp = getBitmap(imageFilePath)

                // Display it
                imageButton.setImageBitmap(bmp)
            }
        }

        imageButton.setOnClickListener(View.OnClickListener {
            cursor?.let {
                if (it.moveToNext()) {
                    // titleTextView.setText(cursor.getString(titleColumn));
                    titleTextView.setText(it.getString(displayColumn))

                    imageFilePath = it.getString(fileColumn)
                    bmp = getBitmap(imageFilePath)

                    // Display it
                    imageButton.setImageBitmap(bmp)
                }
            }
        })
    }

    private fun getBitmap(imageFilePath: String?): Bitmap {
        // Load up the image's dimensions not the image itself
        val bmpFactoryOptions = BitmapFactory.Options()
        bmpFactoryOptions.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions)

        val heightRatio = Math.ceil(bmpFactoryOptions.outHeight / DISPLAYHEIGHT.toDouble()).toInt()
        val widthRatio = Math.ceil(bmpFactoryOptions.outWidth / DISPLAYWIDTH.toDouble()).toInt()

        // If both of the ratios are greater than 1, one of the sides of
        // the image is greater than the screen
        if (heightRatio > 1 || widthRatio > 1) {
            bmpFactoryOptions.inSampleSize = Math.max(heightRatio, widthRatio)
        }

        // Decode it for real
        bmpFactoryOptions.inJustDecodeBounds = false
        bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions)

        return bmp
    }

    companion object {
        const val DISPLAYWIDTH = 200
        const val DISPLAYHEIGHT = 200
    }
}