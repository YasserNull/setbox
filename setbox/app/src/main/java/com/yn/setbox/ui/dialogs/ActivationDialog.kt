package com.yn.setbox.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yn.setbox.R

// حوار يوضح للمستخدم كيفية تفعيل التطبيق عبر ADB.
@Composable
fun ActivationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/YasserNull/setbox"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.activate_app_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                Text(stringResource(R.string.activate_app_description_adb))
                Spacer(Modifier.height(8.dp))

                // عرض أمر ADB الذي يجب على المستخدم تنفيذه.
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "adb shell pm grant com.yn.setbox.plugin android.permission.WRITE_SECURE_SETTINGS",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(stringResource(R.string.github_details))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}