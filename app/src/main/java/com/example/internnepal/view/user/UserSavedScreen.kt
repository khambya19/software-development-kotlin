package com.example.internnepal.view.user

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.model.JobModel
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel

@Composable
fun SavedScreen(
    jobViewModel: JobViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val allJobs by jobViewModel.jobs
    val savedJobIds by jobViewModel.savedJobIds
    val currentUser by userViewModel.currentUser
    val isLoading by jobViewModel.loading
    
    LaunchedEffect(Unit) {
        jobViewModel.fetchAllJobs()
    }
    
    val savedJobs = remember(allJobs, savedJobIds) {
        allJobs.filter { job -> savedJobIds.contains(job.jobId) }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Saved Jobs", 
                fontSize = 24.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${savedJobs.size} Saved Jobs",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Saved Jobs List
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = pink)
            }
        } else if (savedJobs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No Saved Jobs Yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Browse internships and tap the bookmark icon to save your favorites.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp)
            ) {
                items(savedJobs) { job ->
                    SavedJobCard(job, currentUser, jobViewModel)
                }
            }
        }
    }
}

@Composable
fun SavedJobCard(job: JobModel, currentUser: com.example.internnepal.model.UserModel?, jobViewModel: JobViewModel) {
    val context = LocalContext.current
    var showUnsaveDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }
    
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
            // Unsave button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        if (currentUser == null) {
                            Toast.makeText(context, "Log in to manage saved jobs", Toast.LENGTH_SHORT).show()
                        } else {
                            showUnsaveDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Unsave",
                        tint = pink
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
    
    if (showUnsaveDialog) {
        UnsaveConfirmationDialog(
            job = job,
            onDismiss = { showUnsaveDialog = false },
            onConfirm = {
                showUnsaveDialog = false
                currentUser?.let { user
 ->
                    jobViewModel.unsaveJob(user.userId, job.jobId) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) jobViewModel.fetchMySavedJobs(user.userId)
                    }
                }
            }
        )
    }
    
    if (showApplyDialog) {
        ApplyConfirmationDialog(
            job = job,
            currentUser = currentUser!!,
            onDismiss = { showApplyDialog = false },
            onConfirm = { message ->
                showApplyDialog = false
                isApplying = true
                val application = com.example.internnepal.model.ApplicationModel(
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
                    // No need to fetch again as ViewModel already updated the state
                }
            }
        )
    }
}

@Composable
fun UnsaveConfirmationDialog(
    job: JobModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove from Saved?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Are you sure you want to remove this job from your saved list?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
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
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Yes, Remove", color = Color.White)
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

