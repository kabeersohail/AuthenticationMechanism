package com.example.authenticator

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings.ACTION_BIOMETRIC_ENROLL
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat.startActivityForResult


class Authenticator(context: Context) {

    val biometricManager = BiometricManager.from(context)

    fun getPromptInfo() = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            // Can't call setNegativeButtonText() and
            // setAllowedAuthenticators(... or DEVICE_CREDENTIAL) at the same time.
            // .setNegativeButtonText("Use account password")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()



}