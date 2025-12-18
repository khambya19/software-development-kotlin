package com.example.internnepal.view

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internnepal.R
import com.example.internnepal.model.UserModel
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.viewmodel.UserViewModel

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userViewModel = UserViewModel(UserRepoImpl())
    var user by remember { mutableStateOf<UserModel?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.getCurrentUser { fetchedUser ->
            user = fetchedUser
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
                email = user?.email ?: "Loading..."
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileStats()
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileOptions(
                onEditClick = { showEditDialog = true },
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
                    // Logic to save to Firebase would go here
                    user = updatedUser
                    showEditDialog = false
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(fullName: String, email: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.internnepal),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Text(fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(email, color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun ProfileStats() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ProfileStat("12", "Applied")
        ProfileStat("5", "Saved")
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
fun ProfileOptions(onEditClick: () -> Unit, onLogoutClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ProfileOptionItem(icon = Icons.Default.Edit, text = "Edit Profile", onClick = onEditClick)
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        ProfileOptionItem(icon = Icons.Default.Settings, text = "Settings")
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
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    InternNepalTheme {
        ProfileScreen()
    }
}
