package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicUri: String = "preset_1",
    val bio: String = "Laissez-moi un message anonyme en toute liberté !",
    val uniqueLinkSlug: String = "",
    val linkActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SessionLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val deviceName: String = "",
    val osVersion: String = "",
    val ipAddress: String = "",
    val locationEstimate: String = "",
    val active: Boolean = true,
    val sessionToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AnonymousMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val messageText: String = "",
    val receivedAtMillis: Long = 0L,
    val unlockAtMillis: Long = 0L,
    val senderPseudo: String = "Anonyme",
    val senderCoordinates: String = "",
    val senderDeviceInfo: String = "",
    val tag: String = "Secret",
    val read: Boolean = false,
    val favorite: Boolean = false,
    val revealedManually: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val FOUR_DAYS_MS: Long = 4L * 24L * 60L * 60L * 1000L
        val ALLOWED_TAGS = setOf("Bienvenue", "Secret", "Compliment", "Vérité", "Question")
    }

    @get:Exclude
    val isUnlocked: Boolean
        get() = revealedManually || System.currentTimeMillis() >= unlockAtMillis

    @get:Exclude
    val remainingMillis: Long
        get() = (unlockAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
}
