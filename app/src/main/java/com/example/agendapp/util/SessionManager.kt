package com.example.agendapp.util

import android.content.Context
import com.example.agendapp.data.model.User

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("AgenDappPrefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putInt("user_id", user.id)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_gender", user.gender)
            putString("user_password", user.password)   // ✅ AHORA SÍ GUARDA LA CONTRASEÑA
            apply()
        }
    }

    fun getUser(): User? {
        val id = prefs.getInt("user_id", -1)
        if (id == -1) return null

        return User(
            id = id,
            name = prefs.getString("user_name", "") ?: "",
            email = prefs.getString("user_email", "") ?: "",
            password = prefs.getString("user_password", "") ?: "",   // ✅ SE RECUPERA LA CONTRASEÑA
            gender = prefs.getString("user_gender", "") ?: ""
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
