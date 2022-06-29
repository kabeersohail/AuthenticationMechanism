package com.example.authenticationmechanism.fragments

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.authenticationmechanism.databinding.FragmentLaunchBinding


class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding

    private val unlockResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK){
            showToast("Unlocked")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val keyguardManager = requireActivity().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val intent: Intent? = try {
            keyguardManager.createConfirmDeviceCredentialIntent("Enter phone screen lock pattern, PIN, password or fingerprint", "to proceed")
        } catch (e: Exception){
            null
        }

        binding.authenticate.setOnClickListener {
            if(intent!=null){
                unlockResult.launch(intent)
            } else {
                showToast("No auth required")
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}