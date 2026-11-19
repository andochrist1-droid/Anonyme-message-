package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface

enum class AppTab(val title: String) {
    LINK("Mon Lien"),
    MESSAGES("Messages"),
    SESSIONS("Sessions"),
    SETTINGS("Paramètres")
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val currentFilter by viewModel.messageFilter.collectAsStateWithLifecycle()
    val actionNotice by viewModel.actionNotice.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    var currentTabIndex by remember { mutableIntStateOf(0) }

    val messages = (messagesState as? UiState.Success)?.data ?: emptyList()
    val sessions = (sessionsState as? UiState.Success)?.data ?: emptyList()
    val unreadCount = messages.count { !it.read }

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
            when (val profileState = userProfileState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = BluePrimary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Synchronisation avec Cloud Firestore...",
                            fontSize = 14.sp,
                            color = SlateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = StatusError,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Impossible de charger votre profil Firestore",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profileState.message,
                            fontSize = 13.sp,
                            color = SlateTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Se déconnecter", color = Color.White)
                        }
                    }
                }

                is UiState.Success -> {
                    val user = profileState.data
                    if (user == null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = BluePrimary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Initialisation du profil Cloud...",
                                fontSize = 14.sp,
                                color = SlateTextSecondary
                            )
                        }
                    } else {
                        when (currentTabIndex) {
                            0 -> ShareLinkScreen(
                                user = user,
                                totalMessagesCount = messages.size,
                                onSendSimulatedMessage = { message, pseudo, coords, tag ->
                                    viewModel.sendSimulatedMessage(message, pseudo, coords, tag)
                                },
                                simulatedSuccessNotice = actionNotice,
                                onClearSimulatedNotice = { viewModel.clearActionNotice() }
                            )
                            1 -> MessagesInboxScreen(
                                messages = messages,
                                currentFilter = currentFilter,
                                onFilterChange = { viewModel.setMessageFilter(it) },
                                currentTime = currentTime,
                                onMarkAsRead = { viewModel.markMessageAsRead(it) },
                                onToggleFavorite = { id, currentFav -> viewModel.toggleFavorite(id, currentFav) },
                                onUnlockImmediately = { viewModel.unlockCoordinatesImmediately(it) },
                                onDeleteMessage = { viewModel.deleteMessage(it) }
                            )
                            2 -> SessionsHistoryScreen(
                                sessions = sessions,
                                currentSessionId = currentSessionId,
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
                                onLogout = onSignOut
                            )
                        }
                    }
                }
            }

            if (actionError != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    containerColor = StatusError,
                    contentColor = Color.White,
                    action = {
                        TextButton(onClick = { viewModel.clearActionError() }) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Text(actionError ?: "")
                }
            }
        }
    }
}
