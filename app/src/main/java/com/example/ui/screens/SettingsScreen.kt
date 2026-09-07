package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.AVATAR_PRESETS
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface

@Composable
fun SettingsScreen(
    user: UserEntity,
    totalSessionsCount: Int,
    totalMessagesCount: Int,
    onUpdateProfile: (username: String, bio: String, photoUri: String, isLinkActive: Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSecurityInfoDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Paramètres",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SlateTextPrimary
        )
        Text(
            text = "Compte, sécurité et configuration du lien anonyme",
            fontSize = 13.sp,
            color = SlateTextSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Profile Card with Edit button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, BluePrimaryContainer, CircleShape)
                ) {
                    UserAvatar(
                        photoUriOrPreset = user.profilePicUri,
                        username = user.username,
                        size = 56.dp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Slug : @${user.uniqueLinkSlug}",
                        fontSize = 11.sp,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    onClick = { showEditProfileDialog = true },
                    shape = CircleShape,
                    color = BluePrimaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier le profil",
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Lien & Confidentialité
        Text(
            text = "LIEN ANONYME & RÉCEPTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Accepter les messages anonymes", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                            Text("Autoriser les réceptions via votre lien", fontSize = 11.sp, color = SlateTextSecondary)
                        }
                    }

                    Switch(
                        checked = user.isLinkActive,
                        onCheckedChange = { isActive ->
                            onUpdateProfile(user.username, user.bio, user.profilePicUri, isActive)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BluePrimary
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Notifications de nouveaux messages", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                            Text("Alerte instantanée à chaque message secret", fontSize = 11.sp, color = SlateTextSecondary)
                        }
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BluePrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Sécurité & Protocole
        Text(
            text = "SÉCURITÉ & PROTOCOLE 4 JOURS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                SettingActionRow(
                    icon = Icons.Default.Timer,
                    title = "Délai de compte à rebours",
                    subtitle = "Fixé à 4 jours (96 heures)",
                    onClick = { showSecurityInfoDialog = true }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                SettingActionRow(
                    icon = Icons.Default.Lock,
                    title = "Chiffrement AES-256 GCM",
                    subtitle = "Clé locale dérivée et isolée",
                    onClick = { showSecurityInfoDialog = true }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                SettingActionRow(
                    icon = Icons.Default.Storage,
                    title = "Base de données SQLite sécurisée",
                    subtitle = "$totalSessionsCount session(s) • $totalMessagesCount message(s)",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusError, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter de cette session", color = StatusError, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newUsername, newBio, newPhoto ->
                onUpdateProfile(newUsername, newBio, newPhoto, user.isLinkActive)
                showEditProfileDialog = false
            }
        )
    }

    // Security Details Dialog
    if (showSecurityInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Protocole de Sécurité", fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "1. Compte à rebours de 4 jours :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BluePrimary
                    )
                    Text(
                        "Pendant 96 heures après l'envoi, les coordonnées de l'expéditeur restent totalement scellées. Seul le message est lisible.",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "2. Historique des sessions en base :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BluePrimary
                    )
                    Text(
                        "Toutes les connexions sont sauvegardées localement dans la table Room 'sessions' avec IP simulée, nom de machine et empreinte de jeton.",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecurityInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Compris", color = Color.White)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = WhiteSurface
        )
    }
}

@Composable
fun EditProfileDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (username: String, bio: String, photoUri: String) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var photoUri by remember { mutableStateOf(user.profilePicUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Modifier mon profil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SlateTextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    photoUriOrPreset = photoUri,
                    username = username.ifBlank { "U" },
                    size = 72.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimaryContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Changer la photo", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Presets
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Avatars :", fontSize = 11.sp, color = SlateTextMuted)
                    AVATAR_PRESETS.forEach { (id, _) ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (photoUri == id) 2.dp else 1.dp,
                                    color = if (photoUri == id) BluePrimary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { photoUri = id }
                        ) {
                            UserAvatar(photoUriOrPreset = id, username = username, size = 26.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom d'utilisateur") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Message de présentation") },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, bio, photoUri) },
                enabled = username.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = SlateTextSecondary)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = WhiteSurface
    )
}

@Composable
private fun SettingActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
                Text(subtitle, fontSize = 11.sp, color = SlateTextSecondary)
            }
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SlateTextMuted, modifier = Modifier.size(20.dp))
    }
}
