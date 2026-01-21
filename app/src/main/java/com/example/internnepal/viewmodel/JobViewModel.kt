package com.example.internnepal.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.Repository.JobRepo

class JobViewModel(private val repo: JobRepo) : ViewModel() {

    private val _jobs = mutableStateOf<List<JobModel>>(emptyList())
    val jobs: State<List<JobModel>> = _jobs

    private val _applications = mutableStateOf<List<ApplicationModel>>(emptyList())
    val applications: State<List<ApplicationModel>> = _applications

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading
    
    private val _savedJobIds = mutableStateOf<List<String>>(emptyList())
    val savedJobIds: State<List<String>> = _savedJobIds
    
    private val _appliedJobIds = mutableStateOf<List<String>>(emptyList())
    val appliedJobIds: State<List<String>> = _appliedJobIds

    fun postJob(job: JobModel, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.postJob(job) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun updateJob(job: JobModel, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.updateJob(job) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun deleteJob(jobId: String, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.deleteJob(jobId) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun fetchAllJobs() {
        _loading.value = true
        repo.getAllJobs { jobList ->
            _jobs.value = jobList
            _loading.value = false
        }
    }
    
    fun fetchJobsByAdmin(adminId: String) {
        _loading.value = true
        repo.getJobsByAdmin(adminId) { jobList ->
            _jobs.value = jobList
            _loading.value = false
        }
    }

    fun applyForJob(application: ApplicationModel, callback: (Boolean, String) -> Unit) {
        // Check if already applied (first line of defense)
        if (_appliedJobIds.value.contains(application.jobId)) {
            callback(false, "You have already applied to this job")
            return
        }
        
        // Immediately mark as applied to prevent race conditions
        _appliedJobIds.value = _appliedJobIds.value + application.jobId
        _loading.value = true
        
        repo.applyForJob(application) { success, message ->
            _loading.value = false
            if (!success) {
                // If application failed, remove from applied list
                _appliedJobIds.value = _appliedJobIds.value.filter { it != application.jobId }
            }
            callback(success, message)
        }
    }

    fun fetchApplicationsForJob(jobId: String) {
        _loading.value = true
        repo.getApplicationsForJob(jobId) { appList ->
            _applications.value = appList
            _loading.value = false
        }
    }

    fun fetchAllApplications() {
        _loading.value = true
        repo.getAllApplications { appList ->
            _applications.value = appList
            _loading.value = false
        }
    }
    
    fun fetchMyApplications(userId: String) {
        _loading.value = true
        repo.getMyApplications(userId) { appList ->
            _applications.value = appList
            _appliedJobIds.value = appList.map { it.jobId }
            _loading.value = false
        }
    }
    
    fun saveJob(userId: String, jobId: String, callback: (Boolean, String) -> Unit) {
        repo.saveJob(userId, jobId, callback)
    }
    
    fun unsaveJob(userId: String, jobId: String, callback: (Boolean, String) -> Unit) {
        repo.unsaveJob(userId, jobId, callback)
    }
    
    fun fetchMySavedJobs(userId: String) {
        repo.getMySavedJobs(userId) { jobIds ->
            _savedJobIds.value = jobIds
        }
    }
    
    fun updateApplicationStatus(applicationId: String, status: String, adminMessage: String, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.updateApplicationStatus(applicationId, status, adminMessage) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }
    
    fun withdrawApplication(applicationId: String, userId: String, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.withdrawApplication(applicationId) { success, message ->
            if (success) {
                // Refresh the user's applications after withdrawal
                fetchMyApplications(userId)
            }
            _loading.value = false
            callback(success, message)
        }
    }
    
    fun uploadImage(context: Context, uri: android.net.Uri, callback: (String?) -> Unit) {
        _loading.value = true
        repo.uploadImage(context, uri) { imageUrl ->
            _loading.value = false
            callback(imageUrl)
        }
    }
}
