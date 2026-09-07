package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionLogEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.BlueOnPrimaryContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionsHistoryScreen(
    sessions: List<SessionLogEntity>,
    currentSession: SessionLogEntity?,
    onRevokeSession: (Long) -> Unit,
    onRevokeOtherSessions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Journal des Connexions",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateTextPrimary
                )
                Text(
                    text = "Sessions enregistrées en base de données sécurisée",
                    fontSize = 13.sp,
                    color = SlateTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFECFDF5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Audit actif", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Info Bento Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BluePrimaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PROTECTION DES SESSIONS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BlueOnPrimaryContainer,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chaque connexion à votre compte enregistre l'empreinte de l'appareil, l'adresse réseau et l'horodatage en base locale chiffrée.",
                        fontSize = 11.sp,
                        color = BlueOnPrimaryContainer.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Revoke Other Sessions Button
        if (sessions.size > 1) {
            OutlinedButton(
                onClick = onRevokeOtherSessions,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revoke_other_sessions_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusError)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Déconnecter toutes les autres sessions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Sessions List
        Text(
            text = "HISTORIQUE DÉTAILLÉ (${sessions.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions, key = { it.id }) { session ->
                val isCurrent = currentSession?.id == session.id
                SessionCard(
                    session = session,
                    isCurrent = isCurrent,
                    onRevoke = { onRevokeSession(session.id) }
                )
            }
        }
    }
}

@Composable
fun SessionCard(
    session: SessionLogEntity,
    isCurrent: Boolean,
    onRevoke: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH).format(Date(session.loginTimestamp))
    val tokenPreview = session.sessionToken.take(16) + "..."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) BluePrimary else SlateBorder,
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFFFAFDFE) else WhiteSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) BluePrimaryContainer else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isCurrent) BluePrimary else SlateTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = session.deviceName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = session.osVersion,
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                }

                if (isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Actuelle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                } else if (session.isActive) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Déconnectée",
                            fontSize = 11.sp,
                            color = SlateTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details info Bento sub-card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF8FAFF))
                    .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailRow(label = "Date de connexion :", value = dateStr)
                DetailRow(label = "Adresse IP :", value = session.ipAddress)
                DetailRow(label = "Sécurité :", value = session.locationEstimate)
                DetailRow(label = "Jeton de session :", value = tokenPreview, isMonospace = true)
            }

            if (!isCurrent && session.isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onRevoke,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Révoquer la session", color = StatusError, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = SlateTextSecondary)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = SlateTextPrimary,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}
