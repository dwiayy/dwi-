package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Priority
import com.example.model.Task
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: RemindViewModel,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.taskList.collectAsState()
    val selectedTaskId by viewModel.selectedTaskId.collectAsState()

    // Find task if edit mode
    val existingTask = remember(selectedTaskId, tasks) {
        if (isEditMode) tasks.find { it.id == selectedTaskId } else null
    }

    // Form states
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.SEDANG) }
    var deadline by remember { mutableStateOf("") }
    var enableNotification by remember { mutableStateOf(true) }

    // Initialize state if in edit mode
    LaunchedEffect(existingTask) {
        existingTask?.let {
            title = it.title
            description = it.description
            priority = it.priority
            deadline = it.deadline
            enableNotification = it.enableNotification
        }
    }

    // Automatic Priority Rule helper
    fun applyAutoPriority(newDeadline: String) {
        val normalized = newDeadline.trim().lowercase()
        priority = when {
            normalized.contains("hari ini") || normalized.equals("today") -> Priority.TINGGI
            normalized.contains("besok") || normalized.equals("tomorrow") -> Priority.SEDANG
            else -> Priority.RENDAH
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Sunting Tugas" else "Tambah Tugas Baru",
                        fontWeight = FontWeight.Bold,
                        color = NeutralWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.MAIN) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = NeutralWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalNavy
                )
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
                .padding(20.dp)
        ) {
            // Field: Judul Tugas
            Text(
                text = "Judul Tugas",
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Masukkan judul tugas, misal: Kumpulkan Laporan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_title"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalNavy,
                    unfocusedBorderColor = TextLightGray
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Deskripsi
            Text(
                text = "Deskripsi Tugas",
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Tambahkan informasi ringkas detail tugas...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_description"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalNavy,
                    unfocusedBorderColor = TextLightGray
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Deadline & Quick Select Buttons
            Text(
                text = "Tenggat Waktu / Deadline",
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = deadline,
                onValueChange = {
                    deadline = it
                    applyAutoPriority(it)
                },
                placeholder = { Text("Tulis tanggal atau pilih tombol cepat") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_deadline"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalNavy,
                    unfocusedBorderColor = TextLightGray
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Deadline selection buttons that trigger Auto Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hari ini", "Besok", "Minggu ini").forEach { pred ->
                    val isSelected = deadline.trim().lowercase() == pred.lowercase()
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            deadline = pred
                            applyAutoPriority(pred)
                        },
                        label = { Text(pred) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent,
                            selectedLabelColor = NeutralWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Info bar on Auto-Priority calculation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeSoft)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prioritas dihitung otomatis berdasarkan deadline. Anda masih dapat merubah pilihan prioritas di bawah ini.",
                        fontSize = 11.sp,
                        color = TextNavyDark,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Field: Prioritas Pilihan
            Text(
                text = "Tingkat Prioritas",
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.values().forEach { prio ->
                    val isSelected = priority == prio
                    val (label, tint) = when (prio) {
                        Priority.TINGGI -> "Tinggi" to PriorityHigh
                        Priority.SEDANG -> "Sedang" to PriorityMedium
                        Priority.RENDAH -> "Rendah" to PriorityLow
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) tint else tint.copy(alpha = 0.08f)
                            )
                            .clickable { priority = prio }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) NeutralWhite else tint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch: Aktifkan Notifikasi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeutralWhite)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Aktifkan Pengingat",
                        fontWeight = FontWeight.Bold,
                        color = TextNavyDark,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Kirim notifikasi ke daftar aktivitas pengingat",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = enableNotification,
                    onCheckedChange = { enableNotification = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeutralWhite,
                        checkedTrackColor = OrangeAccent
                    ),
                    modifier = Modifier.testTag("switch_notifications")
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Save Action button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Judul tugas tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    } else if (deadline.isBlank()) {
                        Toast.makeText(context, "Harap tentukan tenggat waktu / deadline!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (isEditMode && existingTask != null) {
                            viewModel.updateTask(
                                id = existingTask.id,
                                title = title,
                                description = description,
                                priority = priority,
                                deadline = deadline,
                                notify = enableNotification
                            )
                            Toast.makeText(context, "Tugas berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addTask(
                                title = title,
                                description = description,
                                deadline = deadline,
                                notify = enableNotification,
                                priorityOverride = priority
                            )
                            Toast.makeText(context, "Tugas baru berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.navigateTo(AppScreen.MAIN)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalNavy,
                    contentColor = NeutralWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Tugas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
