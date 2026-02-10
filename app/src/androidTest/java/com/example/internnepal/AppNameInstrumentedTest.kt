package com.example.internnepal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNameInstrumentedTest {

    @Test
    fun appName_isInternNepal() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("Intern Nepal", appContext.getString(R.string.app_name))
    }

    @Test
    fun registerTitle_isCorrect() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("RegisterActivity", appContext.getString(R.string.title_activity_register))
    }
}
