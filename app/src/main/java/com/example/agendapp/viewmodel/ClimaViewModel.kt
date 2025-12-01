package com.example.agendapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agendapp.data.model.CiudadRequest
import com.example.agendapp.data.model.ClimaResponse
import com.example.agendapp.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClimaViewModel : ViewModel() {

    private val _climaList = MutableStateFlow<List<ClimaResponse>>(emptyList())
    val climaList: StateFlow<List<ClimaResponse>> = _climaList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarClima(usuarioId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.verClima(usuarioId)
                if (response.isSuccessful) {
                    _climaList.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun agregarCiudad(usuarioId: Int, ciudad: String) {
        viewModelScope.launch {
            try {
                val request = CiudadRequest(usuarioId, ciudad)
                val response = RetrofitClient.apiService.agregarCiudad(request)
                if (response.isSuccessful) {
                    cargarClima(usuarioId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarCiudad(ciudadId: Int, usuarioId: Int) {
        // Nota: Para eliminar necesitas el ID de la ciudad, no el nombre.
        // Por ahora, este método es un placeholder si tu API requiere ID.
    }
}