package com.example.internnepal.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internnepal.R
import com.example.internnepal.model.UserModel
import com.example.internnepal.model.AdminModel
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.Repository.AdminRepoImpl
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.purple
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.UserViewModel
import com.example.internnepal.viewmodel.AdminViewModel
import com.google.firebase.FirebaseApp

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase before anything else
        FirebaseApp.initializeApp(this)
        
        enableEdgeToEdge()

        val userViewModel = UserViewModel(UserRepoImpl())
        val adminViewModel = AdminViewModel(AdminRepoImpl())
        val userRepoImpl = UserRepoImpl()
        val adminRepoImpl = AdminRepoImpl()

        setContent {
            InternNepalTheme {
                RegisterBody(userViewModel, adminViewModel, userRepoImpl, adminRepoImpl)
            }
        }
    }
}

@Composable
fun RegisterBody(
    userViewModel: UserViewModel? = null,
    adminViewModel: AdminViewModel? = null,
    userRepoImpl: UserRepoImpl? = null,
    adminRepoImpl: AdminRepoImpl? = null
) {

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(white),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(60.dp))
            Text(
                "Create Account",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = pink,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Image(
                painter = painterResource(R.drawable.internnepal),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(10.dp)
            )
            
            // User/Admin Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "User",
                    fontSize = 16.sp,
                    fontWeight = if (!isAdmin) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isAdmin) pink else Color.Gray
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Switch(
                    checked = isAdmin,
                    onCheckedChange = { isAdmin = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = white,
                        checkedTrackColor = pink,
                        uncheckedThumbColor = white,
                        uncheckedTrackColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    "Admin",
                    fontSize = 16.sp,
                    fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAdmin) pink else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                shape = RoundedCornerShape(15.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = purple,
                    unfocusedContainerColor = purple,
                    focusedBorderColor = pink,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                shape = RoundedCornerShape(15.dp),
                placeholder = { Text("abc@gmail.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = purple,
                    unfocusedContainerColor = purple,
                    focusedBorderColor = pink,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        phoneNumber = it
                    }
                },
                label = { Text("Phone Number") },
                shape = RoundedCornerShape(15.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = purple,
                    unfocusedContainerColor = purple,
                    focusedBorderColor = pink,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                trailingIcon = {
                    IconButton(onClick = { visibility = !visibility }) {
                        Icon(
                            if (visibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(15.dp),
                placeholder = { Text("********") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = purple,
                    unfocusedContainerColor = purple,
                    focusedBorderColor = pink,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else if (isRegistering) {
                        Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show()
                    } else {
                        isRegistering = true
                        val trimmedEmail = email.trim()
                        
                        // First check if email exists in users database
                        userRepoImpl?.checkEmailExists(trimmedEmail) { existsInUsers ->
                            if (existsInUsers) {
                                isRegistering = false
                                Toast.makeText(context, "Email already registered", Toast.LENGTH_LONG).show()
                            } else {
                                // Then check if email exists in admins database
                                adminRepoImpl?.checkEmailExists(trimmedEmail) { existsInAdmins ->
                                    if (existsInAdmins) {
                                        isRegistering = false
                                        Toast.makeText(context, "Email already registered as admin", Toast.LENGTH_LONG).show()
                                    } else {
                                        // Email is unique, proceed with registration
                                        if (isAdmin) {
                                            // Register as Admin
                                            val admin = AdminModel(
                                                adminId = System.currentTimeMillis().toString(),
                                                fullName = fullName,
                                                email = trimmedEmail,
                                                password = password,
                                                phoneNumber = phoneNumber
                                            )
                                            adminViewModel?.register(admin) { success, message ->
                                                isRegistering = false
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                if (success) activity?.finish()
                                            }
                                        } else {
                                            // Register as User
                                            val user = UserModel(
                                                userId = System.currentTimeMillis().toString(),
                                                fullName = fullName,
                                                email = trimmedEmail,
                                                password = password,
                                                phoneNumber = phoneNumber
                                            )
                                            userViewModel?.register(user) { success, message ->
                                                isRegistering = false
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                if (success) activity?.finish()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = !isRegistering,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = pink),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 15.dp)
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(if (isAdmin) "Register as Admin" else "Register as User", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                buildAnnotatedString {
                    append("Already a member? ")
                    withStyle(SpanStyle(color = pink, fontWeight = FontWeight.Bold)) {
                        append("Sign In")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity?.finish()
                    },
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    InternNepalTheme {
        RegisterBody(userViewModel = null, adminViewModel = null, userRepoImpl = null, adminRepoImpl = null)
    }
}
