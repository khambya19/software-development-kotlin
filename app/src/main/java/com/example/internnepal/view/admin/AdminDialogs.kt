package com.example.internnepal.view.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.internnepal.R
import com.example.internnepal.model.JobModel
import com.example.internnepal.ui.theme.pink
import com.example.internnepal.view.JOB_CATEGORIES
import com.example.internnepal.viewmodel.JobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobDialog(jobViewModel: JobViewModel, adminId: String, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var openings by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(JOB_CATEGORIES[0]) } // Default to first category
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

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
                    .heightIn(max = 650.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Information", fontSize = 12.sp, color = pink, fontWeight = FontWeight.Bold)
                AdminInputField(title, { title = it }, "Job Title", Icons.Default.Work)
                AdminInputField(company, { company = it }, "Company Name", Icons.Default.Business)
                AdminInputField(location, { location = it }, "Location", Icons.Default.LocationOn)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AdminInputField(
                            salary, 
                            { if (it.isEmpty() || it.all { char -> char.isDigit() }) salary = it }, 
                            "Salary", 
                            Icons.Default.Payments, 
                            KeyboardType.Number
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdminInputField(
                            openings, 
                            { if (it.isEmpty() || it.all { char -> char.isDigit() }) openings = it }, 
                            "Openings", 
                            Icons.Default.Group, 
                            KeyboardType.Number
                        )
                    }
                }
                
                // --- DROPDOWN CATEGORY ---
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp), tint = pink) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = pink,
                            unfocusedBorderColor = Color.LightGray.copy(0.5f)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        JOB_CATEGORIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
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
                    when {
                        title.isBlank() -> Toast.makeText(context, "Job Title is required", Toast.LENGTH_SHORT).show()
                        company.isBlank() -> Toast.makeText(context, "Company Name is required", Toast.LENGTH_SHORT).show()
                        location.isBlank() -> Toast.makeText(context, "Location is required", Toast.LENGTH_SHORT).show()
                        salary.isBlank() -> Toast.makeText(context, "Salary is required", Toast.LENGTH_SHORT).show()
                        openings.isBlank() -> Toast.makeText(context, "Number of Openings is required", Toast.LENGTH_SHORT).show()
                        description.isBlank() -> Toast.makeText(context, "Job Description is required", Toast.LENGTH_SHORT).show()
                        selectedImageUri == null -> Toast.makeText(context, "Please select a logo", Toast.LENGTH_SHORT).show()
                        else -> {
                        isUploading = true
                        try {
                            // MVVM: Use ViewModel to upload image
                            jobViewModel.uploadImage(context, selectedImageUri!!) { imageUrl ->
                                if (imageUrl != null && imageUrl.isNotBlank()) {
                                    val job = JobModel(
                                        title = title,
                                        company = company,
                                        location = location,
                                        salary = salary,
                                        openings = openings,
                                        category = category,
                                        imageUrl = imageUrl,
                                        description = description,
                                        adminId = adminId
                                    )
                                    jobViewModel.postJob(job) { success, msg ->
                                        isUploading = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            jobViewModel.fetchJobsByAdmin(adminId)
                                            onDismiss()
                                        }
                                    }
                                } else {
                                    isUploading = false
                                    Toast.makeText(context, "Image upload failed - please try again", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            isUploading = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJobDialog(job: JobModel, jobViewModel: JobViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(job.title) }
    var company by remember { mutableStateOf(job.company) }
    var location by remember { mutableStateOf(job.location) }
    var salary by remember { mutableStateOf(job.salary) }
    var openings by remember { mutableStateOf(job.openings) }
    var category by remember { mutableStateOf(job.category) }
    var description by remember { mutableStateOf(job.description) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            android.util.Log.d("EditJobDialog", "Image selected: $uri")
            Toast.makeText(context, "Image selected! URI: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
        } else {
            android.util.Log.d("EditJobDialog", "No image selected")
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Internship", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 650.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Information", fontSize = 12.sp, color = pink, fontWeight = FontWeight.Bold)
                AdminInputField(title, { title = it }, "Job Title", Icons.Default.Work)
                AdminInputField(company, { company = it }, "Company Name", Icons.Default.Business)
                AdminInputField(location, { location = it }, "Location", Icons.Default.LocationOn)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AdminInputField(
                            salary, 
                            { if (it.isEmpty() || it.all { char -> char.isDigit() }) salary = it }, 
                            "Salary", 
                            Icons.Default.Payments, 
                            KeyboardType.Number
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdminInputField(
                            openings, 
                            { if (it.isEmpty() || it.all { char -> char.isDigit() }) openings = it }, 
                            "Openings", 
                            Icons.Default.Group, 
                            KeyboardType.Number
                        )
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp), tint = pink) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = pink,
                            unfocusedBorderColor = Color.LightGray.copy(0.5f)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        JOB_CATEGORIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
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
                        Text(if (selectedImageUri != null) "Change Logo" else "Update Logo", color = Color.Black)
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    AsyncImage(
                        model = selectedImageUri ?: job.imageUrl.ifBlank { R.drawable.internnepal },
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.internnepal)
                    )
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
                    android.util.Log.d("EditJobDialog", "Update button clicked. selectedImageUri = $selectedImageUri")
                    when {
                        title.isBlank() -> {
                            Toast.makeText(context, "Job Title is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        company.isBlank() -> {
                            Toast.makeText(context, "Company Name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        location.isBlank() -> {
                            Toast.makeText(context, "Location is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        salary.isBlank() -> {
                            Toast.makeText(context, "Salary is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        openings.isBlank() -> {
                            Toast.makeText(context, "Number of Openings is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        description.isBlank() -> {
                            Toast.makeText(context, "Job Description is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }
                    
                    isUploading = true
                    
                    if (selectedImageUri != null) {
                        try {
                            // MVVM: Use ViewModel to upload image
                            jobViewModel.uploadImage(context, selectedImageUri!!) { imageUrl ->
                                if (imageUrl != null && imageUrl.isNotBlank()) {
                                    val updatedJob = job.copy(
                                        title = title,
                                        company = company,
                                        location = location,
                                        salary = salary,
                                        openings = openings,
                                        category = category,
                                        imageUrl = imageUrl,
                                        description = description
                                    )
                                    jobViewModel.updateJob(updatedJob) { success, msg ->
                                        isUploading = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            jobViewModel.fetchJobsByAdmin(job.adminId)
                                            onDismiss()
                                        }
                                    }
                                } else {
                                    isUploading = false
                                    Toast.makeText(context, "Image upload failed - please try again", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            isUploading = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.util.Log.d("EditJobDialog", "No image selected, updating without image change")
                        val updatedJob = job.copy(
                            title = title,
                            company = company,
                            location = location,
                            salary = salary,
                            openings = openings,
                            category = category,
                            description = description
                        )
                        jobViewModel.updateJob(updatedJob) { success, msg ->
                            isUploading = false
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                jobViewModel.fetchJobsByAdmin(job.adminId)
                                onDismiss()
                            }
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
                    Text("Update Listing")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), enabled = !isUploading) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun AdminInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = pink) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = pink,
            unfocusedBorderColor = Color.LightGray.copy(0.5f)
        )
    )
}
