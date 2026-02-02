package com.example.internnepal.view.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.Repository.JobRepoImpl
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel

class UserDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternNepalTheme {
                UserDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen() {
    // MVVM: Create ViewModels - single source of truth
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    
    val currentUser by userViewModel.currentUser
    
    // MVVM: Fetch data through ViewModels
    LaunchedEffect(Unit) {
        userViewModel.fetchCurrentUser()
        jobViewModel.fetchAllJobs()
    }
    
    // MVVM: Fetch user-specific data when user is loaded
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            jobViewModel.fetchMyApplications(user.userId)
            jobViewModel.fetchMySavedJobs(user.userId)
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    data class NavItem(val label: String, val icon: ImageVector)

    val navItems = listOf(
        NavItem("Home", Icons.Outlined.Home),
        NavItem("Applied", Icons.Outlined.CheckCircle),
        NavItem("Saved", Icons.Outlined.WorkOutline),
        NavItem("Profile", Icons.Outlined.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "User Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Explore internship opportunities",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = white.copy(alpha = 0.9f)
                        )
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
                                text = currentUser?.fullName ?: "Loading...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = white
                            )
                            Text(
                                text = "Student",
                                fontSize = 11.sp,
                                color = white.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = white.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            if (!currentUser?.profileImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentUser?.profileImageUrl,
                                    contentDescription = "User Profile Picture",
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "User Profile",
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
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = pink,
                            selectedTextColor = pink,
                            indicatorColor = pink.copy(0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // MVVM: Pass ViewModels to View layer
            when (selectedIndex) {
                0 -> HomeScreen(jobViewModel, userViewModel)
                1 -> AppliedScreen(jobViewModel, userViewModel)
                2 -> SavedScreen(jobViewModel, userViewModel)
                3 -> ProfileScreen(userViewModel, jobViewModel)
                else -> HomeScreen(jobViewModel, userViewModel)
            }
        }
    }
}
