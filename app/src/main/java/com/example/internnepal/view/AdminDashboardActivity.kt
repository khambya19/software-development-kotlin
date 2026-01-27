package com.example.internnepal.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.Repository.AdminRepoImpl
import com.example.internnepal.Repository.JobRepoImpl
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.view.admin.*
import com.example.internnepal.viewmodel.AdminViewModel
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel

// Job categories constant
val JOB_CATEGORIES = listOf("IT", "Marketing", "Design", "Finance", "Management")

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val jobViewModel = JobViewModel(JobRepoImpl())
        val userViewModel = UserViewModel(UserRepoImpl())
        val adminViewModel = AdminViewModel(AdminRepoImpl())
        setContent {
            InternNepalTheme {
                AdminMainScreen(jobViewModel, userViewModel, adminViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(jobViewModel: JobViewModel, userViewModel: UserViewModel, adminViewModel: AdminViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddJobDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    
    val currentAdmin by adminViewModel.currentAdmin
    
    // Fetch current admin on load
    LaunchedEffect(Unit) {
        adminViewModel.fetchCurrentAdmin()
    }
    
    // Fetch jobs by admin when admin is loaded
    LaunchedEffect(currentAdmin) {
        currentAdmin?.let { admin ->
            jobViewModel.fetchJobsByAdmin(admin.adminId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Manage internships and tracking", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = white.copy(alpha = 0.9f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pink,
                    titleContentColor = white
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = currentAdmin?.fullName ?: "Loading...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = white
                            )
                            Text(
                                text = "Admin",
                                fontSize = 11.sp,
                                color = white.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = white.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            if (!currentAdmin?.profileImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentAdmin?.profileImageUrl,
                                    contentDescription = "Admin Profile Picture",
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Admin Profile",
                                    tint = white,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Listings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = pink, 
                        selectedTextColor = pink, 
                        indicatorColor = pink.copy(0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Applications") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = pink, 
                        selectedTextColor = pink, 
                        indicatorColor = pink.copy(0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = pink, 
                        selectedTextColor = pink, 
                        indicatorColor = pink.copy(0.1f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddJobDialog = true },
                    containerColor = pink,
                    contentColor = white,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Post Job") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFFF8F9FA))
        ) {
            when (selectedTab) {
                0 -> AdminJobsList(jobViewModel)
                1 -> ViewApplicationsScreen(jobViewModel)
                2 -> AdminProfileScreen(adminViewModel)
            }
            
            if (showAddJobDialog) {
                currentAdmin?.let { admin ->
                    AddJobDialog(jobViewModel, admin.adminId, onDismiss = { showAddJobDialog = false })
                }
            }
        }
    }
}
