package com.qrist.quicker.camera.widget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.util.Size
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import java.lang.IllegalStateException
import java.util.*
import kotlin.NoSuchElementException

class CameraScenePreview : TextureView {

    private val previewSize: Size = Size(300, 300)
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var previewRequest: CaptureRequest
    private var ratioWidth = 0
    private var ratioHeight = 0
    private val backgroundThread by lazy {
        HandlerThread("CameraBackground").also {
            it.start()
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        surfaceTextureListener = CameraSurfaceTextureListener()
        setAspectRaio(1, 1)
    }

    override fun onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach()
        backgroundThread.quitSafely()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < ((height * ratioWidth) / ratioHeight)) {
                setMeasuredDimension(width, (width * ratioHeight) / ratioWidth)
            } else {
                setMeasuredDimension((height * ratioWidth) / ratioHeight, height)
            }
        }
    }

    private fun setAspectRaio(width: Int, height: Int) {
       if (width < 0 || height < 0) {
           throw IllegalStateException("size cannot be negative")
       }
        ratioWidth = width
        ratioHeight = height
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val manager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val cameraId: String = manager.cameraIdList.first { cameraId ->
                val characteristics = manager.getCameraCharacteristics(cameraId)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }

            Log.d("camera list", "${manager.cameraIdList}")

            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: NoSuchElementException) {
            Toast.makeText(context, "Camera not found", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreviewSession(camera: CameraDevice) {
        try {
            val texture = surfaceTexture
            val surface = Surface(texture)
            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)
            camera.createCaptureSession(
                Arrays.asList(surface, imageReader.surface),
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {

                        captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            previewRequest = previewRequestBuilder.build()
                            captureSession.setRepeatingRequest(previewRequest,
                                null, Handler(backgroundThread.looper)
                            )
                        } catch (e: CameraAccessException) {
                            Log.e("erfs", e.toString())
                        }

                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        //Tools.makeToast(baseContext, "Failed")
                    }
                }, null)
        } catch (e: CameraAccessException) {
            Log.e("erf", e.toString())
        }

    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            createCameraPreviewSession(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            onDisconnected(camera)
        }
    }

    inner class CameraSurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2)
            openCamera()
        }
    }
}