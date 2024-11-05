package com.example.opscquizapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.opscquizapp.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Define supported languages with corresponding codes
    private val languages = mapOf(
        "English" to "en",
        "Afrikaans" to "af",
        "Zulu" to "zu",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("QuizAppSettings", MODE_PRIVATE)

        setupLanguageSpinner()
        loadSettings()
        loadUserProfile()

        // Dark Mode Switch Listener
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
            saveSettings()
        }

        // Logout functionality
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupLanguageSpinner() {
        val languageNames = languages.keys.toList()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languageNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        // Set current selection based on saved language
        val currentLanguageCode = sharedPreferences.getString("language", "en") ?: "en"
        val currentLanguageName = languages.entries.find { it.value == currentLanguageCode }?.key
        val spinnerPosition = languageNames.indexOf(currentLanguageName)
        if (spinnerPosition >= 0) binding.languageSpinner.setSelection(spinnerPosition)

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var isFirstSelection = true

            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                val selectedLanguageName = parent.getItemAtPosition(position) as String
                val selectedLanguageCode = languages[selectedLanguageName] ?: "en"

                if (selectedLanguageCode != currentLanguageCode) {
                    changeLanguage(selectedLanguageCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            binding.emailTextView.text = user.email ?: "N/A"

            val username = user.displayName
            if (username != null) {
                binding.usernameTextView.text = username
            } else {
                // Retrieve from Firestore if username is not available in FirebaseAuth
                db.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val usernameFromDb = document.getString("username") ?: "N/A"
                            binding.usernameTextView.text = usernameFromDb
                        } else {
                            binding.usernameTextView.text = "N/A"
                        }
                    }
                    .addOnFailureListener {
                        binding.usernameTextView.text = "N/A"
                    }
            }
        } else {
            binding.usernameTextView.text = "N/A"
            binding.emailTextView.text = "N/A"
        }
    }

    private fun changeLanguage(languageCode: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_language))
            .setMessage(getString(R.string.restart_app))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                LocaleManager.setNewLocale(this, languageCode)
                saveSettings()
                restartApp()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setDarkMode(isEnabled: Boolean) {
        val mode = if (isEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun saveSettings() {
        val editor = sharedPreferences.edit()

        // Save the selected language in SharedPreferences
        val selectedLanguageCode = languages[binding.languageSpinner.selectedItem as String] ?: "en"
        editor.putString("language", selectedLanguageCode)

        // Save dark mode state
        editor.putBoolean("darkMode", binding.darkModeSwitch.isChecked)

        editor.apply()
    }

    private fun loadSettings() {
        // Load language
        val languageCode = sharedPreferences.getString("language", "en") ?: "en"
        val languagePosition = languages.keys.indexOfFirst { languages[it] == languageCode }
        if (languagePosition >= 0) binding.languageSpinner.setSelection(languagePosition)

        // Load dark mode state
        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)
        binding.darkModeSwitch.isChecked = isDarkMode
        setDarkMode(isDarkMode)
    }

    private fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }


}
