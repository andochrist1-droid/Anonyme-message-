package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface

enum class AppTab(val title: String) {
    LINK("Mon Lien"),
    MESSAGES("Messages"),
    SESSIONS("Sessions"),
    SETTINGS("Paramètres")
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val currentFilter by viewModel.messageFilter.collectAsStateWithLifecycle()
    val simulatedNotice by viewModel.simulatedSendSuccess.collectAsStateWithLifecycle()

    var currentTabIndex by remember { mutableIntStateOf(0) }

    val user = currentUser ?: return
    val unreadCount = messages.count { !it.isRead }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = WhiteSurface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Lien & Partage
                NavigationBarItem(
                    selected = currentTabIndex == 0,
                    onClick = { currentTabIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = if (currentTabIndex == 0) Icons.Filled.NearMe else Icons.Outlined.NearMe,
                            contentDescription = "Mon Lien",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Mon Lien",
                            fontSize = 11.sp,
                            fontWeight = if (currentTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimaryContainer,
                        unselectedIconColor = SlateTextMuted,
                        unselectedTextColor = SlateTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_link")
                )

                // Tab 1: Messages
                NavigationBarItem(
                    selected = currentTabIndex == 1,
                    onClick = { currentTabIndex = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = BluePrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTabIndex == 1) Icons.Filled.Mail else Icons.Outlined.Mail,
                                contentDescription = "Messages",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            "Messages",
                            fontSize = 11.sp,
                            fontWeight = if (currentTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimaryContainer,
                        unselectedIconColor = SlateTextMuted,
                        unselectedTextColor = SlateTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_messages")
                )

                // Tab 2: Sessions
                NavigationBarItem(
                    selected = currentTabIndex == 2,
                    onClick = { currentTabIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = if (currentTabIndex == 2) Icons.Filled.Devices else Icons.Outlined.Devices,
                            contentDescription = "Sessions",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Sessions",
                            fontSize = 11.sp,
                            fontWeight = if (currentTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimaryContainer,
                        unselectedIconColor = SlateTextMuted,
                        unselectedTextColor = SlateTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_sessions")
                )

                // Tab 3: Paramètres
                NavigationBarItem(
                    selected = currentTabIndex == 3,
                    onClick = { currentTabIndex = 3 },
                    icon = {
                        Icon(
                            imageVector = if (currentTabIndex == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Paramètres",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Paramètres",
                            fontSize = 11.sp,
                            fontWeight = if (currentTabIndex == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimaryContainer,
                        unselectedIconColor = SlateTextMuted,
                        unselectedTextColor = SlateTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBackground)
                .padding(innerPadding)
        ) {
            when (currentTabIndex) {
                0 -> ShareLinkScreen(
                    user = user,
                    totalMessagesCount = messages.size,
                    onSendSimulatedMessage = { message, pseudo, coords, tag ->
                        viewModel.sendSimulatedMessage(message, pseudo, coords, tag)
                    },
                    simulatedSuccessNotice = simulatedNotice,
                    onClearSimulatedNotice = { viewModel.clearSimulatedSendSuccess() }
                )
                1 -> MessagesInboxScreen(
                    messages = messages,
                    currentFilter = currentFilter,
                    onFilterChange = { viewModel.setMessageFilter(it) },
                    currentTime = currentTime,
                    onMarkAsRead = { viewModel.markMessageAsRead(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onUnlockImmediately = { viewModel.unlockCoordinatesImmediately(it) },
                    onDeleteMessage = { viewModel.deleteMessage(it) }
                )
                2 -> SessionsHistoryScreen(
                    sessions = sessions,
                    currentSession = currentSession,
                    onRevokeSession = { viewModel.revokeSession(it) },
                    onRevokeOtherSessions = { viewModel.revokeOtherSessions() }
                )
                3 -> SettingsScreen(
                    user = user,
                    totalSessionsCount = sessions.size,
                    totalMessagesCount = messages.size,
                    onUpdateProfile = { u, b, p, active ->
                        viewModel.updateProfile(u, b, p, active)
                    },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }
}
