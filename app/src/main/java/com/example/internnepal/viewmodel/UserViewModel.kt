package com.example.internnepal.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.internnepal.model.UserModel
import com.example.internnepal.Repository.UserRepo

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    private val _currentUser = mutableStateOf<UserModel?>(null)
    val currentUser: State<UserModel?> = _currentUser

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    fun register(
        user: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.register(user) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        _loading.value = true
        repo.login(email, password) { success, message, role ->
            _loading.value = false
            callback(success, message, role)
        }
    }

    fun fetchCurrentUser() {
        _loading.value = true
        repo.getCurrentUser { user ->
            _currentUser.value = user
            _loading.value = false
        }
    }

    fun logout(
        callback: (Boolean, String) -> Unit
    ) {
        repo.logout { success, message ->
            if (success) _currentUser.value = null
            callback(success, message)
        }
    }
    
    fun resetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.resetPassword(email) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }
    
    fun updateUser(
        user: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.updateUser(user) { success, message ->
            if (success) {
                _currentUser.value = user
            }
            _loading.value = false
            callback(success, message)
        }
    }
    
    fun checkEmailExists(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        repo.checkEmailExists(email, callback)
    }
}
