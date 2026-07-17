package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.RemindViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    viewModel: RemindViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRegisterState by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightNavy, DeepNavy)
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // RemindFlow Rounded Logo Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(OrangeLight, OrangeAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = "RemindFlow Logo",
                    tint = NeutralWhite,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header Text
            Text(
                text = "RemindFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeutralWhite,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Aplikasi Manajemen Tugas & Rutinitas Harian",
                fontSize = 14.sp,
                color = OrangeLight,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralWhite
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterState) "Daftar Akun Baru" else "Masuk Akun",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNavyDark
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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

                    AnimatedVisibility(visible = isRegisterState) {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Lengkap") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = RoyalNavy
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Anda") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = RoyalNavy
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RoyalNavy
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isRegisterState) {
                        // Login Action Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Harap lengkapi email dan password!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val logSuccess = viewModel.loginWithCredentials(email, password)
                                    if (logSuccess) {
                                        Toast.makeText(context, "Masuk berhasil!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Email tidak terdaftar atau kata sandi salah. Silakan daftar terlebih dahulu!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalNavy,
                                contentColor = NeutralWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Masuk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Register Action Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                                    Toast.makeText(context, "Lengkapi seluruh isian pendaftaran!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val regSuccess = viewModel.register(email, password, name)
                                    if (regSuccess) {
                                        Toast.makeText(context, "Pendaftaran Berhasil! Silakan masuk dengan akun baru Anda.", Toast.LENGTH_LONG).show()
                                        isRegisterState = false
                                    } else {
                                        Toast.makeText(context, "Email sudah terdaftar! Gunakan email lain.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeAccent,
                                contentColor = NeutralWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Daftar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Swap Link
                    Text(
                        text = if (isRegisterState) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar",
                        color = OrangeAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isRegisterState = !isRegisterState }
                            .padding(8.dp)
                            .testTag("toggle_register_state")
                    )
                }
            }
        }
    }
}
