package com.example.financefreedom.ui.add

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0F1117)
private val BgCard      = Color(0xFF1E2330)
private val BgCardAlt   = Color(0xFF232838)
private val BgInput     = Color(0xFF161A24)
private val AccentGreen = Color(0xFF34D997)
private val AccentRed   = Color(0xFFFF6B6B)
private val TextPrimary = Color(0xFFF0F2F8)
private val TextSecond  = Color(0xFF8A90A4)
private val TextMuted   = Color(0xFF565C72)
private val DividerCol  = Color(0xFF252A38)

// Backend validates category per transaction type. Use separate lists so income/expense
// only get valid options (e.g. "Lainnya" is invalid for income on backend).
private val expenseCategoryOptions = listOf(
    "Makanan", "Transport", "Belanja", "Hiburan",
    "Kesehatan", "Tagihan", "Pendidikan", "Lainnya"
)
private val incomeCategoryOptions = listOf(
    "Gaji", "Bonus", "Investasi", "Lainnya"
)
// Backend rejects most income category values; only "Other" is known to work.
// Map all income categories to "Other" so every option submits without "Invalid category" error.
// (Category choice is still shown in UI; backend will store as "Other" until it supports more.)
private val incomeCategoryApiValues = mapOf(
    "Gaji" to "Other",
    "Bonus" to "Other",
    "Investasi" to "Other",
    "Lainnya" to "Other"
)

/** Parses amount string: supports "25000", "1.5" and Indonesian format "500.000" / "10.000.000". */
private fun parseAmount(amount: String): Double? {
    if (amount.isBlank()) return null
    val t = amount.trim()
    val dotCount = t.count { it == '.' }
    return when {
        dotCount == 0 -> t.toDoubleOrNull()
        dotCount >= 2 -> t.replace(".", "").toDoubleOrNull() // e.g. 10.000.000
        else -> {
            // Single dot: "500.000" = 500k (thousand sep) vs "1.5" = decimal
            val afterDot = t.substringAfter(".")
            if (afterDot.length == 3 && afterDot.all { it.isDigit() }) {
                t.replace(".", "").toDoubleOrNull() // 500.000 -> 500000
            } else {
                t.toDoubleOrNull() // 1.5 -> 1.5
            }
        }
    }
}

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun AddScreen(transactionRepository: TransactionRepository) {
    var isLoading   by remember { mutableStateOf(false) }
    var isSuccess   by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf<String?>(null) }

    // Form state
    var title       by remember { mutableStateOf("") }
    var amount      by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf("") }
    var typeIncome  by remember { mutableStateOf(false) } // false = expense
    val today       = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val isFormValid = title.isNotBlank() && amount.isNotBlank() && parseAmount(amount) != null && category.isNotBlank()

    Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────────
            AddHeader()

            // ── Type Toggle ───────────────────────────────────────────────
            TypeToggle(
                isIncome   = typeIncome,
                onToggle   = {
                    typeIncome = it
                    category  = "" // Reset so we don't keep invalid category when switching type
                }
            )

            Spacer(Modifier.height(20.dp))

            // ── Form Card ─────────────────────────────────────────────────
            FormCard {
                // Title
                FormField(
                    icon        = Icons.Rounded.Title,
                    label       = "Judul Transaksi",
                    value       = title,
                    placeholder = "Contoh: Makan siang",
                    onValueChange = { title = it },
                    imeAction   = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                FieldDivider()

                // Amount
                FormField(
                    icon          = Icons.Rounded.AttachMoney,
                    label         = "Jumlah (Rp)",
                    value         = amount,
                    placeholder   = "0",
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    keyboardType  = KeyboardType.Number,
                    imeAction     = ImeAction.Next,
                    onImeAction   = { focusManager.moveFocus(FocusDirection.Down) }
                )

                FieldDivider()

                // Date (read-only, shows today)
                ReadOnlyField(
                    icon  = Icons.Rounded.DateRange,
                    label = "Tanggal",
                    value = today
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Category Chips ────────────────────────────────────────────
            SectionLabel("KATEGORI")
            Spacer(Modifier.height(10.dp))
            CategoryGrid(
                options    = if (typeIncome) incomeCategoryOptions else expenseCategoryOptions,
                selected   = category,
                onSelected = { category = it }
            )

            Spacer(Modifier.height(28.dp))

            // ── Status Message ────────────────────────────────────────────
            AnimatedContent(
                targetState = isSuccess to errorMsg,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "status"
            ) { (success, error) ->
                when {
                    success -> StatusBanner(
                        message = "Transaksi berhasil ditambahkan!",
                        isSuccess = true
                    )
                    error != null -> StatusBanner(message = error, isSuccess = false)
                    else -> Spacer(Modifier.height(0.dp))
                }
            }

            if (isSuccess || errorMsg != null) Spacer(Modifier.height(16.dp))

            // ── Submit Button ─────────────────────────────────────────────
            SubmitButton(
                isLoading   = isLoading,
                isEnabled   = isFormValid && !isLoading,
                isIncome    = typeIncome,
                onClick = {
                    isSuccess = false
                    errorMsg  = null
                    isLoading = true
                    focusManager.clearFocus()
                    scope.launch {
                        val amountValue = parseAmount(amount) ?: 0.0
                        val type = if (typeIncome) "income" else "expense"
                        // Backend only accepts "Other" for income; always send that to avoid "Invalid category" error.
                        val categoryForApi = if (typeIncome) "Other" else category
                        val result = transactionRepository.createTransaction(
                            title = title.trim(),
                            amount = amountValue,
                            type = type,
                            category = categoryForApi,
                            date = today,
                            note = ""
                        )
                        isLoading = false
                        result.onSuccess {
                            isSuccess = true
                            title     = ""
                            amount    = ""
                            category  = ""
                        }.onFailure {
                            errorMsg = it.message ?: "Gagal menambahkan transaksi."
                        }
                    }
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun AddHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text          = "Tambah",
                fontSize      = 26.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = "Transaksi",
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = AccentGreen,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector        = Icons.Rounded.Add,
                    contentDescription = null,
                    tint               = AccentGreen,
                    modifier           = Modifier.size(20.dp)
                )
            }
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
                imageVector        = Icons.Rounded.Notes,
                contentDescription = null,
                tint               = AccentGreen,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Type Toggle ──────────────────────────────────────────────────────────────

@Composable
private fun TypeToggle(isIncome: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, DividerCol, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeTab(
            label      = "Pengeluaran",
            icon       = Icons.Rounded.ArrowUpward,
            isSelected = !isIncome,
            color      = AccentRed,
            modifier   = Modifier.weight(1f),
            onClick    = { onToggle(false) }
        )
        TypeTab(
            label      = "Pemasukan",
            icon       = Icons.Rounded.ArrowDownward,
            isSelected = isIncome,
            color      = AccentGreen,
            modifier   = Modifier.weight(1f),
            onClick    = { onToggle(true) }
        )
    }
}

@Composable
private fun TypeTab(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200), label = "type_bg"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) color.copy(alpha = 0.4f) else Color.Transparent,
        animationSpec = tween(200), label = "type_border"
    )
    val contentColor by animateColorAsState(
        targetValue   = if (isSelected) color else TextMuted,
        animationSpec = tween(200), label = "type_content"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

// ─── Form Card ────────────────────────────────────────────────────────────────

@Composable
private fun FormCard(content: @Composable () -> Unit) {
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

@Composable
private fun FormField(
    icon: ImageVector,
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
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
            Icon(imageVector = icon, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value          = value,
                onValueChange  = onValueChange,
                singleLine     = true,
                textStyle      = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
                cursorBrush    = SolidColor(AccentGreen),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                decorationBox  = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(text = placeholder, fontSize = 14.sp, color = TextMuted, fontWeight = FontWeight.Normal)
                        }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun ReadOnlyField(icon: ImageVector, label: String, value: String) {
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
            Icon(imageVector = icon, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BgCardAlt)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(text = "Hari ini", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FieldDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 66.dp)
            .height(1.dp)
            .background(DividerCol)
    )
}

// ─── Category Grid ────────────────────────────────────────────────────────────

@Composable
private fun CategoryGrid(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { cat ->
                    val isSelected   = cat == selected
                    val bgColor by animateColorAsState(
                        targetValue   = if (isSelected) AccentGreen.copy(alpha = 0.15f) else BgCard,
                        animationSpec = tween(200), label = "cat_bg_$cat"
                    )
                    val borderColor by animateColorAsState(
                        targetValue   = if (isSelected) AccentGreen.copy(alpha = 0.5f) else DividerCol,
                        animationSpec = tween(200), label = "cat_border_$cat"
                    )
                    val textColor by animateColorAsState(
                        targetValue   = if (isSelected) AccentGreen else TextSecond,
                        animationSpec = tween(200), label = "cat_text_$cat"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = { onSelected(if (isSelected) "" else cat) }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = cat,
                            fontSize   = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = textColor
                        )
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

// ─── Submit Button ────────────────────────────────────────────────────────────

@Composable
private fun SubmitButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    isIncome: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (isIncome) AccentGreen else AccentRed
    val bgColor     = if (isEnabled) accentColor.copy(alpha = 0.15f) else BgCard
    val borderColor = if (isEnabled) accentColor.copy(alpha = 0.4f) else DividerCol
    val textColor   = if (isEnabled) accentColor else TextMuted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(
                if (isEnabled && !isLoading) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onClick
                    )
                } else Modifier
            )
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "btn_state"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    color       = accentColor,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(22.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Add,
                        contentDescription = null,
                        tint               = textColor,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        text       = if (isIncome) "Tambah Pemasukan" else "Tambah Pengeluaran",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = textColor
                    )
                }
            }
        }
    }
}

// ─── Status Banner ────────────────────────────────────────────────────────────

@Composable
private fun StatusBanner(message: String, isSuccess: Boolean) {
    val color       = if (isSuccess) AccentGreen else AccentRed
    val icon        = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Notes

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text = message, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}