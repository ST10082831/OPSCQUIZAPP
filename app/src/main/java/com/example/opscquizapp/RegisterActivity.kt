package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.opscquizapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executor

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inflate the layout using View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("QuizAppPrefs", Context.MODE_PRIVATE)

        // Initialize Biometric components
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, getString(R.string.biometric_authentication_error, errString), Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Biometric enabled successfully
                enableBiometricAuthentication()
                Toast.makeText(applicationContext, "Biometric authentication enabled.", Toast.LENGTH_SHORT).show()
                // Navigate to MainMenuActivity
                proceedToMainMenu()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, getString(R.string.biometric_authentication_failed), Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_enable_prompt_title))
            .setSubtitle(getString(R.string.biometric_enable_prompt_message)) // Replaced setMessage with setSubtitle
            //.setDescription(getString(R.string.biometric_enable_prompt_message)) // Alternatively, use setDescription
            .setNegativeButtonText(getString(R.string.biometric_enable_negative))
            .build()

        // Set up Register Button Click Listener
        binding.registerButton.setOnClickListener {
            performRegistration()
        }

        // Set up Biometric Enable Button Click Listener
        binding.enableBiometricButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun performRegistration() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        // Input Validation
        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username is required"
            binding.usernameEditText.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            binding.emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            binding.passwordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            binding.confirmPasswordEditText.requestFocus()
            return
        }

        // Show ProgressBar
        binding.registerProgressBar.visibility = View.VISIBLE

        // Create User with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.registerProgressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Save additional user data in Firestore
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "username" to username,
                        "email" to email
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                // Registration successful, show Biometric Enable Button
                                binding.enableBiometricButton.visibility = View.VISIBLE
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "User ID is null.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun enableBiometricAuthentication() {
        // Store biometric preference in SharedPreferences
        sharedPreferences.edit().putBoolean("biometric_enabled", true).apply()

        // Check if biometrics are enrolled
        if (!isBiometricEnrolled()) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.biometric_enroll_prompt_title))
                .setMessage(getString(R.string.biometric_enroll_prompt_message))
                .setPositiveButton(getString(R.string.biometric_enroll_positive)) { dialog, _ ->
                    // Open biometric enrollment settings
                    val enrollIntent = Intent(android.provider.Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    }
                    startActivity(enrollIntent)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.biometric_enroll_negative)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun isBiometricEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun proceedToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }


}



