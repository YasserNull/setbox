package com.yn.setbox.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yn.setbox.R

// حوار يطلب من المستخدم إعادة تشغيل التطبيق لتطبيق التغييرات (مثل تغيير اللغة).
@Composable
fun RestartDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            tonalElevation = 8.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.restart_required),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = stringResource(R.string.restart_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.restart))
                    }
                }
            }
        }
    }
}