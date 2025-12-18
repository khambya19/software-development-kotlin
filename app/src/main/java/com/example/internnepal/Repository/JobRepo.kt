package com.example.internnepal.Repository

import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel

interface JobRepo {
    fun postJob(job: JobModel, callback: (Boolean, String) -> Unit)
    fun updateJob(job: JobModel, callback: (Boolean, String) -> Unit)
    fun deleteJob(jobId: String, callback: (Boolean, String) -> Unit)
    fun getAllJobs(callback: (List<JobModel>) -> Unit)
    
    // Application management
    fun applyForJob(application: ApplicationModel, callback: (Boolean, String) -> Unit)
    fun getApplicationsForJob(jobId: String, callback: (List<ApplicationModel>) -> Unit)
    fun getAllApplications(callback: (List<ApplicationModel>) -> Unit)
}
