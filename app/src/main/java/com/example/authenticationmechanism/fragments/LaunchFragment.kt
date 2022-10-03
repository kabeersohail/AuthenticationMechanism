package com.example.authenticationmechanism.fragments

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.FINGERPRINT_SERVICE
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.authenticationmechanism.databinding.FragmentLaunchBinding


class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding
    private lateinit var biometricManager: BiometricManager
    private lateinit var keyguardManager: KeyguardManager

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock screen")
        .setAllowedAuthenticators(DEVICE_CREDENTIAL or BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        .build()

    private val onBiometricEnrollmentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS){
            Handler(Looper.getMainLooper()).post {
                authenticate()
            }
        }
    }

    private val onConfirmDeviceCredential = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            showToastAndLog("Success")
        }
    }

    private val onsetNewPassword = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK || it.resultCode == Activity.RESULT_FIRST_USER){
            val fingerprintIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
            onFingerprintEnrollResult.launch(fingerprintIntent)
        } else if(it.resultCode == Activity.RESULT_CANCELED){
            showToastAndLog("New password setup cancelled")
        }
    }

    private val onFingerprintEnrollResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            showToastAndLog("Fingerprint enrolled")
        } else {
            showToastAndLog("Fingerprint enroll cancelled")
        }
    }

    private fun authenticate() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(requireActivity(), executor, biometricAuthCallback)
        biometricPrompt.authenticate(promptInfo)
    }

    private val biometricAuthCallback = object : BiometricPrompt.AuthenticationCallback(){
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            showToastAndLog("$errorCode $errString")
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            showToastAndLog("Failed")
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            showToastAndLog("Auth succeeded ${result.authenticationType}")
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
        keyguardManager = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        binding.authenticate.setOnClickListener {

            val result = biometricManager.canAuthenticate(BIOMETRIC_WEAK)

            when(result){
                BIOMETRIC_SUCCESS -> authenticate()

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {

                    if(Build.VERSION.SDK_INT <= 28 ){
                        val fingerprintManager: FingerprintManager = requireActivity().getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
                        fingerprintManager.authenticate()
                        if(!fingerprintManager.hasEnrolledFingerprints()){
                            val fingerprintIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                            onFingerprintEnrollResult.launch(fingerprintIntent)
                        } else {
                            authenticate()
                        }
                    } else {
                        authenticate()
                    }


                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> showToastAndLog("Error unsupported")

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> showToastAndLog("Error hardware unavailable")

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val biometricIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                        onBiometricEnrollmentResult.launch(biometricIntent)
                    } else if(Build.VERSION.SDK_INT >= 28){

                        // due to issue in Note 5 Pro -> First set new password, then set fingerprint enroll

                        val setNewPassword = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                        onsetNewPassword.launch(setNewPassword)
                    }
                    else if(Build.VERSION.SDK_INT < 28 ){
                        if(keyguardManager.isDeviceSecure){
                            val confirmDeviceCredentialIntent: Intent = keyguardManager.createConfirmDeviceCredentialIntent("Title", "description")
                            onConfirmDeviceCredential.launch(confirmDeviceCredentialIntent)
                        } else {

                            val setNewPassword = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                            onsetNewPassword.launch(setNewPassword)

                            showToastAndLog("non enrolled")
                        }
                    } else {
                        val setNewPassword = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                        onsetNewPassword.launch(setNewPassword)
                    }
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> showToastAndLog("No hardware")

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> showToastAndLog("Security update required")

            }
        }
    }

    private fun showToastAndLog(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.d("SOHAIL",message)
    }


}