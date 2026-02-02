package com.example.internnepal.view.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.model.UserModel
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.view.LoginActivity
import com.example.internnepal.viewmodel.UserViewModel
import com.example.internnepal.viewmodel.JobViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    jobViewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val user by userViewModel.currentUser
    val applications by jobViewModel.applications
    val savedJobIds by jobViewModel.savedJobIds

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploadingImage = true
            
            // Upload image to Cloudinary
            jobViewModel.uploadImage(context, it) { imageUrl ->
                isUploadingImage = false
                if (imageUrl != null) {
                    // Update user with new profile image
                    user?.let { currentUser ->
                        val updatedUser = currentUser.copy(profileImageUrl = imageUrl)
                        userViewModel.updateUser(updatedUser) { success, message ->
                            Toast.makeText(context, if (success) "Profile picture updated!" else message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                userViewModel.fetchCurrentUser()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Real-time count of user's applications
    val myApplicationsCount = remember(applications, user) {
        applications.count { it.userId == user?.userId }
    }
    
    // Real-time count of saved jobs
    val mySavedJobsCount = remember(savedJobIds) {
        savedJobIds.size
    }
    
    // Fetch real-time data when user loads
    LaunchedEffect(user) {
        user?.let { currentUser ->
            jobViewModel.fetchMyApplications(currentUser.userId)
            jobViewModel.fetchMySavedJobs(currentUser.userId)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8F5FF), Color.White)
                )
            )
    ) {
        item {
            ProfileHeader(
                fullName = user?.fullName ?: "Loading...",
                email = user?.email ?: "Loading...",
                profileImageUrl = user?.profileImageUrl ?: "",
                onImageClick = { imagePickerLauncher.launch("image/*") },
                isUploading = isUploadingImage
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileStats(appliedCount = myApplicationsCount, savedCount = mySavedJobsCount)
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileOptions(
                onEditClick = { showEditDialog = true },
                onPasswordClick = { showPasswordDialog = true },
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout from Intern Nepal?") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.logout { success, _ ->
                            if (success) {
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                activity?.finish()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = pink)
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = pink) }
            }
        )
    }

    if (showEditDialog) {
        user?.let { currentUser ->
            EditProfileDialog(
                currentUser = currentUser,
                onDismiss = { showEditDialog = false },
                onSave = { updatedUser ->
                    userViewModel.updateUser(updatedUser) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            showEditDialog = false
                            userViewModel.fetchCurrentUser()
                        }
                    }
                }
            )
        }
    }
    
    if (showPasswordDialog) {
        UserChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSave = { currentPassword, newPassword ->
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    
                    user.reauthenticate(credential).addOnCompleteListener { reauth ->
                        if (reauth.isSuccessful) {
                            user.updatePassword(newPassword).addOnCompleteListener { update ->
                                if (update.isSuccessful) {
                                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    showPasswordDialog = false
                                } else {
                                    Toast.makeText(context, "Failed to update password: ${update.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(
    fullName: String,
    email: String,
    profileImageUrl: String,
    onImageClick: () -> Unit,
    isUploading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            if (profileImageUrl.isNotBlank()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, pink, CircleShape)
                        .background(Color.LightGray),
                    error = painterResource(id = R.drawable.internnepal)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.internnepal),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, pink, CircleShape)
                        .background(Color.LightGray)
                )
            }
            
            // Camera button
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { if (!isUploading) onImageClick() },
                color = pink,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = white,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = white,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(email, color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun ProfileStats(appliedCount: Int, savedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ProfileStat(appliedCount.toString(), "Applied")
        ProfileStat(savedCount.toString(), "Saved")
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = pink)
        Text(label, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun ProfileOptions(
    onEditClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ProfileOptionItem(icon = Icons.Default.Edit, text = "Edit Profile", onClick = onEditClick)
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        ProfileOptionItem(icon = Icons.Default.Lock, text = "Change Password", onClick = onPasswordClick)
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        ProfileOptionItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            text = "Logout",
            onClick = onLogoutClick
        )
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun EditProfileDialog(currentUser: UserModel, onDismiss: () -> Unit, onSave: (UserModel) -> Unit) {
    var name by remember { mutableStateOf(currentUser.fullName) }
    var phone by remember { mutableStateOf(currentUser.phoneNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currentUser.copy(fullName = name, phoneNumber = phone)) },
                colors = ButtonDefaults.buttonColors(containerColor = pink)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = pink) }
        }
    )
}

@Composable
fun UserChangePasswordDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(
                                imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> Toast.makeText(context, "Please enter current password", Toast.LENGTH_SHORT).show()
                        newPassword.isBlank() -> Toast.makeText(context, "Please enter new password", Toast.LENGTH_SHORT).show()
                        newPassword.length < 6 -> Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        newPassword != confirmPassword -> Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        else -> onSave(currentPassword, newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = pink)
            ) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = pink) }
        }
    )
}

