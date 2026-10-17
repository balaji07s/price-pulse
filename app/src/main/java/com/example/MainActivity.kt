package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CompareStoresScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrackedListScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PricePulseTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ThemeSetting

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle intent extra from notification click
        val initialProductId = intent?.getLongExtra("selected_product_id", -1L)?.takeIf { it != -1L }
        if (initialProductId != null) {
            viewModel.navigateTo(AppScreen.PRODUCT_DETAIL, initialProductId)
        }

        setContent {
            val themeSetting by viewModel.themeSetting.collectAsState()
            val isDark = when (themeSetting) {
                ThemeSetting.DARK -> true
                ThemeSetting.LIGHT -> false
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            PricePulseTheme(darkTheme = isDark) {
                // Request notification permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        viewModel.setNotificationsEnabled(isGranted)
                    }

                    LaunchedEffect(Unit) {
                        if (!viewModel.notificationHelper.hasNotificationPermission()) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                PricePulseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PricePulseApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.DASHBOARD) Icons.Filled.GridView else Icons.Outlined.GridView,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.TRACKED_ITEMS,
                    onClick = { viewModel.navigateTo(AppScreen.TRACKED_ITEMS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.TRACKED_ITEMS) Icons.Filled.ListAlt else Icons.Outlined.ListAlt,
                            contentDescription = "Tracked"
                        )
                    },
                    label = { Text("Tracked", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_tracked")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.COMPARE_STORES,
                    onClick = { viewModel.navigateTo(AppScreen.COMPARE_STORES) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.COMPARE_STORES) Icons.Filled.CompareArrows else Icons.Outlined.CompareArrows,
                            contentDescription = "Compare"
                        )
                    },
                    label = { Text("Compare", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_compare")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.TRACKED_ITEMS -> TrackedListScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.COMPARE_STORES -> CompareStoresScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = screenModifier)
        }
    }
}
