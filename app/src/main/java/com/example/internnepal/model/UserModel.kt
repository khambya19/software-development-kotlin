package com.example.internnepal.model

data class UserModel(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val role: String = "user" // "user" for jobseekers, "admin" for the admin
)
