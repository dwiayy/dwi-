package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Priority
import com.example.model.Task
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.taskList.collectAsState()
    val selectedTaskId by viewModel.selectedTaskId.collectAsState()

    // Find the current active task
    val task = remember(selectedTaskId, tasks) {
        tasks.find { it.id == selectedTaskId }
    }

    // Timer states synced with ViewModel
    val timeLeftSeconds by viewModel.timeLeftSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    if (task == null) {
        // Fallback or back navigation
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Tugas") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.MAIN) }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tugas tidak ditemukan.")
            }
        }
        return
    }

    // Helper to format time remaining (e.g. 1500 sec -> "25:00")
    val formattedTime = remember(timeLeftSeconds) {
        val minutes = timeLeftSeconds / 60
        val seconds = timeLeftSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    // Animation transition for pulse effect in Pomodoro Circular focus circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_time")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    val progressPercent = task.progressPercent

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mode Fokus RemindFlow", fontWeight = FontWeight.Bold, color = NeutralWhite) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.MAIN) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = NeutralWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy)
            )
        },
        containerColor = NeutralLight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task context banner card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (prioText, prioColor) = when (task.priority) {
                            Priority.TINGGI -> "Prioritas Tinggi" to PriorityHigh
                            Priority.SEDANG -> "Prioritas Sedang" to PriorityMedium
                            Priority.RENDAH -> "Prioritas Rendah" to PriorityLow
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(prioColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = prioText,
                                color = prioColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Deadline: ${task.deadline}",
                            color = OrangeAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = task.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextNavyDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = TextMutedGray,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // POMODORO FOCUS TIMER SECTION (Interactive!)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, shape = RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DeepNavy)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POMODORO TIMER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular clock timer face
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(65.dp))
                            .background(RoyalNavy)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formattedTime,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralWhite,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("pomodoro_countdown_text")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTimerRunning) "FOKUS" else "RELEKS",
                                fontSize = 10.sp,
                                color = if (isTimerRunning) OrangeAccent else TextLightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer Controls Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isTimerRunning) {
                            Button(
                                onClick = { viewModel.startTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_start_timer")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Mulai")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mulai")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = PriorityMedium),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_pause_timer")
                            ) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Jeda")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Jeda")
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetTimer() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeutralWhite),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(NeutralWhite)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_reset_timer")
                        ) {
                            Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PROGRESS CHECKLIST SECTION
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Sub-Tugas & Perkembangan",
                        fontWeight = FontWeight.Bold,
                        color = TextNavyDark,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Progress Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PriorityLow,
                            trackColor = TextLightGray
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$progressPercent%",
                            fontWeight = FontWeight.ExtraBold,
                            color = PriorityLow,
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("progress_percentage_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // checklist items
                    if (task.subTasks.isEmpty()) {
                        Text(
                            text = "Tidak ada sub-tugas dalam item ini.",
                            fontSize = 13.sp,
                            color = TextMutedGray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        task.subTasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleSubTask(task.id, sub.id) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (sub.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (sub.isCompleted) PriorityLow else TextMutedGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = sub.title,
                                    fontSize = 14.sp,
                                    color = if (sub.isCompleted) TextMutedGray else TextNavyDark,
                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main "Tandai Selesai" button
            Button(
                onClick = {
                    viewModel.toggleTaskCompletion(task.id)
                    Toast.makeText(context, "Hebat, pekerjaan selesai!", Toast.LENGTH_SHORT).show()
                    viewModel.navigateTo(AppScreen.MAIN)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 10.dp)
                    .testTag("sub_complete_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.isCompleted) TextMutedGray else OrangeAccent,
                    contentColor = NeutralWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.Undo else Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (task.isCompleted) "Tandai Belum Selesai" else "Tandai Selesai",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
