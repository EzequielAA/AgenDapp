package com.example.agendapp.data.model

data class ClimaResponse(
    val ciudad: String,
    val temperatura: Double,
    val descripcion: String,
    val humedad: Double
)