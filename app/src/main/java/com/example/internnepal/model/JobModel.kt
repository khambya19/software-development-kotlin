package com.example.internnepal.model

data class JobModel(
    val jobId: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val salary: String = "",
    val openings: String = "", // e.g., "5"
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val postedDate: Long = System.currentTimeMillis()
)
