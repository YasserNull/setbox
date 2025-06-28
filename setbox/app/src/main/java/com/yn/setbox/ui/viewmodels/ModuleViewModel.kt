package com.yn.setbox.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yn.setbox.R
import com.yn.setbox.core.AppPreferences
import com.yn.setbox.data.model.Module
import com.yn.setbox.data.repository.ModuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ViewModel لإدارة الوحدات المثبتة.
class ModuleViewModel(
    private val appPreferences: AppPreferences,
    private val repository: ModuleRepository
) : ViewModel() {

    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // مراقبة التغييرات في الوحدات المفعلة لتحديث الواجهة.
        viewModelScope.launch {
            appPreferences.getEnabledModuleIds().collect { enabledIds ->
                val currentModules = _modules.value
                if (currentModules.isNotEmpty() || enabledIds.isNotEmpty()) {
                    _modules.value = currentModules.map { it.copy(isEnabled = it.id in enabledIds) }
                }
            }
        }
        loadModules(isInitialLoad = true)
    }

    fun refreshModules() {
        loadModules(isInitialLoad = false)
    }

    private fun loadModules(isInitialLoad: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedModules = repository.getModules()
                val enabledIds = appPreferences.getEnabledModuleIds().first()
                val updatedModules = fetchedModules.map { module ->
                    module.copy(isEnabled = module.id in enabledIds)
                }
                _modules.value = updatedModules

                // عند التحميل الأولي، يتم تطبيق إعدادات "on" للوحدات المفعلة لضمان استمراريتها.
                if (isInitialLoad) {
                    updatedModules.filter { it.isEnabled }.forEach { module ->
                        repository.applySettingsFromFile(module.path, "on")
                    }
                }
            } catch (e: Exception) {
                _modules.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun installModuleFromZip(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.installFromZip(uri, context)
            if (success) {
                Toast.makeText(context, context.getString(R.string.install_success), Toast.LENGTH_SHORT).show()
                refreshModules() // إعادة تحميل قائمة الوحدات لإظهار الوحدة الجديدة.
            } else {
                Toast.makeText(context, context.getString(R.string.install_failed), Toast.LENGTH_LONG).show()
            }
            _isLoading.value = false
        }
    }

    /**
     * تمكين أو تعطيل وحدة وتطبيق الإعدادات الموافقة.
     * لا يتم حفظ الحالة بشكل دائم إلا إذا نجحت عملية تطبيق الإعدادات.
     */
    fun setModuleEnabled(
        module: Module,
        isEnabled: Boolean,
        scope: CoroutineScope,
        onResult: (success: Boolean) -> Unit
    ) {
        scope.launch {
            val commandFileToRun = if (isEnabled) "on" else "off"
            val success = repository.applySettingsFromFile(module.path, commandFileToRun)
            
            // إذا نجحت العملية، قم بحفظ الحالة الجديدة بشكل دائم.
            if (success) {
                val currentEnabledIds = appPreferences.getEnabledModuleIds().first().toMutableSet()
                if (isEnabled) {
                    currentEnabledIds.add(module.id)
                } else {
                    currentEnabledIds.remove(module.id)
                }
                appPreferences.saveEnabledModuleIds(currentEnabledIds)
            }
            onResult(success)
        }
    }

    fun uninstallModule(module: Module) {
        viewModelScope.launch {
            val success = repository.uninstallModule(module)
            if (success) {
                // إزالة الوحدة من قائمة الوحدات المفعلة إذا كانت موجودة.
                val currentEnabledIds = appPreferences.getEnabledModuleIds().first().toMutableSet()
                if (currentEnabledIds.remove(module.id)) {
                    appPreferences.saveEnabledModuleIds(currentEnabledIds)
                }
                refreshModules() // تحديث القائمة بعد الإزالة.
            }
        }
    }
}