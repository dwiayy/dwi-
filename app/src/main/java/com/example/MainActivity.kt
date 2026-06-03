package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainTab
import com.example.viewmodel.RemindViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RemindFlowApp()
            }
        }
    }
}

@Composable
fun RemindFlowApp(
    viewModel: RemindViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            AppScreen.LOGIN -> {
                LoginScreen(viewModel = viewModel)
            }
            AppScreen.MAIN -> {
                Scaffold(
                    bottomBar = {
                        RemindFlowBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            MainTab.HOME -> HomeScreen(viewModel = viewModel)
                            MainTab.RUTINITAS -> RoutineScreen(viewModel = viewModel)
                            MainTab.NOTIFIKASI -> NotificationScreen(viewModel = viewModel)
                            MainTab.ADMIN -> AdminScreen(viewModel = viewModel)
                        }
                    }
                }
            }
            AppScreen.ADD_TASK -> {
                AddTaskScreen(viewModel = viewModel, isEditMode = false)
            }
            AppScreen.EDIT_TASK -> {
                AddTaskScreen(viewModel = viewModel, isEditMode = true)
            }
            AppScreen.TASK_DETAIL -> {
                TaskDetailScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RemindFlowBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = DeepNavy,
        contentColor = NeutralWhite,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        // Tab 1: HOME (Dashboard)
        NavigationBarItem(
            selected = currentTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeutralWhite,
                selectedTextColor = OrangeLight,
                unselectedIconColor = TextLightGray,
                unselectedTextColor = TextLightGray,
                indicatorColor = OrangeAccent
            ),
            modifier = Modifier.testTag("tab_home")
        )

        // Tab 2: RUTINITAS
        NavigationBarItem(
            selected = currentTab == MainTab.RUTINITAS,
            onClick = { onTabSelected(MainTab.RUTINITAS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.RUTINITAS) Icons.Default.CalendarToday else Icons.Outlined.CalendarToday,
                    contentDescription = "Rutinitas",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text("Rutinitas") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeutralWhite,
                selectedTextColor = OrangeLight,
                unselectedIconColor = TextLightGray,
                unselectedTextColor = TextLightGray,
                indicatorColor = OrangeAccent
            ),
            modifier = Modifier.testTag("tab_rutinitas")
        )

        // Tab 3: NOTIFIKASI
        NavigationBarItem(
            selected = currentTab == MainTab.NOTIFIKASI,
            onClick = { onTabSelected(MainTab.NOTIFIKASI) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.NOTIFIKASI) Icons.Default.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Notifikasi") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeutralWhite,
                selectedTextColor = OrangeLight,
                unselectedIconColor = TextLightGray,
                unselectedTextColor = TextLightGray,
                indicatorColor = OrangeAccent
            ),
            modifier = Modifier.testTag("tab_notifikasi")
        )

        // Tab 4: ADMIN
        NavigationBarItem(
            selected = currentTab == MainTab.ADMIN,
            onClick = { onTabSelected(MainTab.ADMIN) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.ADMIN) Icons.Default.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                    contentDescription = "Admin",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Admin") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeutralWhite,
                selectedTextColor = OrangeLight,
                unselectedIconColor = TextLightGray,
                unselectedTextColor = TextLightGray,
                indicatorColor = OrangeAccent
            ),
            modifier = Modifier.testTag("tab_admin")
        )
    }
}
