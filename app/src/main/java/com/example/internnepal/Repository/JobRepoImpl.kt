package com.example.internnepal.Repository

import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.google.firebase.database.FirebaseDatabase

class JobRepoImpl : JobRepo {
    private val database = FirebaseDatabase.getInstance().getReference("jobs")
    private val appDatabase = FirebaseDatabase.getInstance().getReference("applications")

    override fun postJob(job: JobModel, callback: (Boolean, String) -> Unit) {
        val id = database.push().key ?: ""
        val newJob = job.copy(jobId = id)
        database.child(id).setValue(newJob)
            .addOnSuccessListener { callback(true, "Job posted successfully") }
            .addOnFailureListener { e -> callback(false, "Failed: ${e.message}") }
    }

    override fun updateJob(job: JobModel, callback: (Boolean, String) -> Unit) {
        database.child(job.jobId).setValue(job)
            .addOnSuccessListener { callback(true, "Job updated successfully") }
            .addOnFailureListener { e -> callback(false, "Update failed: ${e.message}") }
    }

    override fun deleteJob(jobId: String, callback: (Boolean, String) -> Unit) {
        database.child(jobId).removeValue()
            .addOnSuccessListener { callback(true, "Job deleted successfully") }
            .addOnFailureListener { e -> callback(false, "Delete failed: ${e.message}") }
    }

    override fun getAllJobs(callback: (List<JobModel>) -> Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val jobList = mutableListOf<JobModel>()
            for (child in snapshot.children) {
                child.getValue(JobModel::class.java)?.let { jobList.add(it) }
            }
            callback(jobList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    override fun applyForJob(application: ApplicationModel, callback: (Boolean, String) -> Unit) {
        val id = appDatabase.push().key ?: ""
        val newApp = application.copy(applicationId = id)
        appDatabase.child(id).setValue(newApp)
            .addOnSuccessListener { callback(true, "Applied successfully") }
            .addOnFailureListener { e -> callback(false, "Application failed: ${e.message}") }
    }

    override fun getApplicationsForJob(jobId: String, callback: (List<ApplicationModel>) -> Unit) {
        appDatabase.orderByChild("jobId").equalTo(jobId).get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<ApplicationModel>()
                for (child in snapshot.children) {
                    child.getValue(ApplicationModel::class.java)?.let { list.add(it) }
                }
                callback(list)
            }.addOnFailureListener { callback(emptyList()) }
    }

    override fun getAllApplications(callback: (List<ApplicationModel>) -> Unit) {
        appDatabase.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<ApplicationModel>()
            for (child in snapshot.children) {
                child.getValue(ApplicationModel::class.java)?.let { list.add(it) }
            }
            callback(list)
        }.addOnFailureListener { callback(emptyList()) }
    }
}
