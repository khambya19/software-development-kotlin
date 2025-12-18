package com.example.internnepal.viewmodel

import androidx.lifecycle.ViewModel
import com.example.internnepal.model.UserModel
import com.example.internnepal.Repository.UserRepo

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    fun register(
        user: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.register(user, callback)
    }

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        repo.login(email, password, callback)
    }

    fun getCurrentUser(
        callback: (UserModel?) -> Unit
    ) {
        repo.getCurrentUser(callback)
    }

    fun logout(
        callback: (Boolean, String) -> Unit
    ) {
        repo.logout(callback)
    }
}
