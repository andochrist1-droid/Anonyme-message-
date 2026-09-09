package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AnonymousApp()
      }
    }
  }
}

@Composable
fun AnonymousApp(viewModel: MainViewModel = viewModel()) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val authState by viewModel.authUiState.collectAsStateWithLifecycle()
  val savedAccounts by viewModel.savedAccounts.collectAsStateWithLifecycle()

  Crossfade(
    targetState = currentUser != null,
    modifier = Modifier.fillMaxSize(),
    label = "auth_crossfade"
  ) { isLoggedIn ->
    if (isLoggedIn) {
      MainScreen(viewModel = viewModel)
    } else {
      AuthScreen(
        state = authState,
        savedAccounts = savedAccounts,
        onModeChange = { viewModel.setAuthMode(it) },
        onInputChange = { id, u, e, p, pic, bio ->
          viewModel.updateAuthInput(id, u, e, p, pic, bio)
        },
        onLoginSubmit = { viewModel.submitLogin() },
        onRegisterSubmit = { viewModel.submitRegister() },
        onFastLogin = { viewModel.fastLogin(it) },
        onRemoveSavedAccount = { viewModel.removeSavedAccount(it) }
      )
    }
  }
}

