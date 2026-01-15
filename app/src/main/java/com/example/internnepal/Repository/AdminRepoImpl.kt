package com.example.internnepal.Repository

import android.util.Log
import com.example.internnepal.model.AdminModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminRepoImpl : AdminRepo {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("admins")

    override fun register(admin: AdminModel, callback: (Boolean, String) -> Unit) {
        val trimmedEmail = admin.email.trim()
        val trimmedPassword = admin.password.trim()

        if (admin.fullName.isBlank() || trimmedEmail.isBlank() || trimmedPassword.isBlank() || admin.phoneNumber.isBlank()) {
            callback(false, "Please fill all fields")
            return
        }

        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Create admin object without password for DB storage
                        val adminToSave = admin.copy(
                            adminId = uid,
                            email = trimmedEmail,
                            password = "", // Security: don't store passwords in plain text DB
                            role = "admin"
                        )
                        
                        database.child(uid).setValue(adminToSave)
                            .addOnSuccessListener {
                                callback(true, "Admin registration successful")
                            }
                            .addOnFailureListener { e ->
                                Log.e("AdminRepo", "DB Save Failed", e)
                                callback(false, "Profile save failed: ${e.message}")
                            }
                    }
                } else {
                    val error = task.exception?.message ?: "Registration failed"
                    callback(false, error)
                }
            }
    }

    override fun getCurrentAdmin(callback: (AdminModel?) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            callback(null)
            return
        }
        database.child(firebaseUser.uid).get().addOnSuccessListener { snapshot ->
            val admin = snapshot.getValue(AdminModel::class.java)
            callback(admin)
        }.addOnFailureListener {
            callback(null)
        }
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        auth.signOut()
        callback(true, "Logged out successfully")
    }
    
    override fun updateAdmin(admin: AdminModel, callback: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(false, "Admin not logged in")
            return
        }
        
        if (admin.fullName.isBlank() || admin.phoneNumber.isBlank()) {
            callback(false, "Name and phone number cannot be empty")
            return
        }
        
        database.child(uid).updateChildren(
            mapOf(
                "fullName" to admin.fullName,
                "phoneNumber" to admin.phoneNumber,
                "profileImageUrl" to admin.profileImageUrl
            )
        ).addOnSuccessListener {
            callback(true, "Profile updated successfully")
        }.addOnFailureListener { e ->
            callback(false, "Update failed: ${e.message}")
        }
    }
    
    override fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        database.orderByChild("email").equalTo(email.trim()).get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
