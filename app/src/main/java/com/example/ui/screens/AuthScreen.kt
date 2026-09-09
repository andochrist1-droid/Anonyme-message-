package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.AuthMode
import com.example.ui.AuthUiState
import com.example.ui.components.AVATAR_PRESETS
import com.example.ui.components.UserAvatar
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface

@Composable
fun AuthScreen(
    state: AuthUiState,
    savedAccounts: List<UserEntity> = emptyList(),
    onModeChange: (AuthMode) -> Unit,
    onInputChange: (identifier: String?, username: String?, email: String?, password: String?, photo: String?, bio: String?) -> Unit,
    onLoginSubmit: () -> Unit,
    onRegisterSubmit: () -> Unit,
    onFastLogin: (UserEntity) -> Unit = {},
    onRemoveSavedAccount: (Long) -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Android Photo Picker for profile photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onInputChange(null, null, null, null, uri.toString(), null)
        }
    }

    // Check if the current registration input matches an existing account
    val matchingAccount = remember(state.usernameInput, state.emailInput, savedAccounts) {
        val u = state.usernameInput.trim().lowercase()
        val e = state.emailInput.trim().lowercase()
        savedAccounts.firstOrNull { acc ->
            (u.isNotEmpty() && acc.username.lowercase() == u) ||
            (e.isNotEmpty() && acc.email.lowercase() == e)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF0F6FF),
                        WhiteBackground,
                        Color(0xFFEBF3FF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BluePrimary, Color(0xFF003FA3))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Messages Anonymes",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Messagerie chiffrée • Compte à rebours 4 jours",
                fontSize = 14.sp,
                color = SlateTextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Bento Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Switch Tabs
                    TabRow(
                        selectedTabIndex = if (state.mode == AuthMode.Login) 0 else 1,
                        containerColor = Color(0xFFF1F5F9),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (state.mode == AuthMode.Login) 0 else 1]),
                                color = BluePrimary,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Tab(
                            selected = state.mode == AuthMode.Login,
                            onClick = { onModeChange(AuthMode.Login) },
                            modifier = Modifier.testTag("tab_login"),
                            text = {
                                Text(
                                    "Connexion",
                                    fontWeight = if (state.mode == AuthMode.Login) FontWeight.Bold else FontWeight.Medium,
                                    color = if (state.mode == AuthMode.Login) BluePrimary else SlateTextSecondary
                                )
                            }
                        )
                        Tab(
                            selected = state.mode == AuthMode.Register,
                            onClick = { onModeChange(AuthMode.Register) },
                            modifier = Modifier.testTag("tab_register"),
                            text = {
                                Text(
                                    "Créer un compte",
                                    fontWeight = if (state.mode == AuthMode.Register) FontWeight.Bold else FontWeight.Medium,
                                    color = if (state.mode == AuthMode.Register) BluePrimary else SlateTextSecondary
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = state.errorMessage,
                                color = StatusError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (state.mode == AuthMode.Login) {
                        // Display saved accounts list for instant one-click login if any exist
                        if (savedAccounts.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Comptes enregistrés sur cet appareil",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SlateTextPrimary
                                    )
                                }
                                Text(
                                    "Connexion instantanée en 1 clic sans retaper votre mot de passe :",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                savedAccounts.forEach { acc ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable { onFastLogin(acc) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            UserAvatar(
                                                photoUriOrPreset = acc.profilePicUri,
                                                username = acc.username,
                                                size = 38.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "@${acc.username}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SlateTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = acc.email,
                                                    fontSize = 11.sp,
                                                    color = SlateTextSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Button(
                                                onClick = { onFastLogin(acc) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Entrer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(
                                                onClick = { onRemoveSavedAccount(acc.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Oublier ce compte",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = SlateBorder)
                                    Text(
                                        " ou autre compte ",
                                        fontSize = 11.sp,
                                        color = SlateTextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = SlateBorder)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // Login fields
                        OutlinedTextField(
                            value = state.identifierInput,
                            onValueChange = { onInputChange(it, null, null, null, null, null) },
                            label = { Text("Nom d'utilisateur ou Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = state.passwordInput,
                            onValueChange = { onInputChange(null, null, null, it, null, null) },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher mot de passe",
                                        tint = SlateTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onLoginSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text(
                                    "Se connecter",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                    } else {
                        // Alert if matching account already exists on device
                        if (matchingAccount != null) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFB45309),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Compte déjà enregistré !",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Ce compte (@${matchingAccount.username}) existe déjà avec ces coordonnées. Pas besoin d'en créer un nouveau !",
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onFastLogin(matchingAccount) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Se connecter instantanément à @${matchingAccount.username}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Register fields with profile photo picker
                        Text(
                            text = "Photo de profil",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextPrimary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                photoUriOrPreset = state.profilePicUri,
                                username = state.usernameInput.ifBlank { "A" },
                                size = 64.dp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimaryContainer),
                                modifier = Modifier.testTag("pick_photo_button")
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choisir photo", color = BluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        // Presets row
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ou avatar :", fontSize = 12.sp, color = SlateTextMuted)
                            AVATAR_PRESETS.forEach { (presetId, _) ->
                                val isSelected = state.profilePicUri == presetId
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) BluePrimary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onInputChange(null, null, null, null, presetId, null)
                                        }
                                ) {
                                    UserAvatar(
                                        photoUriOrPreset = presetId,
                                        username = state.usernameInput.ifBlank { "U" },
                                        size = 28.dp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.usernameInput,
                            onValueChange = { onInputChange(null, it, null, null, null, null) },
                            label = { Text("Nom d'utilisateur") },
                            placeholder = { Text("ex: Alex") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.emailInput,
                            onValueChange = { onInputChange(null, null, it, null, null, null) },
                            label = { Text("Adresse E-mail") },
                            placeholder = { Text("ex: alex@secretmsg.app") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = BluePrimary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.passwordInput,
                            onValueChange = { onInputChange(null, null, null, it, null, null) },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.bioInput,
                            onValueChange = { onInputChange(null, null, null, null, null, it) },
                            label = { Text("Message d'accueil sur votre lien public") },
                            placeholder = { Text("Laissez-moi un message anonyme...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_bio_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onRegisterSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text(
                                    "Créer mon profil anonyme",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bento Security Highlights Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureBadge(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Security,
                    title = "Sessions mémorisées",
                    subtitle = "Base SQLite sécurisée"
                )
                FeatureBadge(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    title = "Compte à rebours 4j",
                    subtitle = "Révélation chiffrée"
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = modifier
            .border(1.dp, SlateBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(BluePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = SlateTextSecondary)
        }
    }
}
