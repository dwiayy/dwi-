package com.example.model

enum class Priority {
    TINGGI, SEDANG, RENDAH
}

data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val deadline: String, // e.g., "Hari ini", "Besok", "Minggu ini"
    val isCompleted: Boolean,
    val enableNotification: Boolean,
    val subTasks: List<SubTask> = emptyList()
) {
    val progressPercent: Int
        get() {
            if (subTasks.isEmpty()) return if (isCompleted) 100 else 0
            val completed = subTasks.count { it.isCompleted }
            return (completed * 100) / subTasks.size
        }
}

data class Routine(
    val id: String,
    val title: String,
    val time: String,
    val isCompleted: Boolean
)

data class AppNotification(
    val id: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

data class UserAdmin(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String
)
