package com.yn.setbox.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// مصنع لإنشاء PermissionViewModel.
class PermissionViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}