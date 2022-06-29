package com.example.authenticationmechanism.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.authenticationmechanism.databinding.FragmentLaunchBinding
import com.example.authenticationmechanism.utils.Constants.AUTH_CANCELLED_BY_USER
import java.util.concurrent.Executor


class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding

    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var executor: Executor

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Use fingerprint to unlock")
        .setAllowedAuthenticators(DEVICE_CREDENTIAL or BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        .build()

    private val onBiometricEnrollmentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        showToast("onBiometricEnrollmentResult out")
        if(activityResult.resultCode == Activity.RESULT_OK){
            showToast("onBiometricEnrollmentResult")
        }
    }

    private val biometricAuthCallback = object: BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            showToast("$errString")
            if(errorCode == AUTH_CANCELLED_BY_USER){
                biometricPrompt.authenticate(promptInfo)
            }
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            showToast("Auth failed")
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            showToast("Biometric Auth Success")
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
        biometricManager = BiometricManager.from(requireContext())
        executor = ContextCompat.getMainExecutor(requireContext())
        canAuthenticateViaBiometric()
        biometricPrompt = BiometricPrompt(requireActivity(), executor, biometricAuthCallback)

        binding.authenticate.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }



    private fun canAuthenticateViaBiometric() {
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showToast("Success")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> showToast("No hardware")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> showToast("Hardware unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showToast("None enrolled")
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                onBiometricEnrollmentResult.launch(enrollIntent)
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> showToast("status unknown")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> showToast("unsupported")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> showToast("update required")
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}