package com.yn.setbox.core

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

// كائن لإدارة لغة التطبيق وتطبيقها.
object LocaleManager {

    // StateFlow لتتبع لغة التطبيق الحالية وإعلام الواجهة بأي تغييرات.
    private val _currentAppLanguageState = MutableStateFlow(AppLanguage.SYSTEM)
    val currentAppLanguageState: StateFlow<AppLanguage> = _currentAppLanguageState

    // دالة للحصول على لغة النظام الحالية.
    private fun getSystemLocale(): Locale {
        val configuration = Resources.getSystem().configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            configuration.locale
        }
    }

    // يحول AppLanguage إلى كائن Locale الذي يستخدمه أندرويد.
    fun getLocale(language: AppLanguage): Locale {
        return when (language) {
            AppLanguage.ENGLISH -> Locale("en")
            AppLanguage.ARABIC -> Locale("ar")
            AppLanguage.SYSTEM -> getSystemLocale()
        }
    }

    // حفظ اختيار اللغة في DataStore وتحديث الحالة الفورية.
    fun saveLanguage(context: Context, language: AppLanguage) {
        val appPreferences = AppPreferences(context)

        CoroutineScope(Dispatchers.IO).launch {
            appPreferences.saveLanguage(language.name)
        }

        _currentAppLanguageState.value = language
    }

    // استرجاع اللغة المحفوظة من DataStore عند بدء تشغيل التطبيق.
    suspend fun getSavedLanguage(context: Context): AppLanguage {
        val appPreferences = AppPreferences(context)
        val savedLanguageString = appPreferences.getLanguage()
        val savedLanguage = try {
            AppLanguage.valueOf(savedLanguageString)
        } catch (e: IllegalArgumentException) {
            AppLanguage.SYSTEM // العودة إلى لغة النظام إذا كانت القيمة المحفوظة غير صالحة.
        }
        _currentAppLanguageState.value = savedLanguage
        return savedLanguage
    }

    // دالة محورية: تطبق اللغة المختارة على سياق (Context) معين لترجمة الواجهة.
    fun applyLocaleToContext(context: Context, language: AppLanguage): Context {
        val locale = getLocale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}