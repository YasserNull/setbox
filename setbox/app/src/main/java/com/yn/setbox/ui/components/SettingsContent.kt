package com.yn.setbox.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yn.setbox.R
import com.yn.setbox.core.*
import com.yn.setbox.ui.dialogs.LanguagesDialog
import com.yn.setbox.ui.dialogs.RestartDialog
import com.yn.setbox.ui.theme.*
import com.yn.setbox.ui.viewmodels.ThemeViewModel
import com.yn.setbox.ui.viewmodels.ThemeViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// شاشة الإعدادات الكاملة مع شريط الأدوات.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit, onLanguageChangeConfirmed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { IconButton(onClick = onBackPressed) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        SettingsContent(
            modifier = Modifier.padding(paddingValues),
            onLanguageChangeConfirmed = onLanguageChangeConfirmed
        )
    }
}

// محتوى شاشة الإعدادات.
@Composable
fun SettingsContent(modifier: Modifier = Modifier, onLanguageChangeConfirmed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(AppPreferences(context)))

    var showRestartDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentAppLanguage by LocaleManager.currentAppLanguageState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        SectionTitle(stringResource(R.string.theme))
        ThemeSettingsSection(themeViewModel)

        // تم استبدال Divider بـ HorizontalDivider
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        SectionTitle(stringResource(R.string.language))
        LanguageSettingsSection(onClick = { showLanguageDialog = true })

        // تم استبدال Divider بـ HorizontalDivider
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        SectionTitle(stringResource(R.string.community_and_contribution))
        CommunityLinksSection()
    }

    if (showLanguageDialog) {
        LanguagesDialog(
            currentLanguage = currentAppLanguage,
            onDismissRequest = { showLanguageDialog = false },
            onLanguageSelected = { selectedLanguage ->
                if (selectedLanguage != currentAppLanguage) {
                    scope.launch {
                        LocaleManager.saveLanguage(context, selectedLanguage)
                        showRestartDialog = true
                    }
                }
            }
        )
    }

    if (showRestartDialog) {
        RestartDialog(
            onDismissRequest = { showRestartDialog = false },
            onConfirm = {
                showRestartDialog = false
                onLanguageChangeConfirmed()
            }
        )
    }
}

// عنوان قسم في الإعدادات.
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// قسم إعدادات الثيم.
@Composable
fun ThemeSettingsSection(themeViewModel: ThemeViewModel) {
    val currentTheme by themeViewModel.currentTheme
    val isBlackThemeEnabled by themeViewModel.isBlackThemeEnabled
    val isMaterialYouEnabled by themeViewModel.isMaterialYouEnabled
    val hueShift by themeViewModel.hueShift
    val saturationShift by themeViewModel.saturationShift

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(Modifier.selectableGroup()) {
            AppTheme.values().forEach { theme ->
                SettingItemRow(
                    text = stringResource(id = when (theme) {
                        AppTheme.LIGHT -> R.string.theme_light
                        AppTheme.DARK -> R.string.theme_dark
                        AppTheme.SYSTEM -> R.string.theme_system
                    }),
                    selected = theme == currentTheme,
                    onClick = { themeViewModel.setTheme(theme) },
                    icon = when (theme) {
                        AppTheme.LIGHT -> Icons.Default.LightMode
                        AppTheme.DARK -> Icons.Default.DarkMode
                        AppTheme.SYSTEM -> Icons.Default.SettingsSystemDaydream
                    }
                ) { RadioButton(selected = (theme == currentTheme), onClick = null) }
            }
        }
    }

    // إظهار خيار الثيم الأسود فقط في حالة الثيم الداكن.
    AnimatedVisibility(visible = currentTheme == AppTheme.DARK || (currentTheme == AppTheme.SYSTEM && isSystemInDarkTheme())) {
        SettingItemRow(
            text = stringResource(R.string.black_theme),
            selected = isBlackThemeEnabled,
            onClick = { themeViewModel.setBlackThemeEnabled(!isBlackThemeEnabled) },
            icon = Icons.Default.InvertColors
        ) { Switch(checked = isBlackThemeEnabled, onCheckedChange = null) }
    }

    SettingItemRow(
        text = stringResource(R.string.enable_material_you),
        selected = isMaterialYouEnabled,
        onClick = { themeViewModel.setMaterialYouEnabled(!isMaterialYouEnabled) },
        icon = Icons.Default.ColorLens
    ) { Switch(checked = isMaterialYouEnabled, onCheckedChange = null) }

    // إظهار شريط تغيير اللون فقط إذا كان Material You معطلاً.
    AnimatedVisibility(visible = !isMaterialYouEnabled) {
        Column {
            HueShiftSlider(
                hueShift = hueShift,
                onHueShiftChanged = { newShift -> themeViewModel.setHueShift(newShift) },
                currentTheme = currentTheme
            )
            SaturationShiftSlider(
                saturationShift = saturationShift,
                onSaturationShiftChanged = { newShift -> themeViewModel.setSaturationShift(newShift) }
            )
        }
    }
}

// قسم إعدادات اللغة.
@Composable
fun LanguageSettingsSection(onClick: () -> Unit) {
    val currentAppLanguage by LocaleManager.currentAppLanguageState.collectAsState()
    val currentLanguageName = when (currentAppLanguage) {
        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
        AppLanguage.ARABIC -> stringResource(R.string.language_arabic)
        AppLanguage.TURKISH -> stringResource(R.string.language_turkish)
        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.language)) },
        supportingContent = { Text(currentLanguageName) },
        leadingContent = { Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(24.dp)) },
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

// قسم روابط المجتمع والمساهمة.
@Composable
fun CommunityLinksSection() {
    val context = LocalContext.current
    val sourceCodeUrl = "https://github.com/YasserNull/setbox"
    val modulesRepoUrl = "https://github.com/YasserNull/setbox-repo"

    ListItem(
        headlineContent = { Text(stringResource(R.string.source_code)) },
        supportingContent = { Text(sourceCodeUrl) },
        leadingContent = { Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(24.dp)) },
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceCodeUrl))
                context.startActivity(intent)
            },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
    ListItem(
        headlineContent = { Text(stringResource(R.string.contribute_to_modules)) },
        supportingContent = { Text(modulesRepoUrl) },
        leadingContent = { Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(24.dp)) },
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(modulesRepoUrl))
                context.startActivity(intent)
            },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

// صف عنصر إعدادات قابل لإعادة الاستخدام.
@Composable
fun SettingItemRow(text: String, selected: Boolean, onClick: () -> Unit, icon: ImageVector, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, Modifier.weight(1f))
        control()
    }
}

// شريط تمرير لتغيير درجة اللون (Hue).
@Composable
fun HueShiftSlider(
    hueShift: Float,
    onHueShiftChanged: (Float) -> Unit,
    currentTheme: AppTheme
) {
    var localSliderPosition by remember(hueShift) { mutableStateOf(hueShift) }
    val isDarkTheme = when (currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val basePrimaryColor = if (isDarkTheme) DarkColors.primary else LightColors.primary
    val dynamicThumbColor = adjustHue(basePrimaryColor, localSliderPosition)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.hue_shift))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${localSliderPosition.roundToInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dynamicThumbColor
                )
                IconButton(onClick = { onHueShiftChanged(0f) }) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.reset_hue_shift))
                }
            }
        }
        Slider(
            value = localSliderPosition,
            onValueChange = { newPosition -> localSliderPosition = newPosition },
            onValueChangeFinished = { onHueShiftChanged(localSliderPosition) },
            valueRange = -180f..180f,
            colors = SliderDefaults.colors(
                thumbColor = dynamicThumbColor,
                activeTrackColor = dynamicThumbColor.copy(alpha = 0.7f),
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// شريط تمرير جديد لتغيير تشبع اللون.
@Composable
fun SaturationShiftSlider(
    saturationShift: Float,
    onSaturationShiftChanged: (Float) -> Unit
) {
    var localSliderPosition by remember(saturationShift) { mutableStateOf(saturationShift) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.saturation_shift))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${localSliderPosition.roundToInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { onSaturationShiftChanged(0f) }) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.reset_hue_shift)) 
                }
            }
        }
        Slider(
            value = localSliderPosition,
            onValueChange = { newPosition -> localSliderPosition = newPosition },
            onValueChangeFinished = { onSaturationShiftChanged(localSliderPosition) },
            valueRange = -100f..200f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SettingsContentPreview() {
    Theme(
        currentTheme = AppTheme.SYSTEM,
        isBlackThemeEnabled = false,
        isMaterialYouEnabled = true,
        hueShift = 0f, 
        saturationShift = 0f
    ) {
        SettingsContent(modifier = Modifier.padding(PaddingValues(0.dp)), onLanguageChangeConfirmed = {})
    }
}