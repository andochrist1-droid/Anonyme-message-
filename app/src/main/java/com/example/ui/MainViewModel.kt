package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnonymousMessage
import com.example.data.model.SessionLog
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(
    private val repository: AppRepository,
    private val currentUserId: String
) : ViewModel() {

    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Erreur lors du chargement du profil"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _messageFilter = MutableStateFlow("ALL") // "ALL", "UNREAD", "FAVORITES"
    val messageFilter: StateFlow<String> = _messageFilter.asStateFlow()

    val messagesState: StateFlow<UiState<List<AnonymousMessage>>> = combine(
        repository.observeMessages(currentUserId),
        _messageFilter
    ) { msgList, filter ->
        val filtered = when (filter) {
            "UNREAD" -> msgList.filter { !it.read }
            "FAVORITES" -> msgList.filter { it.favorite }
            else -> msgList
        }
        UiState.Success(filtered) as UiState<List<AnonymousMessage>>
    }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing messages", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Erreur lors du chargement des messages"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val sessionsState: StateFlow<UiState<List<SessionLog>>> = repository.observeSessions(currentUserId)
        .map<List<SessionLog>, UiState<List<SessionLog>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing sessions", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Erreur lors du chargement des sessions"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _actionNotice = MutableStateFlow<String?>(null)
    val actionNotice: StateFlow<String?> = _actionNotice.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    init {
        viewModelScope.launch {
            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser != null && firebaseUser.uid == currentUserId) {
                val res = repository.ensureUserProfileAndSession(firebaseUser)
                res.onSuccess { sessionId ->
                    _currentSessionId.value = sessionId
                }.onFailure { err ->
                    _actionError.value = err.localizedMessage ?: "Erreur de synchronisation Cloud"
                }
            }
        }

        // Countdown ticker updating every second
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTimeMillis.value = System.currentTimeMillis()
            }
        }
    }

    fun setMessageFilter(filter: String) {
        _messageFilter.value = filter
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            repository.markMessageAsRead(currentUserId, messageId)
                .onFailure { _actionError.value = it.localizedMessage }
        }
    }

    fun toggleFavorite(messageId: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, messageId, !currentFavorite)
                .onFailure { _actionError.value = it.localizedMessage }
        }
    }

    fun unlockCoordinatesImmediately(messageId: String) {
        viewModelScope.launch {
            repository.unlockCoordinatesImmediately(currentUserId, messageId)
                .onFailure { _actionError.value = it.localizedMessage }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(currentUserId, messageId)
                .onFailure { _actionError.value = it.localizedMessage }
        }
    }

    fun updateProfile(username: String, bio: String, profilePicUri: String, linkActive: Boolean) {
        viewModelScope.launch {
            repository.updateProfile(currentUserId, username, bio, profilePicUri, linkActive)
                .onSuccess { _actionNotice.value = "Profil synchronisé sur Cloud Firestore !" }
                .onFailure { _actionError.value = it.localizedMessage ?: "Échec de mise à jour du profil" }
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            repository.revokeSession(currentUserId, sessionId)
                .onFailure { _actionError.value = it.localizedMessage }
        }
    }

    fun revokeOtherSessions() {
        viewModelScope.launch {
            repository.revokeOtherSessions(currentUserId, _currentSessionId.value)
                .onFailure { _actionError.value = it.localizedMessage }
        }
    }

    suspend fun markCurrentSessionInactiveBeforeSignOut() {
        val sessId = _currentSessionId.value
        if (!sessId.isNullOrBlank()) {
            repository.revokeSession(currentUserId, sessId)
        }
    }

    fun sendSimulatedMessage(
        messageText: String,
        senderPseudo: String,
        senderCoordinates: String,
        tag: String
    ) {
        viewModelScope.launch {
            repository.sendAnonymousMessage(
                userId = currentUserId,
                messageText = messageText,
                senderPseudo = senderPseudo,
                senderCoordinates = senderCoordinates,
                tag = tag
            ).onSuccess {
                _actionNotice.value = "Message anonyme enregistré sur Firebase Firestore ! Compte à rebours de 4 jours démarré."
            }.onFailure {
                _actionError.value = it.localizedMessage ?: "Échec de l'envoi sur Firestore"
            }
        }
    }

    fun clearActionNotice() {
        _actionNotice.value = null
    }

    fun clearActionError() {
        _actionError.value = null
    }

    private companion object {
        const val TAG = "MainViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
