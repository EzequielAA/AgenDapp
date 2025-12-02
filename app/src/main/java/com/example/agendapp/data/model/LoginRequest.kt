package com.example.agendapp.data.model

// Esta clase es un DTO (Data Transfer Object) que representa
// lo que enviamos a la API para iniciar sesión.

data class LoginRequest(
    val email: String,
    val password: String
)