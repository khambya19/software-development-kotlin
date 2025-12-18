package com.example.internnepal.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.Repository.JobRepoImpl
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.model.ApplicationModel
import com.example.internnepal.model.JobModel
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.ui.theme.white
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val jobViewModel = JobViewModel(JobRepoImpl())
        val userViewModel = UserViewModel(UserRepoImpl())
        setContent {
            InternNepalTheme {
                AdminMainScreen(jobViewModel, userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(jobViewModel: JobViewModel, userViewModel: UserViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddJobDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val tabs = listOf("Jobs", "Applicants")

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.Bold)
                        Text("Manage internships and tracking", fontSize = 12.sp, fontWeight = FontWeight.Normal)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = pink,
                    titleContentColor = white,
                    scrolledContainerColor = pink
                ),
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = white)
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
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = pink, selectedTextColor = pink, indicatorColor = pink.copy(0.1f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Applications") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = pink, selectedTextColor = pink, indicatorColor = pink.copy(0.1f))
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
            }
            
            if (showAddJobDialog) {
                AddJobDialog(jobViewModel, onDismiss = { showAddJobDialog = false })
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Logout", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to logout from Intern Nepal?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                userViewModel.logout { _, _ ->
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    activity?.finish()
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
        }
    }
}

@Composable
fun AdminJobsList(jobViewModel: JobViewModel) {
    var jobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        jobViewModel.getAllJobs { fetchedJobs ->
            jobs = fetchedJobs
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = pink)
        }
    } else if (jobs.isEmpty()) {
        EmptyState(Icons.Default.WorkOutline, "No jobs posted yet", "Start by creating your first internship opportunity.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(jobs) { job ->
                AdminJobItem(job, onDelete = {
                    jobViewModel.deleteJob(job.jobId) { _, _ ->
                        jobViewModel.getAllJobs { jobs = it }
                    }
                })
            }
        }
    }
}

@Composable
fun AdminJobItem(job: JobModel, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = job.imageUrl.ifBlank { R.drawable.internnepal },
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F3F4)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.internnepal)
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(job.company, fontSize = 14.sp, color = pink, fontWeight = FontWeight.Medium)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Text(job.location, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if(job.salary.isNotBlank()) "NPR ${job.salary}" else "Negotiable",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    if (job.openings.isNotBlank()) {
                        Text(text = "${job.openings} Openings", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                
                Text(
                    text = job.category,
                    modifier = Modifier
                        .background(pink.copy(0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = pink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ViewApplicationsScreen(jobViewModel: JobViewModel) {
    var applications by remember { mutableStateOf<List<ApplicationModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        jobViewModel.getAllApplications { 
            applications = it 
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = pink)
        }
    } else if (applications.isEmpty()) {
        EmptyState(Icons.Default.PeopleOutline, "No applications yet", "All job applications from students will appear here.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(applications) { app ->
                ApplicationItem(app)
            }
        }
    }
}

@Composable
fun ApplicationItem(app: ApplicationModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(pink),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.fullName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(app.email, fontSize = 13.sp, color = Color.Gray)
                }
                
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        app.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(
                    text = "Applied for: ${app.jobTitle}",
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(
                    text = app.phoneNumber,
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
            
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(app.appliedDate))
            Text(
                text = "Submitted on $date",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun AddJobDialog(jobViewModel: JobViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var openings by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Internship", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Information", fontSize = 12.sp, color = pink, fontWeight = FontWeight.Bold)
                AdminInputField(title, { title = it }, "Job Title", Icons.Default.Work)
                AdminInputField(company, { company = it }, "Company Name", Icons.Default.Business)
                AdminInputField(location, { location = it }, "Location", Icons.Default.LocationOn)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AdminInputField(salary, { salary = it }, "Salary", Icons.Default.Payments)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdminInputField(openings, { openings = it }, "Openings", Icons.Default.Group)
                    }
                }
                
                AdminInputField(category, { category = it }, "Category", Icons.Default.Category)
                
                Text("Media & Description", fontSize = 12.sp, color = pink, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Select Logo", color = Color.Black)
                    }
                    
                    if (selectedImageUri != null) {
                        Spacer(Modifier.width(12.dp))
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = pink)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || company.isBlank()) {
                        Toast.makeText(context, "Title and Company are required", Toast.LENGTH_SHORT).show()
                    } else if (selectedImageUri == null) {
                        Toast.makeText(context, "Please select a logo", Toast.LENGTH_SHORT).show()
                    } else {
                        isUploading = true
                        val fileName = UUID.randomUUID().toString()
                        val ref = storage.reference.child("job_logos/$fileName")
                        
                        ref.putFile(selectedImageUri!!)
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener { url ->
                                    val job = JobModel(
                                        title = title,
                                        company = company,
                                        location = location,
                                        salary = salary,
                                        openings = openings,
                                        category = category,
                                        imageUrl = url.toString(),
                                        description = description
                                    )
                                    jobViewModel.postJob(job) { _, msg ->
                                        isUploading = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }
                            }
                            .addOnFailureListener {
                                isUploading = false
                                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = pink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Publish Listing")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), enabled = !isUploading) {
                Text("Discard", color = Color.Gray)
            }
        }
    )
}

@Composable
fun AdminInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = pink) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = pink,
            unfocusedBorderColor = Color.LightGray.copy(0.5f)
        )
    )
}

@Composable
fun EmptyState(icon: ImageVector, title: String, desc: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
        Text(desc, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
    }
}

fun Modifier.size(size: androidx.compose.ui.unit.Dp) = this.then(Modifier.width(size).height(size))
