package com.qrist.quicker.register

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisterBinding
import com.qrist.quicker.extentions.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*

class RegisterFragment : Fragment() {
    private val viewModel: RegisterViewModel
            by lazy { obtainViewModel(RegisterViewModel::class.java) }
    private val serviceName by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceName }
    private val serviceIconUrl by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceIconUrl }
    private var qrImageBitmap: Bitmap? = null
    private var serviceIconImageBitmap: Bitmap? = null

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
                        this@RegisterFragment.view?.qrImageView?.setImageBitmap(bmp)
                        this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
                        viewModel.updateQRCodeImageUrl(uri.toString())
                        qrImageBitmap = bmp
                    }
                }
                IntentActionType.RESULT_PICK_SERVICE_ICON -> {
                    onPickImageFile(resultData) { bmp, uri ->
                        this@RegisterFragment.serviceIconImageView.setImageBitmap(bmp)
                        this@RegisterFragment.view?.addIconButton?.visibility = View.INVISIBLE
                        viewModel.updateServiceIconUrl(uri.toString())
                        serviceIconImageBitmap = bmp
                    }
                }
            }
        }
    }
}
