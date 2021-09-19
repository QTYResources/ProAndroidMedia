package com.apress.proandroidmedia.ch2.timelapsesnapshot

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

class TimelapseSnapShot : AppCompatActivity(), SurfaceHolder.Callback, View.OnClickListener,
    Camera.PictureCallback {

    private lateinit var cameraView: SurfaceView
    private lateinit var startStopButton: Button
    private lateinit var countdownTextView: TextView
    private val timerUpdateHandler: Handler = Handler()
    private var camera: Camera? = null

    private var timelapseRunning = false
    private var currentTime = 0
    private var rotation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.CameraView)
        cameraView.holder.addCallback(this)
        countdownTextView = findViewById(R.id.CountDownTextView)
        startStopButton = findViewById(R.id.CountDownButton)
        startStopButton.setOnClickListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera = Camera.open()
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
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            it.parameters = parameters
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
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
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun onClick(v: View?) {
        if (!timelapseRunning) {
            startStopButton.text = "Stop"
            timelapseRunning = true
            timerUpdateHandler.post(timerUpdateTask)
        } else {
            startStopButton.text = "Start"
            timelapseRunning = false
            timerUpdateHandler.removeCallbacks(timerUpdateTask)
        }
    }

    private val timerUpdateTask = object : Runnable {
        override fun run() {
            if (currentTime < SECONDS_BETWEEN_PHOTOS) {
                currentTime++
            } else {
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
                    it.takePicture(null, null, null, this@TimelapseSnapShot)
                }
                currentTime = 0
            }

            timerUpdateHandler.postDelayed(this, 1000)
            countdownTextView.text = "$currentTime"
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
        const val TAG = "TimelapseSnapShot"
        const val SECONDS_BETWEEN_PHOTOS = 10
    }

}