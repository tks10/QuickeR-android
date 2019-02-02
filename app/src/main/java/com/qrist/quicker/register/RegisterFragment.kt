package com.qrist.quicker.register

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisterBinding
import com.qrist.quicker.extentions.*
import com.qrist.quicker.models.TutorialComponent
import com.qrist.quicker.utils.MyApplication
import com.qrist.quicker.utils.QRCodeDetector
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.util.*

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

    private lateinit var sequence: TapTargetSequence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentRegisterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        val toolbar: Toolbar = activity!!.findViewById(R.id.tool_bar)
        toolbar.menu.clear()
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
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack(R.id.qrContainerFragment, false)
            }
        }

        binding.root.serviceNameEditText.afterTextChanged {
            viewModel.updateServiceName(it)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupTutorial()
        sequence.start()
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
                TapTarget.forView(view!!.serviceNameTextInputLayout, context!!.resources.getString(R.string.tutorial_service_name))
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
