package com.yn.setbox.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yn.setbox.R
import com.yn.setbox.core.AppTheme
import com.yn.setbox.ui.theme.Theme

// شريط الأدوات العلوي الرئيسي للتطبيق.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInstallFromZipClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
            // قائمة الإجراءات الإضافية (مثل الإعدادات والتثبيت).
            AppToolbarActions(
                onSettingsClick = onSettingsClick,
                onInstallFromZipClick = onInstallFromZipClick
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AppToolbarPreview() {
    Theme(
        currentTheme = AppTheme.SYSTEM,
        isBlackThemeEnabled = false,
        isMaterialYouEnabled = true,
        hueShift = 0f, 
        saturationShift = 0f
    ) {
        AppToolbar(
            onSearchClick = {},
            onSettingsClick = {},
            onInstallFromZipClick = {}
        )
    }
}