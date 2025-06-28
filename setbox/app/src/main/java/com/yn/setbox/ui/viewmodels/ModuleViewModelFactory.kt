package com.yn.setbox.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yn.setbox.core.AppPreferences
import com.yn.setbox.data.repository.ModuleRepository

// مصنع لإنشاء ModuleViewModel وتزويده بالتبعيات اللازمة.
class ModuleViewModelFactory(
    private val appPreferences: AppPreferences,
    private val repository: ModuleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModuleViewModel(appPreferences, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}