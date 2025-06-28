package com.yn.setbox.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yn.setbox.core.AppPreferences

// مصنع لإنشاء ThemeViewModel وتزويده بالـ AppPreferences.
class ThemeViewModelFactory(private val preferences: AppPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}