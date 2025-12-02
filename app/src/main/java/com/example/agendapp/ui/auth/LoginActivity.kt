package com.example.agendapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.agendapp.R
import com.example.agendapp.ui.main.MainActivity
import com.example.agendapp.util.SessionManager
import com.example.agendapp.viewmodel.AuthViewModel
import com.example.agendapp.viewmodel.LoginState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🔥 1. Ya NO usamos AgenDappDatabaseHelper (Login migrado a la Nube)
        sessionManager = SessionManager(this)

        // Si ya hay sesión, ir directamente al home
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 2. Inicializar ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Referencias a tus vistas
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // 3. Acción del Botón
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // 🔥 LLAMADA A RENDER (AuthViewModel)
                authViewModel.login(email, password)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Escuchar la respuesta de la Nube (Observar el Estado)
        lifecycleScope.launch {
            authViewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginState.Loading -> {
                        btnLogin.isEnabled = false
                        btnLogin.text = "Cargando..."
                    }
                    is LoginState.Success -> {
                        // Login Exitoso: Obtenemos el ID y Nombre de la respuesta de Render
                        val usuario = state.usuario

                        // 🔥 CORRECCIÓN 1: Usamos .name
                        sessionManager.saveUserSession(usuario.id, usuario.nombre)

                        // 🔥 CORRECCIÓN 2: Usamos .name
                        Toast.makeText(this@LoginActivity, "Bienvenido ${usuario.nombre}", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                    is LoginState.Error -> {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Ingresar"
                        Toast.makeText(this@LoginActivity, state.mensaje, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}