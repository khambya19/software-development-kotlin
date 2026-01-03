package com.example.internnepal.Repository

import android.util.Log
import com.example.internnepal.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepoImpl : UserRepo {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun register(user: UserModel, callback: (Boolean, String) -> Unit) {
        val trimmedEmail = user.email.trim()
        val trimmedPassword = user.password.trim()

        if (user.fullName.isBlank() || trimmedEmail.isBlank() || trimmedPassword.isBlank() || user.phoneNumber.isBlank()) {
            callback(false, "Please fill all fields")
            return
        }

        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Create user object without password for DB storage
                        val userToSave = user.copy(
                            userId = uid,
                            email = trimmedEmail,
                            password = "", // Security: don't store passwords in plain text DB
                            role = "user"
                        )
                        
                        database.child(uid).setValue(userToSave)
                            .addOnSuccessListener {
                                callback(true, "Registration successful")
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserRepo", "DB Save Failed", e)
                                callback(false, "Profile save failed: ${e.message}")
                            }
                    }
                } else {
                    val error = task.exception?.message ?: "Registration failed"
                    callback(false, error)
                }
            }
    }

    override fun login(email: String, password: String, callback: (Boolean, String, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            callback(false, "Email and password cannot be empty", null)
            return
        }

        // Firebase Auth Login
        auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Check in users database first
                        database.child(uid).get().addOnSuccessListener { userSnapshot ->
                            if (userSnapshot.exists()) {
                                val user = userSnapshot.getValue(UserModel::class.java)
                                callback(true, "Login successful", user?.role ?: "user")
                            } else {
                                // Check in admins database
                                val adminDatabase = FirebaseDatabase.getInstance().getReference("admins")
                                adminDatabase.child(uid).get().addOnSuccessListener { adminSnapshot ->
                                    if (adminSnapshot.exists()) {
                                        callback(true, "Admin Login successful", "admin")
                                    } else {
                                        // Default to user if not found in either
                                        callback(true, "Login successful", "user")
                                    }
                                }.addOnFailureListener {
                                    callback(true, "Login successful", "user")
                                }
                            }
                        }.addOnFailureListener {
                            callback(true, "Login successful", "user")
                        }
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Invalid email or password"
                    callback(false, errorMsg, null)
                }
            }
    }

    override fun getCurrentUser(callback: (UserModel?) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            callback(null)
            return
        }
        database.child(firebaseUser.uid).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(UserModel::class.java)
            callback(user)
        }.addOnFailureListener {
            callback(null)
        }
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        auth.signOut()
        callback(true, "Logged out successfully")
    }
    
    override fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
        val trimmedEmail = email.trim()
        
        if (trimmedEmail.isBlank()) {
            callback(false, "Email cannot be empty")
            return
        }
        
        auth.sendPasswordResetEmail(trimmedEmail)
            .addOnSuccessListener {
                callback(true, "Reset link sent to your email")
            }
            .addOnFailureListener { e ->
                callback(false, "Error: ${e.message}")
            }
    }
    
    override fun updateUser(user: UserModel, callback: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(false, "User not logged in")
            return
        }
        
        if (user.fullName.isBlank() || user.phoneNumber.isBlank()) {
            callback(false, "Name and phone number cannot be empty")
            return
        }
        
        database.child(uid).updateChildren(
            mapOf(
                "fullName" to user.fullName,
                "phoneNumber" to user.phoneNumber,
                "profileImageUrl" to user.profileImageUrl
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
