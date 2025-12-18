package com.example.internnepal.model

data class ApplicationModel(
    val applicationId: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val appliedDate: Long = System.currentTimeMillis(),
    val status: String = "Pending"
)
