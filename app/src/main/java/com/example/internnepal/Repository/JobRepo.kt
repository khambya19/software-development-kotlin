package com.example.internnepal.Repository

import android.content.Context
import android.net.Uri
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.model.SavedJobModel

interface JobRepo {
    fun postJob(job: JobModel, callback: (Boolean, String) -> Unit)
    fun updateJob(job: JobModel, callback: (Boolean, String) -> Unit)
    fun deleteJob(jobId: String, callback: (Boolean, String) -> Unit)
    fun getAllJobs(callback: (List<JobModel>) -> Unit)
    fun getJobsByAdmin(adminId: String, callback: (List<JobModel>) -> Unit)
    
    // Image upload
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?
    
    // Application management
    fun applyForJob(application: ApplicationModel, callback: (Boolean, String) -> Unit)
    fun getApplicationsForJob(jobId: String, callback: (List<ApplicationModel>) -> Unit)
    fun getAllApplications(callback: (List<ApplicationModel>) -> Unit)
    fun getMyApplications(userId: String, callback: (List<ApplicationModel>) -> Unit)
    fun updateApplicationStatus(applicationId: String, status: String, adminMessage: String, callback: (Boolean, String) -> Unit)
    fun withdrawApplication(applicationId: String, callback: (Boolean, String) -> Unit)
    
    // Saved Jobs management
    fun saveJob(userId: String, jobId: String, callback: (Boolean, String) -> Unit)
    fun unsaveJob(userId: String, jobId: String, callback: (Boolean, String) -> Unit)
    fun getMySavedJobs(userId: String, callback: (List<String>) -> Unit)
}
