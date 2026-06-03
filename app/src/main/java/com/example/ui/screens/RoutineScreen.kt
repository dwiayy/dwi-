package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MainHeader
import com.example.ui.theme.*
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routineList.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var routineTitle by remember { mutableStateOf("") }
    var routineTime by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NeutralLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = RoyalNavy,
                contentColor = NeutralWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 80.dp) // Offset above the bottom tab navigation
                    .testTag("add_routine_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Rutinitas",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MainHeader(
                title = "Rutinitas Harian",
                subtitle = "Kontrol jadwal dan bangun kebiasaan produktif.",
                onLogoutClick = { viewModel.logout() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub header and counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terjadwal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextNavyDark
                )

                val completed = routines.count { it.isCompleted }
                val total = routines.size
                Text(
                    text = "$completed/$total Selesai",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PriorityLow,
                    modifier = Modifier
                        .background(PriorityLow.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("routine_completed_badge")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Routine Lists
            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = TextMutedGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Rutinitas",
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
                    items(routines, key = { it.id }) { routine ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                                .testTag("routine_card_${routine.id}"),
                            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Completion Icon Switch
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleRoutineCompletion(routine.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (routine.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = "Tandai Selesai",
                                        tint = if (routine.isCompleted) PriorityLow else OrangeAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = routine.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (routine.isCompleted) TextMutedGray else TextNavyDark,
                                        textDecoration = if (routine.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = RoyalNavy,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Pukul " + routine.time,
                                            fontSize = 12.sp,
                                            color = RoyalNavy,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                IconButton(
                                    onClick = { viewModel.deleteRoutine(routine.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Hapus",
                                        tint = PriorityHigh,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Routine Dialog Pop-up
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Rutinitas Baru", fontWeight = FontWeight.Bold, color = TextNavyDark) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = routineTitle,
                            onValueChange = { routineTitle = it },
                            label = { Text("Nama Rutinitas (misal: Olahraga)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_routine_title_input")
                        )

                        OutlinedTextField(
                            value = routineTime,
                            onValueChange = { routineTime = it },
                            label = { Text("Waktu Pelaksanaan (misal: 07:00)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_routine_time_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (routineTitle.isBlank() || routineTime.isBlank()) {
                                Toast.makeText(context, "Harap lengkapi semua isian!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addRoutine(routineTitle, routineTime)
                                Toast.makeText(context, "Rutinitas berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                                routineTitle = ""
                                routineTime = ""
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier.testTag("dialog_routine_confirm_btn")
                    ) {
                        Text("Tambah", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Batal", color = TextMutedGray)
                    }
                }
            )
        }
    }
}
