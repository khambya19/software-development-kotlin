package com.example.internnepal.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.internnepal.ui.theme.InternNepalTheme
import com.example.internnepal.ui.theme.purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {

    var searchText by remember { mutableStateOf("") }

    val recentSearches = listOf("Software Engineer", "Marketing Intern", "Graphic Designer")
    val popularCategories = listOf("IT", "Marketing", "Design", "Content Writing", "Management", "Finance")

    // Main Container
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
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

        // --- 1. RECENT SEARCHES ---
        Text(
            text = "Recent searches",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow {
            items(recentSearches.size) { index ->
                SearchChip(text = recentSearches[index])
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. POPULAR CATEGORIES ---
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

@Composable
fun SearchChip(text: String) {
    Card(
        modifier = Modifier
            .padding(end = 10.dp)
            .clickable { },
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontWeight = FontWeight.Medium,
            color = Color(0xFFDC075E)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    InternNepalTheme {
        SearchScreen()
    }
}
