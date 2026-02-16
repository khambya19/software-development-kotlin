package com.example.internnepal.view.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ViewApplicationsScreen(jobViewModel: JobViewModel) {
    val jobs by jobViewModel.jobs
    val applications by jobViewModel.applications
    val isLoading by jobViewModel.loading
    var selectedJob by remember { mutableStateOf<JobModel?>(null) }

    // Fetch applications only (jobs are already filtered by admin in AdminMainScreen)
    LaunchedEffect(Unit) {
        jobViewModel.fetchAllApplications()
    }
    
    // Filter applications to only show those for THIS admin's jobs
    val adminJobIds = remember(jobs) {
        jobs.map { it.jobId }.toSet()
    }
    
    val filteredApplications = remember(applications, adminJobIds) {
        applications.filter { it.jobId in adminJobIds }
    }
    
    // Calculate application counts per job (using filtered applications)
    val jobApplicationCounts = remember(jobs, filteredApplications) {
        jobs.map { job ->
            job to filteredApplications.count { it.jobId == job.jobId }
        }
    }

    if (selectedJob != null) {
        ApplicantsForJobScreen(
            job = selectedJob!!,
            jobViewModel = jobViewModel,
            onBack = { selectedJob = null }
        )
    } else {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = pink)
            }
        } else if (jobs.isEmpty()) {
            EmptyState(Icons.Default.WorkOff, "No jobs posted yet", "Post some jobs to see applications")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Job Listings with Applications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(jobApplicationCounts) { (job, count) ->
                    JobApplicationCard(
                        job = job,
                        applicantCount = count,
                        onClick = { selectedJob = job }
                    )
                }
            }
        }
    }
}

@Composable
fun JobApplicationCard(job: JobModel, applicantCount: Int, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Job Image
            AsyncImage(
                model = job.imageUrl.ifBlank { R.drawable.internnepal },
                contentDescription = job.company,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F3F4)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.internnepal)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = job.company,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = pink
                    )
                    Text(
                        text = "$applicantCount Applicant${if (applicantCount != 1) "s" else ""}",
                        modifier = Modifier.padding(start = 4.dp),
                        fontSize = 13.sp,
                        color = pink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "View Applicants",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun ApplicantsForJobScreen(job: JobModel, jobViewModel: JobViewModel, onBack: () -> Unit) {
    val allApplications by jobViewModel.applications
    val context = LocalContext.current
    
    // Filter applications for this specific job only
    val jobApplications = remember(allApplications, job.jobId) {
        allApplications.filter { it.jobId == job.jobId }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button
        Surface(
            color = pink,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = white
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        color = white,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${jobApplications.size} Applicant${if (jobApplications.size != 1) "s" else ""}",
                        color = white.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        if (jobApplications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PeopleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No applications yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobApplications) { application ->
                    ApplicantCard(
                        application = application,
                        jobViewModel = jobViewModel,
                        onStatusUpdated = {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicantCard(
    application: ApplicationModel,
    jobViewModel: JobViewModel,
    onStatusUpdated: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }
    val statusColor = when(application.status.lowercase()) {
        "accepted" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFE53935)
        else -> Color(0xFFFF9800)
    }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(pink),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = application.fullName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(application.appliedDate))
                    Text(
                        text = "Applied on $date",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = application.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp
            )
            
            // Contact Information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Text(
                    text = application.email,
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 14.sp
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Text(
                    text = application.phoneNumber,
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 14.sp
                )
            }
            
            // Message
            if (application.message.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Message:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = application.message,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
            }
            
            // Action Buttons (only show if status is Pending)
            if (application.status.lowercase() == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showConfirmDialog = "Rejected" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE53935))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Reject")
                    }
                    
                    Button(
                        onClick = { showConfirmDialog = "Accepted" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog != null) {
        var adminMessage by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            icon = {
                Icon(
                    if (showConfirmDialog == "Accepted") Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (showConfirmDialog == "Accepted") Color(0xFF4CAF50) else Color(0xFFE53935),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "$showConfirmDialog Application?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Are you sure you want to ${showConfirmDialog?.lowercase()} ${application.fullName}'s application?"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Message to applicant: *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    
                    OutlinedTextField(
                        value = adminMessage,
                        onValueChange = { adminMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = { 
                            Text(
                                if (showConfirmDialog == "Accepted") 
                                    "e.g., Congratulations! Please check your email for further details." 
                                else 
                                    "e.g., Thank you for applying. We found other candidates more suitable.",
                                fontSize = 12.sp
                            ) 
                        },
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (showConfirmDialog == "Accepted") Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    )
                    
                    if (adminMessage.isBlank()) {
                        Text(
                            "* Message is required",
                            fontSize = 12.sp,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminMessage.isNotBlank()) {
                            val status = showConfirmDialog!!
                            jobViewModel.updateApplicationStatus(application.applicationId, status, adminMessage.trim()) { success, msg ->
                                if (success) {
                                    onStatusUpdated("Application $status")
                                    // Refresh applications
                                    jobViewModel.fetchAllApplications()
                                } else {
                                    onStatusUpdated(msg)
                                }
                            }
                            showConfirmDialog = null
                        } else {
                            // Show toast that message is required
                            Toast.makeText(context, "Please provide a message to the applicant", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showConfirmDialog == "Accepted") Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                ) {
                    Text(showConfirmDialog!!)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
