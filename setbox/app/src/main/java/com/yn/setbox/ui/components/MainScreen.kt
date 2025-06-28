package com.yn.setbox.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.yn.setbox.R
import com.yn.setbox.core.PermissionManager
import com.yn.setbox.ui.dialogs.ActivationDialog
import com.yn.setbox.ui.navigation.AppScreen
import com.yn.setbox.ui.viewmodels.ModuleViewModel
import com.yn.setbox.ui.viewmodels.PermissionViewModel
import com.yn.setbox.ui.viewmodels.RepoViewModel

// الواجهة الرئيسية للتطبيق التي تحتوي على شريط الأدوات، التنقل السفلي، ومحتوى الشاشات.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    moduleViewModel: ModuleViewModel,
    repoViewModel: RepoViewModel,
    permissionViewModel: PermissionViewModel,
    onSettingsClick: () -> Unit,
    onInstallFromZipClick: () -> Unit
) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.primary
    val navigationBarColor = MaterialTheme.colorScheme.surface

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current
    val permissionState by permissionViewModel.activationState.collectAsState()
    val showDialog by permissionViewModel.showDialog.collectAsState()

    // SideEffect لضبط ألوان النظام والتركيز على حقل البحث.
    SideEffect {
        systemUiController.setStatusBarColor(color = statusBarColor, darkIcons = statusBarColor.luminance() > 0.5f)
        systemUiController.setNavigationBarColor(color = navigationBarColor, darkIcons = navigationBarColor.luminance() > 0.5f)
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    if (showDialog) {
        ActivationDialog(onDismiss = { permissionViewModel.dismissDialog() })
    }

    Scaffold(
        topBar = {
            // التبديل بين شريط الأدوات العادي وشريط البحث.
            if (isSearchActive) {
                SearchToolbar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        isSearchActive = false
                        searchQuery = ""
                    },
                    focusRequester = focusRequester
                )
            } else {
                AppToolbar(
                    onSearchClick = { isSearchActive = true },
                    onSettingsClick = onSettingsClick,
                    onInstallFromZipClick = onInstallFromZipClick
                )
            }
        },
        bottomBar = {
            // شريط التنقل السفلي.
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val navItems = listOf(AppScreen.Modules, AppScreen.Repo)

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleResId)) },
                        label = { Text(stringResource(screen.titleResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // بانر التفعيل الذي يظهر في الأعلى عند الحاجة.
            ActivationBanner(
                activationState = permissionState,
                onClick = {
                    if (permissionState == PermissionManager.ActivationStatus.PLUGIN_NOT_INSTALLED) {
                        // إذا لم يكن البلوقن مثبتًا، يفتح رابط GitHub مباشرة.
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YasserNull/setbox"))
                        context.startActivity(intent)
                    } else {
                        // في حالات الفشل الأخرى، يطلب إظهار حوار المساعدة.
                        permissionViewModel.onActivationHelpRequested()
                    }
                }
            )

            // NavHost لإدارة التنقل بين الشاشات (الوحدات والمستودع).
            NavHost(navController, startDestination = AppScreen.Modules.route, modifier = Modifier) {
                composable(AppScreen.Modules.route) {
                    MainContent(
                        viewModel = moduleViewModel,
                        searchQuery = searchQuery,
                        onPermissionsRefresh = { permissionViewModel.checkAndTryToActivate(context) }
                    )
                }
                composable(AppScreen.Repo.route) {
                    val installedModules by moduleViewModel.modules.collectAsState()
                    RepoScreen(
                        viewModel = repoViewModel,
                        installedModules = installedModules,
                        onDownloadComplete = { moduleViewModel.refreshModules() },
                        searchQuery = searchQuery,
                        onPermissionsRefresh = { permissionViewModel.checkAndTryToActivate(context) }
                    )
                }
            }
        }
    }
}

// شريط الأدوات الخاص بالبحث.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onPrimary),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_modules_placeholder),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}