package com.qrist.quicker.register

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisterBinding
import com.qrist.quicker.extentions.*
import com.qrist.quicker.utils.MyApplication
import com.qrist.quicker.utils.QRCodeDetector
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.lang.Exception


class RegisterFragment : Fragment() {
    private val viewModel: RegisterViewModel
            by lazy { obtainViewModel(RegisterViewModel::class.java) }
    private val serviceName by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceName }
    private val serviceIconUrl by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceIconUrl }
    private var qrImageBitmap: Bitmap? = null
    private var serviceIconImageBitmap: Bitmap? = null

    private val CROP_QR = 0
    private val CROP_ICON = 1
    private var kindOfCrop = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentRegisterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel.apply {
            if (this@RegisterFragment.serviceIconUrl.isNotBlank()) {
                initServiceInformation(
                    this@RegisterFragment.serviceName,
                    this@RegisterFragment.serviceIconUrl
                )
            }
        }

        binding.root.addQRButton.setOnClickListener {
            onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
        }

        binding.root.qrImageView.setOnClickListener {
            onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
        }

        binding.root.addIconButton.setOnClickListener {
            onClickImagePicker(IntentActionType.RESULT_PICK_SERVICE_ICON)
        }

        binding.root.serviceIconImageView.setOnClickListener {
            onClickImagePicker(IntentActionType.RESULT_PICK_SERVICE_ICON)
        }

        binding.root.addButton.setOnClickListener {
            qrImageBitmap?.let { bmp ->
                viewModel.saveQRCode(bmp, serviceIconImageBitmap)
                activity!!.supportFragmentManager.popBackStack()
            }
        }

        binding.root.serviceNameEditText.afterTextChanged {
            viewModel.updateServiceName(it)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IntentActionType.RESULT_PICK_QRCODE -> {
                    onPickImageFile(resultData) { bmp, uri ->
                        val onDetect = { barcodes: List<FirebaseVisionBarcode> ->
                            val trimmedBitmap = QRCodeDetector.trimQRCodeIfDetected(bmp, barcodes)
                            trimmedBitmap?.let {
                                // If Detected
                                qrImageBitmap = it
                                this@RegisterFragment.view?.qrImageView?.setImageBitmap(it)
                                this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
                                viewModel.updateQRCodeImageUrl(uri.toString())
                            } ?: run {
                                kindOfCrop = CROP_QR
                                CropImage
                                    .activity(uri)
                                    .start(MyApplication.instance, this)
                            }
                        }
                        val onFailure = { func: Exception ->
                            func.printStackTrace()
                            kindOfCrop = CROP_QR
                            CropImage
                                .activity(uri)
                                .start(MyApplication.instance, this)
                        }
                        val onSuccessNegativeImage = { barcodes: List<FirebaseVisionBarcode> ->
                            run { onDetect(barcodes) }
                        }
                        val onSuccessOriginalImage = { barcodes: List<FirebaseVisionBarcode> ->
                            if (barcodes.isEmpty()) {
                                // "The Second Try" using negative bitmap.
                                // `onSuccessNegativeImage` is called when the detection succeeded.
                                QRCodeDetector.detectOnNegativeImage(bmp, onSuccessNegativeImage, onFailure)
                            } else {
                                run { onDetect(barcodes) }
                            }
                        }

                        // "The First Try" using original bitmap.
                        // `onSuccessOriginalImage` is called when the detection succeeded.
                        QRCodeDetector.detect(bmp, onSuccessOriginalImage, onFailure)
                    }
                }
                IntentActionType.RESULT_PICK_SERVICE_ICON -> {
                    onPickImageFile(resultData) { _, uri ->
                        kindOfCrop = CROP_ICON
                        CropImage
                            .activity(uri)
                            .start(MyApplication.instance, this)
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    when (kindOfCrop) {
                        CROP_QR -> {
                            onCropImageFile(resultData) { bmp, uri ->
                                qrImageBitmap = bmp
                                this@RegisterFragment.view?.qrImageView?.setImageBitmap(bmp)
                                this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
                                viewModel.updateQRCodeImageUrl(uri.toString())
                            }
                        }
                        CROP_ICON -> {
                            onCropImageFile(resultData) { bmp, uri ->
                                this@RegisterFragment.serviceIconImageView.setImageBitmap(bmp)
                                this@RegisterFragment.view?.addIconButton?.visibility = View.INVISIBLE
                                viewModel.updateServiceIconUrl(uri.toString())
                                serviceIconImageBitmap = bmp
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
