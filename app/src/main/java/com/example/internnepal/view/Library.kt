package com.example.internnepal.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internnepal.R
import com.example.internnepal.ui.theme.InternNepalTheme

data class SavedJob(
    val title: String,
    val company: String,
    val location: String,
    val imageRes: Int
)

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {

    val savedJobs = listOf(
        SavedJob("Software Engineer Intern", "Google", "Kathmandu, Nepal", R.drawable.come),
        SavedJob("Marketing Intern", "Facebook", "Lalitpur, Nepal", R.drawable.envy),
        SavedJob("Graphic Design Intern", "Canva", "Bhaktapur, Nepal", R.drawable.the)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8F5FF), Color.White)
                )
            )
            .padding(16.dp)
    ) {
        Text("Saved Jobs", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(savedJobs) { job ->
                SavedJobCard(job = job)
            }
        }
    }
}

@Composable
fun SavedJobCard(job: SavedJob) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = job.imageRes),
                contentDescription = job.company,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(job.company, color = Color.Gray, fontSize = 14.sp)
                Text(job.location, color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = { /* Handle delete */ }) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Job", tint = Color.Gray)
            }
            IconButton(onClick = { /* Handle details */ }) {
                Icon(Icons.Default.Info, contentDescription = "Job Details", tint = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryPreview() {
    InternNepalTheme {
        LibraryScreen()
    }
}
