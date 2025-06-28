package com.yn.setbox.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection 
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection 
import androidx.compose.ui.unit.dp
import com.yn.setbox.R
import com.yn.setbox.data.model.RemoteModule
import com.yn.setbox.ui.viewmodels.DownloadState

// عنصر يمثل وحدة (Module) واحدة متاحة للتنزيل في المستودع.
@Composable
fun RepoItem(
    module: RemoteModule,
    downloadState: DownloadState,
    onButtonClick: () -> Unit
) {
    val repositoryUrlNotFoundString = stringResource(R.string.repository_url_not_found)
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp) 
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable {
                            val url = module.repository
                            if (url.isBlank()) {
                                Toast.makeText(context, repositoryUrlNotFoundString, Toast.LENGTH_SHORT).show()
                            } else if (url.startsWith("http", ignoreCase = true)) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.error_opening_link, url), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.invalid_repository_url), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (module.description.isNotBlank()) {
                        Text(
                            text = module.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .align(Alignment.End) 
                        .padding(end = 16.dp), 
                    enabled = downloadState != DownloadState.DOWNLOADING
                ) {
                    when (downloadState) {
                        DownloadState.DOWNLOADING -> {
                            SizedCircularProgressIndicator(size = 24.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.downloading))
                        }
                        DownloadState.FAILED -> Text(stringResource(R.string.retry))
                        else -> Text(stringResource(R.string.download))
                    }
                }
            }
        }
    }
}

@Composable
fun SizedCircularProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        strokeWidth = 2.5.dp
    )
}