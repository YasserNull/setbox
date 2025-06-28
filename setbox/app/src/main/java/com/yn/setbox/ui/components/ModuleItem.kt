package com.yn.setbox.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection // New import
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection // New import
import androidx.compose.ui.unit.dp
import com.yn.setbox.R
import com.yn.setbox.data.model.Module
import kotlinx.coroutines.delay

@Composable
fun ModuleItem(
    module: Module,
    onEnabledChange: (Boolean) -> Unit,
    onUninstallClick: () -> Unit
) {
    val repositoryUrlNotFoundString = stringResource(R.string.repository_url_not_found)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val context = LocalContext.current

                        Text(
                            text = module.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .clickable {
                                    module.repository?.let { url ->
                                        if (url.startsWith("http")) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    } ?: Toast.makeText(context, repositoryUrlNotFoundString, Toast.LENGTH_SHORT).show()
                                }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = module.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        module.description?.let {
                            if (it.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    var isToggleLoading by remember { mutableStateOf(false) }

                    Switch(
                        checked = module.isEnabled,
                        onCheckedChange = {
                            isToggleLoading = true
                            onEnabledChange(it)
                        },
                        thumbContent = if (isToggleLoading) {
                            { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) }
                        } else null
                    )

                    LaunchedEffect(module.isEnabled) {
                        isToggleLoading = false
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUninstallClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.uninstall))
                }
            }
        }
    }
}