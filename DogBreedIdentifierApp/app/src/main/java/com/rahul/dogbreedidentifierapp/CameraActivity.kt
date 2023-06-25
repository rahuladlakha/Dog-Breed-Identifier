package com.rahul.dogbreedidentifierapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rahul.dogbreedidentifierapp.databinding.ActivityCameraBinding
import kotlinx.android.synthetic.main.activity_camera.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    companion object {
        fun getResult(bitmap : Bitmap, context : Context){
//            val dim = Math.min(bitmap.width, bitmap.height)
//            val bmap = ThumbnailUtils.extractThumbnail(bitmap, dim, dim)
            val result = ImageClassifier.classifyImage(bitmap, context)
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("result", result)
            context.startActivity(intent)
        }
    }
    //A better practice is to use arrays like this to get all permissios all at once.
    private val requiredPermissions = arrayOf("android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE","android.permission.RECORD_AUDIO")
    private lateinit var viewBinding: ActivityCameraBinding //the type is ActivityCameraBinding for the name of this file is CameraActiity

    private var imageCapture: ImageCapture? = null
    // This variable will help in flipping of camera and keeping track of wheher front or back camera is being used.
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    private lateinit var cameraExecutor: ExecutorService


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (requestCode == 5){
                val uri = data?.data
                if (uri == null) {
                    Toast.makeText(this, "Null uri", Toast.LENGTH_SHORT).show()
                    return
                }
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                CameraActivity.getResult(bitmap, this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        ImageClassifier.setup(this)
        //type is ActivityCameraBinding for CameraActivity class
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        supportActionBar?.hide()
        cameraExecutor = Executors.newSingleThreadExecutor()
        captureButton.setOnClickListener{  takePhoto() } //a second call to the captureVideo method pauses and saves the recording

        fab_rotate_camera.setOnClickListener{
            lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        fab_gallery.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 5)
        }

        for (per in requiredPermissions){
            if (ContextCompat.checkSelfPermission(this, per) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, requiredPermissions, 1)
            }
        }
        startCamera()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            // val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA //This code is replaced by lensfacing variable to make it easier to flip the camera.

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                //Without the imageCapture, YOu won't be able to click a image
                imageCapture = ImageCapture.Builder().build()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle( //You may keep any one of imageCapture or videoCapture as per requirement.
                    this, lensFacing, preview, imageCapture)

            } catch(exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat("ddMMyyyy-HH-ss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity,"error",Toast.LENGTH_SHORT).show()
                    // Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"

                    val uri = output.savedUri
                    var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    CameraActivity.getResult(bitmap, this@CameraActivity)
                    // Log.d(TAG, msg)
                }
            }
        )
    }
}