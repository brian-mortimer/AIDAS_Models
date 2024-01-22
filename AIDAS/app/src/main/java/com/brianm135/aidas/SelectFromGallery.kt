package com.brianm135.aidas

import ObjectDetectionHelper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import org.tensorflow.lite.task.gms.vision.detector.Detection
import java.io.FileDescriptor
import java.io.IOException
import java.util.LinkedList

class SelectFromGallery : AppCompatActivity(), ObjectDetectionHelper.DetectorListener {

    private var selectedModel = 0

    private lateinit var imageView: ImageView

    private val TAG = "DETECTION"

    private lateinit var objectDetectionHelper: ObjectDetectionHelper
    private lateinit var overlayView: OverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_from_gallery)

        val backBtn: View = findViewById(R.id.backBtn)
        imageView = findViewById(R.id.imageView)
        overlayView = findViewById(R.id.overlayView)
        val selectAnotherImageBtn: View = findViewById(R.id.button2)


        // Get Selected Model
        selectedModel = intent.getSerializableExtra("model") as Int

        // Create Object Detection helper
        objectDetectionHelper = ObjectDetectionHelper(currentModel = selectedModel, context = this, objectDetectorListener = this)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {uri ->
            if(uri != null) {
                Log.d("MediaPicker", "Selected URI $uri")
                val inputImage = uriToBitmap(uri)
                imageView.setImageBitmap(inputImage)
                if (inputImage != null) {
                    objectDetectionHelper.detect(inputImage, 0)
                }
            }
            else {
                Log.d("MediaPicker", "No Media Selected")
            }
        }


        // Add back button functionality
        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Add selectAnother Image functionality
        selectAnotherImageBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }


        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }


    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    private fun debugPrint(results : List<Detection>, inferenceTime : Long) {
        Log.d(TAG, "Inference Time $inferenceTime")
        for ((i, obj) in results.withIndex()) {
            val box = obj.boundingBox

            Log.d(TAG, "Detected object: ${i} ")
            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            for ((j, category) in obj.categories.withIndex()) {
                Log.d(TAG, "    Label $j: ${category.label}")
                val confidence: Int = category.score.times(100).toInt()
                Log.d(TAG, "    Confidence: ${confidence}%")
            }
        }
    }

    override fun onInitialized() {
        objectDetectionHelper.setupObjectDetector()
    }

    override fun onError(error: String) {
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        if (results != null) {
            debugPrint(results, inferenceTime)

            overlayView.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth)

            overlayView.invalidate()
        }
    }

}