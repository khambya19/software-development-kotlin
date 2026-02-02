package com.example.internnepal.view.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internnepal.R
import com.example.internnepal.Repository.JobRepoImpl
import com.example.internnepal.Repository.UserRepoImpl
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.purple
import com.example.internnepal.viewmodel.JobViewModel
import com.example.internnepal.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    jobViewModel: JobViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by jobViewModel.jobs
    val currentUser by userViewModel.currentUser
    val isLoading by jobViewModel.loading

    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        jobViewModel.fetchAllJobs()
        userViewModel.fetchCurrentUser()
    }

    val popularCategories = listOf("IT", "Marketing", "Design", "Content Writing", "Management", "Finance")

    // Filter jobs based on search text
    val searchResults = if (searchText.isBlank()) {
        emptyList()
    } else {
        jobs.filter {
            it.title.contains(searchText, ignoreCase = true) ||
            it.company.contains(searchText, ignoreCase = true) ||
            it.location.contains(searchText, ignoreCase = true) ||
            it.category.contains(searchText, ignoreCase = true) ||
            it.description.contains(searchText, ignoreCase = true) ||
            it.salary.contains(searchText, ignoreCase = true)
        }
    }

    // Main Container
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        // Title
        Text(
            text = "Search Jobs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search jobs, companies, or keywords") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = purple,
                unfocusedBorderColor = Color.Gray,
                cursorColor = purple
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Show search results or categories
        if (searchText.isNotBlank()) {
            // Search Results Section
            Text(
                text = "Search Results (${searchResults.size})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = purple)
                }
            } else if (searchResults.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No results found for \"$searchText\"",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Try different keywords",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Display search results
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { job ->
                        JobListItem(job, currentUser, jobViewModel)
                    }
                }
            }
        } else {
            // Popular Categories when no search
            Text(
                text = "Popular Categories",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            popularCategories.forEach { category ->
                Column {
                    Text(
                        text = category,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { searchText = category }
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    InternNepalTheme {
        SearchScreen(
            jobViewModel = JobViewModel(JobRepoImpl()),
            userViewModel = UserViewModel(UserRepoImpl())
        )
    }
}
