import android.content.Context
import android.graphics.Bitmap
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ObjectDetectionHelper (
    var threshold: Float = 0.5f,
    var numThreads: Int = 2,
    var maxResults: Int = 6,
    var currentDelegate: Int = 0,
    var currentModel: Int = 0,
    val context: Context,
    val objectDetectorListener: DetectorListener
) {
    private val TAG = "ObjectDetectionHelper"

    private var objectDetector: ObjectDetector? = null
    private var gpuSupported = false

    init {

        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable: Boolean ->
            val optionsBuilder =
                TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            objectDetectorListener.onInitialized()
        }.addOnFailureListener{
            objectDetectorListener.onError("TfLiteVision failed to initialize: "
                    + it.message)
        }
    }

    fun setupObjectDetector() {
        if (!TfLiteVision.isInitialized()) {
            Log.e(TAG, "setupObjectDetector: TfLiteVision is not initialized yet")
            return
        }

        // Create the base options for the detector using specifies max results and score threshold
        val optionsBuilder =
            ObjectDetector.ObjectDetectorOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)

        // Set general detection options, including number of used threads
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)

        // Use the specified hardware for running the model. Default to CPU
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (gpuSupported) {
                    baseOptionsBuilder.useGpu()
                } else {
                    objectDetectorListener.onError("GPU is not supported on this device")
                }
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        val modelName = when(currentModel) {
            MODEL_MOBILENETV1 -> "mobilenetv1.tflite"
            MODEL_TRAFFIC_LIGHTV1 -> "Road_Sign_Detection_v1.tflite" // TODO: FILL THIS FILE NAME
            else -> "mobilenetv1.tflite"
        }

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build())
        } catch (e: Exception) {
            objectDetectorListener.onError(
                "Object detector failed to initialize. See error logs for details"
            )
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
        }
    }

//    fun detect(image: Bitmap, imageRotation: Int) {
//        if (!TfLiteVision.isInitialized()) {
//            Log.e(TAG, "detect: TfLiteVision is not initialized yet")
//            return
//        }
//
//        if (objectDetector == null) {
//            setupObjectDetector()
//        }
//        var inferenceTime = SystemClock.uptimeMillis()
//
//        // Create preprocessor for the image.
//        val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()
//
//        // Preprocess the image and convert it into a TensorImage for detection.
////        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))
//
//        // Preprocess the image and convert it into a TensorImage for detection.
//        val tensorImage = TensorImage.fromBitmap(image)
//        tensorImage.load(image)
//        imageProcessor.process(tensorImage)
//
//        // Manually normalize the input tensor
//        tensorImage.buffer.rewind()
//        val mean = floatArrayOf(0.0f, 0.0f, 0.0f)
//        val std = floatArrayOf(1.0f, 1.0f, 1.0f)
//        for (i in 0 until 3) {
//            for (pixel in 0 until tensorImage.width * tensorImage.height) {
//                val value = (tensorImage.buffer.getFloat() - mean[i]) / std[i]
//                tensorImage.buffer.putFloat(value)
//            }
//        }
//        val results = objectDetector?.detect(tensorImage)
//        Log.d("TEST", results.toString())
//
//        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
//        objectDetectorListener.onResults(
//            results,
//            inferenceTime,
//            tensorImage.height,
//            tensorImage.width)
//    }
fun detect(image: Bitmap, imageRotation: Int) {
    if (!TfLiteVision.isInitialized()) {
        Log.e(TAG, "detect: TfLiteVision is not initialized yet")
        return
    }


    var inferenceTime = SystemClock.uptimeMillis()

    // Create preprocessor for the image.
    val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()

    // Preprocess the image and convert it into a TensorImage for detection.
//        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

    // Preprocess the image and convert it into a TensorImage for detection.
    val tensorImage = TensorImage.fromBitmap(image)
    tensorImage.load(image)
    imageProcessor.process(tensorImage)

    // Manually normalize the input tensor
    tensorImage.buffer.rewind()
    val mean = floatArrayOf(0.0f, 0.0f, 0.0f)
    val std = floatArrayOf(1.0f, 1.0f, 1.0f)
    for (i in 0 until 3) {
        for (pixel in 0 until tensorImage.width * tensorImage.height) {
            val value = (tensorImage.buffer.getFloat() - mean[i]) / std[i]
            tensorImage.buffer.putFloat(value)
        }
    }


    val modelName = when(currentModel) {
        MODEL_MOBILENETV1 -> "mobilenetv1.tflite"
        MODEL_TRAFFIC_LIGHTV1 -> "Road_Sign_Detection_v1.tflite" // TODO: FILL THIS FILE NAME
        else -> "mobilenetv1.tflite"
    }
    val modelFile: File = loadModelFromAssets(this.context, modelName)
    val options: InterpreterApi.Options = InterpreterApi.Options()
    val interpreter = InterpreterApi.create(modelFile, options)

    Log.d("TEST", "shape ${interpreter.getOutputTensor(0)}")


    val outputBuffer: ByteBuffer = ByteBuffer.allocateDirect(interpreter.getOutputTensor(0).numBytes())
    outputBuffer.order(ByteOrder.nativeOrder())

    interpreter.run(tensorImage, outputBuffer )


    inferenceTime = SystemClock.uptimeMillis() - inferenceTime
}

    fun loadModelFromAssets(context: Context, path: String): File {
        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open(path)
        val outputFile = File(context.cacheDir, path)

        FileOutputStream(outputFile).use { outputStream ->
            val buffer = ByteArray(4*1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
        }
        return outputFile
    }


    interface DetectorListener {
        fun onInitialized()
        fun onError(error: String)
        fun onResults(
            results: MutableList<Detection>?,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int
        )
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
        const val MODEL_MOBILENETV1 = 0
        const val MODEL_TRAFFIC_LIGHTV1 = 1
    }
}