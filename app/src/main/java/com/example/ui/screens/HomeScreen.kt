package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Priority
import com.example.ui.components.MainHeader
import com.example.ui.components.TaskCard
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.taskList.collectAsState()
    val userEmail by viewModel.currentUserEmail.collectAsState()

    // Calculated fields based on reactive state
    val totalTasks = tasks.size
    val highPriorityTasks = tasks.count { it.priority == Priority.TINGGI && !it.isCompleted }
    val completedTasks = tasks.count { it.isCompleted }

    // Grab user prefix for a clean persona touch
    val username = userEmail.substringBefore("@")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.ADD_TASK) },
                containerColor = OrangeAccent,
                contentColor = NeutralWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 80.dp) // Offset above the bottom navigation bar safely
                    .testTag("floating_add_task_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Tugas Baru",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = NeutralLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Main Branding Header
            MainHeader(
                title = "Halo, $username!",
                subtitle = "Temukan fokus dan selesaikan tugas harianmu.",
                onLogoutClick = { viewModel.logout() }
            )

            // Summary Panels in horizontal grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    title = "Total Tugas",
                    count = totalTasks,
                    accentColor = RoyalNavy,
                    icon = Icons.Default.Assignment,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Prioritas Tinggi",
                    count = highPriorityTasks,
                    accentColor = PriorityHigh,
                    icon = Icons.Default.Error,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Selesai",
                    count = completedTasks,
                    accentColor = PriorityLow,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Task List Segment Header
            Text(
                text = "Daftar Tugas Harian",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Tasks presentation
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = TextMutedGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Tugas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextNavyDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ketuk tombol + untuk menambahkan tugas baru.",
                            fontSize = 13.sp,
                            color = TextMutedGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onCompleteToggle = { viewModel.toggleTaskCompletion(task.id) },
                            onClick = { viewModel.navigateTo(AppScreen.TASK_DETAIL, task.id) },
                            onEditClick = { viewModel.navigateTo(AppScreen.EDIT_TASK, task.id) },
                            onDeleteClick = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = count.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextNavyDark
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextMutedGray,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
