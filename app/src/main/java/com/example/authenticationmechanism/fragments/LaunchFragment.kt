package com.example.authenticationmechanism.fragments

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_BIOMETRIC_ENROLL
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricManager.Authenticators.*
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
    private lateinit var keyguardManager: KeyguardManager

    private var canPrompt = -1

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Use fingerprint to unlock")
        .setAllowedAuthenticators(DEVICE_CREDENTIAL or BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        .build()

    private val onBiometricEnrollmentResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            showToast("onBiometricEnrollmentResult out")
            if (activityResult.resultCode == Activity.RESULT_OK) {
                showToast("onBiometricEnrollmentResult")
            }
        }

    private val unlockResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                showToast("Unlocked")
            }
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

            when (errorCode) {

                BiometricPrompt.ERROR_CANCELED -> {
                    showToast("cancelled")
                }
                BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                    keyguardManager =
                        requireActivity().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                    val confirmCredentialIntent: Intent? =
                        keyguardManager.createConfirmDeviceCredentialIntent("Enter phone screen lock pattern, PIN, password or fingerprint",
                            "to proceed")

                    confirmCredentialIntent?.let {
                        unlockResult.launch(confirmCredentialIntent)
                    } ?: run {

                        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
                            BIOMETRIC_SUCCESS -> {
                                val intent = Intent(ACTION_BIOMETRIC_ENROLL)
                                startActivity(intent)
                            }

                            else -> startActivity(Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD))

                        }


                    }
                }
                BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                    TODO()
                }
                BiometricPrompt.ERROR_LOCKOUT -> {
                    TODO()
                }
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                    TODO()
                }
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                    TODO()
                }
                BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                    val intent = Intent(ACTION_BIOMETRIC_ENROLL)
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        val y = e
                    }
                    showToast("Not implemented yet")
                }
                BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                    keyguardManager =
                        requireActivity().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                    val confirmCredentialIntent: Intent? =
                        keyguardManager.createConfirmDeviceCredentialIntent("Enter phone screen lock pattern, PIN, password or fingerprint",
                            "to proceed")

                    confirmCredentialIntent?.let {
                        unlockResult.launch(confirmCredentialIntent)
                    } ?: run {
                        try {
                            startActivity(Intent(ACTION_BIOMETRIC_ENROLL))
                        } catch (e: ActivityNotFoundException) {
                            val intent = Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
                            startActivity(intent)
                        }
                    }
                }
                BiometricPrompt.ERROR_NO_SPACE -> {
                    TODO()
                }
                BiometricPrompt.ERROR_TIMEOUT -> {
                    TODO()
                }
                BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> {
                    TODO()
                }
                BiometricPrompt.ERROR_USER_CANCELED -> {
                    showToast("User cancelled")
                }
                BiometricPrompt.ERROR_VENDOR -> {
                    TODO()
                }
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

//        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
//            BIOMETRIC_SUCCESS -> showToast("BIOMETRIC_STRONG")
//            BIOMETRIC_ERROR_NONE_ENROLLED -> showToast("Can, but not enrolled")
//            else -> showToast("No BIOMETRIC_STRONG")
//        }

//        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
//            BIOMETRIC_SUCCESS -> showToast("BIOMETRIC_WEAK")
//            BIOMETRIC_ERROR_NONE_ENROLLED -> showToast("Can, but not enrolled")
//            else -> showToast("No BIOMETRIC_WEAK")
//
//        }


//        when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL)) {
//            BIOMETRIC_SUCCESS -> showToast("DEVICE_CREDENTIAL")
//            BIOMETRIC_ERROR_NONE_ENROLLED -> showToast("Can, but not enrolled")
//            BIOMETRIC_ERROR_UNSUPPORTED -> showToast("not compatible with current android version")
//            else -> showToast("No DEVICE_CREDENTIAL")
//        }

//        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
//            BIOMETRIC_SUCCESS -> {
//                biometricPrompt = BiometricPrompt(requireActivity(), executor, biometricAuthCallback)
//                binding.authenticate.setOnClickListener {
//                    biometricPrompt.authenticate(promptInfo)
//                }
//            }
//            BIOMETRIC_STATUS_UNKNOWN -> {}
//            BIOMETRIC_ERROR_UNSUPPORTED -> {}
//            BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}
//            BIOMETRIC_ERROR_NONE_ENROLLED -> {}
//            BIOMETRIC_ERROR_NO_HARDWARE -> {}
//            BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}
//        }



        executor = ContextCompat.getMainExecutor(requireContext())

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.d("sohail", message)
    }


}