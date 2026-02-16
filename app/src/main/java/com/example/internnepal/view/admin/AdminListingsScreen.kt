package com.example.internnepal.view.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.model.JobModel
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.view.JOB_CATEGORIES
import com.example.internnepal.viewmodel.JobViewModel

@Composable
fun AdminJobsList(jobViewModel: JobViewModel) {
    val jobs by jobViewModel.jobs
    val isLoading by jobViewModel.loading
    var selectedCategory by remember { mutableStateOf("All") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<JobModel?>(null) }

    // No need to fetch jobs here - they're already filtered by admin in AdminMainScreen

    val filterCategories = listOf("All") + JOB_CATEGORIES

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
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
                        selectedLabelColor = white
                    )
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = pink)
            }
        } else {
            val filteredJobs = if (selectedCategory == "All") jobs else jobs.filter { it.category == selectedCategory }
            
            if (filteredJobs.isEmpty()) {
                EmptyState(Icons.Default.FilterList, "No jobs in this category", "Try selecting another category or post a new job.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredJobs) { job ->
                        AdminJobItem(
                            job = job,
                            onEdit = {
                                selectedJob = job
                                showEditDialog = true
                            },
                            onDelete = {
                                jobViewModel.deleteJob(job.jobId) { _, _ ->
                                    jobViewModel.fetchJobsByAdmin(job.adminId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showEditDialog && selectedJob != null) {
        EditJobDialog(
            job = selectedJob!!,
            jobViewModel = jobViewModel,
            onDismiss = {
                showEditDialog = false
                selectedJob = null
            }
        )
    }
}

@Composable
fun AdminJobItem(job: JobModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Debug: Log the imageUrl
                android.util.Log.d("JobCard", "Job: ${job.title}, ImageURL: '${job.imageUrl}'")
                
                AsyncImage(
                    model = if (job.imageUrl.isNotBlank()) job.imageUrl else R.drawable.internnepal,
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F3F4)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.internnepal),
                    onSuccess = { android.util.Log.d("JobCard", "Image loaded successfully for ${job.title}") },
                    onError = { android.util.Log.e("JobCard", "Image load failed for ${job.title}: ${it.result.throwable.message}") }
                )
                
                Spacer(Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        job.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        job.company,
                        fontSize = 15.sp,
                        color = pink,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(Modifier.height(6.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.Gray
                        )
                        Text(
                            job.location,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(0.5f))
            Spacer(Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if(job.salary.isNotBlank()) "NPR ${job.salary}" else "Negotiable",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    if (job.openings.isNotBlank()) {
                        Text(
                            text = "${job.openings} openings",
                            fontSize = 12.sp,
                            color = pink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = pink.copy(0.12f)
                ) {
                    Text(
                        text = job.category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = pink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(0.5f))
            Spacer(Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = pink
                    ),
                    border = BorderStroke(1.dp, pink)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Delete Job Listing?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${job.title}\" at ${job.company}? This action cannot be undone.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
