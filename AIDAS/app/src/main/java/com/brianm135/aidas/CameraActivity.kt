package com.brianm135.aidas

import ObjectDetectionHelper
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.brianm135.aidas.databinding.ActivityCameraBinding
import org.tensorflow.lite.task.gms.vision.detector.Detection
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), ObjectDetectionHelper.DetectorListener {
    private var currModel: Int = 0
    private lateinit var viewBinding: ActivityCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var _objectDetectionHelper: ObjectDetectionHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        currModel = intent.getSerializableExtra("model") as Int

//        // Update model text in activity
//        val modelTxt: TextView = findViewById(R.id.modelTxt)
//        modelTxt.text = model.name

        _objectDetectionHelper = ObjectDetectionHelper(
            currentModel = currModel,
            context = this,
            objectDetectorListener = this)

        // Add back button functionality
        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        viewBinding.overlayView.clear()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
            Log.println(Log.INFO, "Permissions", "Permissions Granted")
        } else {
            requestPermissions()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Function to initialise and start the camera.
     */
    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider) }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(viewBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }
                        // Detect Objects in image
                        detectObjects(image)
                    }
                }
            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            }catch (exc: Exception){
                Log.e("CAMERA", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
        Log.i("CAMERA", "Camera Successfully Started.")
    }

    /**
    * Detect Objects in image
     */
    private fun detectObjects(image: ImageProxy){
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)}
        val imageRotation = image.imageInfo.rotationDegrees
        _objectDetectionHelper.detect(bitmapBuffer, imageRotation)
    }

    /**
    * Request Permissions.
     */
    private fun requestPermissions(){
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    /**
     * Checks the Permissions have been granted.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    companion object{
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            // Add WRITE_EXTERNAL_STORAGE permission for older Android versions
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        viewBinding.overlayView.setResults(
            results ?: LinkedList<Detection>(),
            imageHeight,
            imageWidth)

        viewBinding.overlayView.invalidate()
//        Log.i("Result", "Inference Time: $inferenceTime")
    }

    override fun onError(error: String) {

    }

    override fun onInitialized() {
        _objectDetectionHelper.setupObjectDetector()
    }
}