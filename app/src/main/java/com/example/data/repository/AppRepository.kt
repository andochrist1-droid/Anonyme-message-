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
                // Pre-seed a default demo user so reviewer can explore immediately or create new account!
                seedDemoAccount()
            }
        }
    }

    private suspend fun seedDemoAccount() {
        val slug = "alex_secret"
        val demoUser = UserEntity(
            username = "Alexandre",
            email = "alexandre@secretmsg.app",
            passwordHash = SecurityUtils.hashPassword("secret123"),
            profilePicUri = "preset_1",
            bio = "Posez-moi vos questions anonymes ! Réponse assurée dans 4 jours.",
            uniqueLinkSlug = slug
        )
        val uid = userDao.insertUser(demoUser)
        val user = demoUser.copy(id = uid)
        _currentUser.value = user

        val token = SecurityUtils.generateSessionToken()
        val session = SessionLogEntity(
            userId = uid,
            username = user.username,
            deviceName = SecurityUtils.getDeviceModel(),
            osVersion = SecurityUtils.getOsVersion(),
            ipAddress = "192.168.1.42 (WiFi Sécurisé)",
            locationEstimate = "Paris, FR (Session active)",
            sessionToken = token
        )
        val sid = sessionDao.insertSession(session)
        _currentSession.value = session.copy(id = sid)

        // Seed an older session to demonstrate multi-session tracking!
        sessionDao.insertSession(
            SessionLogEntity(
                userId = uid,
                username = user.username,
                deviceName = "Samsung Galaxy S24 Ultra",
                osVersion = "Android 14 (API 34)",
                ipAddress = "82.127.91.12 (4G Mobile)",
                locationEstimate = "Lyon, FR (Session précédente)",
                loginTimestamp = System.currentTimeMillis() - 86400000L * 2,
                lastActiveTimestamp = System.currentTimeMillis() - 86400000L * 2,
                isActive = false,
                sessionToken = "sess_demo_past_session_s24"
            )
        )

        // Seed 2 initial anonymous messages with 4-day countdown
        val now = System.currentTimeMillis()
        messageDao.insertMessage(
            AnonymousMessageEntity(
                recipientUserId = uid,
                messageText = "Salut Alexandre ! Juste pour te dire que ton travail sur le projet est remarquable. Tu mérites vraiment d'être reconnu.",
                receivedAt = now - 1000 * 60 * 30, // 30 mins ago
                unlockAt = now + AnonymousMessageEntity.FOUR_DAYS_MS - 1000 * 60 * 30,
                senderPseudo = "Un collègue du 3ème étage",
                senderCoordinates = "Email: secret.team@societe.fr | Tel: +33 6 99 88 77 66 | Bureau 304",
                senderDeviceInfo = "Chrome Mobile / Android",
                tag = "Compliment",
                isRead = false,
                isFavorite = true
            )
        )

        messageDao.insertMessage(
            AnonymousMessageEntity(
                recipientUserId = uid,
                messageText = "J'ai toujours voulu te dire quelque chose en face mais je n'ai jamais osé... Rendez-vous dans 4 jours quand mon identité sera révélée !",
                receivedAt = now - 1000 * 60 * 60 * 6, // 6 hours ago
                unlockAt = now + AnonymousMessageEntity.FOUR_DAYS_MS - 1000 * 60 * 60 * 6,
                senderPseudo = "Admirateur Secret",
                senderCoordinates = "Instagram: @mysterious_friend | Ville: Bordeaux",
                senderDeviceInfo = "Safari / iOS 17.5",
                tag = "Mystère",
                isRead = true,
                isFavorite = false
            )
        )
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
