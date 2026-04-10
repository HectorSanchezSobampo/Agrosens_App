package com.example.myapplication.model

data class User(
    val id: Int? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)