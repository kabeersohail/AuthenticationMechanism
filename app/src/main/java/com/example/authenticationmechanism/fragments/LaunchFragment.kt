package com.example.authenticationmechanism.fragments

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_SECURITY_SETTINGS
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
import java.util.concurrent.Executor


class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var executor: Executor

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock screen")
        .setAllowedAuthenticators(DEVICE_CREDENTIAL or BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        .build()

    private val unlockResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                showToast("unlocked")
            }
        }

    private val onBiometricEnrollmentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        showToast("onBiometricEnrollmentResult out")
        if(activityResult.resultCode == Activity.RESULT_OK){
            showToast("onBiometricEnrollmentResult")
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }

    private val setNewPassword =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                showToast("Password setup successful")
            } else {
                showToast("${activityResult.resultCode} result cancelled")
            }
        }

    private val biometricAuthCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(requireContext(), "Error $errorCode $errString", Toast.LENGTH_SHORT)
                .show()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(requireContext(), "Success $result", Toast.LENGTH_SHORT).show()
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

        binding.authenticate.setOnClickListener {
            biometricManager = BiometricManager.from(requireContext())
            if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS ||
                biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS ||
                biometricManager.canAuthenticate(DEVICE_CREDENTIAL) == BIOMETRIC_SUCCESS
            ) {
                executor = ContextCompat.getMainExecutor(requireContext())
                biometricPrompt =
                    BiometricPrompt(requireActivity(), executor, biometricAuthCallback)
                biometricPrompt.authenticate(promptInfo)
            } else {
                log(BIOMETRIC_STRONG)
                log(BIOMETRIC_WEAK)
                log(DEVICE_CREDENTIAL)

                val biometricEnrollIntent: Intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG and DEVICE_CREDENTIAL and BIOMETRIC_WEAK)
                }

                try {
                    onBiometricEnrollmentResult.launch(biometricEnrollIntent)
                } catch (e: ActivityNotFoundException){
                    val keyguardManager =
                        requireActivity().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                    val confirmCredentialIntent: Intent? =
                        keyguardManager.createConfirmDeviceCredentialIntent("Enter phone screen lock pattern, PIN, password or fingerprint",
                            "to proceed")
                    confirmCredentialIntent?.let {
                        unlockResult.launch(confirmCredentialIntent)
                    } ?: run {


                        val securitySettingsIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                        setNewPassword.launch(securitySettingsIntent)

                    }
                }
            }
        }
    }

    private fun log(x: Int) {
        Log.d("SOHAIL", biometricManager.canAuthenticate(x).toString())
    }


}