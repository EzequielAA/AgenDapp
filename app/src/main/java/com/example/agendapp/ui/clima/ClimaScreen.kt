package com.example.agendapp.ui.clima

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agendapp.data.model.ClimaResponse
import com.example.agendapp.viewmodel.ClimaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaScreen(
    usuarioId: Int, // Recibimos el ID como Int
    viewModel: ClimaViewModel = viewModel()
) {
    val climaList by viewModel.climaList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarClima(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Ciudades 🌤️") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Ciudad")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6200EE))
                }
            } else {
                if (climaList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes ciudades guardadas. ¡Agrega una!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(climaList) { clima ->
                            ClimaCard(clima, onDelete = {
                                // Para borrar necesitaríamos el ID de la ciudad, pero ClimaResponse solo trae nombre.
                                // Por ahora, puedes implementar borrar por nombre o dejarlo visual.
                                // viewModel.eliminarCiudad(clima.id)
                            })
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddCiudadDialog(
            onDismiss = { showDialog = false },
            onConfirm = { ciudad ->
                viewModel.agregarCiudad(usuarioId, ciudad)
                showDialog = false
            }
        )
    }
}

@Composable
fun ClimaCard(clima: ClimaResponse, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(clima.ciudad, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(clima.descripcion.replaceFirstChar { it.uppercase() }, color = Color.Gray)
                Text("Humedad: ${clima.humedad}%", fontSize = 12.sp, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${clima.temperatura}°",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF6200EE)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                // Botón de borrar (Opcional)
                /*
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                }
                */
            }
        }
    }
}

@Composable
fun AddCiudadDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Ciudad") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nombre (Ej: Madrid)") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}