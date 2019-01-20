package com.qrist.quicker.register

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qrist.quicker.R
import com.qrist.quicker.databinding.FragmentRegisterBinding
import com.qrist.quicker.extentions.IntentActionType
import com.qrist.quicker.extentions.obtainViewModel
import com.qrist.quicker.extentions.onClickImagePicker
import com.qrist.quicker.extentions.onPickImageFile
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*


class RegisterFragment : Fragment() {
    private val viewModel: RegisterViewModel
            by lazy { obtainViewModel(RegisterViewModel::class.java) }
    private val serviceName by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceName }
    private val serviceIconUrl by lazy { RegisterFragmentArgs.fromBundle(arguments!!).serviceIconUrl }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentRegisterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel.apply {
            initServiceInformation(
                this@RegisterFragment.serviceName,
                this@RegisterFragment.serviceIconUrl
            )
        }

        binding.root.addQRButton.setOnClickListener {
            onClickImagePicker(IntentActionType.RESULT_PICK_QRCODE)
        }

        binding.root.serviceIconImageView.setOnClickListener {
            onClickImagePicker(IntentActionType.RESULT_PICK_SERVICE_ICON)
        }

        binding.root.addButton.setOnClickListener {

        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IntentActionType.RESULT_PICK_QRCODE -> {
                    onPickImageFile(resultData) { bmp, uri ->
                        this@RegisterFragment.view?.qrImage?.setImageBitmap(bmp)
                        this@RegisterFragment.view?.addQRButton?.visibility = View.INVISIBLE
                    }
                }
                IntentActionType.RESULT_PICK_SERVICE_ICON -> {
                    onPickImageFile(resultData) { bmp, uri ->
                        this@RegisterFragment.serviceIconImageView.setImageBitmap(bmp)
                    }
                }
            }
        }
    }
}
