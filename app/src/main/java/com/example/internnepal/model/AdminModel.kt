package com.example.internnepal.model

data class AdminModel(
    val adminId: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val role: String = "admin",
    val profileImageUrl: String = "" // URL to profile picture
)
