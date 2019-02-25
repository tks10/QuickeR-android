package com.qrist.quicker.register

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisterBinding
import com.qrist.quicker.extentions.*
import com.qrist.quicker.models.TutorialComponent
import com.qrist.quicker.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class RegisterFragment : Fragment() {
    private val viewModel: RegisterViewModel
            by lazy { obtainViewModel(RegisterViewModel::class.java) }
    private val serviceName by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceName }
    private val serviceIconUrl by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceIconUrl }
    private val directory = File(storeDirectory)
    private var qrImageBitmap: Bitmap? = null
    private var serviceIconImageBitmap: Bitmap? = null

    private var kindOfCrop = -1

    private lateinit var sequence: TapTargetSequence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.apply { // in generally, viewmodel is gonna be initialized in onCreate()
            if (this@RegisterFragment.serviceIconUrl.isNotBlank()) {
                initServiceInformation(
                    this@RegisterFragment.serviceName,
                    this@RegisterFragment.serviceIconUrl
                )
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        DataBindingUtil.inflate<FragmentRegisterBinding>(inflater, R.layout.fragment_register, container, false).apply {
            setLifecycleOwner(this@RegisterFragment)
            viewmodel = viewModel
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT < 23) {
            makeAppDirectory(directory)
        }

        addQRButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
                requestExternalStoragePermission(REQUEST_PERMISSION_ON_QR)
            } else {
                onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
            }
        }

        qrImageView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
                requestExternalStoragePermission(REQUEST_PERMISSION_ON_QR)
            } else {
                onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
            }
        }

        addIconButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
                requestExternalStoragePermission(REQUEST_PERMISSION_ON_ICON)
            } else {
                onClickImagePicker(IntentActionType.RESULT_PICK_SERVICE_ICON)
            }
        }

        serviceIconImageView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkPermission()) {
                requestExternalStoragePermission(REQUEST_PERMISSION_ON_ICON)
            } else {
                onClickImagePicker(IntentActionType.RESULT_PICK_SERVICE_ICON)
            }
        }

        addButton.setOnClickListener {
            qrImageBitmap?.let { bmp ->
                viewModel.saveQRCode(bmp, serviceIconImageBitmap)
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .popBackStack(R.id.qrContainerFragment, false)
            }
        }

        serviceNameEditText.afterTextChanged {
            viewModel.updateServiceName(it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            tool_bar.menu.clear()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(QRCODE_IMAGE_URL, viewModel.qrCodeImageUrl.value)
        savedInstanceState.putString(SERVICE_NAME, viewModel.serviceName.value)
        savedInstanceState.putString(SERVICE_ICON_URL, viewModel.serviceIconUrl.value)
        savedInstanceState.putBoolean(IS_DEFAULT_SERVICE, viewModel.isDefaultService.value!!)
        savedInstanceState.putInt(KIND_OF_CROP, kindOfCrop)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            val qrCodeImageUrl = savedInstanceState.getString(QRCODE_IMAGE_URL) ?: ""
            val serviceName = savedInstanceState.getString(SERVICE_NAME) ?: ""
            val serviceIconUrl = savedInstanceState.getString(SERVICE_ICON_URL) ?: ""
            val isDefaultService = savedInstanceState.getBoolean(IS_DEFAULT_SERVICE)
            kindOfCrop = savedInstanceState.getInt(KIND_OF_CROP)

            viewModel.restoreValues(
                qrCodeImageUrl,
                serviceName,
                serviceIconUrl,
                isDefaultService
            )

            if (qrCodeImageUrl.isNotBlank()) {
                qrImageBitmap = try {
                    getBitmapFromUri(Uri.parse(qrCodeImageUrl))
                } catch (e: FileNotFoundException) {
                    getBitmapFromUri(Uri.fromFile(File(qrCodeImageUrl)))
                }
                this@RegisterFragment.view?.qrImageView?.setImageBitmap(qrImageBitmap)
                this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
            }
            if (serviceIconUrl.isNotBlank() && !isDefaultService) {
                serviceIconImageBitmap = getBitmapFromUri(Uri.parse(serviceIconUrl))
                this@RegisterFragment.view?.serviceIconImageView?.setImageBitmap(serviceIconImageBitmap)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupTutorial()
        sequence.start()
    }

    private fun requestExternalStoragePermission(requestCode: Int) {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(activity, R.string.accept_me, Toast.LENGTH_LONG).show()
        }
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            requestCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makeAppDirectory(directory)
            when (requestCode) {
                REQUEST_PERMISSION_ON_QR -> onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
                REQUEST_PERMISSION_ON_ICON -> onClickImagePicker(IntentActionType.RESULT_PICK_SERVICE_ICON)
            }
        }
    }

    private fun setupTutorial() {
        var id = 0
        val targets = ArrayList<TapTarget>()
        val components = LinkedList<TutorialComponent>()

        if (viewModel.hasNotDoneTutorial(TutorialComponent.QRImageView)) {
            targets.add(
                TapTarget.forView(view!!.addQRButton, context!!.resources.getString(R.string.tutorial_qr_image))
                    .outerCircleColor(R.color.colorAccent)
                    .titleTextColor(R.color.colorTextOnSecondary)
                    .drawShadow(true)
                    .outerCircleAlpha(0.9f)
                    .cancelable(true)
                    .tintTarget(false)
                    .id(id++)
            )

            components.add(TutorialComponent.QRImageView)
        }

        if (viewModel.hasNotDoneTutorial(TutorialComponent.ServiceIconImageView) && !viewModel.isDefaultService.value!!) {
            targets.add(
                TapTarget.forView(view!!.addIconButton, context!!.resources.getString(R.string.tutorial_service_icon))
                    .outerCircleColor(R.color.colorAccent)
                    .titleTextColor(R.color.colorTextOnSecondary)
                    .drawShadow(true)
                    .outerCircleAlpha(0.9f)
                    .cancelable(true)
                    .tintTarget(false)
                    .id(id++)
            )

            components.add(TutorialComponent.ServiceIconImageView)
        }

        if (viewModel.hasNotDoneTutorial(TutorialComponent.ServiceNameEditText) && !viewModel.isDefaultService.value!!) {
            targets.add(
                TapTarget.forView(
                    view!!.serviceNameTextInputLayout,
                    context!!.resources.getString(R.string.tutorial_service_name)
                )
                    .outerCircleColor(R.color.colorAccent)
                    .titleTextColor(R.color.colorTextOnSecondary)
                    .drawShadow(true)
                    .outerCircleAlpha(0.9f)
                    .cancelable(true)
                    .tintTarget(false)
                    .id(id++)
            )

            components.add(TutorialComponent.ServiceNameEditText)
        }

        if (viewModel.hasNotDoneTutorial(TutorialComponent.DoneButton)) {
            targets.add(
                TapTarget.forView(view!!.addButton, context!!.resources.getString(R.string.tutorial_done))
                    .outerCircleColor(R.color.colorAccent)
                    .titleTextColor(R.color.colorTextOnSecondary)
                    .drawShadow(true)
                    .outerCircleAlpha(0.9f)
                    .cancelable(true)
                    .tintTarget(false)
                    .id(id++)
            )

            components.add(TutorialComponent.DoneButton)
        }

        sequence = TapTargetSequence(activity).targets(targets).listener(object : TapTargetSequence.Listener {
            override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                Log.d("TTV", "Step, $targetClicked")
                viewModel.doneTutorial(components.poll())
            }

            override fun onSequenceFinish() {}

            override fun onSequenceCanceled(lastTarget: TapTarget?) {
                Log.d("TTV", "Cancel")
                viewModel.doneTutorial(components.poll())
            }
        })

        sequence.continueOnCancel(false)
        sequence.considerOuterCircleCanceled(false)
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
                                val tmpUri = generateTemporaryUri()
                                saveImage(it, tmpUri, IMAGE_QR_MAX)
                                qrImageBitmap = it
                                this@RegisterFragment.view?.qrImageView?.setImageBitmap(it)
                                this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
                                viewModel.updateQRCodeImageUrl(tmpUri)
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
                                viewModel.updateServiceIconUrl(uri.toString())
                                serviceIconImageBitmap = bmp
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val QRCODE_IMAGE_URL = "qrImageUrl"
        const val SERVICE_NAME = "serviceName"
        const val SERVICE_ICON_URL = "serviceIconUrl"
        const val IS_DEFAULT_SERVICE = "isDefaultService"
        const val KIND_OF_CROP = "kindOfCrop"

        private const val CROP_QR = 0
        private const val CROP_ICON = 1
        private const val REQUEST_PERMISSION_ON_QR: Int = 1000
        private const val REQUEST_PERMISSION_ON_ICON: Int = 1001
    }
}
