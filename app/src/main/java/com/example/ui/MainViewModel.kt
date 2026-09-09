package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnonymousMessageEntity
import com.example.data.model.SessionLogEntity
import com.example.data.model.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthMode {
    data object Login : AuthMode
    data object Register : AuthMode
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val identifierInput: String = "",
    val usernameInput: String = "",
    val emailInput: String = "",
    val passwordInput: String = "",
    val profilePicUri: String = "preset_1",
    val bioInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application.applicationContext)

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
    val currentSession: StateFlow<SessionLogEntity?> = repository.currentSession

    val savedAccounts: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sessions: StateFlow<List<SessionLogEntity>> = repository.getSessionsForCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _messageFilter = MutableStateFlow("ALL") // "ALL", "UNREAD", "FAVORITES"
    val messageFilter: StateFlow<String> = _messageFilter.asStateFlow()

    val messages: StateFlow<List<AnonymousMessageEntity>> = combine(
        repository.getMessagesForCurrentUser(),
        _messageFilter
    ) { msgList, filter ->
        when (filter) {
            "UNREAD" -> msgList.filter { !it.isRead }
            "FAVORITES" -> msgList.filter { it.isFavorite }
            else -> msgList
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // Test sender simulation state
    private val _simulatedSendSuccess = MutableStateFlow<String?>(null)
    val simulatedSendSuccess: StateFlow<String?> = _simulatedSendSuccess.asStateFlow()

    init {
        // Countdown ticker that updates every second
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTimeMillis.value = System.currentTimeMillis()
            }
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _authUiState.value = _authUiState.value.copy(
            mode = mode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateAuthInput(
        identifier: String? = null,
        username: String? = null,
        email: String? = null,
        password: String? = null,
        profilePic: String? = null,
        bio: String? = null
    ) {
        _authUiState.value = _authUiState.value.copy(
            identifierInput = identifier ?: _authUiState.value.identifierInput,
            usernameInput = username ?: _authUiState.value.usernameInput,
            emailInput = email ?: _authUiState.value.emailInput,
            passwordInput = password ?: _authUiState.value.passwordInput,
            profilePicUri = profilePic ?: _authUiState.value.profilePicUri,
            bioInput = bio ?: _authUiState.value.bioInput,
            errorMessage = null
        )
    }

    fun submitLogin() {
        val state = _authUiState.value
        if (state.identifierInput.isBlank() || state.passwordInput.isBlank()) {
            _authUiState.value = state.copy(errorMessage = "Veuillez remplir tous les champs")
            return
        }

        viewModelScope.launch {
            _authUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = repository.login(state.identifierInput, state.passwordInput)
            if (result.isSuccess) {
                _authUiState.value = AuthUiState() // Reset
            } else {
                _authUiState.value = state.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Échec de connexion"
                )
            }
        }
    }

    fun submitRegister() {
        val state = _authUiState.value
        if (state.usernameInput.isBlank() || state.emailInput.isBlank() || state.passwordInput.isBlank()) {
            _authUiState.value = state.copy(errorMessage = "Veuillez remplir les informations requises")
            return
        }

        viewModelScope.launch {
            _authUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = repository.register(
                username = state.usernameInput,
                email = state.emailInput,
                password = state.passwordInput,
                profilePicUri = state.profilePicUri,
                bio = state.bioInput
            )
            if (result.isSuccess) {
                _authUiState.value = AuthUiState() // Reset
            } else {
                _authUiState.value = state.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Erreur d'inscription"
                )
            }
        }
    }

    fun fastLogin(user: UserEntity) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.fastLogin(user)
            if (result.isSuccess) {
                _authUiState.value = AuthUiState()
            } else {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Échec de connexion instantanée"
                )
            }
        }
    }

    fun removeSavedAccount(userId: Long) {
        viewModelScope.launch {
            repository.removeSavedAccount(userId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun revokeSession(sessionId: Long) {
        viewModelScope.launch {
            repository.revokeSession(sessionId)
        }
    }

    fun revokeOtherSessions() {
        viewModelScope.launch {
            repository.revokeOtherSessions()
        }
    }

    fun setMessageFilter(filter: String) {
        _messageFilter.value = filter
    }

    fun markMessageAsRead(messageId: Long) {
        viewModelScope.launch {
            repository.markMessageAsRead(messageId)
        }
    }

    fun toggleFavorite(messageId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(messageId)
        }
    }

    fun unlockCoordinatesImmediately(messageId: Long) {
        viewModelScope.launch {
            repository.unlockCoordinatesImmediately(messageId)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun updateProfile(username: String, bio: String, profilePicUri: String, isLinkActive: Boolean) {
        viewModelScope.launch {
            repository.updateProfile(username, bio, profilePicUri, isLinkActive)
        }
    }

    fun sendSimulatedMessage(
        messageText: String,
        senderPseudo: String,
        senderCoordinates: String,
        tag: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.sendAnonymousMessage(
                targetUserId = user.id,
                messageText = messageText,
                senderPseudo = senderPseudo,
                senderCoordinates = senderCoordinates,
                tag = tag
            )
            if (res.isSuccess) {
                _simulatedSendSuccess.value = "Message anonyme chiffré envoyé avec succès ! Le compte à rebours de 4 jours a démarré."
            }
        }
    }

    fun clearSimulatedSendSuccess() {
        _simulatedSendSuccess.value = null
    }
}
