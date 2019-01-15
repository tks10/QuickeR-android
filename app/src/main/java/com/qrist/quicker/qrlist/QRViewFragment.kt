package com.qrist.quicker.qrlist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.qrist.quicker.R
import kotlinx.android.synthetic.main.fragment_qrview.view.*


class QRViewFragment : Fragment() {
    private val position by lazy { arguments!!.getInt("position") }
    private val viewModel: QRViewViewModel by lazy {
        ViewModelProviders.of(activity!!).get(position.toString(), QRViewViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qrview, container, false)
        Glide.with(this)
            .load(Environment.getExternalStorageDirectory().absolutePath + "/DCIM/QuickeR/qr_code.png")
            .into(view.qrImageView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("", "$position $viewModel")
    }

    companion object {
        fun newInstance(position: Int): QRViewFragment {
            return QRViewFragment().apply {
                arguments = Bundle().apply {
                    putInt("position", position)
                }
            }
        }
    }
}
