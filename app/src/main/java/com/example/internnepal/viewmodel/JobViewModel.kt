package com.example.internnepal.viewmodel

import androidx.lifecycle.ViewModel
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.Repository.JobRepo

class JobViewModel(private val repo: JobRepo) : ViewModel() {

    fun postJob(job: JobModel, callback: (Boolean, String) -> Unit) {
        repo.postJob(job, callback)
    }

    fun updateJob(job: JobModel, callback: (Boolean, String) -> Unit) {
        repo.updateJob(job, callback)
    }

    fun deleteJob(jobId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteJob(jobId, callback)
    }

    fun getAllJobs(callback: (List<JobModel>) -> Unit) {
        repo.getAllJobs(callback)
    }

    fun applyForJob(application: ApplicationModel, callback: (Boolean, String) -> Unit) {
        repo.applyForJob(application, callback)
    }

    fun getApplicationsForJob(jobId: String, callback: (List<ApplicationModel>) -> Unit) {
        repo.getApplicationsForJob(jobId, callback)
    }

    fun getAllApplications(callback: (List<ApplicationModel>) -> Unit) {
        repo.getAllApplications(callback)
    }
}
