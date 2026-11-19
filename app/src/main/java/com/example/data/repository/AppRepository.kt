package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.AnonymousMessage
import com.example.data.model.SessionLog
import com.example.data.model.UserProfile
import com.example.util.SecurityUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AppRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("L'utilisateur doit être connecté avec Google avant d'accéder à Firestore.")
        require(uid.isNotBlank()) { "Identifiant utilisateur invalide." }
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "L'utilisateur connecté a changé ; écriture refusée." }
        }
        return uid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        requireUserId(userId)
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeMessages(userId: String): Flow<List<AnonymousMessage>> = flow {
        requireUserId(userId)
        val path = "users/$userId/messages"
        emitAll(
            db.collection("users").document(userId).collection("messages")
                .orderBy("receivedAtMillis", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AnonymousMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeSessions(userId: String): Flow<List<SessionLog>> = flow {
        requireUserId(userId)
        val path = "users/$userId/sessions"
        emitAll(
            db.collection("users").document(userId).collection("sessions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SessionLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfileAndSession(firebaseUser: FirebaseUser): Result<String> {
        val uid = firebaseUser.uid
        var currentPath = "users/$uid"
        return try {
            requireUserId(uid)
            val userDocRef = db.collection("users").document(uid)
            val snapshot = userDocRef.get().await()

            val displayUsername: String
            if (!snapshot.exists()) {
                val rawName = firebaseUser.displayName?.trim()
                    ?: firebaseUser.email?.substringBefore("@")?.trim()
                    ?: "Utilisateur"
                val cleanUsername = rawName.ifBlank { "Utilisateur" }.take(40).let {
                    if (it.length < 2) "${it}_user" else it
                }
                val cleanEmail = (firebaseUser.email ?: "").take(150)
                val cleanPhoto = (firebaseUser.photoUrl?.toString() ?: "preset_1").take(500)
                val slugBase = cleanUsername.lowercase()
                    .replace(Regex("[^a-z0-9]+"), "_")
                    .trim('_')
                    .ifBlank { "user" }
                    .take(40)
                val slug = "${slugBase}_${uid.take(6).lowercase()}"

                val profilePayload = mapOf(
                    "userId" to uid,
                    "username" to cleanUsername,
                    "email" to cleanEmail,
                    "profilePicUri" to cleanPhoto,
                    "bio" to "Laissez-moi un message anonyme en toute liberté !",
                    "uniqueLinkSlug" to slug,
                    "linkActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                currentPath = userDocRef.path
                userDocRef.set(profilePayload).await()
                displayUsername = cleanUsername

                val publicLinkRef = db.collection("publicLinks").document(slug)
                currentPath = publicLinkRef.path
                val publicLinkPayload = mapOf(
                    "slug" to slug,
                    "ownerId" to uid,
                    "username" to cleanUsername,
                    "profilePicUri" to cleanPhoto,
                    "bio" to "Laissez-moi un message anonyme en toute liberté !",
                    "linkActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                publicLinkRef.set(publicLinkPayload).await()

                // Create welcome anonymous message with 4-day timer in Firestore
                val now = System.currentTimeMillis()
                val welcomeMsgRef = userDocRef.collection("messages").document()
                currentPath = welcomeMsgRef.path
                val welcomePayload = mapOf(
                    "userId" to uid,
                    "messageText" to "Bienvenue sur votre plateforme de messagerie anonyme ! Vos messages et sessions sont désormais synchronisés en temps réel sur Cloud Firestore.",
                    "receivedAtMillis" to now,
                    "unlockAtMillis" to now + AnonymousMessage.FOUR_DAYS_MS,
                    "senderPseudo" to "Équipe Sécurité",
                    "senderCoordinates" to "Support vérifié Cloud Firestore",
                    "senderDeviceInfo" to "Serveur Central Chiffré",
                    "tag" to "Bienvenue",
                    "read" to false,
                    "favorite" to true,
                    "revealedManually" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                welcomeMsgRef.set(welcomePayload).await()
            } else {
                displayUsername = snapshot.getString("username")?.take(40)?.ifBlank { "Utilisateur" } ?: "Utilisateur"
                val existingSlug = snapshot.getString("uniqueLinkSlug")
                if (!existingSlug.isNullOrBlank()) {
                    val publicLinkRef = db.collection("publicLinks").document(existingSlug)
                    val publicSnap = publicLinkRef.get().await()
                    if (!publicSnap.exists()) {
                        currentPath = publicLinkRef.path
                        val backfilledPublic = mapOf(
                            "slug" to existingSlug,
                            "ownerId" to uid,
                            "username" to displayUsername,
                            "profilePicUri" to (snapshot.getString("profilePicUri") ?: "preset_1").take(500),
                            "bio" to (snapshot.getString("bio") ?: "").take(300),
                            "linkActive" to (snapshot.getBoolean("linkActive") ?: true),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        publicLinkRef.set(backfilledPublic).await()
                    }
                }
            }

            // Record active session in Firestore
            val sessionRef = userDocRef.collection("sessions").document()
            currentPath = sessionRef.path
            val sessionPayload = mapOf(
                "userId" to uid,
                "username" to displayUsername,
                "deviceName" to SecurityUtils.getDeviceModel().take(100),
                "osVersion" to SecurityUtils.getOsVersion().take(80),
                "ipAddress" to SecurityUtils.generateSimulatedIp().take(80),
                "locationEstimate" to "Google Sign-In • Cloud Firestore",
                "active" to true,
                "sessionToken" to SecurityUtils.generateSessionToken().take(128),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            sessionRef.set(sessionPayload).await()

            Result.success(sessionRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, currentPath)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: String,
        username: String,
        bio: String,
        profilePicUri: String,
        linkActive: Boolean
    ): Result<Unit> {
        var docPath = "users/$userId"
        return try {
            requireUserId(userId)
            val cleanUsername = username.trim()
            require(cleanUsername.length in 2..40) { "Le nom d'utilisateur doit contenir entre 2 et 40 caractères." }
            val cleanBio = bio.trim().take(300)
            val cleanPhoto = profilePicUri.trim().ifBlank { "preset_1" }.take(500)

            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            val slug = userSnap.getString("uniqueLinkSlug")

            userRef.update(
                mapOf(
                    "username" to cleanUsername,
                    "bio" to cleanBio,
                    "profilePicUri" to cleanPhoto,
                    "linkActive" to linkActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (!slug.isNullOrBlank()) {
                val publicLinkRef = db.collection("publicLinks").document(slug)
                docPath = publicLinkRef.path
                val publicSnap = publicLinkRef.get().await()
                if (publicSnap.exists()) {
                    publicLinkRef.update(
                        mapOf(
                            "username" to cleanUsername,
                            "bio" to cleanBio,
                            "profilePicUri" to cleanPhoto,
                            "linkActive" to linkActive,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    publicLinkRef.set(
                        mapOf(
                            "slug" to slug,
                            "ownerId" to userId,
                            "username" to cleanUsername,
                            "bio" to cleanBio,
                            "profilePicUri" to cleanPhoto,
                            "linkActive" to linkActive,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun sendAnonymousMessage(
        userId: String,
        messageText: String,
        senderPseudo: String,
        senderCoordinates: String,
        tag: String
    ): Result<String> {
        var docPath = "users/$userId/messages"
        return try {
            requireUserId(userId)
            val cleanMsg = messageText.trim()
            require(cleanMsg.length in 1..1000) { "Le message doit contenir entre 1 et 1000 caractères." }
            val safeTag = if (tag in AnonymousMessage.ALLOWED_TAGS) tag else "Secret"
            val cleanPseudo = senderPseudo.trim().ifBlank { "Anonyme mystère" }.take(80)
            val cleanCoords = senderCoordinates.trim().ifBlank { "Non renseigné par l'expéditeur" }.take(200)
            val deviceInfo = "${SecurityUtils.getDeviceModel()} (${SecurityUtils.getOsVersion()})".take(150)

            val now = System.currentTimeMillis()
            val docRef = db.collection("users").document(userId).collection("messages").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to userId,
                "messageText" to cleanMsg,
                "receivedAtMillis" to now,
                "unlockAtMillis" to now + AnonymousMessage.FOUR_DAYS_MS,
                "senderPseudo" to cleanPseudo,
                "senderCoordinates" to cleanCoords,
                "senderDeviceInfo" to deviceInfo,
                "tag" to safeTag,
                "read" to false,
                "favorite" to false,
                "revealedManually" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markMessageAsRead(userId: String, messageId: String): Result<Unit> {
        val docPath = "users/$userId/messages/$messageId"
        return try {
            requireUserId(userId)
            require(messageId.isNotBlank()) { "Identifiant du message manquant." }
            db.collection("users").document(userId).collection("messages").document(messageId)
                .update(
                    mapOf(
                        "read" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(userId: String, messageId: String, newFavorite: Boolean): Result<Unit> {
        val docPath = "users/$userId/messages/$messageId"
        return try {
            requireUserId(userId)
            require(messageId.isNotBlank()) { "Identifiant du message manquant." }
            db.collection("users").document(userId).collection("messages").document(messageId)
                .update(
                    mapOf(
                        "favorite" to newFavorite,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun unlockCoordinatesImmediately(userId: String, messageId: String): Result<Unit> {
        val docPath = "users/$userId/messages/$messageId"
        return try {
            requireUserId(userId)
            require(messageId.isNotBlank()) { "Identifiant du message manquant." }
            db.collection("users").document(userId).collection("messages").document(messageId)
                .update(
                    mapOf(
                        "revealedManually" to true,
                        "unlockAtMillis" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(userId: String, messageId: String): Result<Unit> {
        val docPath = "users/$userId/messages/$messageId"
        return try {
            requireUserId(userId)
            require(messageId.isNotBlank()) { "Identifiant du message manquant." }
            db.collection("users").document(userId).collection("messages").document(messageId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun revokeSession(userId: String, sessionId: String): Result<Unit> {
        val docPath = "users/$userId/sessions/$sessionId"
        return try {
            requireUserId(userId)
            require(sessionId.isNotBlank()) { "Identifiant de session manquant." }
            db.collection("users").document(userId).collection("sessions").document(sessionId)
                .update(
                    mapOf(
                        "active" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun revokeOtherSessions(userId: String, currentSessionId: String?): Result<Unit> {
        val path = "users/$userId/sessions"
        return try {
            requireUserId(userId)
            val activeSnapshots = db.collection("users").document(userId).collection("sessions")
                .whereEqualTo("active", true)
                .get()
                .await()
            for (doc in activeSnapshots.documents) {
                if (doc.id != currentSessionId) {
                    doc.reference.update(
                        mapOf(
                            "active" to false,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
