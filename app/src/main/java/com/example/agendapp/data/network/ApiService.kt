package com.example.agendapp.data.network

import com.example.agendapp.data.model.Ciudad
import com.example.agendapp.data.model.CiudadRequest
import com.example.agendapp.data.model.ClimaResponse
import com.example.agendapp.data.model.User // Usamos tu modelo User existente
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Login y Registro
    @POST("/api/usuarios/login")
    suspend fun login(@Body request: User): Response<User>

    @POST("/api/usuarios/registro")
    suspend fun registrar(@Body usuario: User): Response<User>

    // Clima
    @GET("/api/clima/usuario/{id}")
    suspend fun verClima(@Path("id") id: Int): Response<List<ClimaResponse>>

    @POST("/api/clima")
    suspend fun agregarCiudad(@Body request: CiudadRequest): Response<Ciudad>

    @DELETE("/api/clima/{id}")
    suspend fun eliminarCiudad(@Path("id") id: Int): Response<Void>
}