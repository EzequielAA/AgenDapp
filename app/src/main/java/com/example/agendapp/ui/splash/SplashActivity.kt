package com.example.agendapp.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import com.example.agendapp.R
import com.example.agendapp.ui.auth.LoginActivity
import com.example.agendapp.ui.auth.RegisterActivity
import com.example.agendapp.util.SessionManager
import com.example.agendapp.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val session = SessionManager(this)
        val user = session.getUser()

        // Si el usuario ya tiene sesión → ir directo al Main
        if (user != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity() // evita volver atrás al splash
            }, 600) // pequeño delay opcional
            return
        }

        // Si NO hay sesión, mostrar botones
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
