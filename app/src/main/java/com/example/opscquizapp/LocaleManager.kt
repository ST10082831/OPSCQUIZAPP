package com.example.opscquizapp

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context): Context {
        return updateResources(context, getLanguage(context))
    }

    fun setNewLocale(context: Context, language: String): Context {
        persistLanguage(context, language)
        return updateResources(context, language)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("QuizAppSettings", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    private fun persistLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("QuizAppSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("language", language).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
