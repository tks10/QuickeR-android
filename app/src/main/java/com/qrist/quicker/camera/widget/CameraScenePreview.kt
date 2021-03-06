package com.qrist.quicker.camera.widget

import android.content.Context
import android.util.AttributeSet
import java.lang.Long.signum
import android.annotation.SuppressLint
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Size
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.qrist.quicker.utils.QRCodeDetector
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.NoSuchElementException
import kotlin.experimental.inv

class CameraScenePreview @JvmOverloads constructor(
    @get:JvmName("_context")
    val context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : TextureView(context, attrs, defaultStyle) {

    private lateinit var previewSize: Size
    private lateinit var videoSize: Size
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var previewRequest: CaptureRequest
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var threadPool: ExecutorService? = null
    private var cameraDevice: CameraDevice? = null
    private var ratioWidth = 0
    private var ratioHeight = 0
    private var counter = 0
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    var qrCodeCallback: (QRCodeValue) -> Unit = {}

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        counter++
        threadPool?.execute {
            val image = try {
                reader.acquireLatestImage() ?: return@execute
            } catch (exception: IllegalStateException) {
                Log.d(TAG, "$exception")
                return@execute
            }
            val firebaseImage = when (counter) {
                0 -> FirebaseVisionImage.fromMediaImage(image, FirebaseVisionImageMetadata.ROTATION_0)
                10 -> {
                    counter = -10
                    FirebaseVisionImage.fromByteArray(
                        getNegativeYUVByteArray(image),
                        FirebaseVisionImageMetadata.Builder().also {
                            it.setWidth(image.width)
                            it.setHeight(image.height)
                            it.setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                            it.setRotation(FirebaseVisionImageMetadata.ROTATION_0)
                        }.build()
                    )
                }
                else -> {
                    image.close()
                    return@execute
                }
            }
            QRCodeDetector.detect(firebaseImage, {
                it.forEach { barcode ->
                    Log.d(TAG, "detect barcode: ${barcode.rawValue}")
                    barcode.rawValue?.also { value ->
                        qrCodeCallback(QRCodeValue.create(value))
                    }
                }
            }, { exception ->
                Log.d(TAG, "error occurs: ${exception.stackTrace}")
            })
            image.close()
        }
    }

    private val cameraSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }
    }

    private fun getNegativeYUVByteArray(image: Image): ByteArray {
        val y = image.planes[0].buffer
        val buffer = ByteArray(y.capacity())
        y.get(buffer)
        for (i in 0 until buffer.size / 16) {
            val base = i * 16
            buffer[base] = buffer[base].inv()
            buffer[base + 1] = buffer[base + 1].inv()
            buffer[base + 2] = buffer[base + 2].inv()
            buffer[base + 3] = buffer[base + 3].inv()
            buffer[base + 4] = buffer[base + 4].inv()
            buffer[base + 5] = buffer[base + 5].inv()
            buffer[base + 6] = buffer[base + 6].inv()
            buffer[base + 7] = buffer[base + 7].inv()
            buffer[base + 8] = buffer[base + 8].inv()
            buffer[base + 9] = buffer[base + 9].inv()
            buffer[base + 10] = buffer[base + 10].inv()
            buffer[base + 11] = buffer[base + 11].inv()
            buffer[base + 12] = buffer[base + 12].inv()
            buffer[base + 13] = buffer[base + 13].inv()
            buffer[base + 14] = buffer[base + 14].inv()
            buffer[base + 15] = buffer[base + 15].inv()
        }

        return buffer
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {}

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            createCameraPreviewSession(camera)
            cameraDevice = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            onDisconnected(camera)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width > ((height * ratioWidth) / ratioHeight)) {
                setMeasuredDimension(width, (width * ratioHeight) / ratioWidth)
            } else {
                setMeasuredDimension((height * ratioWidth) / ratioHeight, height)
            }
        }
    }

    fun startCameraPreview() {
        if (cameraDevice != null) return
        startBackgroundThread()
        if (this.isAvailable) {
            openCamera()
        } else {
            surfaceTextureListener = cameraSurfaceTextureListener
        }
    }

    fun stopCameraPreview() {
        cameraDevice!!.close()
        captureSession.close()
        stopBackgroundThread()
        imageReader.close()
        cameraDevice = null
    }

    private fun startBackgroundThread() {
        threadPool = Executors.newFixedThreadPool(3)
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        threadPool!!.shutdown()
        while (!threadPool!!.isTerminated) {
        }
        threadPool = null
    }

    private fun setAspectRatio(width: Int, height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalStateException("size cannot be negative")
        }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList.first { cameraId ->
                cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }

            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP) ?:
                throw RuntimeException("Cannot get available preview/video sizes")
            // chose small video size to move smoothly
            videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder::class.java))
            // chose big preview size to see fancy video
            //previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
            //    width, height, choosePreviewSize(map.getOutputSizes(MediaRecorder::class.java)))
            previewSize = choosePreviewSize(map.getOutputSizes(MediaRecorder::class.java))
            setAspectRatio(previewSize.height, previewSize.width)
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
            Log.d("camera list", "${cameraManager.cameraIdList}")
        } catch (e: NoSuchElementException) {
            Toast.makeText(context, "Camera not found", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "${e.stackTrace}")
        }
    }

    private fun createCameraPreviewSession(camera: CameraDevice) {
        try {
            val surface = Surface(surfaceTexture)
            imageReader = ImageReader.newInstance(videoSize.width, videoSize.height,
                ImageFormat.YUV_420_888, 2)
            surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
            Log.d(TAG, "video size is ${videoSize.width}x${videoSize.height}")
            Log.d(TAG, "preview size is ${previewSize.width}x${previewSize.height}")
            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)
            previewRequestBuilder.addTarget(imageReader.surface)
            camera.createCaptureSession(
                listOf(surface, imageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        cameraDevice ?: return
                        captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            previewRequest = previewRequestBuilder.build()
                            captureSession.setRepeatingRequest(previewRequest,
                                captureCallback, backgroundHandler)
                        } catch (exception: Exception) {
                            Log.e("erfs", exception.toString())
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(context, "Failed opening camera", Toast.LENGTH_LONG).show()
                    }
                }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("erf", e.toString())
        }
    }

    /**
     * Given [choices] of [Size]s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal [Size], or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
            choices: Array<Size>,
            width: Int,
            height: Int,
            aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val w = aspectRatio.width
        val h = aspectRatio.height
        val bigEnough = choices.filter {
            it.height == it.width * h / w && it.width >= width && it.height >= height }

        // Pick the smallest of those, assuming we found any
        return if (bigEnough.isNotEmpty()) {
            Collections.min(bigEnough, CompareSizesByArea())
        } else {
            choices[0]
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private fun choosePreviewSize(choices: Array<Size>) = choices.firstOrNull {
        it.width == it.height * 4 / 3 && it.width <= 1080 } ?: choices[choices.size - 1]

    private fun chooseVideoSize(choices: Array<Size>) = choices.firstOrNull {
        it.width in 125..(if (Runtime.getRuntime().maxMemory() / 1024 / 1024 < 256) 380 else 730) && it.width == it.height * 4 / 3 } ?: choices[choices.size - 1]

    private class CompareSizesByArea : Comparator<Size> {

        // We cast here to ensure the multiplications won't overflow
        override fun compare(lhs: Size, rhs: Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }

    companion object {
        private const val TAG = "camerasceenpreview"
    }
}
