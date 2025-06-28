package com.yn.setbox.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yn.setbox.core.AppPreferences
import com.yn.setbox.core.AppTheme
import kotlinx.coroutines.launch

// ViewModel لإدارة حالة الثيم والتفاعل مع تفضيلات المستخدم.
class ThemeViewModel(private val preferences: AppPreferences) : ViewModel() {

    private val _currentTheme = mutableStateOf(AppTheme.SYSTEM)
    val currentTheme: State<AppTheme> = _currentTheme

    private val _isBlackThemeEnabled = mutableStateOf(false)
    val isBlackThemeEnabled: State<Boolean> = _isBlackThemeEnabled

    private val _isMaterialYouEnabled = mutableStateOf(true)
    val isMaterialYouEnabled: State<Boolean> = _isMaterialYouEnabled

    private val _hueShift = mutableStateOf(0f)
    val hueShift: State<Float> = _hueShift

    init {
        // مراقبة التغييرات في DataStore لتحديث حالة الواجهة تلقائيًا.
        viewModelScope.launch {
            preferences.getTheme().collect { themeName ->
                _currentTheme.value = AppTheme.valueOf(themeName)
            }
        }
        viewModelScope.launch {
            preferences.isBlackThemeEnabled().collect { isEnabled ->
                _isBlackThemeEnabled.value = isEnabled
            }
        }
        viewModelScope.launch {
            preferences.isMaterialYouEnabled().collect { isEnabled ->
                _isMaterialYouEnabled.value = isEnabled
            }
        }
        viewModelScope.launch {
            preferences.getHueShift().collect { shift ->
                _hueShift.value = shift
            }
        }
    }

    // الدوال التالية تقوم بحفظ الإعدادات الجديدة في DataStore.
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferences.saveTheme(theme.name)
        }
    }

    fun setBlackThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBlackThemeEnabled(enabled)
        }
    }

    fun setMaterialYouEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setMaterialYouEnabled(enabled)
        }
    }

    fun setHueShift(shift: Float) {
        viewModelScope.launch {
            preferences.saveHueShift(shift)
        }
    }
}