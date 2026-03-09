package com.example.financefreedom.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.financefreedom.R
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.financeUiColors

@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    val ui = financeUiColors()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ui.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "Finance Freedom app icon",
                            modifier = Modifier
                                .scale(1.3F)
                                .width(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Finance Freedom",
                            style = MaterialTheme.typography.titleLarge,
                            color = ui.primaryText,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Kelola uang dengan rapi dan aman",
                            style = MaterialTheme.typography.bodySmall,
                            color = ui.secondaryTextReadable
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Kontrol penuh untuk pemasukan, pengeluaran, dan target tabungan.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ui.primaryText,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Satu aplikasi untuk mencatat transaksi, memantau ringkasan, dan menjaga pengingat finansial tetap teratur.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ui.secondaryTextReadable
                )

                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = ui.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FeatureRow(
                            icon = Icons.Rounded.Payments,
                            title = "Track transaksi",
                            description = "Catat pemasukan dan pengeluaran dengan kategori yang jelas."
                        )
                        FeatureRow(
                            icon = Icons.Rounded.Savings,
                            title = "Pantau tabungan",
                            description = "Lihat progres target finansial secara terukur."
                        )
                        FeatureRow(
                            icon = Icons.Rounded.Schedule,
                            title = "Ingat jatuh tempo",
                            description = "Kelola pengingat tagihan dan cicilan lebih rapi."
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ui.accent,
                        contentColor = ui.onAccent
                    )
                ) {
                    Text("Mulai Sekarang")
                }

                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ui.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ui.surface,
                        contentColor = ui.secondaryTextReadable
                    )
                ) {
                    Text("Sudah punya akun? Masuk")
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    val ui = financeUiColors()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .background(ui.surfaceAlt, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ui.accent
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ui.primaryText,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ui.secondaryTextReadable
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFDFDFDF)
@Composable
private fun EntryScreenPreview() {
    FinanceFreedomTheme(darkTheme = false) {
        EntryScreen(
            onGetStarted = {},
            onLogin = {}
        )
    }
}
