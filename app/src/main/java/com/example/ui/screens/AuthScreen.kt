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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.WhiteBackground
import com.example.ui.theme.WhiteSurface

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleSignInClick: () -> Unit
) {
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
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo & Header
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BluePrimary, Color(0xFF003FA3))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Logo Messages Anonymes",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Messages Anonymes",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateTextPrimary,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Messagerie chiffrée • Base Cloud Firestore",
                fontSize = 14.sp,
                color = SlateTextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
            )

            // Main Bento Sign-In Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BluePrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FIREBASE CLOUD SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Accès sécurisé à votre espace",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Connectez-vous avec votre compte Google pour synchroniser vos messages anonymes, votre lien personnel et vos sessions en temps réel sur Firebase.",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = StatusError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = onGoogleSignInClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Connexion en cours...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Se connecter avec Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chaque compte est strictement isolé par règles Firestore",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bento Highlights Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureBadge(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CloudDone,
                    title = "Firebase Firestore",
                    subtitle = "Temps réel multi-appareils"
                )
                FeatureBadge(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    title = "Compte à rebours 4j",
                    subtitle = "Révélation chiffrée 96h"
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector,
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
