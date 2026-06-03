package com.example.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    LOGIN,
    MAIN,
    ADD_TASK,
    EDIT_TASK,
    TASK_DETAIL
}

enum class MainTab {
    HOME,
    RUTINITAS,
    NOTIFIKASI,
    ADMIN
}

class RemindViewModel : ViewModel() {

    // Navigation & Screen states
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Auth state
    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    // Task list state
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    // Routine list state
    private val _routineList = MutableStateFlow<List<Routine>>(emptyList())
    val routineList: StateFlow<List<Routine>> = _routineList.asStateFlow()

    // Notifications state
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Selected Task for Details / Editing
    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTaskId: StateFlow<String?> = _selectedTaskId.asStateFlow()

    // Interactive 25-min focus timer states
    private val _timeLeftSeconds = MutableStateFlow(25 * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // System Log Monitored
    private val _systemScanOngoing = MutableStateFlow(false)
    val systemScanOngoing: StateFlow<Boolean> = _systemScanOngoing.asStateFlow()

    private val _systemStatus = MutableStateFlow("Sistem Stabil & Siaga")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    // Dummy Admin Users list
    val dummyUsers = listOf(
        UserAdmin("1", "Dwi Ayu Latifah", "dwiayulatifah50@gmail.com", "Admin Utama", "Aktif"),
        UserAdmin("2", "Ahmad Fauzi", "fauzi.ahmad@remindflow.com", "Pengguna VIP", "Aktif"),
        UserAdmin("3", "Siti Aminah", "siti@remindflow.com", "Pengguna Standard", "Aktif"),
        UserAdmin("4", "Budi Santoso", "budi@remindflow.com", "Pengguna Baru", "Pending")
    )

    init {
        loadInitialDummyData()
    }

    private fun loadInitialDummyData() {
        // Initial Tasks
        _taskList.value = listOf(
            Task(
                id = "task-1",
                title = "Kumpulkan Laporan",
                description = "Kumpulkan laporan tugas akhir untuk dosen pembimbing",
                priority = Priority.TINGGI,
                deadline = "Hari ini",
                isCompleted = false,
                enableNotification = true,
                subTasks = listOf(
                    SubTask("sub-1", "Mengumpulkan bahan laporan", true),
                    SubTask("sub-2", "Membuat pendahuluan", true),
                    SubTask("sub-3", "Revisi isi laporan", false),
                    SubTask("sub-4", "Kirim ke dosen via e-mail", false)
                )
            ),
            Task(
                id = "task-2",
                title = "Belajar React Native & Compose",
                description = "Pelajari konsep navigation dan state management",
                priority = Priority.SEDANG,
                deadline = "Besok",
                isCompleted = false,
                enableNotification = true,
                subTasks = listOf(
                    SubTask("sub-5", "Nonton video tutorial", false),
                    SubTask("sub-6", "Eksperimen kode Compose", false)
                )
            ),
            Task(
                id = "task-3",
                title = "Bersihkan Catatan Kuliah",
                description = "Merapikan draf catatan kuliah minggu lalu",
                priority = Priority.RENDAH,
                deadline = "Minggu ini",
                isCompleted = true,
                enableNotification = false,
                subTasks = emptyList()
            )
        )

        // Initial Routines
        _routineList.value = listOf(
            Routine("rot-1", "Bangun pagi", "05:00", true),
            Routine("rot-2", "Kuliah", "08:00", false),
            Routine("rot-3", "Belajar mandiri", "16:00", false),
            Routine("rot-4", "Review tugas harian", "20:00", false),
            Routine("rot-5", "Istirahat tidur malam", "22:00", false)
        )

        // Initial Notifications
        _notifications.value = listOf(
            AppNotification("notif-1", "Tugas \"Kumpulkan Laporan\" harus selesai hari ini!", "10 menit lalu"),
            AppNotification("notif-2", "Belajar React Native & Compose dijadwalkan besok", "2 jam lalu"),
            AppNotification("notif-3", "Rutinitas \"Review tugas harian\" belum diselesaikan", "Kemarin")
        )
    }

    // Auth functions
    fun login(email: String) {
        _currentUserEmail.value = email.ifBlank { "guest@remindflow.com" }
        _currentScreen.value = AppScreen.MAIN
        _currentTab.value = MainTab.HOME
        addNotification("Selamat datang kembali, ${email.ifBlank { "Pengguna" }}!")
    }

    fun logout() {
        _currentUserEmail.value = ""
        _currentScreen.value = AppScreen.LOGIN
        resetTimer()
    }

    // Navigation functions
    fun navigateTo(screen: AppScreen, taskId: String? = null) {
        _selectedTaskId.value = taskId
        if (screen == AppScreen.TASK_DETAIL) {
            resetTimer() // Reset timer whenever we open a new focus task
        }
        _currentScreen.value = screen
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    // Task CRUD operations
    fun addTask(title: String, description: String, deadline: String, notify: Boolean, priorityOverride: Priority? = null) {
        val calculatedPriority = priorityOverride ?: calculatePriority(deadline)
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = calculatedPriority,
            deadline = deadline,
            isCompleted = false,
            enableNotification = notify,
            subTasks = listOf(
                SubTask(UUID.randomUUID().toString(), "Persiapan awal", false),
                SubTask(UUID.randomUUID().toString(), "Analisis materi", false),
                SubTask(UUID.randomUUID().toString(), "Penyelesaian tugas", false)
            )
        )
        _taskList.value = _taskList.value + newTask

        if (notify) {
            triggerNotificationForTask(newTask)
        }
    }

    fun updateTask(id: String, title: String, description: String, priority: Priority, deadline: String, notify: Boolean) {
        _taskList.value = _taskList.value.map { task ->
            if (task.id == id) {
                task.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    deadline = deadline,
                    enableNotification = notify
                )
            } else task
        }
        addNotification("Tugas \"$title\" berhasil diperbarui")
    }

    fun toggleTaskCompletion(id: String) {
        _taskList.value = _taskList.value.map { task ->
            if (task.id == id) {
                val nextState = !task.isCompleted
                if (nextState) {
                    addNotification("Hebat! Tugas \"${task.title}\" selesai.")
                }
                task.copy(isCompleted = nextState)
            } else task
        }
    }

    fun toggleSubTask(taskId: String, subTaskId: String) {
        _taskList.value = _taskList.value.map { task ->
            if (task.id == taskId) {
                val updatedSubs = task.subTasks.map { sub ->
                    if (sub.id == subTaskId) {
                        sub.copy(isCompleted = !sub.isCompleted)
                    } else sub
                }
                // Check if all subtasks are complete, then maybe toggle the main task or just update subtasks
                task.copy(subTasks = updatedSubs)
            } else task
        }
    }

    fun deleteTask(id: String) {
        val label = _taskList.value.find { it.id == id }?.title ?: ""
        _taskList.value = _taskList.value.filter { it.id != id }
        if (label.isNotEmpty()) {
            addNotification("Tugas \"$label\" telah dihapus")
        }
    }

    // Routine actions
    fun addRoutine(title: String, time: String) {
        val newRoutine = Routine(
            id = UUID.randomUUID().toString(),
            title = title,
            time = time,
            isCompleted = false
        )
        _routineList.value = _routineList.value + newRoutine
        addNotification("Rutinitas harian \"$title\" ditambahkan pada pukul $time")
    }

    fun toggleRoutineCompletion(id: String) {
        _routineList.value = _routineList.value.map { rot ->
            if (rot.id == id) {
                val nextState = !rot.isCompleted
                if (nextState) {
                    addNotification("Memulai aktivitas pagi: \"${rot.title}\" selesai dikerjakan.")
                }
                rot.copy(isCompleted = nextState)
            } else rot
        }
    }

    fun deleteRoutine(id: String) {
        _routineList.value = _routineList.value.filter { it.id != id }
    }

    // Notification helpers
    fun addNotification(message: String) {
        val newNotif = AppNotification(
            id = UUID.randomUUID().toString(),
            message = message,
            timestamp = "Baru saja"
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    private fun triggerNotificationForTask(task: Task) {
        val message = when (task.priority) {
            Priority.TINGGI -> "PENTING: Tugas \"${task.title}\" jatuh tempo hari ini! Selesaikan sekarang."
            Priority.SEDANG -> "Pengingat: Tugas \"${task.title}\" dijadwalkan besok. Persiapkan dirimu."
            Priority.RENDAH -> "Inspirasi tugas: \"${task.title}\" dapat dicicil sepanjang minggu ini."
        }
        addNotification(message)
    }

    // Auto Priority calculator logic requested:
    // Jika deadline hari ini, prioritas menjadi Tinggi
    // Jika deadline besok, prioritas menjadi Sedang
    // Jika deadline masih lama (e.g. minggu ini), prioritas menjadi Rendah
    fun calculatePriority(deadline: String): Priority {
        val normalized = deadline.trim().lowercase()
        return when {
            normalized.contains("hari") || normalized.contains("today") -> Priority.TINGGI
            normalized.contains("besok") || normalized.contains("tomorrow") -> Priority.SEDANG
            else -> Priority.RENDAH
        }
    }

    // Focus Timer Operations
    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timeLeftSeconds.value--
            }
            if (_timeLeftSeconds.value == 0) {
                _isTimerRunning.value = false
                addNotification("Sesi Fokus Berhasil! Istirahatlah sejenak.")
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timeLeftSeconds.value = 25 * 60
    }

    // Admin Monitor simulation
    fun triggerSystemScan() {
        if (_systemScanOngoing.value) return
        _systemScanOngoing.value = true
        viewModelScope.launch {
            _systemStatus.value = "Memulai Pemindaian Jaringan..."
            delay(800)
            _systemStatus.value = "Mengoptimalkan Alokasi Memori Core..."
            delay(800)
            _systemStatus.value = "Memeriksa Integritas Basis Data Lokal..."
            delay(1000)
            _systemStatus.value = "Sistem OK - 100% Berjalan Lancar"
            _systemScanOngoing.value = false
            addNotification("Pemantauan Admin: Diagnosis performa sistem selesai.")
        }
    }
}
