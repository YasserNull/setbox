package com.yn.setbox.ui.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yn.setbox.R
import com.yn.setbox.core.AppPreferences
import com.yn.setbox.core.AppTheme
import com.yn.setbox.core.LocaleManager
import com.yn.setbox.ui.components.SettingsScreen
import com.yn.setbox.ui.theme.Theme
import com.yn.setbox.ui.viewmodels.ThemeViewModel
import com.yn.setbox.ui.viewmodels.ThemeViewModelFactory
import kotlinx.coroutines.runBlocking

class SettingsActivity : ComponentActivity() {

    // تطبيق اللغة قبل إنشاء الواجهة.
    override fun attachBaseContext(newBase: Context) {
        val appLanguage = runBlocking { LocaleManager.getSavedLanguage(newBase) }
        val localeContext = LocaleManager.applyLocaleToContext(newBase, appLanguage)
        super.attachBaseContext(localeContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = AppPreferences(this)
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(preferences))
            Theme(
                currentTheme = themeViewModel.currentTheme.value,
                isBlackThemeEnabled = themeViewModel.isBlackThemeEnabled.value,
                isMaterialYouEnabled = themeViewModel.isMaterialYouEnabled.value,
                hueShift = themeViewModel.hueShift.value
            ) {
                // الحصول على لوحة الألوان وحالة الثيم الداكن لتطبيقها على أشرطة النظام.
                val colorScheme = MaterialTheme.colorScheme
                val useDarkTheme = when (themeViewModel.currentTheme.value) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }

                val view = LocalView.current
                if (!view.isInEditMode) {
                    // حساب قيم ألوان شريط الحالة والتنقل وحالة الأيقونات.
                    val statusBarColor = colorScheme.primary.toArgb()
                    val navigationBarColor = colorScheme.background.toArgb()
                    val isLightStatusBar = !useDarkTheme
                    val isLightNavigationBar = !useDarkTheme

                    // تطبيق ألوان شريط الحالة والتنقل وحالة أيقوناتهم.
                    SideEffect {
                        val window = (view.context as Activity).window
                        window.statusBarColor = statusBarColor
                        window.navigationBarColor = navigationBarColor

                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightStatusBar
                        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLightNavigationBar
                    }
                }

                SettingsScreen(
                    onBackPressed = { finish() },
                    onLanguageChangeConfirmed = {
                        // إرسال نتيجة إلى MainActivity لإعلامها بتغيير اللغة.
                        setResult(MainActivity.RESULT_CODE_LANGUAGE_CHANGED)
                        finish()
                    }
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        // تطبيق انتقال الرسوم المتحركة عند إنهاء الـ Activity.
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.fade_out)
    }
}