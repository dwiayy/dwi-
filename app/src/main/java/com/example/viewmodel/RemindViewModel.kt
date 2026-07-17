package com.example.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
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
    PROFIL
}

class RemindViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("remind_flow_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

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
        checkSavedLoginSession()
    }

    private fun checkSavedLoginSession() {
        val isLoggedIn = sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val savedEmail = sharedPrefs.getString(KEY_USER_EMAIL, "") ?: ""
        if (isLoggedIn && savedEmail.isNotEmpty()) {
            _currentUserEmail.value = savedEmail
            _currentScreen.value = AppScreen.MAIN
            _currentTab.value = MainTab.HOME
        }
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
        val finalEmail = email.ifBlank { "guest@remindflow.com" }
        _currentUserEmail.value = finalEmail
        _currentScreen.value = AppScreen.MAIN
        _currentTab.value = MainTab.HOME
        
        sharedPrefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, finalEmail)
            .apply()

        addNotification("Selamat datang kembali, $finalEmail!")
    }

    fun loginWithCredentials(email: String, javaStringPassword: String): Boolean {
        val lowerEmail = email.trim().lowercase()
        val savedPassword = sharedPrefs.getString("pass_$lowerEmail", null)
        if (savedPassword != null && savedPassword == javaStringPassword) {
            login(email.trim())
            return true
        }
        return false
    }

    fun register(email: String, javaStringPassword: String, name: String): Boolean {
        val lowerEmail = email.trim().lowercase()
        if (sharedPrefs.contains("pass_$lowerEmail")) {
            return false // Email taken
        }
        
        sharedPrefs.edit()
            .putString("pass_$lowerEmail", javaStringPassword)
            .putString("name_$lowerEmail", name)
            .apply()
        return true
    }

    fun logout() {
        _currentUserEmail.value = ""
        _currentScreen.value = AppScreen.LOGIN
        
        sharedPrefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_USER_EMAIL, "")
            .apply()

        resetTimer()
    }

    fun getCurrentUserName(): String {
        val email = _currentUserEmail.value.trim().lowercase()
        if (email.isEmpty()) return "Pengguna"
        return sharedPrefs.getString("name_$email", null) ?: email.substringBefore("@")
    }

    fun updateProfileName(newName: String): Boolean {
        val email = _currentUserEmail.value.trim().lowercase()
        if (email.isEmpty() || newName.isBlank()) return false
        sharedPrefs.edit()
            .putString("name_$email", newName.trim())
            .apply()
        addNotification("Nama profil berhasil diubah menjadi: $newName")
        return true
    }

    fun updateProfilePassword(oldPass: String, newPass: String): Boolean {
        val email = _currentUserEmail.value.trim().lowercase()
        if (email.isEmpty() || oldPass.isBlank() || newPass.isBlank()) return false
        val savedPassword = sharedPrefs.getString("pass_$email", null)
        if (savedPassword == oldPass) {
            sharedPrefs.edit()
                .putString("pass_$email", newPass)
                .apply()
            addNotification("Kata sandi profil berhasil diperbarui.")
            return true
        }
        return false
    }

    fun getProfileAvatar(): String {
        val email = _currentUserEmail.value.trim().lowercase()
        if (email.isEmpty()) return ""
        return sharedPrefs.getString("avatar_$email", "") ?: ""
    }

    fun updateProfileAvatar(avatarPath: String) {
        val email = _currentUserEmail.value.trim().lowercase()
        if (email.isNotEmpty()) {
            sharedPrefs.edit()
                .putString("avatar_$email", avatarPath)
                .apply()
            addNotification("Foto profil berhasil diperbarui.")
        }
    }

    fun updateProfileEmail(newEmail: String): Boolean {
        val oldEmail = _currentUserEmail.value.trim().lowercase()
        val formattedNewEmail = newEmail.trim().lowercase()
        if (oldEmail.isEmpty() || formattedNewEmail.isEmpty() || !formattedNewEmail.contains("@")) {
            return false
        }
        if (oldEmail == formattedNewEmail) {
            return true // No change needed
        }
        // Check if the target email is already registered
        if (sharedPrefs.contains("pass_$formattedNewEmail")) {
            return false // Already taken
        }

        val pass = sharedPrefs.getString("pass_$oldEmail", "sandi123") ?: "sandi123"
        val name = sharedPrefs.getString("name_$oldEmail", "Pengguna") ?: "Pengguna"
        val avatar = sharedPrefs.getString("avatar_$oldEmail", "") ?: ""

        sharedPrefs.edit()
            // Write to new keys
            .putString("pass_$formattedNewEmail", pass)
            .putString("name_$formattedNewEmail", name)
            .putString("avatar_$formattedNewEmail", avatar)
            // Remove old keys
            .remove("pass_$oldEmail")
            .remove("name_$oldEmail")
            .remove("avatar_$oldEmail")
            // Update active session values
            .putString(KEY_USER_EMAIL, formattedNewEmail)
            .apply()

        _currentUserEmail.value = formattedNewEmail
        addNotification("Email berhasil diperbarui ke $formattedNewEmail.")
        return true
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
        showSystemNotification(message)
    }

    private fun showSystemNotification(message: String) {
        try {
            val channelId = "remind_flow_channel"
            val channelName = "RemindFlow Notifications"
            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Channel untuk notifikasi aplikasi RemindFlow"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val builder = NotificationCompat.Builder(getApplication<Application>(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("RemindFlow")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
