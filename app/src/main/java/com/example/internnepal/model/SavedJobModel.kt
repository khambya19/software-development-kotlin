package com.example.internnepal.model

data class SavedJobModel(
    val savedId: String = "",
    val userId: String = "",
    val jobId: String = "",
    val savedDate: Long = System.currentTimeMillis()
)
