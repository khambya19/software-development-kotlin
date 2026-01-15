package com.example.internnepal.Repository

import com.example.internnepal.model.AdminModel

interface AdminRepo {
    fun register(admin: AdminModel, callback: (Boolean, String) -> Unit)
    fun getCurrentAdmin(callback: (AdminModel?) -> Unit)
    fun logout(callback: (Boolean, String) -> Unit)
    fun updateAdmin(admin: AdminModel, callback: (Boolean, String) -> Unit)
    fun checkEmailExists(email: String, callback: (Boolean) -> Unit)
}
