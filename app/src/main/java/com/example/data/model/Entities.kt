package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val profilePicUri: String = "",
    val bio: String = "Laissez-moi un message anonyme en toute franchise !",
    val uniqueLinkSlug: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isLinkActive: Boolean = true
)

@Entity(tableName = "sessions")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val username: String,
    val deviceName: String,
    val osVersion: String,
    val ipAddress: String,
    val locationEstimate: String,
    val loginTimestamp: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val sessionToken: String
)

@Entity(tableName = "anonymous_messages")
data class AnonymousMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientUserId: Long,
    val messageText: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val unlockAt: Long = System.currentTimeMillis() + FOUR_DAYS_MS,
    val senderPseudo: String = "Anonyme",
    val senderCoordinates: String = "",
    val senderDeviceInfo: String = "",
    val tag: String = "Secret",
    val isRead: Boolean = false,
    val isFavorite: Boolean = false,
    val isRevealedManually: Boolean = false
) {
    companion object {
        const val FOUR_DAYS_MS: Long = 4L * 24L * 60L * 60L * 1000L // 4 days in milliseconds
    }

    val isUnlocked: Boolean
        get() = isRevealedManually || System.currentTimeMillis() >= unlockAt

    val remainingMillis: Long
        get() = (unlockAt - System.currentTimeMillis()).coerceAtLeast(0L)
}
