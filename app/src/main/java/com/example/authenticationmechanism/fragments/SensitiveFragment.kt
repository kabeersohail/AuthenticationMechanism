package com.example.authenticationmechanism.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.authenticationmechanism.databinding.FragmentSensitiveBinding

class SensitiveFragment : Fragment() {

    lateinit var binding: FragmentSensitiveBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSensitiveBinding.inflate(inflater, container, false)
        return binding.root
    }
}