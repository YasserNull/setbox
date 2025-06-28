package com.yn.setbox.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yn.setbox.data.repository.RepoRepository

// مصنع لإنشاء RepoViewModel وتزويده بالـ RepoRepository.
class RepoViewModelFactory(
    private val repository: RepoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}