package com.example.internnepal.view.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppliedScreen(
    jobViewModel: JobViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val applications by jobViewModel.applications
    val allJobs by jobViewModel.jobs
    val currentUser by userViewModel.currentUser
    val isLoading by jobViewModel.loading

    LaunchedEffect(Unit) {
        jobViewModel.fetchAllJobs()
    }
    
    // Fetch user's applications when currentUser is loaded
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            jobViewModel.fetchMyApplications(user.userId)
        }
    }
    
    // Match applications with their job details
    val appliedJobsWithDetails = remember(applications, allJobs) {
        applications.mapNotNull { app ->
            val job = allJobs.find { it.jobId == app.jobId }
            job?.let { Pair(job, app) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "My Applications",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${appliedJobsWithDetails.size} Applications",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Applications List
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = pink)
            }
        } else if (appliedJobsWithDetails.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No Applications Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start applying to jobs to see them here",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appliedJobsWithDetails) { (job, application) ->
                    ApplicationJobCard(
                        job = job,
                        application = application,
                        jobViewModel = jobViewModel,
                        userViewModel = userViewModel
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ApplicationJobCard(
    job: JobModel,
    application: ApplicationModel,
    jobViewModel: JobViewModel,
    userViewModel: UserViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(application.appliedDate))
    
    val statusColor = when(application.status.lowercase()) {
        "accepted" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800) // Pending
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = white)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            // Applied Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Applied on $formattedDate",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge and Withdraw Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Status:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = application.status,
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // View Status button (show for Accepted or Rejected)
                    if (application.status.lowercase() != "pending") {
                        OutlinedButton(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = statusColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "View Status",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Withdraw button (only show if status is Pending)
                    if (application.status.lowercase() == "pending") {
                        OutlinedButton(
                            onClick = { showWithdrawDialog = true },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Withdraw",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Withdraw Confirmation Dialog
    if (showWithdrawDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Withdraw Application?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to withdraw your application for ${job.title} at ${job.company}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentUser = userViewModel.currentUser.value
                        currentUser?.let { user ->
                            jobViewModel.withdrawApplication(application.applicationId, user.userId) { success, message ->
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Withdraw")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showStatusDialog) {
        StatusDetailsDialog(
            application = application,
            statusColor = statusColor,
            onDismiss = { showStatusDialog = false }
        )
    }
}

@Composable
fun StatusDetailsDialog(
    application: ApplicationModel,
    statusColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Application Status",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (application.status.lowercase()) {
                                "accepted" -> Icons.Default.CheckCircle
                                "rejected" -> Icons.Default.Cancel
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status: ${application.status}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Admin Message Section
                Text(
                    text = "Admin Message:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = application.adminMessage.ifBlank { "No message provided" },
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = statusColor
                )
            ) {
                Text("Close")
            }
        }
    )
}
