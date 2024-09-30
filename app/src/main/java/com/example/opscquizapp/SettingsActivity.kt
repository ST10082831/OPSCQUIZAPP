package com.example.opscquizapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.opscquizapp.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding


    private val PREFS_NAME = "QuizAppSettings"
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupLanguageSpinner()
        loadSettings()

        // Dark Mode Switch Listener
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
            saveSettings()
        }

        // Game Sounds Switch Listener
        binding.gameSoundsSwitch.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }


        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        usernameTextView = findViewById(R.id.usernameTextView)
        emailTextView = findViewById(R.id.emailTextView)

        displayUserInfo()

    }
    private fun displayUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            emailTextView.text = user.email

            // get username from FirebaseAuth user profile
            val username = user.displayName
            if (username != null) {
                usernameTextView.text = username
            } else {
                // or retrieve from Firestore
                db.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val usernameFromDb = document.getString("username")
                            usernameTextView.text = usernameFromDb ?: "N/A"
                        } else {
                            usernameTextView.text = "N/A"
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsActivity", "Error fetching user data", e)
                        usernameTextView.text = "N/A"
                    }
            }
        } else {
            // User not logged in
            usernameTextView.text = "N/A"
            emailTextView.text = "N/A"
        }
    }
    private fun setupLanguageSpinner() {
        val languages = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_spanish),
            getString(R.string.language_french)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var isFirstSelection = true

            override fun onItemSelected(
                parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                val selectedLanguage = when (position) {
                    0 -> "en"
                    1 -> "es"
                    2 -> "fr"
                    else -> "en"
                }

                Log.d("SettingsActivity", "Language selected: $selectedLanguage")

                if (selectedLanguage != getCurrentLanguage()) {
                    setLocale(selectedLanguage)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

    }

    private fun getCurrentLanguage(): String {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    private fun setLocale(languageCode: String) {
        // Save the new language to user
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("language", languageCode)
        editor.apply()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        // Log the language change for debugging
        Log.d("SettingsActivity", "Language set to: $languageCode")

        // Restart the app to apply the new language
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity() // Close all previous activities
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    private fun setDarkMode(isEnabled: Boolean) {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val newMode = if (isEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        if (currentMode != newMode) {
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()

        // Save the selected language in SharedPreferences
        val selectedLanguage = when (binding.languageSpinner.selectedItemPosition) {
            0 -> "en"
            1 -> "es"
            2 -> "fr"
            else -> "en"
        }
        editor.putString("language", selectedLanguage)

        // Save dark mode state
        editor.putBoolean("darkMode", binding.darkModeSwitch.isChecked)

        // Save game sounds state
        editor.putBoolean("gameSounds", binding.gameSoundsSwitch.isChecked)

        editor.apply()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load language
        val languageCode = prefs.getString("language", "en")
        val languagePosition = when (languageCode) {
            "en" -> 0
            "es" -> 1
            "fr" -> 2
            else -> 0
        }
        binding.languageSpinner.setSelection(languagePosition)

        // Load dark mode state
        val isDarkMode = prefs.getBoolean("darkMode", false)
        binding.darkModeSwitch.isChecked = isDarkMode
        setDarkMode(isDarkMode)

        // Load game sounds state
        val isGameSoundsOn = prefs.getBoolean("gameSounds", true)
        binding.gameSoundsSwitch.isChecked = isGameSoundsOn
    }

}
