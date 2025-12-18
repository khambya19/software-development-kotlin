package com.example.internnepal.Repository

import com.example.internnepal.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepoImpl : UserRepo {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun register(user: UserModel, callback: (Boolean, String) -> Unit) {
        if (user.fullName.isEmpty() || user.email.isEmpty() || user.password.isEmpty() || user.phoneNumber.isEmpty()) {
            callback(false, "Please fill all fields")
            return
        }

        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        database.child(it.uid).setValue(user)
                            .addOnSuccessListener {
                                callback(true, "Registration successful")
                            }
                            .addOnFailureListener { e ->
                                callback(false, "Failed to save user data: ${e.message}")
                            }
                    }
                } else {
                    callback(false, "Registration failed: ${task.exception?.message}")
                }
            }
    }

    override fun login(email: String, password: String, callback: (Boolean, String, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false, "Email and password cannot be empty", null)
            return
        }

        // Hardcoded Admin Login
        if (email == "ankitadmin" && password == "ankitpassword") {
            callback(true, "Admin Login successful", "admin")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        database.child(uid).get().addOnSuccessListener { snapshot ->
                            val user = snapshot.getValue(UserModel::class.java)
                            callback(true, "Login successful", user?.role ?: "user")
                        }.addOnFailureListener {
                            callback(true, "Login successful", "user")
                        }
                    } else {
                        callback(true, "Login successful", "user")
                    }
                } else {
                    callback(false, "Invalid email or password", null)
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
}
