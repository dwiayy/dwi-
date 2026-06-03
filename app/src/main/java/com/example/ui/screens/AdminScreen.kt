package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.MainHeader
import com.example.ui.theme.*
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.taskList.collectAsState()
    val systemScanOngoing by viewModel.systemScanOngoing.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()

    val totalActiveTasks = tasks.count { !it.isCompleted }
    val totalCompletedTasks = tasks.count { it.isCompleted }
    val simulatedUsers = viewModel.dummyUsers

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NeutralLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MainHeader(
                title = "Dasbor Admin",
                subtitle = "Pemantauan server, integritas sistem & metrik pengguna.",
                onLogoutClick = { viewModel.logout() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time admin statistics grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Pengguna",
                    count = simulatedUsers.size,
                    accentColor = RoyalNavy,
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )

                AdminStatCard(
                    title = "Tugas Aktif",
                    count = totalActiveTasks,
                    accentColor = OrangeAccent,
                    icon = Icons.Default.Pending,
                    modifier = Modifier.weight(1f)
                )

                AdminStatCard(
                    title = "Tugas Selesai",
                    count = totalCompletedTasks,
                    accentColor = PriorityLow,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive health monitoring scan button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Status Operasional",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedGray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = systemStatus,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (systemScanOngoing) OrangeAccent else PriorityLow,
                            modifier = Modifier.testTag("system_status_text")
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerSystemScan() },
                        enabled = !systemScanOngoing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalNavy,
                            disabledContainerColor = TextLightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("scan_system_btn")
                    ) {
                        if (systemScanOngoing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = TextNavyDark
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Diagnostik", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registrasi Pengguna Sistem",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Users tables list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(simulatedUsers, key = { it.id }) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .shadow(1.dp, shape = RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(RoyalNavy.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = RoyalNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = user.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextNavyDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = user.email,
                                        fontSize = 12.sp,
                                        color = TextMutedGray
                                    )
                                }
                            }

                            // Right alignment role tag
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = user.role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (user.status == "Aktif") SuccessBg else TextLightGray
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = user.status,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.status == "Aktif") SuccessText else TextMutedGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    count: Int,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(1.dp, shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = count.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextNavyDark
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextMutedGray,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
