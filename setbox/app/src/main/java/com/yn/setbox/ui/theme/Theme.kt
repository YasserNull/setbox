package com.yn.setbox.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yn.setbox.core.AppTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Composable الرئيسي لتطبيق الثيم على التطبيق.
@Composable
fun Theme(
    currentTheme: AppTheme,
    isBlackThemeEnabled: Boolean,
    isMaterialYouEnabled: Boolean,
    hueShift: Float,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (currentTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current

    // الخطوة 1: تحديد لوحة الألوان الأساسية (ديناميكية من النظام أو ثابتة).
    val baseColorScheme = when {
        isMaterialYouEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }
    
    // الخطوة 2: تطبيق إزاحة اللون (Hue Shift) إذا كان Material You معطلاً.
    val preBlackThemeScheme = if (isMaterialYouEnabled) {
        baseColorScheme
    } else {
        applyHueShiftToScheme(baseColorScheme, hueShift)
    }

    // الخطوة 3: تطبيق الثيم الأسود (AMOLED) فوق اللوحة الناتجة إذا كان مفعلاً.
    val finalColorScheme = if (useDarkTheme && isBlackThemeEnabled) {
        preBlackThemeScheme.copy(
            background = Color.Black,
            surface = Color.Black
            // ... يمكن إضافة باقي ألوان السطح هنا
        )
    } else {
        preBlackThemeScheme
    }

    // ضبط ألوان شريط الحالة والنظام.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = finalColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}

// يطبق إزاحة اللون على جميع ألوان اللوحة.
 fun applyHueShiftToScheme(colorScheme: ColorScheme, hueShift: Float): ColorScheme {
    if (hueShift == 0f) return colorScheme
    return colorScheme.copy(
        primary = adjustHue(colorScheme.primary, hueShift),
        onPrimary = adjustHue(colorScheme.onPrimary, hueShift),
        primaryContainer = adjustHue(colorScheme.primaryContainer, hueShift),
        onPrimaryContainer = adjustHue(colorScheme.onPrimaryContainer, hueShift),
        secondary = adjustHue(colorScheme.secondary, hueShift),
        onSecondary = adjustHue(colorScheme.onSecondary, hueShift),
        secondaryContainer = adjustHue(colorScheme.secondaryContainer, hueShift),
        onSecondaryContainer = adjustHue(colorScheme.onSecondaryContainer, hueShift),
        tertiary = adjustHue(colorScheme.tertiary, hueShift),
        onTertiary = adjustHue(colorScheme.onTertiary, hueShift),
        tertiaryContainer = adjustHue(colorScheme.tertiaryContainer, hueShift),
        onTertiaryContainer = adjustHue(colorScheme.onTertiaryContainer, hueShift),
        background = adjustHue(colorScheme.background, hueShift),
        onBackground = adjustHue(colorScheme.onBackground, hueShift),
        surface = adjustHue(colorScheme.surface, hueShift),
        onSurface = adjustHue(colorScheme.onSurface, hueShift),
        surfaceVariant = adjustHue(colorScheme.surfaceVariant, hueShift),
        onSurfaceVariant = adjustHue(colorScheme.onSurfaceVariant, hueShift),
        error = adjustHue(colorScheme.error, hueShift),
        onError = adjustHue(colorScheme.onError, hueShift),
        errorContainer = adjustHue(colorScheme.errorContainer, hueShift),
        onErrorContainer = adjustHue(colorScheme.onErrorContainer, hueShift),
        outline = adjustHue(colorScheme.outline, hueShift)
    )
}

// دالة مساعدة لتعديل درجة اللون (Hue) للون معين.
 fun adjustHue(color: Color, adjustment: Float): Color {
    val hsl = color.toHsl()
    val newHue = (hsl[0] + adjustment + 360f) % 360f
    return hslToColor(newHue, hsl[1], hsl[2])
}

// تحويل اللون من RGB إلى HSL.
 fun Color.toHsl(): FloatArray {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    var h = 0f
    val s: Float
    val l = (max + min) / 2
    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = if (l < 0.5f) delta / (max + min) else delta / (2 - max - min)
        when (max) {
            r -> h = ((g - b) / delta) % 6
            g -> h = (b - r) / delta + 2
            b -> h = (r - g) / delta + 4
        }
        h *= 60f
        if (h < 0) h += 360f
    }
    return floatArrayOf(h, s, l)
}

// تحويل اللون من HSL إلى RGB.
 fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f % 2f) - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(red = (r + m), green = (g + m), blue = (b + m))
}