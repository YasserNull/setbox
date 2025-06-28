package com.yn.setbox.data.repository

import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * هذا الكلاس مسؤول عن تنفيذ أوامر تغيير الإعدادات باستخدام Android API مباشرة.
 * تم استبدال استخدامه المباشر بنظام البلوقن، ولكنه يمثل المنطق الذي يستخدمه البلوقن داخليًا.
 */
object SettingsApiManager {

    private const val TAG = "SettingsApiManager"

    /**
     * يفسر وينفذ أمرًا واحدًا لتغيير إعداد في النظام.
     * @param context الـ Context للوصول إلى ContentResolver.
     * @param commandLine السطر الكامل من الملف، مثال: "system show_touches 1".
     */
    fun executeCommand(context: Context, commandLine: String) {
        val parts = commandLine.trim().split("\\s+".toRegex())
        if (parts.size < 3) return

        val table = parts[0].lowercase() // "system", "global", "secure"
        val key = parts[1]
        val valueStr = parts.drop(2).joinToString(" ")

        try {
            val contentResolver = context.contentResolver
            val success = when (table) {
                "system" -> putDynamicValue(Settings.System::class.java, contentResolver, key, valueStr)
                "global" -> putDynamicValue(Settings.Global::class.java, contentResolver, key, valueStr)
                "secure" -> putDynamicValue(Settings.Secure::class.java, contentResolver, key, valueStr)
                else -> return
            }

            if (!success) {
                Log.e(TAG, "فشل تنفيذ الأمر: settings put $table $key '$valueStr'")
            }

        } catch (e: Exception) {
            Log.e(TAG, "فشل تنفيذ الأمر مع استثناء: '$commandLine'", e)
        }
    }

    /**
     * يجرب تخزين القيمة باستخدام putInt، putFloat، و putString بالترتيب.
     * @return true إذا نجحت إحدى العمليات، false إذا فشلت جميعها.
     */
    private fun putDynamicValue(
        settingsClass: Class<*>,
        contentResolver: android.content.ContentResolver,
        key: String,
        valueStr: String
    ): Boolean {
        // محاولة putInt إذا كانت القيمة عددًا صحيحًا.
        if (valueStr.toIntOrNull() != null) {
            try {
                val success = settingsClass.getMethod("putInt", android.content.ContentResolver::class.java, String::class.java, Int::class.javaPrimitiveType)
                    .invoke(null, contentResolver, key, valueStr.toInt()) as Boolean
                if (success) return true
            } catch (e: Exception) { /* تجاهل ومتابعة */ }
        }

        // محاولة putFloat إذا كانت القيمة عددًا عشريًا.
        if (valueStr.toFloatOrNull() != null) {
            try {
                val success = settingsClass.getMethod("putFloat", android.content.ContentResolver::class.java, String::class.java, Float::class.javaPrimitiveType)
                    .invoke(null, contentResolver, key, valueStr.toFloat()) as Boolean
                if (success) return true
            } catch (e: Exception) { /* تجاهل ومتابعة */ }
        }

        // محاولة putString كحل أخير.
        try {
            val success = settingsClass.getMethod("putString", android.content.ContentResolver::class.java, String::class.java, String::class.java)
                .invoke(null, contentResolver, key, valueStr) as Boolean
            if (success) return true
        } catch (e: Exception) { /* تجاهل ومتابعة */ }

        return false
    }
}