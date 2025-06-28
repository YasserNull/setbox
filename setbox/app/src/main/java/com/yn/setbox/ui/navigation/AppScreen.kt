package com.yn.setbox.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.ui.graphics.vector.ImageVector
import com.yn.setbox.R

// يمثل الشاشات المختلفة في شريط التنقل السفلي.
sealed class AppScreen(val route: String, @StringRes val titleResId: Int, val icon: ImageVector) {
    // شاشة الوحدات المثبتة.
    object Modules : AppScreen("modules", R.string.screen_title_modules, Icons.Default.Extension)
    // شاشة المستودع.
    object Repo : AppScreen("repo", R.string.screen_title_repo, Icons.Outlined.CloudDownload)
}