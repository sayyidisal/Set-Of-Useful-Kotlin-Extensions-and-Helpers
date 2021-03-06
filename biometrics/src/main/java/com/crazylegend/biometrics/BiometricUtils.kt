package com.crazylegend.biometrics

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors


/**
 * Created by hristijan on 4/2/19 to long live and prosper !
 */

/**
 * Use [canAuthenticate] before calling this function, just to see if the user can be authenticated, don't do stupid shit and blame the API or function
 * if you get a weird result code that you forgot to handle, peace.
 */
fun FragmentActivity.biometricAuth(
        promptInfo: BiometricPrompt.PromptInfo,
        onAuthFailed: () -> Unit,
        onAuthError: (errorCode: Int, errorMessage: String) -> Unit = { _, _ -> },
        onAuthSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit = { _ -> }): BiometricPrompt {
    val executor = Executors.newSingleThreadExecutor()

    val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onAuthError(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess(result)
        }
    })

    biometricPrompt.authenticate(promptInfo)
    return biometricPrompt
}

/**
 * Use [canAuthenticate] before calling this function, just to see if the user can be authenticated, don't do stupid shit and blame the API or function
 * if you get a weird result code that you forgot to handle, peace.
 */
fun Fragment.biometricAuth(
        promptInfo: BiometricPrompt.PromptInfo,
        onAuthFailed: () -> Unit,
        onAuthError: (errorCode: Int, errorMessage: String) -> Unit = { _, _ -> },
        onAuthSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit = { _ -> }): BiometricPrompt {
    val executor = Executors.newSingleThreadExecutor()

    val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onAuthError(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess(result)
        }
    })

    biometricPrompt.authenticate(promptInfo)
    return biometricPrompt
}


fun FragmentActivity.canAuthenticate(hardwareUnavailable: () -> Unit = {}, noFingerprintsEnrolled: () -> Unit = {}, canAuthenticateAction: () -> Unit = {}) {
    when (BiometricManager.from(this).canAuthenticate()) {
        BIOMETRIC_SUCCESS -> {
            canAuthenticateAction()
        }
        BIOMETRIC_ERROR_NONE_ENROLLED -> {
            noFingerprintsEnrolled()
        }
        BIOMETRIC_ERROR_NO_HARDWARE -> {
            hardwareUnavailable()
        }

        BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            hardwareUnavailable()
        }
    }
}

fun Fragment.canAuthenticate(hardwareUnavailable: () -> Unit = {}, noFingerprintsEnrolled: () -> Unit = {}, canAuthenticateAction: () -> Unit) {
    when (BiometricManager.from(requireContext()).canAuthenticate()) {
        BIOMETRIC_SUCCESS -> {
            canAuthenticateAction()
        }
        BIOMETRIC_ERROR_NONE_ENROLLED -> {
            noFingerprintsEnrolled()
        }
        BIOMETRIC_ERROR_NO_HARDWARE -> {
            hardwareUnavailable()
        }
        BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            hardwareUnavailable()
        }
    }
}


/**
 * Use [canAuthenticate] before calling this function, just to see if the user can be authenticated, don't do stupid shit and blame the API or function
 * if you get a weird result code that you forgot to handle, peace.
 */
fun Fragment.biometricAuth(
        promptInfoAction: BiometricPrompt.PromptInfo.Builder.() -> BiometricPrompt.PromptInfo.Builder,
        onAuthFailed: () -> Unit,
        onAuthError: (errorCode: Int, errorMessage: String) -> Unit = { _, _ -> },
        onAuthSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit = { _ -> }): BiometricPrompt {
    val executor = Executors.newSingleThreadExecutor()

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .promptInfoAction()

    val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onAuthError(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess(result)
        }
    })

    biometricPrompt.authenticate(promptInfo.build())
    return biometricPrompt
}


/**
 * Use [canAuthenticate] before calling this function, just to see if the user can be authenticated, don't do stupid shit and blame the API or function
 * if you get a weird result code that you forgot to handle, peace.
 */
fun FragmentActivity.biometricAuth(
        promptInfoAction: BiometricPrompt.PromptInfo.Builder.() -> BiometricPrompt.PromptInfo.Builder,
        onAuthFailed: () -> Unit,
        onAuthError: (errorCode: Int, errorMessage: String) -> Unit = { _, _ -> },
        onAuthSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit = { _ -> }): BiometricPrompt {
    val executor = Executors.newSingleThreadExecutor()

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .promptInfoAction()

    val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onAuthError(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess(result)
        }
    })

    biometricPrompt.authenticate(promptInfo.build())
    return biometricPrompt
}