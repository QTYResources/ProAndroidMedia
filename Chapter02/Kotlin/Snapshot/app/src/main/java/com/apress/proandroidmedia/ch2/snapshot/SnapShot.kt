package com.apress.proandroidmedia.ch2.snapshot

import android.content.ContentValues
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast

class SnapShot : AppCompatActivity(), SurfaceHolder.Callback, View.OnClickListener,
    Camera.PictureCallback {

    private lateinit var cameraView: SurfaceView
    private var camera: Camera? = null

    private var rotation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.CameraView)
        cameraView.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        cameraView.holder.addCallback(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            cameraView.isFocusable = true
        } else {
            cameraView.focusable = View.FOCUSABLE
        }
        cameraView.isFocusableInTouchMode = true
        cameraView.isClickable = true

        cameraView.setOnClickListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera = Camera.open(0)
        Log.d(TAG, "surfaceCreated=>camera: $camera")
        camera?.let {
            Log.d(TAG, "surfaceCreated=>setPreviewDisplay")
            val parameters = it.parameters
            it.setPreviewDisplay(holder)
            var rotation = 0
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> {
                    rotation = 90
                }
                Surface.ROTATION_180 -> {
                    rotation = 270
                }
                Surface.ROTATION_270 -> {
                    rotation = 180
                }
                Surface.ROTATION_90 -> {
                    rotation = 0
                }
            }
            parameters.set("rotation", rotation)
            parameters.setRotation(rotation)
            it.setDisplayOrientation(rotation)

            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

            // Effects are for Android Version 2.0 and higher
            val colorEffects = parameters.supportedColorEffects
            val iterable = colorEffects.iterator()
            while (iterable.hasNext()) {
                val currentEffect = iterable.next()
                if (currentEffect.equals(Camera.Parameters.EFFECT_SOLARIZE)) {
                    parameters.colorEffect = Camera.Parameters.EFFECT_SOLARIZE
                    break
                }
            }
            // End Effects for Android Version 2.0 and highter

            it.parameters = parameters
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged...")
        camera?.let {
            it.setPreviewDisplay(holder)
            val parameters = it.parameters
            var rotation = 0
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> {
                    rotation = 90
                }
                Surface.ROTATION_180 -> {
                    rotation = 270
                }
                Surface.ROTATION_270 -> {
                    rotation = 180
                }
                Surface.ROTATION_90 -> {
                    rotation = 0
                }
            }
            parameters.set("rotation", rotation)
            parameters.setRotation(rotation)
            it.setDisplayOrientation(rotation)
            it.parameters = parameters
            it.startPreview()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed...")
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun onClick(v: View?) {
        camera?.let {
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> {
                    rotation = 90
                }
                Surface.ROTATION_180 -> {
                    rotation = 270
                }
                Surface.ROTATION_270 -> {
                    rotation = 180
                }
                Surface.ROTATION_90 -> {
                    rotation = 0
                }
            }
            it.takePicture(null, null, null, this)
        }
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        data?.let { d ->
            val imageFileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
            val imageFileOS = imageFileUri?.let { contentResolver.openOutputStream(it) }
            var bmp = BitmapFactory.decodeByteArray(d, 0, d.size)
            val matrix = Matrix()
            matrix.setRotate(rotation.toFloat())
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS)
            imageFileOS?.flush()
            imageFileOS?.close()
            Toast.makeText(this, "Saved JPEG!", Toast.LENGTH_SHORT).show()
        }
        camera?.startPreview()
    }

    companion object {
        const val TAG = "SnapShot"
    }
}