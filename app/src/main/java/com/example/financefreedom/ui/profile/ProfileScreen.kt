package com.example.financefreedom.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.financefreedom.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0F1117)
private val BgCard      = Color(0xFF1E2330)
private val BgCardAlt   = Color(0xFF232838)
private val AccentGreen = Color(0xFF34D997)
private val AccentRed   = Color(0xFFFF6B6B)
private val TextPrimary = Color(0xFFF0F2F8)
private val TextSecond  = Color(0xFF8A90A4)
private val TextMuted   = Color(0xFF565C72)
private val DividerCol  = Color(0xFF252A38)

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onLogout: () -> Unit
) {
    var profileEmail   by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(true) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoggingOut   by remember { mutableStateOf(false) }
    val context        = LocalContext.current
    val profilePhotoFile = remember { File(context.applicationContext.filesDir, "profile_photo.jpg") }
    var profilePhotoPath by remember { mutableStateOf<String?>(profilePhotoFile.takeIf { it.exists() }?.absolutePath) }
    val scope = rememberCoroutineScope()

    // Launcher to pick image from gallery; saves to app file and updates avatar
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(profilePhotoFile).use { output ->
                        input.copyTo(output)
                    }
                    profilePhotoFile.absolutePath
                }
            }
            if (path != null) profilePhotoPath = path
        }
    }

    LaunchedEffect(Unit) {
        authRepository.me()
            .onSuccess {
                profileEmail = it.email
                isLoading = false
            }
            .onFailure {
                errorMessage = it.message
                isLoading = false
            }
    }

    // ── Logout Confirmation Dialog ─────────────────────────────────────────
    if (showLogoutDialog) {
        LogoutDialog(
            isLoggingOut = isLoggingOut,
            onConfirm = {
                isLoggingOut = true
                scope.launch {
                    authRepository.logout()
                    onLogout()
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDeep
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────────
            ProfileHeader()

            // ── Avatar + Email Card ───────────────────────────────────────
            AvatarCard(
                email           = profileEmail,
                isLoading       = isLoading,
                profilePhotoPath = profilePhotoPath,
                onAddOrChangePhoto = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(Modifier.height(16.dp))

            // ── Error banner ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter   = fadeIn(tween(300)) + slideInVertically(tween(300))
            ) {
                ErrorBanner(
                    message  = errorMessage.orEmpty(),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Info Items ────────────────────────────────────────────────
            if (!isLoading) {
                Spacer(Modifier.height(8.dp))
                SectionLabel("AKUN")
                Spacer(Modifier.height(8.dp))

                ProfileInfoCard {
                    InfoRow(
                        icon  = Icons.Rounded.Email,
                        label = "Email",
                        value = profileEmail.ifBlank { "-" }
                    )
                    RowDivider()
                    InfoRow(
                        icon  = Icons.Rounded.Security,
                        label = "Status",
                        value = "Terverifikasi",
                        valueColor = AccentGreen
                    )
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("APLIKASI")
                Spacer(Modifier.height(8.dp))

                ProfileInfoCard {
                    InfoRow(
                        icon  = Icons.Rounded.Info,
                        label = "Versi",
                        value = "1.0.0"
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Logout Button ─────────────────────────────────────────────
            LogoutButton(
                isLoading = isLoggingOut,
                onClick   = { showLogoutDialog = true }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text     = "Profil",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color    = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text     = "Kelola akun kamu",
                fontSize = 14.sp,
                color    = TextSecond,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BgCard)
                .border(1.dp, DividerCol, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Rounded.Person,
                contentDescription = null,
                tint               = AccentGreen,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Avatar Card ──────────────────────────────────────────────────────────────

@Composable
private fun AvatarCard(
    email: String,
    isLoading: Boolean,
    profilePhotoPath: String?,
    onAddOrChangePhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
            .border(1.dp, DividerCol, RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle: show photo if set, else default icon; tap to add/change
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddOrChangePhoto
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentGreen.copy(alpha = 0.2f),
                                    AccentGreen.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(AccentGreen.copy(alpha = 0.6f), AccentGreen.copy(alpha = 0.1f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoPath != null) {
                        AsyncImage(
                            model = File(profilePhotoPath),
                            contentDescription = "Foto profil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint               = AccentGreen,
                            modifier          = Modifier.size(48.dp)
                        )
                    }
                }
                // "Ganti foto" badge at bottom-right of avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                        .border(1.5.dp, BgCard, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.CameraAlt,
                        contentDescription = "Ganti foto profil",
                        tint               = Color.White,
                        modifier          = Modifier.size(14.dp)
                    )
                }
            }

            // Hint text
            Text(
                text       = if (profilePhotoPath != null) "Ketuk untuk ganti foto" else "Ketuk untuk tambah foto",
                fontSize   = 11.sp,
                color      = TextMuted,
                fontWeight = FontWeight.Medium
            )

            // Email / loading
            if (isLoading) {
                CircularProgressIndicator(
                    color       = AccentGreen,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(20.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = email.ifBlank { "-" },
                        fontSize      = 15.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = TextPrimary,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    // Active badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AccentGreen.copy(alpha = 0.12f))
                            .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen)
                            )
                            Text(
                                text       = "Online",
                                fontSize   = 11.sp,
                                color      = AccentGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Medium,
        color         = TextMuted,
        letterSpacing = 1.5.sp,
        modifier      = Modifier.padding(horizontal = 20.dp)
    )
}

// ─── Profile Info Card ────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .border(1.dp, DividerCol, RoundedCornerShape(20.dp))
    ) {
        Column { content() }
    }
}

// ─── Info Row ─────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = AccentGreen,
                modifier           = Modifier.size(17.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text       = label,
            fontSize   = 13.sp,
            color      = TextSecond,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(1f)
        )

        Text(
            text       = value,
            fontSize   = 13.sp,
            color      = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp)
            .height(1.dp)
            .background(DividerCol)
    )
}

// ─── Logout Button ────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AccentRed.copy(alpha = 0.1f))
            .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .then(
                if (!isLoading) Modifier.clickableSafe(onClick) else Modifier
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color       = AccentRed,
                strokeWidth = 2.dp,
                modifier    = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Logout,
                    contentDescription = "Logout",
                    tint               = AccentRed,
                    modifier           = Modifier.size(18.dp)
                )
                Text(
                    text       = "Keluar dari Akun",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AccentRed
                )
            }
        }
    }
}

// ─── Logout Confirmation Dialog ───────────────────────────────────────────────

@Composable
private fun LogoutDialog(
    isLoggingOut: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest  = { if (!isLoggingOut) onDismiss() },
        containerColor    = BgCard,
        shape             = RoundedCornerShape(24.dp),
        title = {
            Text(
                text       = "Keluar dari Akun?",
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
        },
        text = {
            Text(
                text     = "Kamu akan keluar dari sesi ini. Pastikan data sudah tersimpan.",
                fontSize = 13.sp,
                color    = TextSecond
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoggingOut) {
                Text(text = "Batal", color = TextSecond, fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentRed.copy(alpha = 0.15f))
                    .border(1.dp, AccentRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .then(if (!isLoggingOut) Modifier.clickableSafe(onConfirm) else Modifier)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        color       = AccentRed,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text       = "Keluar",
                        color      = AccentRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp
                    )
                }
            }
        }
    )
}

// ─── Error Banner ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentRed.copy(alpha = 0.1f))
            .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AccentRed)
        )
        Text(text = message, fontSize = 13.sp, color = AccentRed, fontWeight = FontWeight.Medium)
    }
}

// ─── Click Helper ─────────────────────────────────────────────────────────────

private fun Modifier.clickableSafe(onClick: () -> Unit): Modifier =
    this.then(
        androidx.compose.ui.Modifier.clickable(
            interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
            indication        = null,
            onClick           = onClick
        )
    )