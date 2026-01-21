package com.example.internnepal.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.internnepal.model.AdminModel
import com.example.internnepal.Repository.AdminRepo

class AdminViewModel(private val repo: AdminRepo) : ViewModel() {

    private val _currentAdmin = mutableStateOf<AdminModel?>(null)
    val currentAdmin: State<AdminModel?> = _currentAdmin

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    fun register(
        admin: AdminModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.register(admin) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun fetchCurrentAdmin() {
        _loading.value = true
        repo.getCurrentAdmin { admin ->
            _currentAdmin.value = admin
            _loading.value = false
        }
    }

    fun logout(
        callback: (Boolean, String) -> Unit
    ) {
        repo.logout { success, message ->
            if (success) _currentAdmin.value = null
            callback(success, message)
        }
    }
    
    fun updateAdmin(
        admin: AdminModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.updateAdmin(admin) { success, message ->
            if (success) {
                _currentAdmin.value = admin
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
