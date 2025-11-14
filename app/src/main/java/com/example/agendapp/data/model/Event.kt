package com.example.agendapp.data.model


data class Event(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val date: String,
    val time: String? = null,
    val imagePath: String? = null
)
