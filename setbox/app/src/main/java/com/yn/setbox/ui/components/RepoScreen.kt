package com.yn.setbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yn.setbox.R
import com.yn.setbox.data.model.Module
import com.yn.setbox.ui.viewmodels.DownloadState
import com.yn.setbox.ui.viewmodels.RepoViewModel

// شاشة عرض الوحدات المتاحة للتنزيل من المستودع.
@Composable
fun RepoScreen(
    viewModel: RepoViewModel,
    installedModules: List<Module>,
    onDownloadComplete: () -> Unit,
    searchQuery: String,
    onPermissionsRefresh: () -> Unit // دالة لإعادة التحقق من الصلاحيات.
) {
    val remoteModules by viewModel.modules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val downloadStates by viewModel.downloadStates.collectAsState()
    val errorMessageKey by viewModel.errorMessage.collectAsState()
    val rawJsonForDebug by viewModel.rawJsonForDebug.collectAsState()

    val installedModuleIds = remember(installedModules) { installedModules.map { it.id }.toSet() }

    // إخفاء الوحدات المثبتة بالفعل من قائمة المستودع.
    val modulesToShow = remember(remoteModules, installedModuleIds) {
        remoteModules.filter { it.id !in installedModuleIds }
    }

    // تصفية الوحدات بناءً على استعلام البحث.
    val filteredModulesToShow = if (searchQuery.isBlank()) {
        modulesToShow
    } else {
        modulesToShow.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    // استخدام rememberSwipeRefreshState من مكتبة Accompanist القديمة لتجنب أخطاء الترجمة
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.fetchRepoModules()
            onPermissionsRefresh() // استدعاء دالة تحديث الصلاحيات.
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            // حالة التحميل الأولية.
            isLoading && remoteModules.isEmpty() && errorMessageKey == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // حالة وجود خطأ في تحميل المستودع.
            !errorMessageKey.isNullOrEmpty() -> {
                val displayMessage = when (errorMessageKey) {
                    "repo_file_empty" -> stringResource(id = R.string.repo_file_empty)
                    "repo_load_failed" -> stringResource(id = R.string.repo_load_failed)
                    else -> errorMessageKey
                }
                ErrorStateView(displayMessage, rawJsonForDebug)
            }
            // حالة عدم وجود وحدات للعرض.
            !isLoading && filteredModulesToShow.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    val message = if (searchQuery.isNotBlank()) {
                        stringResource(id = R.string.no_search_results)
                    } else if (remoteModules.isEmpty()) {
                        stringResource(R.string.repo_is_empty)
                    } else {
                        stringResource(R.string.all_modules_up_to_date)
                    }
                    Text(message, textAlign = TextAlign.Center)
                }
            }
            // الحالة الافتراضية: عرض قائمة الوحدات.
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredModulesToShow, key = { it.repository }) { module ->
                        RepoItem(
                            module = module,
                            downloadState = downloadStates[module.repository] ?: DownloadState.IDLE,
                            onButtonClick = { viewModel.downloadModule(module, onDownloadComplete) }
                        )
                    }
                }
            }
        }
    }
}

// واجهة عرض الخطأ مع معلومات للمساعدة في تصحيحه.
@Composable
private fun ErrorStateView(errorMessage: String?, rawJson: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorMessage ?: stringResource(id = R.string.unknown_error_occurred),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        if (!rawJson.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.raw_response_for_debugging),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = rawJson,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}