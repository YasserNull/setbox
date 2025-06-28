package com.yn.setbox.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yn.setbox.R
import com.yn.setbox.core.AppTheme
import com.yn.setbox.ui.theme.Theme

// القائمة المنسدلة (الإجراءات) في شريط الأدوات العلوي.
@Composable
fun AppToolbarActions(
    onSettingsClick: () -> Unit,
    onInstallFromZipClick: () -> Unit // معالج النقر للتثبيت من ZIP.
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = !showMenu }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            // خيار "تثبيت من ZIP".
            DropdownMenuItem(
                text = { Text(stringResource(R.string.install_from_zip)) },
                onClick = {
                    onInstallFromZipClick()
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings)) },
                onClick = {
                    onSettingsClick()
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppToolbarActionsPreview() {
    Theme(
        currentTheme = AppTheme.SYSTEM,
        isBlackThemeEnabled = false,
        isMaterialYouEnabled = true,
        hueShift = 0f
    ) {
        AppToolbarActions(onSettingsClick = {}, onInstallFromZipClick = {})
    }
}