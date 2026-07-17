package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.MainHeader
import com.example.ui.theme.*
import com.example.viewmodel.RemindViewModel

// Beautiful built-in avatar emojis with matching soft background colors
data class AvatarPreset(val label: String, val emoji: String, val bgColor: Color)

val avatarPresets = listOf(
    AvatarPreset("Cat", "🐱", Color(0xFFE3F2FD)),
    AvatarPreset("Lion", "🦁", Color(0xFFFFF3E0)),
    AvatarPreset("Panda", "🐼", Color(0xFFF5F5F5)),
    AvatarPreset("Fox", "🦊", Color(0xFFFFE0B2)),
    AvatarPreset("Rocket", "🚀", Color(0xFFEDE7F6)),
    AvatarPreset("Tech", "💻", Color(0xFFE0F2F1)),
    AvatarPreset("Study", "📚", Color(0xFFE8F5E9)),
    AvatarPreset("Coffee", "☕", Color(0xFFFFEBEE))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userEmail by viewModel.currentUserEmail.collectAsState()
    val tasks by viewModel.taskList.collectAsState()
    val routines by viewModel.routineList.collectAsState()

    // Profile state
    val initialName = remember(userEmail) { viewModel.getCurrentUserName() }
    var profileName by remember { mutableStateOf(initialName) }
    var profileEmail by remember { mutableStateOf(userEmail) }
    var avatarPath by remember { mutableStateOf(viewModel.getProfileAvatar()) }

    // Synchronize states when logged in user changes
    LaunchedEffect(userEmail) {
        profileName = viewModel.getCurrentUserName()
        profileEmail = userEmail
        avatarPath = viewModel.getProfileAvatar()
    }

    // Password change states
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    // Statistics calculations
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }

    val totalRoutines = routines.size
    val completedRoutines = routines.count { it.isCompleted }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val path = uri.toString()
            avatarPath = path
            viewModel.updateProfileAvatar(path)
            Toast.makeText(context, "Foto profil berhasil diubah!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NeutralLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            MainHeader(
                title = "Profil Pengguna",
                subtitle = "Kelola detail akun Anda dan tinjau performa harian.",
                onLogoutClick = { viewModel.logout() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Image/Icon display
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(OrangeSoft)
                            .clickable {
                                photoPickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarPath.isNotEmpty()) {
                            val isEmojiPreset = avatarPresets.any { it.emoji == avatarPath }
                            if (isEmojiPreset) {
                                val preset = avatarPresets.first { it.emoji == avatarPath }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(preset.bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = avatarPath,
                                        fontSize = 44.sp
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = avatarPath,
                                    contentDescription = "Foto Profil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        // Edit icon overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "UBAH",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profileName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNavyDark,
                        modifier = Modifier.testTag("profile_display_name")
                    )

                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = TextMutedGray,
                        modifier = Modifier.testTag("profile_display_email")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = TextLightGray, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini Statistics Grid (Removed "Rasio Sukses", showing Completed Tasks & Routines)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            count = "$completedTasks/$totalTasks",
                            label = "Tugas Selesai",
                            icon = Icons.Default.CheckCircle,
                            iconColor = PriorityLow
                        )
                        StatItem(
                            count = "$completedRoutines/$totalRoutines",
                            label = "Rutinitas Selesai",
                            icon = Icons.Default.Refresh,
                            iconColor = RoyalNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Edit Profile Section
            Text(
                text = "Edit Detail Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextNavyDark,
                        unfocusedTextColor = TextNavyDark,
                        focusedLabelColor = RoyalNavy,
                        unfocusedLabelColor = TextMutedGray,
                        focusedLeadingIconColor = RoyalNavy,
                        unfocusedLeadingIconColor = TextMutedGray,
                        focusedBorderColor = RoyalNavy,
                        unfocusedBorderColor = TextLightGray
                    )

                    // Profile Name Input
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_edit_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profile Email Input (Changed Email is now supported!)
                    OutlinedTextField(
                        value = profileEmail,
                        onValueChange = { profileEmail = it },
                        label = { Text("Alamat Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_email_edit_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar Selection Label
                    Text(
                        text = "Pilih Preset Avatar Emojimu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNavyDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // LazyRow of avatar presets
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(avatarPresets) { preset ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(if (avatarPath == preset.emoji) 3.dp else 0.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(preset.bgColor)
                                    .clickable {
                                        avatarPath = preset.emoji
                                        viewModel.updateProfileAvatar(preset.emoji)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.emoji,
                                    fontSize = 24.sp
                                )
                                if (avatarPath == preset.emoji) {
                                    // Small check mark or highlight
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom URL Field in case they want a web photo
                    var tempUrl by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            label = { Text("URL Foto Web") },
                            placeholder = { Text("https://...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_avatar_url_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tempUrl.isNotBlank() && tempUrl.startsWith("http")) {
                                    avatarPath = tempUrl
                                    viewModel.updateProfileAvatar(tempUrl)
                                    Toast.makeText(context, "URL Foto diatur!", Toast.LENGTH_SHORT).show()
                                    tempUrl = ""
                                } else {
                                    Toast.makeText(context, "Masukkan URL valid!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Set")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Option to load from gallery
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_gallery_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil Foto dari Galeri HP")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Save Profile Details
                    Button(
                        onClick = {
                            val oldEmail = userEmail
                            if (profileName.isBlank()) {
                                Toast.makeText(context, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            } else if (profileEmail.isBlank() || !profileEmail.contains("@")) {
                                Toast.makeText(context, "Masukkan alamat email yang valid!", Toast.LENGTH_SHORT).show()
                            } else {
                                val nameSuccess = viewModel.updateProfileName(profileName)
                                var emailSuccess = true
                                if (profileEmail.trim().lowercase() != oldEmail.trim().lowercase()) {
                                    emailSuccess = viewModel.updateProfileEmail(profileEmail)
                                }

                                if (nameSuccess && emailSuccess) {
                                    Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                } else if (!emailSuccess) {
                                    Toast.makeText(context, "Email sudah terdaftar atau gagal diperbarui!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_save_name_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan Profil", color = NeutralWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Change Password Section
            Text(
                text = "Keamanan Akun (Ganti Sandi)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextNavyDark,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextNavyDark,
                        unfocusedTextColor = TextNavyDark,
                        focusedLabelColor = RoyalNavy,
                        unfocusedLabelColor = TextMutedGray,
                        focusedLeadingIconColor = RoyalNavy,
                        unfocusedLeadingIconColor = TextMutedGray,
                        focusedBorderColor = RoyalNavy,
                        unfocusedBorderColor = TextLightGray
                    )

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Kata Sandi Lama") },
                        leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                        visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showOldPassword = !showOldPassword }) {
                                Icon(
                                    imageVector = if (showOldPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showOldPassword) "Sembunyikan" else "Tampilkan"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_old_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Kata Sandi Baru") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showNewPassword) "Sembunyikan" else "Tampilkan"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_new_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (oldPassword.isBlank() || newPassword.isBlank()) {
                                Toast.makeText(context, "Mohon isi kata sandi lama dan baru!", Toast.LENGTH_SHORT).show()
                            } else {
                                val success = viewModel.updateProfilePassword(oldPassword, newPassword)
                                if (success) {
                                    Toast.makeText(context, "Kata sandi sukses diperbarui!", Toast.LENGTH_SHORT).show()
                                    oldPassword = ""
                                    newPassword = ""
                                } else {
                                    Toast.makeText(context, "Kata sandi lama salah atau tidak sesuai!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_save_password_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perbarui Kata Sandi", color = NeutralWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatItem(
    count: String,
    label: String,
    icon: ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextNavyDark
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMutedGray
        )
    }
}
