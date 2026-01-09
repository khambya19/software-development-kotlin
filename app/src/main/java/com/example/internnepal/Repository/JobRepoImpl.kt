package com.example.internnepal.Repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.model.SavedJobModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class JobRepoImpl : JobRepo {
    private val database = FirebaseDatabase.getInstance().getReference("jobs")
    private val appDatabase = FirebaseDatabase.getInstance().getReference("applications")
    private val savedDatabase = FirebaseDatabase.getInstance().getReference("savedJobs")
    
    // Cloudinary configuration
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "daiazjvcr",
            "api_key" to "629169381168885",
            "api_secret" to "Bfb5Qdja4YzMhMFJdRyc_KC_B4M"
        )
    )

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
        database.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val jobList = mutableListOf<JobModel>()
                for (child in snapshot.children) {
                    child.getValue(JobModel::class.java)?.let { jobList.add(it) }
                }
                callback(jobList)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                callback(emptyList())
            }
        })
    }
    
    override fun getJobsByAdmin(adminId: String, callback: (List<JobModel>) -> Unit) {
        database.orderByChild("adminId").equalTo(adminId)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val jobList = mutableListOf<JobModel>()
                    for (child in snapshot.children) {
                        child.getValue(JobModel::class.java)?.let { jobList.add(it) }
                    }
                    callback(jobList)
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    callback(emptyList())
                }
            })
    }
    
    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                android.util.Log.d("CloudinaryUpload", "=== Upload Started ===")
                android.util.Log.d("CloudinaryUpload", "Image URI: $imageUri")
                
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    android.util.Log.e("CloudinaryUpload", "Failed to open input stream")
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                    return@execute
                }
                
                var fileName = getFileNameFromUri(context, imageUri)
                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"
                
                android.util.Log.d("CloudinaryUpload", "Starting upload for: $fileName")
                android.util.Log.d("CloudinaryUpload", "Cloud name: ${cloudinary.config.cloudName}")

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?
                android.util.Log.d("CloudinaryUpload", "Received URL: $imageUrl")

                imageUrl = imageUrl?.replace("http://", "https://")
                android.util.Log.d("CloudinaryUpload", "Final URL: $imageUrl")
                android.util.Log.d("CloudinaryUpload", "=== Upload Success ===")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                android.util.Log.e("CloudinaryUpload", "=== Upload Failed ===")
                android.util.Log.e("CloudinaryUpload", "Error: ${e.message}")
                android.util.Log.e("CloudinaryUpload", "Stack trace:", e)
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            } finally {
                executor.shutdown()
            }
        }
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    override fun applyForJob(application: ApplicationModel, callback: (Boolean, String) -> Unit) {
        // First check if user has already applied to this job
        appDatabase
            .orderByChild("userId").equalTo(application.userId)
            .get()
            .addOnSuccessListener { snapshot ->
                var alreadyApplied = false
                for (child in snapshot.children) {
                    val app = child.getValue(ApplicationModel::class.java)
                    if (app?.jobId == application.jobId) {
                        alreadyApplied = true
                        break
                    }
                }
                
                if (alreadyApplied) {
                    callback(false, "You have already applied to this job")
                } else {
                    // Create new application
                    val id = appDatabase.push().key ?: ""
                    val newApp = application.copy(applicationId = id)
                    appDatabase.child(id).setValue(newApp)
                        .addOnSuccessListener { callback(true, "Applied successfully") }
                        .addOnFailureListener { e -> callback(false, "Application failed: ${e.message}") }
                }
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to check application status: ${e.message}")
            }
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
            val seenIds = mutableSetOf<String>()
            for (child in snapshot.children) {
                child.getValue(ApplicationModel::class.java)?.let { app ->
                    // Only add if we haven't seen this applicationId before
                    if (app.applicationId !in seenIds) {
                        list.add(app)
                        seenIds.add(app.applicationId)
                    }
                }
            }
            callback(list)
        }.addOnFailureListener { callback(emptyList()) }
    }
    
    override fun getMyApplications(userId: String, callback: (List<ApplicationModel>) -> Unit) {
        appDatabase.orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<ApplicationModel>()
                val seenIds = mutableSetOf<String>()
                for (child in snapshot.children) {
                    child.getValue(ApplicationModel::class.java)?.let { app ->
                        // Only add if we haven't seen this applicationId before
                        if (app.applicationId !in seenIds) {
                            list.add(app)
                            seenIds.add(app.applicationId)
                        }
                    }
                }
                callback(list)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
    
    override fun saveJob(userId: String, jobId: String, callback: (Boolean, String) -> Unit) {
        val savedId = savedDatabase.push().key ?: return callback(false, "Error generating ID")
        val savedJob = SavedJobModel(savedId, userId, jobId)
        savedDatabase.child(savedId).setValue(savedJob)
            .addOnSuccessListener { callback(true, "Job saved successfully") }
            .addOnFailureListener { callback(false, "Failed to save job") }
    }
    
    override fun unsaveJob(userId: String, jobId: String, callback: (Boolean, String) -> Unit) {
        savedDatabase.orderByChild("userId").equalTo(userId).get().addOnSuccessListener { snapshot ->
            var found = false
            for (child in snapshot.children) {
                val saved = child.getValue(SavedJobModel::class.java)
                if (saved?.jobId == jobId) {
                    child.ref.removeValue()
                        .addOnSuccessListener { callback(true, "Job removed from saved") }
                        .addOnFailureListener { callback(false, "Failed to unsave job") }
                    found = true
                    break
                }
            }
            if (!found) callback(false, "Job not found in saved list")
        }.addOnFailureListener { callback(false, "Failed to unsave job") }
    }
    
    override fun getMySavedJobs(userId: String, callback: (List<String>) -> Unit) {
        savedDatabase.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobIds = mutableListOf<String>()
                    for (child in snapshot.children) {
                        child.getValue(SavedJobModel::class.java)?.let { 
                            jobIds.add(it.jobId)
                        }
                    }
                    callback(jobIds)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
    
    override fun updateApplicationStatus(applicationId: String, status: String, adminMessage: String, callback: (Boolean, String) -> Unit) {
        val updates = mapOf(
            "status" to status,
            "adminMessage" to adminMessage
        )
        appDatabase.child(applicationId).updateChildren(updates)
            .addOnSuccessListener { callback(true, "Application $status") }
            .addOnFailureListener { e -> callback(false, "Failed: ${e.message}") }
    }

    override fun withdrawApplication(applicationId: String, callback: (Boolean, String) -> Unit) {
        appDatabase.child(applicationId).removeValue()
            .addOnSuccessListener { callback(true, "Application withdrawn successfully") }
            .addOnFailureListener { e -> callback(false, "Failed to withdraw: ${e.message}") }
    }
}
