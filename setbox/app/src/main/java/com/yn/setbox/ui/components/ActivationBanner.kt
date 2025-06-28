package com.yn.setbox.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yn.setbox.R
import com.yn.setbox.core.PermissionManager

// البانر الذي يظهر في أعلى الشاشة لإعلام المستخدم بحالة تفعيل التطبيق.
@Composable
fun ActivationBanner(
    activationState: PermissionManager.ActivationStatus,
    onClick: () -> Unit,
) {
    // إخفاء البانر أثناء التحقق أو إذا كان التطبيق مفعلاً لتجنب الوميض.
    val isVisible = when (activationState) {
        PermissionManager.ActivationStatus.ACTIVATED,
        PermissionManager.ActivationStatus.CHECKING -> false
        else -> true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = activationState != PermissionManager.ActivationStatus.CHECKING &&
                              activationState != PermissionManager.ActivationStatus.ACTIVATED,
                    onClick = onClick
                ),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // عرض رسالة مختلفة بناءً على حالة التفعيل.
                val textToShow = when (activationState) {
                    PermissionManager.ActivationStatus.PLUGIN_NOT_INSTALLED -> stringResource(R.string.plugin_not_installed_banner)
                    else -> stringResource(R.string.app_not_activated_banner)
                }
                Text(
                    text = textToShow,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}