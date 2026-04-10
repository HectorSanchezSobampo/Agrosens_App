package com.example.myapplication.domain.model

data class User(
    val id: Int? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)