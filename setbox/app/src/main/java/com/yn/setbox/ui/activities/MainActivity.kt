package com.yn.setbox.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yn.setbox.R
import com.yn.setbox.core.AppPreferences
import com.yn.setbox.core.LocaleManager
import com.yn.setbox.core.ShizukuManager
import com.yn.setbox.data.repository.ModuleRepository
import com.yn.setbox.data.repository.RepoRepository
import com.yn.setbox.ui.components.MainScreen
import com.yn.setbox.ui.theme.Theme
import com.yn.setbox.ui.viewmodels.*
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var zipFileLauncher: ActivityResultLauncher<String>
    private val permissionViewModel: PermissionViewModel by viewModels { PermissionViewModelFactory() }

    // تطبيق اللغة قبل إنشاء الواجهة لضمان عرضها باللغة الصحيحة.
    override fun attachBaseContext(newBase: Context) {
        val appLanguage = runBlocking { LocaleManager.getSavedLanguage(newBase) }
        val localeContext = LocaleManager.applyLocaleToContext(newBase, appLanguage)
        super.attachBaseContext(localeContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تهيئة مدير Shizuku عند إنشاء الـ Activity.
        ShizukuManager.initialize()

        val appPreferences = AppPreferences(this)
        val moduleRepository = ModuleRepository(this)
        val repoRepository = RepoRepository(this)

        setContent {
            val moduleViewModel: ModuleViewModel = viewModel(factory = ModuleViewModelFactory(appPreferences, moduleRepository))
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(appPreferences))
            val repoViewModel: RepoViewModel = viewModel(factory = RepoViewModelFactory(repoRepository))

            val isShizukuReady by ShizukuManager.isReady.collectAsState()
            val isShizukuPermissionGranted by ShizukuManager.isPermissionGranted.collectAsState()

            // LaunchedEffect لطلب صلاحية Shizuku تلقائيًا عند جاهزيته.
            LaunchedEffect(isShizukuReady, isShizukuPermissionGranted) {
                if (isShizukuReady && !isShizukuPermissionGranted) {
                    ShizukuManager.requestPermission()
                }
            }

            // LaunchedEffect لتشغيل محاولة التفعيل التلقائي عند بدء التشغيل أو تغير حالة Shizuku.
            LaunchedEffect(isShizukuReady, isShizukuPermissionGranted) {
                permissionViewModel.checkAndTryToActivate(this@MainActivity)
            }

            // Launcher لاختيار ملف ZIP لتثبيت وحدة جديدة.
            zipFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    Toast.makeText(this, getString(R.string.installing_module), Toast.LENGTH_SHORT).show()
                    moduleViewModel.installModuleFromZip(uri, this)
                }
            }

            // Launcher لمراقبة نتيجة العودة من شاشة الإعدادات.
            val languageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                // إذا تغيرت اللغة، أعد إنشاء الـ Activity لتطبيق التغيير.
                if (result.resultCode == RESULT_CODE_LANGUAGE_CHANGED) {
                    this.recreate()
                }
            }

            Theme(
                currentTheme = themeViewModel.currentTheme.value,
                isBlackThemeEnabled = themeViewModel.isBlackThemeEnabled.value,
                isMaterialYouEnabled = themeViewModel.isMaterialYouEnabled.value,
                hueShift = themeViewModel.hueShift.value
            ) {
                MainScreen(
                    moduleViewModel = moduleViewModel,
                    repoViewModel = repoViewModel,
                    permissionViewModel = permissionViewModel,
                    onSettingsClick = {
                        val intent = Intent(this, SettingsActivity::class.java)
                        languageLauncher.launch(intent)
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out)
                    },
                    onInstallFromZipClick = { zipFileLauncher.launch("application/zip") }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // تنظيف موارد Shizuku لمنع تسرب الذاكرة.
        ShizukuManager.destroy()
    }

    companion object {
        // رمز نتيجة مخصص للإشارة إلى أن اللغة قد تغيرت.
        const val RESULT_CODE_LANGUAGE_CHANGED = 1001
    }
}