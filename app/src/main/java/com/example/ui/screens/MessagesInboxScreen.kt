package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnonymousMessageEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.BlueTertiaryContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface
import com.example.util.SecurityUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesInboxScreen(
    messages: List<AnonymousMessageEntity>,
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    currentTime: Long,
    onMarkAsRead: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onUnlockImmediately: (Long) -> Unit,
    onDeleteMessage: (Long) -> Unit
) {
    var selectedMessageForDetail by remember { mutableStateOf<AnonymousMessageEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Messagerie Chiffrée",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "${messages.size} message(s) anonyme(s)",
                    fontSize = 13.sp,
                    color = SlateTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BluePrimaryContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7DCFC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AES-256", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "Tous",
                "UNREAD" to "Non lus",
                "FAVORITES" to "Favoris"
            ).forEach { (key, label) ->
                val selected = currentFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { onFilterChange(key) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = WhiteSurface,
                        labelColor = SlateTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selected) BluePrimary else SlateBorder,
                        enabled = true,
                        selected = selected
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEBF3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mail, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucun message anonyme pour l'instant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Partagez votre lien public pour commencer à recevoir des messages chiffrés avec compte à rebours !",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageCard(
                        message = msg,
                        currentTime = currentTime,
                        onClick = {
                            onMarkAsRead(msg.id)
                            selectedMessageForDetail = msg
                        },
                        onToggleFavorite = { onToggleFavorite(msg.id) }
                    )
                }
            }
        }
    }

    // Message Detail Dialog
    selectedMessageForDetail?.let { activeMsg ->
        val updatedMsg = messages.firstOrNull { it.id == activeMsg.id } ?: activeMsg
        MessageDetailDialog(
            message = updatedMsg,
            currentTime = currentTime,
            onDismiss = { selectedMessageForDetail = null },
            onUnlockNow = { onUnlockImmediately(updatedMsg.id) },
            onDelete = {
                onDeleteMessage(updatedMsg.id)
                selectedMessageForDetail = null
            }
        )
    }
}

@Composable
fun MessageCard(
    message: AnonymousMessageEntity,
    currentTime: Long,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val remaining = if (message.isRevealedManually) 0L else (message.unlockAt - currentTime).coerceAtLeast(0L)
    val isUnlocked = remaining <= 0L
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.FRENCH).format(Date(message.receivedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (!message.isRead) 1.5.dp else 1.dp,
                color = if (!message.isRead) BluePrimary else SlateBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .testTag("message_card_${message.id}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!message.isRead) Color(0xFFFAFDFE) else WhiteSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Tag, Unread dot, Date, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BluePrimaryContainer
                    ) {
                        Text(
                            text = "#${message.tag.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (!message.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BluePrimary)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = SlateTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (message.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (message.isFavorite) Color(0xFFEF4444) else SlateTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message text
            Text(
                text = message.messageText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = SlateTextPrimary,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Day Countdown Bento Pill Section
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isUnlocked) Color(0xFFECFDF5) else Color(0xFFF8FAFF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUnlocked) Color(0xFFA7F3D0) else SlateBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) StatusSuccess else BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUnlocked) "Coordonnées révélées" else "Révélation dans :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnlocked) Color(0xFF065F46) else SlateTextSecondary
                        )
                    }

                    if (!isUnlocked) {
                        Text(
                            text = SecurityUtils.formatCountdown(remaining),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = BluePrimary
                        )
                    } else {
                        Text(
                            text = "Voir coordonnées",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageDetailDialog(
    message: AnonymousMessageEntity,
    currentTime: Long,
    onDismiss: () -> Unit,
    onUnlockNow: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val remaining = if (message.isRevealedManually) 0L else (message.unlockAt - currentTime).coerceAtLeast(0L)
    val isUnlocked = remaining <= 0L
    val total4Days = AnonymousMessageEntity.FOUR_DAYS_MS.toFloat()
    val progress = ((total4Days - remaining.toFloat()) / total4Days).coerceIn(0f, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isUnlocked) StatusSuccess else BluePrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message chiffré",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SlateTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BluePrimaryContainer
                ) {
                    Text(
                        "#${message.tag.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Main quote bubble in Bento card style
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFF8FAFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "\"${message.messageText}\"",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextPrimary,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "— ${message.senderPseudo}",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = SlateTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bento Countdown Box
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color(0xFFECFDF5) else BluePrimaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isUnlocked) Color(0xFFA7F3D0) else Color(0xFFB9D7FE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) StatusSuccess else BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUnlocked) "Identité déverrouillée" else "Compte à rebours 4 jours",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isUnlocked) Color(0xFF065F46) else BluePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!isUnlocked) {
                            Text(
                                text = SecurityUtils.formatCountdown(remaining),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = BluePrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BluePrimary,
                                trackColor = Color(0xFFE1E2E9)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Les coordonnées de l'expéditeur sont scellées. Elles seront dévoilées à 0j 00h 00m 00s.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                lineHeight = 15.sp
                            )
                        } else {
                            Text(
                                text = "Le compte à rebours de 4 jours est écoulé. Les coordonnées chiffrées ont été décodées avec succès.",
                                fontSize = 12.sp,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coordinates display section
                Text(
                    text = "Coordonnées de l'expéditeur :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = WhiteSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (!isUnlocked) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = SlateTextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "••••••••••••••••••••••••",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = SlateTextMuted,
                                    letterSpacing = 2.sp
                                )
                            }
                            Text(
                                text = "Masqué jusqu'à expiration du compte à rebours",
                                fontSize = 10.sp,
                                color = SlateTextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Unlocked actual details
                            Text(
                                text = "Expéditeur : ${message.senderPseudo}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coordonnées : ${message.senderCoordinates}",
                                fontSize = 13.sp,
                                color = BluePrimary,
                                fontWeight = FontWeight.Medium
                            )
                            if (message.senderDeviceInfo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Terminal : ${message.senderDeviceInfo}",
                                    fontSize = 11.sp,
                                    color = SlateTextMuted
                                )
                            }
                        }
                    }
                }

                // Fast forward demonstration button if still locked
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onUnlockNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fast_forward_unlock_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary)
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accélérer pour le test (Déverrouiller)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Row {
                // Share as story card
                TextButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Message anonyme reçu : \"${message.messageText}\"\nEnvoyé via https://anonym.link"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager ce message"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = BluePrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Partager", color = BluePrimary)
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Fermer", color = Color.White)
                }
            }
        },
        dismissButton = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = StatusError)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = WhiteSurface
    )
}
