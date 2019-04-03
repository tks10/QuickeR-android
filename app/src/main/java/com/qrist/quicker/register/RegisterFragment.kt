package com.qrist.quicker.register

import android.Manifest
import android.annotation.SuppressLint
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
import com.qrist.quicker.MainActivity
import com.qrist.quicker.OnSendActivity
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisterBinding
import com.qrist.quicker.extentions.*
import com.qrist.quicker.models.TutorialType
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
    private val qrImageUrl by lazy { RegisterFragmentArgs.fromBundle(arguments!!).qrImageUrl }
    private val serviceName by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceName }
    private val serviceIconUrl by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceIconUrl }
    private val directory = File(storeDirectory)
    private var qrImageBitmap: Bitmap? = null
    private lateinit var sequence: TapTargetSequence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.apply {
            // in generally, viewmodel is gonna be initialized in onCreate()
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
            if (Build.VERSION.SDK_INT >= 23 && !checkStoragePermission()) {
                requestExternalStoragePermission(REQUEST_PERMISSION_ON_QR)
            } else {
                onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
            }
        }

        qrImageView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkStoragePermission()) {
                requestExternalStoragePermission(REQUEST_PERMISSION_ON_QR)
            } else {
                onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
            }
        }

        addButton.setOnClickListener {
            qrImageBitmap?.let { bmp ->
                viewModel.saveQRCode(bmp)
                when (val act = requireActivity()) {
                    is MainActivity -> Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .popBackStack(R.id.qrContainerFragment, false)
                    is OnSendActivity -> act.finish()
                    else -> {
                    }
                }
            }
        }

        serviceNameEditText.afterTextChanged {
            viewModel.updateServiceName(it)
        }

        if (viewModel.isDefaultService.value!!) {
            view.serviceIconImageView.borderWidth = 0
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

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState ?: return

        val qrCodeImageUrl = savedInstanceState.getString(QRCODE_IMAGE_URL) ?: ""
        val serviceName = savedInstanceState.getString(SERVICE_NAME) ?: ""
        val serviceIconUrl = savedInstanceState.getString(SERVICE_ICON_URL) ?: ""
        val isDefaultService = savedInstanceState.getBoolean(IS_DEFAULT_SERVICE)

        viewModel.restoreValues(
            qrCodeImageUrl,
            serviceName,
            serviceIconUrl,
            isDefaultService
        )

        if (qrCodeImageUrl.isNotBlank()) {
            qrImageBitmap = try {
                getBitmapFromUri(context!!, Uri.parse(qrCodeImageUrl))
            } catch (e: FileNotFoundException) {
                getBitmapFromUri(context!!, Uri.fromFile(File(qrCodeImageUrl)))
            }
            this@RegisterFragment.view?.qrImageView?.setImageBitmap(qrImageBitmap)
            this@RegisterFragment.view?.addQRButton?.isVisible = false
            this@RegisterFragment.view?.qrHintTextView?.isGone = true
        }
    }

    @SuppressLint("ShowToast")
    override fun onStart() {
        super.onStart()
        MyApplication.analytics.setCurrentScreen(
            requireActivity(),
            this.javaClass.simpleName,
            this.javaClass.simpleName
        )

        // Set QR Code Image if it is attached by other application.
        if (qrImageUrl.isNotBlank() && viewModel.qrCodeImageUrl.value == "") {
            val uri = try {
                Uri.parse(qrImageUrl)
            } catch (e: FileNotFoundException) {
                Uri.fromFile(File(qrImageUrl))
            }
            try {
                val bmp = getBitmapFromUri(requireContext(), uri)
                viewModel.updateQRCodeImageUrl(qrImageUrl)
                detectAndSet(bmp, uri)
            } catch (e: Exception) {
                Log.d("RegisterFragment", e.toString())
                Toast.makeText(requireContext(), R.string.cannot_open_image, Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
        }

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
            }
        }
    }

    private fun setupTutorial() {
        var id = 0
        val targets = ArrayList<TapTarget>()
        val tutorialTypes = LinkedList<TutorialType>()

        if (viewModel.hasNotDoneTutorial(TutorialType.QRImageView)) {
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

            tutorialTypes.add(TutorialType.QRImageView)
        }

        if (viewModel.hasNotDoneTutorial(TutorialType.ServiceNameEditText) && !viewModel.isDefaultService.value!!) {
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

            tutorialTypes.add(TutorialType.ServiceNameEditText)
        }

        if (viewModel.hasNotDoneTutorial(TutorialType.DoneButton)) {
            @Suppress("UNUSED_CHANGED_VALUE")
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

            tutorialTypes.add(TutorialType.DoneButton)
        }

        sequence = TapTargetSequence(activity).targets(targets).listener(object : TapTargetSequence.Listener {
            override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                Log.d("TTV", "Step, $targetClicked")
                viewModel.doneTutorial(tutorialTypes.poll())
            }

            override fun onSequenceFinish() {}

            override fun onSequenceCanceled(lastTarget: TapTarget?) {
                Log.d("TTV", "Cancel")
                viewModel.doneTutorial(tutorialTypes.poll())
            }
        })

        sequence.continueOnCancel(false)
        sequence.considerOuterCircleCanceled(false)
    }

    private fun detectAndSet(bmp: Bitmap, uri: Uri) {
        val onDetect = { barcodes: List<FirebaseVisionBarcode> ->
            val trimmedBitmap = QRCodeDetector.trimQRCodeIfDetected(bmp, barcodes)
            trimmedBitmap?.let {
                // If Detected
                val tmpUri = generateTemporaryUri()
                saveImage(it, tmpUri, IMAGE_QR_MAX)
                qrImageBitmap = it
                this@RegisterFragment.view?.qrImageView?.setImageBitmap(it)
                this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
                this@RegisterFragment.view?.qrHintTextView?.isGone = true
                viewModel.updateQRCodeImageUrl(tmpUri)
            } ?: run {
                CropImage
                    .activity(uri)
                    .start(MyApplication.instance, this)
            }
        }
        val onFailure = { func: Exception ->
            func.printStackTrace()
            Log.e("FirebaseMLKit", func.toString())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IntentActionType.RESULT_PICK_QRCODE -> {
                    onPickImageFile(resultData) { bmp, uri ->
                        detectAndSet(bmp, uri)
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    onCropImageFile(resultData) { bmp, uri ->
                        qrImageBitmap = bmp
                        this@RegisterFragment.view?.qrImageView?.setImageBitmap(bmp)
                        this@RegisterFragment.view?.addQRButton?.isVisible = false
                        this@RegisterFragment.view?.qrHintTextView?.isGone = true
                        viewModel.updateQRCodeImageUrl(uri.toString())
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

        private const val REQUEST_PERMISSION_ON_QR: Int = 1000
    }
}
