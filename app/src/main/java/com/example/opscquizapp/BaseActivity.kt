package com.example.opscquizapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val context = LocaleManager.setLocale(newBase)
        super.attachBaseContext(context)
    }
}