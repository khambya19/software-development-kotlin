package com.example.internnepal

import com.example.internnepal.model.UserModel
import org.junit.Assert.assertEquals
import org.junit.Test

class UserModelTest {

    @Test
    fun defaultRole_isUser() {
        val user = UserModel()
        assertEquals("user", user.role)
    }

    @Test
    fun userFields_areStoredCorrectly() {
        val user = UserModel(
            userId = "123",
            fullName = "Test User",
            email = "test@example.com",
            password = "secret",
            phoneNumber = "9800000000",
            role = "admin",
            profileImageUrl = "http://example.com/image.jpg"
        )

        assertEquals("123", user.userId)
        assertEquals("Test User", user.fullName)
        assertEquals("test@example.com", user.email)
        assertEquals("secret", user.password)
        assertEquals("9800000000", user.phoneNumber)
        assertEquals("admin", user.role)
        assertEquals("http://example.com/image.jpg", user.profileImageUrl)
    }
}
