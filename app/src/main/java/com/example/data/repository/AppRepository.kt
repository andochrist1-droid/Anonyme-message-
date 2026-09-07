package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.AnonymousMessageEntity
import com.example.data.model.SessionLogEntity
import com.example.data.model.UserEntity
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val sessionDao = db.sessionDao()
    private val messageDao = db.messageDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentSession = MutableStateFlow<SessionLogEntity?>(null)
    val currentSession: StateFlow<SessionLogEntity?> = _currentSession.asStateFlow()

    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        repoScope.launch {
            // Purge any legacy demo account so users land directly on login/register
            userDao.deleteDemoUser()

            // Restore last registered/logged in user if available
            val latest = userDao.getLatestUser()
            if (latest != null) {
                _currentUser.value = latest
                // Create or refresh session
                val token = SecurityUtils.generateSessionToken()
                val session = SessionLogEntity(
                    userId = latest.id,
                    username = latest.username,
                    deviceName = SecurityUtils.getDeviceModel(),
                    osVersion = SecurityUtils.getOsVersion(),
                    ipAddress = SecurityUtils.generateSimulatedIp(),
                    locationEstimate = "Session chiffrée SSL/TLS",
                    sessionToken = token
                )
                val sid = sessionDao.insertSession(session)
                _currentSession.value = session.copy(id = sid)
            } else {
                _currentUser.value = null
                _currentSession.value = null
            }
        }
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.findByIdentifier(identifier.trim())
            ?: return@withContext Result.failure(Exception("Nom d'utilisateur ou e-mail introuvable"))

        val hashed = SecurityUtils.hashPassword(password)
        if (user.passwordHash != hashed) {
            return@withContext Result.failure(Exception("Mot de passe incorrect"))
        }

        val sessionToken = SecurityUtils.generateSessionToken()
        val session = SessionLogEntity(
            userId = user.id,
            username = user.username,
            deviceName = SecurityUtils.getDeviceModel(),
            osVersion = SecurityUtils.getOsVersion(),
            ipAddress = SecurityUtils.generateSimulatedIp(),
            locationEstimate = "Connexion chiffrée SSL",
            sessionToken = sessionToken
        )
        val sid = sessionDao.insertSession(session)

        _currentUser.value = user
        _currentSession.value = session.copy(id = sid)
        Result.success(user)
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        profilePicUri: String,
        bio: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        val cleanEmail = email.trim()

        if (cleanUser.length < 3) {
            return@withContext Result.failure(Exception("Le nom d'utilisateur doit comporter au moins 3 caractères"))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext Result.failure(Exception("Format d'e-mail invalide"))
        }
        if (password.length < 4) {
            return@withContext Result.failure(Exception("Le mot de passe doit comporter au moins 4 caractères"))
        }

        val existing = userDao.findByIdentifier(cleanUser) ?: userDao.findByIdentifier(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("Ce nom d'utilisateur ou cet e-mail est déjà utilisé"))
        }

        val slug = cleanUser.lowercase().replace(" ", "_") + "_" + (100..999).random()
        val newUser = UserEntity(
            username = cleanUser,
            email = cleanEmail,
            passwordHash = SecurityUtils.hashPassword(password),
            profilePicUri = profilePicUri.ifBlank { "preset_1" },
            bio = bio.ifBlank { "Laissez-moi un message anonyme en toute liberté !" },
            uniqueLinkSlug = slug
        )

        val uid = userDao.insertUser(newUser)
        val savedUser = newUser.copy(id = uid)

        // Record initial connection session in secure database
        val sessionToken = SecurityUtils.generateSessionToken()
        val session = SessionLogEntity(
            userId = uid,
            username = savedUser.username,
            deviceName = SecurityUtils.getDeviceModel(),
            osVersion = SecurityUtils.getOsVersion(),
            ipAddress = SecurityUtils.generateSimulatedIp(),
            locationEstimate = "Première connexion sécurisée",
            sessionToken = sessionToken
        )
        val sid = sessionDao.insertSession(session)

        _currentUser.value = savedUser
        _currentSession.value = session.copy(id = sid)

        // Add welcome message with 4-day timer
        val now = System.currentTimeMillis()
        messageDao.insertMessage(
            AnonymousMessageEntity(
                recipientUserId = uid,
                messageText = "Bienvenue sur votre plateforme de messagerie anonyme ! Partagez votre lien public pour recevoir des messages chiffrés. Leurs coordonnées resteront secrètes pendant 4 jours.",
                receivedAt = now,
                unlockAt = now + AnonymousMessageEntity.FOUR_DAYS_MS,
                senderPseudo = "Équipe Sécurité",
                senderCoordinates = "Support: security@anonym-platform.com | Clé PGP vérifiée",
                senderDeviceInfo = "Serveur Central Chiffré",
                tag = "Bienvenue",
                isRead = false,
                isFavorite = true
            )
        )

        Result.success(savedUser)
    }

    suspend fun updateProfile(
        username: String,
        bio: String,
        profilePicUri: String,
        isLinkActive: Boolean
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext Result.failure(Exception("Non connecté"))
        val updated = current.copy(
            username = username.trim().ifBlank { current.username },
            bio = bio.trim(),
            profilePicUri = profilePicUri.ifBlank { current.profilePicUri },
            isLinkActive = isLinkActive
        )
        userDao.updateUser(updated)
        _currentUser.value = updated
        Result.success(updated)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val currentSess = _currentSession.value
        if (currentSess != null) {
            sessionDao.revokeSession(currentSess.id)
        }
        _currentUser.value = null
        _currentSession.value = null
    }

    fun getSessionsForCurrentUser(): Flow<List<SessionLogEntity>> {
        val user = _currentUser.value ?: return emptyFlow()
        return sessionDao.getSessionsForUser(user.id)
    }

    suspend fun revokeSession(sessionId: Long) = withContext(Dispatchers.IO) {
        sessionDao.revokeSession(sessionId)
        if (_currentSession.value?.id == sessionId) {
            _currentUser.value = null
            _currentSession.value = null
        }
    }

    suspend fun revokeOtherSessions() = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val sess = _currentSession.value ?: return@withContext
        sessionDao.revokeOtherSessions(user.id, sess.id)
    }

    fun getMessagesForCurrentUser(): Flow<List<AnonymousMessageEntity>> {
        return currentUser.flatMapLatest { user ->
            if (user == null) emptyFlow() else messageDao.getMessagesForUser(user.id)
        }
    }

    suspend fun sendAnonymousMessage(
        targetUserId: Long,
        messageText: String,
        senderPseudo: String,
        senderCoordinates: String,
        tag: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (messageText.isBlank()) {
            return@withContext Result.failure(Exception("Le message ne peut pas être vide"))
        }

        val msg = AnonymousMessageEntity(
            recipientUserId = targetUserId,
            messageText = messageText.trim(),
            receivedAt = System.currentTimeMillis(),
            unlockAt = System.currentTimeMillis() + AnonymousMessageEntity.FOUR_DAYS_MS,
            senderPseudo = senderPseudo.trim().ifBlank { "Anonyme mystère" },
            senderCoordinates = senderCoordinates.trim().ifBlank { "Non renseigné par l'expéditeur" },
            senderDeviceInfo = "${SecurityUtils.getDeviceModel()} (${SecurityUtils.getOsVersion()})",
            tag = tag.ifBlank { "Secret" },
            isRead = false,
            isFavorite = false
        )
        val id = messageDao.insertMessage(msg)
        Result.success(id)
    }

    suspend fun markMessageAsRead(messageId: Long) = withContext(Dispatchers.IO) {
        messageDao.markAsRead(messageId)
    }

    suspend fun toggleFavorite(messageId: Long) = withContext(Dispatchers.IO) {
        messageDao.toggleFavorite(messageId)
    }

    suspend fun unlockCoordinatesImmediately(messageId: Long) = withContext(Dispatchers.IO) {
        messageDao.unlockCoordinatesImmediately(messageId)
    }

    suspend fun deleteMessage(messageId: Long) = withContext(Dispatchers.IO) {
        messageDao.deleteMessage(messageId)
    }
}
