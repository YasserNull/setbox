package com.yn.setbox.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yn.setbox.R
import com.yn.setbox.ui.viewmodels.ModuleViewModel

// المحتوى الرئيسي لشاشة الوحدات المثبتة.
@Composable
fun MainContent(
    viewModel: ModuleViewModel,
    searchQuery: String,
    onPermissionsRefresh: () -> Unit // دالة لإعادة التحقق من الصلاحيات عند السحب للتحديث.
) {
    val modules by viewModel.modules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // تصفية الوحدات بناءً على استعلام البحث.
    val filteredModules = if (searchQuery.isBlank()) {
        modules
    } else {
        modules.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true) ||
            it.description?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    // استخدام rememberSwipeRefreshState من مكتبة Accompanist القديمة لتجنب أخطاء الترجمة
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    // حاوية تدعم السحب للتحديث.
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.refreshModules()
            onPermissionsRefresh() // استدعاء دالة تحديث الصلاحيات.
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading && filteredModules.isEmpty()) {
            // عرض مؤشر التحميل في المنتصف.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredModules.isEmpty()) {
            // عرض رسالة في حالة عدم وجود وحدات أو نتائج بحث.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // جعلها قابلة للتمرير لتمكين السحب.
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val message = if (modules.isEmpty()) {
                    stringResource(id = R.string.no_modules_found)
                } else {
                    stringResource(id = R.string.no_search_results)
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            // عرض قائمة الوحدات.
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val moduleApplyFailedMessage = stringResource(id = R.string.module_apply_failed)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(items = filteredModules, key = { it.id }) { module ->
                    ModuleItem(
                        module = module,
                        onEnabledChange = { isEnabled ->
                            viewModel.setModuleEnabled(module, isEnabled, scope) { success ->
                                if (!success) {
                                    Toast.makeText(context, moduleApplyFailedMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onUninstallClick = {
                            viewModel.uninstallModule(module)
                        }
                    )
                }
            }
        }
    }
}