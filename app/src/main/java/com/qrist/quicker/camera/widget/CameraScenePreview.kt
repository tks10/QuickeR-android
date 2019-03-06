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
import android.os.Environment
import android.util.Size
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.qrist.quicker.BuildConfig
import com.qrist.quicker.utils.QRCodeDetector
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.*
import kotlin.NoSuchElementException
import com.qrist.quicker.utils.saveImage

class CameraScenePreview : TextureView {

    private lateinit var previewSize: Size
    private lateinit var videoSize: Size
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var previewRequest: CaptureRequest
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var cameraDevice: CameraDevice? = null
    private var ratioWidth = 0
    private var ratioHeight = 0
    private var counter = 0
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    var qrCodeCallback: (String?) -> Unit = {}

    private val rotation: Int
        get() = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)?.also { sensorOrientation ->
                when ((sensorOrientation + 270) % 360) {
                    0 -> FirebaseVisionImageMetadata.ROTATION_0
                    90 -> FirebaseVisionImageMetadata.ROTATION_90
                    180 -> FirebaseVisionImageMetadata.ROTATION_180
                    270 -> FirebaseVisionImageMetadata.ROTATION_270
                    else -> FirebaseVisionImageMetadata.ROTATION_0
                }
            } ?: FirebaseVisionImageMetadata.ROTATION_0

    private val onImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener
            counter++
            if (counter == 10 || counter == 20) {
                val firebaseImage = FirebaseVisionImage.fromMediaImage(image, FirebaseVisionImageMetadata.ROTATION_0)
                QRCodeDetector.detect(firebaseImage, {
                    Log.d(TAG, "barcodes: ${it.size}")
                    it.forEach { barcode ->
                        Log.d(TAG, "${barcode.rawValue}")
                        qrCodeCallback(barcode.rawValue)
                    }
                }, { exception ->
                    Log.d(TAG, "error occurs: ${exception.stackTrace}")
                })
            } else if (counter == 30) {
                yuvToBitmap(image)?.also { bitmap ->
                    QRCodeDetector.detectOnNegativeImage(bitmap, {
                        Log.d(TAG, "negative barcodes: ${it.size}")
                        it.forEach { barcode ->
                            Log.d(TAG, "${barcode.rawValue}")
                            qrCodeCallback(barcode.rawValue)
                        }
                    }, { exception ->
                        Log.d(TAG, "error occurs: ${exception.stackTrace}")
                    })
                }
                counter = 0
            }
            image.close()
        }

    private fun yuvToBitmap(image: Image): Bitmap? {
        val y = image.planes[0].buffer
        //val u = image.planes[1].buffer
        //val v = image.planes[2].buffer
        //val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val byteArray = ByteArray(y.capacity())
        y.get(byteArray)
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, BitmapFactory.Options().also {
            it.inMutable = true
        })
        //for (j in 0 until image.height) {
        //    for (i in 0 until image.width) {
        //        //val r = (y[i * j] + 1.402 * v[(i + j)]).toInt()
        //        //val g = (y[i * j] - 0.344 * u[(i + j)]).toInt()
        //        //val b = (y[i * j] + 1.772 * u[(i + j)]).toInt()
        //        //val color = Color.rgb(r, g, b)
        //        bitmap.setPixel(i, j, y[i * j].toInt())
        //    }
        //}
        saveImage(bitmap, "/sdcard/Android/data/com.qrist.quicker/test.png", 1080f)
        return bitmap
    }

    private val captureCallback =
        object : CameraCaptureSession.CaptureCallback() {
        }

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

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
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
        startBackgroundThread()
        if (this.isAvailable) {
            openCamera()
        } else {
            surfaceTextureListener = CameraSurfaceTextureListener()
        }
    }

    fun stopCameraPreview() {
        stopBackgroundThread()
        captureSession.close()
        imageReader.close()
    }

    private fun startBackgroundThread() {
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
            videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder::class.java))
            previewSize = choosePreviewSize(map.getOutputSizes(MediaRecorder::class.java))
            // I can't understand optimal size.
            // chosen video size is smallest one and it gonna be very smooth.
            //chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java), width, height, videoSize)
            setAspectRatio(previewSize.height, previewSize.width)
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
            Log.d("camera list", "${cameraManager.cameraIdList}")
        } catch (e: NoSuchElementException) {
            Toast.makeText(context, "Camera not found", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreviewSession(camera: CameraDevice) {
        try {
            val surface = Surface(surfaceTexture)
            imageReader = ImageReader.newInstance(videoSize.width, videoSize.height, ImageFormat.JPEG, 2)
            Log.d(TAG, "previewSize: ${videoSize.width} x ${videoSize.height}")
            imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)
            previewRequestBuilder.addTarget(imageReader.surface)
            camera.createCaptureSession(
                listOf(surface, imageReader.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {

                        captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            previewRequest = previewRequestBuilder.build()
                            captureSession.setRepeatingRequest(previewRequest,
                                captureCallback, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            Log.e("erfs", e.toString())
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        //Tools.makeToast(baseContext, "Failed")
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
        it.width <= 240 } ?: choices[choices.size - 1]

    inner class CameraSurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }
    }

    class CompareSizesByArea : Comparator<Size> {

        // We cast here to ensure the multiplications won't overflow
        override fun compare(lhs: Size, rhs: Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }

    companion object {
        private const val TAG = "camerasceenpreview"
    }
}
