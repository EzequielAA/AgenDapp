package com.example.agendapp.ui.clima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class ClimaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recibimos el ID como Int (sin conversiones raras)
        val userId = intent.getIntExtra("USER_ID", -1)

        setContent {
            MaterialTheme {
                ClimaScreen(usuarioId = userId)
            }
        }
    }
}