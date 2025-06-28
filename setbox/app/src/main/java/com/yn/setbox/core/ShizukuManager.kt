package com.yn.setbox.core

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

/**
 * مدير Shizuku مصمم للعمل مع Jetpack Compose باستخدام StateFlow.
 * يعزل كل المنطق المعقد الخاص بـ Shizuku في مكان واحد.
 */
object ShizukuManager {

    private const val TAG = "ShizukuManager"
    private const val SHIZUKU_PERMISSION_REQUEST_CODE = 101

    // خاصية للتحقق مما إذا كان Shizuku متاحًا لتجنب الانهيار.
    private val isShizukuAvailable: Boolean by lazy {
        try {
            Shizuku.isPreV11()
            true
        } catch (e: Throwable) {
            Log.w(TAG, "إطار عمل Shizuku غير موجود. سيعمل التطبيق في وضع غير مميز.")
            false
        }
    }

    // StateFlow لعرض حالة Shizuku لواجهة المستخدم (UI) بشكل آمن.
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted = _isPermissionGranted.asStateFlow()

    // Listener لاستقبال نتيجة طلب الإذن من Shizuku.
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            _isPermissionGranted.value = (grantResult == PackageManager.PERMISSION_GRANTED)
            Log.d(TAG, "تم تحديث صلاحية Shizuku: ${_isPermissionGranted.value}")
        }
    }

    // Listener لمراقبة ما إذا كانت خدمة Shizuku قد اتصلت بالتطبيق.
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "تم استقبال Shizuku binder. الخدمة متصلة الآن.")
        checkShizukuStatus()
    }

    // Listener لمراقبة ما إذا انقطع الاتصال بخدمة Shizuku.
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "انقطع اتصال Shizuku binder. الخدمة غير متصلة.")
        _isReady.value = false
        _isPermissionGranted.value = false
    }

    /**
     * يجب استدعاء هذه الدالة مرة واحدة في `onCreate` من الـ Activity الرئيسي.
     * تقوم بتسجيل كل الـ Listeners اللازمة.
     */
    fun initialize() {
        if (!isShizukuAvailable) return

        Log.d(TAG, "تهيئة ShizukuManager وتسجيل الـ listeners.")
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)

        if (Shizuku.pingBinder()) {
            checkShizukuStatus()
        }
    }
    /**
     * يجب استدعاء هذه الدالة في `onDestroy` من الـ Activity الرئيسي.
     * تقوم بإزالة تسجيل الـ Listeners لمنع تسريب الذاكرة (Memory Leaks).
     */
    fun destroy() {
        if (!isShizukuAvailable) return
        Log.d(TAG, "تدمير ShizukuManager وإلغاء تسجيل الـ listeners.")
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
    }

    /**
     * دالة عامة لطلب إذن Shizuku.
     */
    fun requestPermission() {
        if (!isShizukuAvailable) return
        Log.d(TAG, "طلب صلاحية Shizuku من المستخدم.")
        Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
    }

    /**
     * دالة خاصة للتحقق من حالة Shizuku الحالية وتحديث الـ StateFlows.
     */
    private fun checkShizukuStatus() {
        if (!isShizukuAvailable) return

        if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            _isReady.value = false
            _isPermissionGranted.value = false
            Log.e(TAG, "إصدار Shizuku قديم جدا. يرجى تحديث تطبيق Shizuku.")
            return
        }

        val isServiceReady = Shizuku.pingBinder()
        _isReady.value = isServiceReady

        if (isServiceReady) {
            _isPermissionGranted.value = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "خدمة Shizuku جاهزة. الصلاحية ممنوحة: ${_isPermissionGranted.value}")
        } else {
            _isPermissionGranted.value = false
            Log.d(TAG, "خدمة Shizuku ليست جاهزة.")
        }
    }
}