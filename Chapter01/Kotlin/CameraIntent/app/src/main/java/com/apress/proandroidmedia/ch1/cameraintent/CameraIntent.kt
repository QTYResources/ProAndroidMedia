package com.apress.proandroidmedia.ch1.cameraintent

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView

class CameraIntent : AppCompatActivity() {

    private lateinit var imv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(i, CAMERA_RESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val bmp = data?.extras?.get("data") as Bitmap
            imv = findViewById(R.id.ReturnedImageView)
            imv.setImageBitmap(bmp)
        }
    }

    companion object {
        const val CAMERA_RESULT = 0
    }
}