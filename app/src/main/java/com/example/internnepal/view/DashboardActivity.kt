package com.example.internnepal.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.internnepal.ui.theme.black
import com.example.internnepal.ui.theme.grey
import com.example.internnepal.ui.theme.white
import com.example.internnepal.ui.theme.InternNepalTheme

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternNepalTheme {
                DashboardBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody() {

    var selectedIndex by remember { mutableIntStateOf(0) }

    data class NavItem(val label: String, val icon: ImageVector)

    val navItems = listOf(
        NavItem("Home", Icons.Outlined.Home),
        NavItem("Search", Icons.Outlined.Search),
        NavItem("Saved", Icons.Outlined.WorkOutline), // RENAMED FROM LIBRARY
        NavItem("Profile", Icons.Outlined.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intern Nepal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = grey,
                    titleContentColor = white
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = grey
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = white,
                            selectedTextColor = white,
                            unselectedIconColor = white.copy(alpha = 0.6f),
                            unselectedTextColor = white.copy(alpha = 0.6f),
                            indicatorColor = black.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedIndex) {
                0 -> Home()
                1 -> SearchScreen()
                2 -> LibraryScreen() // This is now your "Saved Jobs" screen
                3 -> ProfileScreen()
                else -> Home()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    InternNepalTheme {
        DashboardBody()
    }
}
