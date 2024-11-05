package com.example.opscquizapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.opscquizapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executor

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001  // Request code

    // Biometric variables
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var isBiometricSetup = false
    private var tempEmail: String? = null
    private var tempPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Executor and BiometricPrompt
        executor = ContextCompat.getMainExecutor(this)
        setupBiometricPrompt()

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_authentication))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_prompt_negative))
            .build()

        // Set Click Listener for Login Button
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In Button Click
        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        // Biometric Login Button Click
        binding.biometricLoginButton.setOnClickListener {
            showBiometricLogin()
        }

        checkBiometricAvailability()
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if (isBiometricSetup && tempEmail != null && tempPassword != null) {
                    saveBiometricPreference(tempEmail!!, tempPassword!!)
                    isBiometricSetup = false
                    tempEmail = null
                    tempPassword = null
                    navigateToMainMenu()
                } else {
                    val sharedPref = getEncryptedSharedPreferences()
                    val email = sharedPref.getString("biometric_email", null)
                    val password = sharedPref.getString("biometric_password", null)
                    if (email != null && password != null) {
                        signInWithEmailBiometric(email, password)
                    } else {
                        Toast.makeText(this@LoginActivity, "Biometric credentials not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "biometric_prefs",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val sharedPref = getEncryptedSharedPreferences()
                if (sharedPref.getBoolean("biometric_enabled", false)) {
                    binding.biometricLoginButton.visibility = View.VISIBLE
                }
            }
            else -> binding.biometricLoginButton.visibility = View.GONE
        }
    }

    private fun promptForBiometricSetup() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.biometric_enable_prompt_title))
            .setMessage(getString(R.string.biometric_enable_prompt_message))
            .setPositiveButton(getString(R.string.biometric_enable_positive)) { dialog, _ ->
                enableBiometricAuthentication()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.biometric_enable_negative)) { dialog, _ ->
                dialog.dismiss()
                navigateToMainMenu()
            }
            .setCancelable(false)
            .show()
    }

    private fun enableBiometricAuthentication() {
        isBiometricSetup = true
        biometricPrompt.authenticate(promptInfo)
    }

    private fun saveBiometricPreference(email: String, password: String) {
        val sharedPref = getEncryptedSharedPreferences()
        with(sharedPref.edit()) {
            putBoolean("biometric_enabled", true)
            putString("biometric_email", email)
            putString("biometric_password", password)
            apply()
        }
        binding.biometricLoginButton.visibility = View.VISIBLE
    }

    private fun showBiometricLogin() {
        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    tempEmail = email
                    tempPassword = password
                    promptForBiometricSetup()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Google sign in canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-In successful.", Toast.LENGTH_SHORT).show()
                    navigateToMainMenu()
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithEmailBiometric(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainMenu()
                } else {
                    Toast.makeText(this, "Biometric login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}



