package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MainHeader
import com.example.ui.theme.*
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.notifications.collectAsState()

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
                title = "Notifikasi",
                subtitle = "Pusat aktivitas dan pengingat jadwal tugas harian.",
                onLogoutClick = { viewModel.logout() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Pengingat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextNavyDark
                )

                Text(
                    text = "${alerts.size} Baru",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier
                        .background(OrangeSoft, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("notification_count_badge")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Log rendering
            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = TextMutedGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Pengingat",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextNavyDark
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(alerts, key = { it.id }) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .shadow(1.dp, shape = RoundedCornerShape(12.dp))
                                .testTag("notification_card_${notif.id}"),
                            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (notif.message.contains("PENTING")) PriorityHigh.copy(alpha = 0.12f)
                                            else OrangeAccent.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (notif.message.contains("PENTING")) Icons.Default.NotificationsActive else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (notif.message.contains("PENTING")) PriorityHigh else OrangeAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = notif.message,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextNavyDark,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = notif.timestamp,
                                        fontSize = 11.sp,
                                        color = TextMutedGray,
                                        fontWeight = FontWeight.SemiBold
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
