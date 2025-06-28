package com.yn.setbox.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// تهيئة DataStore لحفظ الإعدادات بشكل دائم.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// كلاس لإدارة حفظ واسترجاع تفضيلات المستخدم باستخدام DataStore.
class AppPreferences(private val context: Context) {

    companion object {
        // مفاتيح لتخزين قيم الإعدادات المختلفة.
        private val THEME_KEY = stringPreferencesKey("theme_key")
        private val LANGUAGE_KEY = stringPreferencesKey("language_key")
        private val BLACK_THEME_KEY = booleanPreferencesKey("black_theme_key")
        private val MATERIAL_YOU_KEY = booleanPreferencesKey("material_you_key")
        private val HUE_SHIFT_KEY = floatPreferencesKey("hue_shift_key")
        private val MODULES_ENABLED_KEY = stringSetPreferencesKey("modules_enabled_key")
    }

    // حفظ مجموعة معرفات الوحدات (Modules) المفعلة.
    suspend fun saveEnabledModuleIds(ids: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[MODULES_ENABLED_KEY] = ids
        }
    }

    // استرجاع معرفات الوحدات المفعلة كـ Flow لمراقبة التغييرات.
    fun getEnabledModuleIds(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[MODULES_ENABLED_KEY] ?: emptySet()
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun getLanguage(): String {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: AppLanguage.SYSTEM.name
        }.first()
    }

    suspend fun saveTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeName
        }
    }

    fun getTheme(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
        }
    }

    suspend fun setBlackThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BLACK_THEME_KEY] = enabled
        }
    }

    fun isBlackThemeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BLACK_THEME_KEY] ?: false
        }
    }

    suspend fun setMaterialYouEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MATERIAL_YOU_KEY] = enabled
        }
    }

    fun isMaterialYouEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[MATERIAL_YOU_KEY] ?: true
        }
    }

    suspend fun saveHueShift(shift: Float) {
        context.dataStore.edit { preferences ->
            preferences[HUE_SHIFT_KEY] = shift
        }
    }

    fun getHueShift(): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[HUE_SHIFT_KEY] ?: 0f
        }
    }
}