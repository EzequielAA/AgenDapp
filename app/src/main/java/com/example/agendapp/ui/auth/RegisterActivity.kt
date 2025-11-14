package com.example.agendapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.example.agendapp.R
import com.example.agendapp.data.database.AgenDappDatabaseHelper
import com.example.agendapp.data.model.User
import com.example.agendapp.util.SessionManager
import com.example.agendapp.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val db = AgenDappDatabaseHelper(this)
        val session = SessionManager(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val genderOptions = arrayOf("Masculino", "Femenino")
        spinnerGender.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val gender = spinnerGender.selectedItem.toString()

            // Validación de campos vacíos
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de correo simple
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de contraseña
            if (password.length < 4) {
                Toast.makeText(this, "La contraseña debe tener mínimo 4 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                name = name,
                email = email,
                password = password,
                gender = gender
            )

            val success = db.registerUser(newUser)

            if (success) {
                val loggedUser = db.loginUser(email, password)
                if (loggedUser != null) {
                    session.saveUser(loggedUser)
                }

                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity() // evita volver al registro con el botón atrás
            } else {
                Toast.makeText(this, "Error: el usuario ya existe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
