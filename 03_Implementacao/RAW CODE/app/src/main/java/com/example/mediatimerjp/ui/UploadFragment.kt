package com.example.mediatimerjp.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.databinding.FragmentUploadBinding
import com.example.mediatimerjp.model.TimerWrapper


class UploadFragment : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    private lateinit var wrapper: TimerWrapper
    private lateinit var uploadEditText: EditText
    private lateinit var uploadButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper = TimerWrapper.getInstance()
        binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadEditText = binding.timerCodeET
        uploadButton = binding.button
        uploadButton.setOnClickListener {
            uploadTimer()
        }
    }

    private fun uploadTimer() {
        val code = uploadEditText.text.toString()
        context?.let { Database.getTimer(code, it) }
        val r = Runnable { findNavController().navigate(R.id.action_uploadFragment_to_mainFragment) }
        Handler().postDelayed(r, 1000)
    }


}