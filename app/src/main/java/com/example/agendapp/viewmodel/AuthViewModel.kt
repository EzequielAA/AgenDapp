package com.example.agendapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendapp.data.model.LoginRequest
import com.example.agendapp.data.model.User // Usamos el modelo User que actualizamos
import com.example.agendapp.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Estado del Login (Inicial, Cargando, Exito, Error)
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    // Estado del Registro
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun login(email: String, pass: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                // Llama al Backend
                val response = RetrofitClient.apiService.login(LoginRequest(email, pass))

                if (response.isSuccessful && response.body() != null) {
                    // Login Exitoso. Devuelve el usuario completo (incluye el ID, nombre, etc.)
                    val usuario = response.body()!!
                    _loginState.value = LoginState.Success(usuario)
                } else {
                    // Fallo: Credenciales incorrectas
                    _loginState.value = LoginState.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                // Fallo de red o conexión
                _loginState.value = LoginState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun register(usuario: User) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.registrar(usuario)
                if (response.isSuccessful) {
                    _registerState.value = RegisterState.Success
                } else {
                    _registerState.value = RegisterState.Error("Error al registrar: El correo ya existe")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Fallo de red: ${e.message}")
            }
        }
    }
}

// ==========================================
// Clases de Estado para la UI
// ==========================================

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val usuario: User) : LoginState() // Usamos el modelo User de tu proyecto
    data class Error(val mensaje: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val mensaje: String) : RegisterState()
}