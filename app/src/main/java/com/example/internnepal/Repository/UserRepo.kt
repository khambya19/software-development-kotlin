package com.example.internnepal.Repository

import com.example.internnepal.model.UserModel

interface UserRepo {

    fun register(
        user: UserModel,
        callback: (Boolean, String) -> Unit
    )

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String, String?) -> Unit
    )

    fun getCurrentUser(
        callback: (UserModel?) -> Unit
    )

    fun logout(
        callback: (Boolean, String) -> Unit
    )
}
