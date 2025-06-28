package com.yn.setbox.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yn.setbox.R
import com.yn.setbox.core.AppLanguage

// حوار لاختيار لغة التطبيق.
@Composable
fun LanguagesDialog(
    currentLanguage: AppLanguage,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // قائمة اللغات القابلة للتمرير.
                Column(
                    Modifier
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                ) {
                    AppLanguage.values().forEach { language ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = when (language) {
                                        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                                        AppLanguage.ARABIC -> stringResource(R.string.language_arabic)
                                        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                                    }
                                )
                            },
                            modifier = Modifier.selectable(
                                selected = (language == currentLanguage),
                                onClick = {
                                    // يتم استدعاء الدالة فوراً عند الاختيار وإغلاق الحوار.
                                    onLanguageSelected(language)
                                    onDismissRequest() 
                                },
                                role = Role.RadioButton
                            ),
                            leadingContent = {
                                RadioButton(selected = (language == currentLanguage), onClick = null)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // زر الإغلاق.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(R.string.close))
                    }
                }
            }
        }
    }
}