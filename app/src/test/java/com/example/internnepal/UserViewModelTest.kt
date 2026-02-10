package com.example.internnepal

import com.example.internnepal.Repository.UserRepo
import com.example.internnepal.model.UserModel
import com.example.internnepal.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class UserViewModelTest {

    private class FakeUserRepo(private val userToReturn: UserModel?) : UserRepo {

        var lastLoginEmail: String? = null
        var lastLoginPassword: String? = null
        override fun register(user: UserModel, callback: (Boolean, String) -> Unit) {
            callback(true, "Registered")
        }

        override fun login(
            email: String,
            password: String,
            callback: (Boolean, String, String?) -> Unit
        ) {
            lastLoginEmail = email
            lastLoginPassword = password
            callback(true, "Logged in", "user")
        }

        override fun getCurrentUser(callback: (UserModel?) -> Unit) {
            callback(userToReturn)
        }

        override fun logout(callback: (Boolean, String) -> Unit) {
            callback(true, "Logged out")
        }

        override fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
            callback(true, "Reset")
        }

        override fun updateUser(user: UserModel, callback: (Boolean, String) -> Unit) {
            callback(true, "Updated")
        }

        override fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
            callback(false)
        }
    }

    @Test
    fun fetchCurrentUser_updatesStateAndStopsLoading() {
        val expectedUser = UserModel(userId = "1", fullName = "Test User", email = "test@example.com")
        val viewModel = UserViewModel(FakeUserRepo(expectedUser))

        // initial state
        assertNull(viewModel.currentUser.value)
        assertFalse(viewModel.loading.value)

        // act
        viewModel.fetchCurrentUser()

        // assert
        assertEquals(expectedUser, viewModel.currentUser.value)
        assertFalse(viewModel.loading.value)
    }

    @Test
    fun login_invokesRepositoryAndStopsLoading() {
        val fakeRepo = FakeUserRepo(null)
        val viewModel = UserViewModel(fakeRepo)

        var callbackSuccess = false
        var callbackMessage: String? = null
        var callbackRole: String? = null

        viewModel.login("user@example.com", "password123") { success, message, role ->
            callbackSuccess = success
            callbackMessage = message
            callbackRole = role
        }

        assertEquals("user@example.com", fakeRepo.lastLoginEmail)
        assertEquals("password123", fakeRepo.lastLoginPassword)
        assertFalse(viewModel.loading.value)
        assertEquals(true, callbackSuccess)
        assertEquals("Logged in", callbackMessage)
        assertEquals("user", callbackRole)
    }
}
