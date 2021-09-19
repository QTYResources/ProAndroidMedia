package com.apress.proandroidmedia.ch2.timersnapshot

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class TimerSnapShot : AppCompatActivity(), SurfaceHolder.Callback, View.OnClickListener,
    Camera.PictureCallback {

    private lateinit var cameraView: SurfaceView
    private lateinit var startButton: Button
    private lateinit var countdownTextView: TextView

    private val timerUpdateHandler = Handler()
    private var camera: Camera? = null

    private var currentTime = 10
    private var rotation = 0
    private var timerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.CameraView)
        cameraView.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        cameraView.holder.addCallback(this)

        countdownTextView = findViewById(R.id.CountDownTextView)
        startButton = findViewById(R.id.CountDownButton)
        startButton.setOnClickListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera = Camera.open()
        camera?.let {
            it.setPreviewDisplay(holder)
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> rotation = 90
                Surface.ROTATION_90 -> rotation = 0
                Surface.ROTATION_180 -> rotation = 270
                Surface.ROTATION_270 -> rotation = 180
            }
            val parameters = it.parameters
            parameters.set("rotation", rotation)
            parameters.setRotation(rotation)
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            it.setDisplayOrientation(rotation)
            it.parameters = parameters
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        camera?.let {
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> rotation = 90
                Surface.ROTATION_90 -> rotation = 0
                Surface.ROTATION_180 -> rotation = 270
                Surface.ROTATION_270 -> rotation = 180
            }
            val parameters = it.parameters
            parameters.set("rotation", rotation)
            parameters.setRotation(rotation)
            it.setDisplayOrientation(rotation)
            it.parameters = parameters
            it.startPreview()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.let {
            it.stopPreview()
            it.release()
        }
        camera = null
    }

    override fun onClick(v: View?) {
        if (!timerRunning) {
            timerRunning = true
            timerUpdateHandler.post(timerUpdateTask)
        }
    }

    private val timerUpdateTask = object : Runnable {
        override fun run() {
            if (currentTime > 1) {
                currentTime--
                timerUpdateHandler.postDelayed(this, 1000)
            } else {
                camera?.let {
                    when (windowManager.defaultDisplay.rotation) {
                        Surface.ROTATION_0 -> rotation = 90
                        Surface.ROTATION_90 -> rotation = 0
                        Surface.ROTATION_180 -> rotation = 270
                        Surface.ROTATION_270 -> rotation = 180
                    }
                    it.takePicture(null, null, null, this@TimerSnapShot)
                }
                timerRunning = false
                currentTime = 10
            }
            countdownTextView.text = "$currentTime"
        }
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        val imageFileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        val imageFileOS = imageFileUri?.let { contentResolver.openOutputStream(it) }
        var bmp = data?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        val matrix = Matrix()
        matrix.setRotate(rotation.toFloat())
        bmp = bmp?.let { Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true) }
        bmp?.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOS)
        imageFileOS?.flush()
        imageFileOS?.close()
        Toast.makeText(this, "Saved JPEG!", Toast.LENGTH_SHORT).show()
        camera?.startPreview()
    }

    companion object {
        const val TAG = "TimerSnapShot"
    }

}