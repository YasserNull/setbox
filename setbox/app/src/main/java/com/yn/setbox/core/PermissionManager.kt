package com.yn.setbox.core

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.InputStream

// كائن لإدارة الصلاحيات الحساسة التي يحتاجها التطبيق.
object PermissionManager {

    // اسم حزمة التطبيق المساعد (البلوقن) الذي ينفذ الأوامر.
    private const val PLUGIN_PACKAGE_NAME = "com.yn.setbox.plugin"

    // حالات التفعيل المختلفة التي تظهر للمستخدم.
    enum class ActivationStatus {
        CHECKING, // جاري التحقق
        ACTIVATED, // مفعل بنجاح
        PLUGIN_NOT_INSTALLED, // البلوقن غير مثبت
        NEEDS_PERMISSION_SHIZUKU_AVAILABLE, // يحتاج صلاحية وشيزوكو متاح
        NEEDS_PERMISSION_ADB_REQUIRED // يحتاج صلاحية عبر ADB
    }

    // التحقق مما إذا كان البلوقن يمتلك صلاحية WRITE_SECURE_SETTINGS.
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        if (!isPluginInstalled(context)) {
            return false
        }
        return context.packageManager.checkPermission(
            Manifest.permission.WRITE_SECURE_SETTINGS,
            PLUGIN_PACKAGE_NAME
        ) == PackageManager.PERMISSION_GRANTED
    }

    // التحقق من تثبيت البلوقن.
    fun isPluginInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(PLUGIN_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // التحقق من صلاحية WRITE_SETTINGS (أقل حساسية).
    fun hasWriteSettingsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    // طلب صلاحية WRITE_SETTINGS من المستخدم.
    fun requestWriteSettingsPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        }
    }

    // التحقق مما إذا كان التطبيق يمتلك صلاحية Shizuku.
    fun isShizukuPermissionGranted(): Boolean {
        return try {
            if (Shizuku.isPreV11()) {
                true
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Exception) {
            Log.e("PermissionManager", "requestShizukuPermission called before binder was ready.", e)
        }
    }

    // محاولة منح صلاحية WRITE_SECURE_SETTINGS للبلوقن باستخدام صلاحيات الروت (su).
    suspend fun grantPermissionWithRoot(): Boolean = withContext(Dispatchers.IO) {
        val command = "pm grant $PLUGIN_PACKAGE_NAME ${Manifest.permission.WRITE_SECURE_SETTINGS}"
        Log.d("PermissionManager", "Executing su command: $command")
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.i("PermissionManager", "تم منح الصلاحيات بنجاح للبلوقن عبر su.")
                true
            } else {
                val error = process.errorStream.readText()
                Log.e("PermissionManager", "فشل أمر su للبلوقن. رمز الخروج: $exitCode. الخطأ: $error")
                false
            }
        } catch (e: Exception) {
            Log.e("PermissionManager", "استثناء أثناء منح الصلاحيات للبلوقن عبر su", e)
            false
        }
    }

    // محاولة منح صلاحية WRITE_SECURE_SETTINGS للبلوقن باستخدام Shizuku.
    suspend fun grantPermissionWithShizuku(): Boolean = withContext(Dispatchers.IO) {
        val command = "pm grant $PLUGIN_PACKAGE_NAME ${Manifest.permission.WRITE_SECURE_SETTINGS}"
        Log.d("PermissionManager", "Executing Shizuku command: $command")
        try {
            // العودة إلى استخدام الدالة القديمة لتجنب أخطاء الترجمة
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            process.waitFor()
            val exitCode = process.exitValue()
            if (exitCode == 0) {
                Log.i("PermissionManager", "تم منح الصلاحيات بنجاح للبلوقن عبر Shizuku.")
                true
            } else {
                val error = process.errorStream.readText()
                Log.e("PermissionManager", "فشل أمر Shizuku للبلوقن. رمز الخروج: $exitCode. الخطأ: $error")
                false
            }
        } catch (e: Exception) {
            Log.e("PermissionManager", "استثناء أثناء منح الصلاحيات للبلوقن عبر Shizuku", e)
            false
        }
    }

    private fun InputStream.readText(): String {
        return bufferedReader().use { it.readText() }
    }
}