package com.example.agendapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.agendapp.R
import com.example.agendapp.data.model.User // Usamos tu modelo User local para la sesión
import com.example.agendapp.ui.main.MainActivity
import com.example.agendapp.viewmodel.AuthViewModel
import com.example.agendapp.viewmodel.RegisterState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Inicializar ViewModel (Conexión a Render)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Referencias a tus vistas (mantenemos los IDs originales)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Lógica del Spinner (Mantenida)
        val genderOptions = arrayOf("Masculino", "Femenino")
        spinnerGender.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)

        // 2. Escuchar la respuesta de la Nube (Observar el Estado)
        lifecycleScope.launch {
            authViewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterState.Loading -> {
                        btnRegister.text = "Registrando..."
                        btnRegister.isEnabled = false
                    }
                    is RegisterState.Success -> {
                        // Registro exitoso en la nube
                        Toast.makeText(this@RegisterActivity, "Cuenta creada en la Nube!", Toast.LENGTH_LONG).show()
                        // Navegamos al Login (flujo correcto para obtener token/ID de sesión)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                    is RegisterState.Error -> {
                        btnRegister.isEnabled = true
                        btnRegister.text = "Registrarse"
                        Toast.makeText(this@RegisterActivity, "Error: ${state.mensaje}", Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        // 3. Acción del Botón
        btnRegister.setOnClickListener {
            val nombre = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val gender = spinnerGender.selectedItem.toString()

            // Validaciones
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Verifica datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 🔥 CORRECCIÓN: Usamos 4 caracteres como mínimo (si ese es el requisito)
            if (password.length < 4) {
                Toast.makeText(this, "La contraseña debe tener mínimo 4 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 CORRECCIÓN: Aseguramos que el ID sea 0, ya que Render lo generará
            val newUser = User(
                id = 0, // Pasamos el valor inicial de la PK
                nombre = nombre,
                email = email,
                password = password,
                gender = gender
            )

            // LLAMADA A RENDER (NUBE) para guardar el usuario
            authViewModel.register(newUser)
        }
    }
}