package com.example.internnepal.view.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
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

// Use the global categories constant for consistency
val JOB_CATEGORIES_LIST = listOf("IT", "Marketing", "Design", "Finance", "Management")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    jobViewModel: JobViewModel,
    userViewModel: UserViewModel
) {
    // Proper MVVM: Observe state from ViewModel
    val jobs by jobViewModel.jobs
    val currentUser by userViewModel.currentUser
    val isLoading by jobViewModel.loading
    
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        jobViewModel.fetchAllJobs()
    }

    val filterCategories = listOf("All") + JOB_CATEGORIES_LIST

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Search
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
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

        // Categories (Live Filter)
        item {
            Text(
                "Categories",
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterCategories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = pink,
                            selectedLabelColor = Color.White
                        )
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
        } else {
            // Filter by category
            var filteredJobs = if (selectedCategory == "All") jobs else jobs.filter { it.category == selectedCategory }
            
            // Filter by search query (title, company, location, description, category, salary)
            if (searchQuery.isNotBlank()) {
                filteredJobs = filteredJobs.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.company.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.salary.contains(searchQuery, ignoreCase = true)
                }
            }
            
            if (filteredJobs.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                        Text("No internships found in this category.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredJobs) { job ->
                    JobListItem(job, currentUser, jobViewModel)
                }
            }
        }
        
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun JobListItem(job: JobModel, currentUser: UserModel?, jobViewModel: JobViewModel) {
    val context = LocalContext.current
    var isApplying by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showApplyDialog by remember { mutableStateOf(false) }
    
    val savedJobIds by jobViewModel.savedJobIds
    val isSaved = remember(savedJobIds, job.jobId) {
        savedJobIds.contains(job.jobId)
    }
    
    val appliedJobIds by jobViewModel.appliedJobIds
    val hasApplied = remember(appliedJobIds, job.jobId) {
        appliedJobIds.contains(job.jobId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = white)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bookmark button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        if (currentUser == null) {
                            Toast.makeText(context, "Log in to save jobs", Toast.LENGTH_SHORT).show()
                        } else {
                            if (isSaved) {
                                jobViewModel.unsaveJob(currentUser.userId, job.jobId) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) jobViewModel.fetchMySavedJobs(currentUser.userId)
                                }
                            } else {
                                jobViewModel.saveJob(currentUser.userId, job.jobId) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) jobViewModel.fetchMySavedJobs(currentUser.userId)
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = if (isSaved) pink else Color.Gray
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.Top) {
                // Job Image
                AsyncImage(
                    model = job.imageUrl.ifBlank { R.drawable.internnepal },
                    contentDescription = job.company,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F3F4)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.internnepal)
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text(job.company, fontSize = 14.sp, color = Color.DarkGray)
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(job.location, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = pink.copy(0.1f)
                        ) {
                            Text(
                                text = job.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = pink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("NPR ${job.salary}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = pink)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(0.5f))
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { showDetailsDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = pink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View Details")
                }
                
                Spacer(Modifier.width(12.dp))
                
                Button(
                    onClick = {
                        if (currentUser == null) {
                            Toast.makeText(context, "Log in to apply", Toast.LENGTH_SHORT).show()
                        } else if (!hasApplied) {
                            showApplyDialog = true
                        }
                    },
                    enabled = !isApplying && !hasApplied,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasApplied) Color.Gray else pink,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (hasApplied) "Already Applied" else "Apply Now",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    if (showDetailsDialog) {
        JobDetailsDialog(job = job, onDismiss = { showDetailsDialog = false })
    }
    
    if (showApplyDialog) {
        ApplyConfirmationDialog(
            job = job,
            currentUser = currentUser!!,
            onDismiss = { showApplyDialog = false },
            onConfirm = { message ->
                showApplyDialog = false
                isApplying = true
                val application = ApplicationModel(
                    jobId = job.jobId,
                    jobTitle = job.title,
                    userId = currentUser.userId,
                    fullName = currentUser.fullName,
                    email = currentUser.email,
                    phoneNumber = currentUser.phoneNumber,
                    message = message
                )
                jobViewModel.applyForJob(application) { success, msg ->
                    isApplying = false
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        // Refresh applications list to show in Applied tab
                        jobViewModel.fetchMyApplications(currentUser.userId)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsDialog(job: JobModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Image
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = job.imageUrl.ifBlank { R.drawable.internnepal },
                        contentDescription = job.company,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F3F4)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.internnepal)
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            job.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            job.company,
                            fontSize = 16.sp,
                            color = pink,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                
                // Details Section
                DetailRow(label = "Location", value = job.location)
                DetailRow(label = "Salary", value = if (job.salary.isNotBlank()) "NPR ${job.salary}" else "Negotiable")
                DetailRow(label = "Openings", value = if (job.openings.isNotBlank()) "${job.openings} positions" else "Not specified")
                DetailRow(label = "Category", value = job.category)
                
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                
                // Description
                Text(
                    "Job Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    job.description.ifBlank { "No description provided." },
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
                
                Spacer(Modifier.height(20.dp))
                
                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = pink),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ApplyConfirmationDialog(
    job: JobModel,
    currentUser: UserModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Application",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "You are about to apply for:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(pink.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = job.company,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = job.location,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(0.5f))
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Your Details:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Text("Name: ${currentUser.fullName}", fontSize = 13.sp, color = Color.Black)
                    Text("Email: ${currentUser.email}", fontSize = 13.sp, color = Color.Black)
                    Text("Phone: ${currentUser.phoneNumber}", fontSize = 13.sp, color = Color.Black)
                }
                
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(0.5f))
                
                // Optional Message Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Message (Optional):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Why are you interested in this position?", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = pink,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        maxLines = 4
                    )
                }
                
                Text(
                    text = "⚠️ Make sure your details are correct before applying.",
                    fontSize = 12.sp,
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(message) },
                colors = ButtonDefaults.buttonColors(containerColor = pink),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm & Apply", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = pink),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
