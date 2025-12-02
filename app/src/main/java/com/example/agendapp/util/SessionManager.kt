package com.example.agendapp.util

import android.content.Context
import android.content.SharedPreferences
import com.example.agendapp.data.model.User

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("AgenDappPrefs", Context.MODE_PRIVATE)

    companion object {
        // Constantes para evitar errores de escritura en SharedPreferences
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        // Mantenemos las claves para el User completo
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_USER_GENDER = "user_gender"
    }

    // 🔥 1. NUEVO MÉTODO: Usado por LoginActivity para iniciar sesión
    // Guardamos ID (Int) y Nombre (mínimo esencial)
    fun saveUserSession(id: Int, name: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, id)
            putString(KEY_USER_NAME, name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // 🔥 2. NUEVO MÉTODO: Verificar si el usuario ya está logueado
    fun isLoggedIn(): Boolean {
        // Si existe un ID guardado y el flag de login está true
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && prefs.contains(KEY_USER_ID)
    }

    // Método completo que ya tenías (Actualizado para guardar el estado de login)
    fun saveUser(user: User) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.nombre)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_PASSWORD, user.password)
            putString(KEY_USER_GENDER, user.gender)
            putBoolean(KEY_IS_LOGGED_IN, true) // Marcamos como activo
            apply()
        }
    }

    fun getUser(): User? {
        if (!isLoggedIn()) return null

        val id = prefs.getInt(KEY_USER_ID, -1)
        if (id == -1) return null

        // Recuperamos todos los datos (incluido password, necesario para validación en MainActivity)
        return User(
            id = id,
            nombre = prefs.getString(KEY_USER_NAME, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            password = prefs.getString(KEY_USER_PASSWORD, "") ?: "",
            gender = prefs.getString(KEY_USER_GENDER, "") ?: ""
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}