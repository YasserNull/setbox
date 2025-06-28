package com.yn.setbox.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yn.setbox.data.repository.ModuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// يستقبل حدث إقلاع الجهاز (Boot) لتطبيق إعدادات الوحدات المفعلة.
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d("BootReceiver", "تم استقبال حدث إقلاع الجهاز. بدء الخدمة.")
        val pendingResult = goAsync() // للسماح بمواصلة العمل في الخلفية بعد انتهاء onReceive.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appPreferences = AppPreferences(context)
                val moduleRepository = ModuleRepository(context)

                // الحصول على الوحدات التي كانت مفعلة قبل إعادة التشغيل.
                val enabledIds = appPreferences.getEnabledModuleIds().first()
                if (enabledIds.isEmpty()) {
                    Log.d("BootReceiver", "لا توجد وحدات مفعلة.")
                    return@launch
                }

                // تطبيق إعدادات التشغيل "on" لكل وحدة مفعلة.
                val allModules = moduleRepository.getModules()
                allModules.filter { it.id in enabledIds }.forEach { module ->
                    Log.i("BootReceiver", "تطبيق إعدادات 'on' للوحدة المفعلة عند الإقلاع: ${module.name}")
                    moduleRepository.applySettingsFromFile(module.path, "on")
                }

            } catch(e: Exception) {
                Log.e("BootReceiver", "خطأ أثناء التنفيذ عند الإقلاع", e)
            } finally {
                pendingResult.finish() // إعلام النظام بأن العمل في الخلفية قد انتهى.
                Log.d("BootReceiver", "انتهت الخدمة.")
            }
        }
    }
}