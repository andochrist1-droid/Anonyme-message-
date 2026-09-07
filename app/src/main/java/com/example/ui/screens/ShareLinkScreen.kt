package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.BlueOnPrimaryContainer
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.BlueTertiaryContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface

@Composable
fun ShareLinkScreen(
    user: UserEntity,
    totalMessagesCount: Int,
    onSendSimulatedMessage: (message: String, pseudo: String, coordinates: String, tag: String) -> Unit,
    simulatedSuccessNotice: String?,
    onClearSimulatedNotice: () -> Unit
) {
    val context = LocalContext.current
    val publicUrl = "https://secretlink.app/send.html?to=${user.uniqueLinkSlug}"
    var showSimulateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bento Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF004582), BluePrimary, Color(0xFF1565C0))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        UserAvatar(
                            photoUriOrPreset = user.profilePicUri,
                            username = user.username,
                            size = 64.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.username,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (user.bio.isNotBlank()) user.bio else "Messagerie anonyme active",
                            fontSize = 12.sp,
                            color = Color(0xFFD1E4FF),
                            maxLines = 2
                        )
                    }

                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Actif",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bento Section 1: Secret Link Card (with micro-header, mono text, bento styling)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Micro-header as in Bento Grid spec
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOTRE LIEN SECRET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 1.5.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BluePrimaryContainer
                    ) {
                        Text(
                            text = "PRIVÉ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueOnPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Public URL Box in monospace Bento styling
                Surface(
                    color = Color(0xFFF8FAFF),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = publicUrl,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = BluePrimary,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Lien anonyme", publicUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Lien copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BluePrimaryContainer)
                                .testTag("copy_link_button")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copier le lien",
                                tint = BluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons in Bento styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Écris-moi un message anonyme, pose-moi une question ou fais une confession ! Mes coordonnées restent secrètes 4 jours : $publicUrl"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager mon lien anonyme"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_link_intent_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partager", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(publicUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Lien anonyme", publicUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Lien copié : $publicUrl", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("open_web_link_button"),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BluePrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ouvrir page", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showSimulateDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("simulate_message_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextSecondary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tester l'envoi en direct (Simulation)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bento Dual Feature Grid (Messages Counter Bento + Security Bento)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bento Tile 1: Soft Blue Pastel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BluePrimaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MESSAGES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueOnPrimaryContainer,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = String.format("%02d", totalMessagesCount),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueOnPrimaryContainer
                    )

                    Text(
                        text = "Protocole 4 jours actif",
                        fontSize = 10.sp,
                        color = BlueOnPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bento Tile 2: Deep Blue Cobalt Tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BluePrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROTECTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1E4FF),
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Données isolées",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "AES-256 scellé",
                        fontSize = 10.sp,
                        color = Color(0xFFD1E4FF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (simulatedSuccessNotice != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFECFDF5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = simulatedSuccessNotice,
                        fontSize = 12.sp,
                        color = Color(0xFF065F46),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = onClearSimulatedNotice) {
                        Text("×", fontSize = 18.sp, color = Color(0xFF065F46))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Explanatory Bento Protocol Steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROTOCOLE EN 3 ÉTAPES",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = BluePrimary,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StepCard(
            step = "1",
            title = "Partagez votre lien secret",
            desc = "Diffusez votre URL personnelle sur vos réseaux, stories ou bio."
        )

        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            step = "2",
            title = "Réception chiffrée AES-256",
            desc = "Vous lisez les messages secrets. Les coordonnées restent scellées."
        )

        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            step = "3",
            title = "Compte à rebours de 4 jours",
            desc = "Après 96h, les coordonnées de l'expéditeur sont automatiquement révélées !"
        )
    }

    // Dialog for simulating receiving a message
    if (showSimulateDialog) {
        SimulateMessageDialog(
            recipientName = user.username,
            onDismiss = { showSimulateDialog = false },
            onSend = { message, pseudo, coords, tag ->
                onSendSimulatedMessage(message, pseudo, coords, tag)
                showSimulateDialog = false
            }
        )
    }
}

@Composable
private fun StepCard(step: String, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BluePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(step, fontWeight = FontWeight.ExtraBold, color = BluePrimary, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, fontSize = 12.sp, color = SlateTextSecondary, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun SimulateMessageDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onSend: (message: String, pseudo: String, coords: String, tag: String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var senderPseudo by remember { mutableStateOf("") }
    var coordinates by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Secret") }

    val tags = listOf("Secret", "Compliment", "Vérité", "Question")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Simuler un visiteur",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SlateTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ce formulaire simule ce qu'un internaute envoie via votre lien partagé à $recipientName.",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Message anonyme *") },
                    placeholder = { Text("Tapez votre message secret...") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_msg_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = senderPseudo,
                    onValueChange = { senderPseudo = it },
                    label = { Text("Pseudo / Indice (Optionnel)") },
                    placeholder = { Text("ex: Une personne qui te regarde souvent") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = coordinates,
                    onValueChange = { coordinates = it },
                    label = { Text("Coordonnées de l'expéditeur *") },
                    placeholder = { Text("ex: Email, Instagram ou Téléphone") },
                    supportingText = { Text("Protégées pendant 4 jours par compte à rebours", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_coords_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Type de message :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = tag == selectedTag
                        Surface(
                            onClick = { selectedTag = tag },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) BluePrimary else Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tag,
                                color = if (isSelected) Color.White else SlateTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val coords = coordinates.ifBlank { "Contact anonyme non spécifié" }
                        onSend(messageText, senderPseudo, coords, selectedTag)
                    }
                },
                enabled = messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_simulate_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Envoyer (4 jours)", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = SlateTextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = WhiteSurface
    )
}
