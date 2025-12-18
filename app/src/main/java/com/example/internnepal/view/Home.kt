package com.example.internnepal.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internnepal.Repository.JobRepoImpl
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.model.UserModel
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val jobViewModel = JobViewModel(JobRepoImpl())
    val userViewModel = UserViewModel(UserRepoImpl())
    val context = LocalContext.current
    
    var jobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var currentUser by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        jobViewModel.getAllJobs { fetchedJobs ->
            jobs = fetchedJobs
            isLoading = false
        }
        userViewModel.getCurrentUser { fetchedUser ->
            currentUser = fetchedUser
        }
    }

    val categories = listOf("All", "IT", "Marketing", "Design", "Management", "Finance")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Search
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Search internships...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = pink
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Categories
        item {
            Text(
                "Categories",
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = category == "All",
                        onClick = { },
                        label = { Text(category) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Job Listings
        item {
            Text(
                "Latest Opportunities",
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = pink)
                }
            }
        } else if (jobs.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                    Text("No internships available at the moment.", color = Color.Gray)
                }
            }
        } else {
            items(jobs) { job ->
                JobListItem(job, currentUser, jobViewModel)
            }
        }
        
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun JobListItem(job: JobModel, currentUser: UserModel?, jobViewModel: JobViewModel) {
    val context = LocalContext.current
    var isApplying by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = white)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Work,
                    contentDescription = null,
                    tint = pink,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text(job.company, fontSize = 14.sp, color = Color.DarkGray)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(job.location, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                Spacer(Modifier.width(16.dp))
                Text("NPR ${job.salary}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = pink)
            }
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = {
                    if (currentUser == null) {
                        Toast.makeText(context, "Please complete your profile to apply", Toast.LENGTH_SHORT).show()
                    } else {
                        isApplying = true
                        val application = ApplicationModel(
                            jobId = job.jobId,
                            jobTitle = job.title,
                            userId = currentUser.userId,
                            fullName = currentUser.fullName,
                            email = currentUser.email,
                            phoneNumber = currentUser.phoneNumber
                        )
                        jobViewModel.applyForJob(application) { success, msg ->
                            isApplying = false
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isApplying,
                colors = ButtonDefaults.buttonColors(containerColor = pink),
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isApplying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Apply Now", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    InternNepalTheme {
        Home()
    }
}
