
package com.example.financefreedom.ui.add

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financefreedom.data.repository.ReminderRepository
import com.example.financefreedom.data.repository.TransactionRepository
import com.example.financefreedom.domain.model.MonthlySummary
import com.example.financefreedom.domain.model.ReminderItem
import com.example.financefreedom.domain.model.TransactionCategories
import com.example.financefreedom.domain.model.TransactionItem
import com.example.financefreedom.ui.components.FinanceCardSurface
import com.example.financefreedom.ui.theme.FinanceCorners
import com.example.financefreedom.ui.theme.FinanceFreedomTheme
import com.example.financefreedom.ui.theme.FinanceSpacing
import com.example.financefreedom.ui.theme.financeUiColors
import com.example.financefreedom.utils.MoneyInputFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class AddTab {
    TRANSACTION,
    REMINDER
}

private val BgCard = Color(0xFFF7F7F4)
private val BgCardAlt = Color(0xFFE8EFE8)
private val AccentGreen = Color(0xFF70AD77)
private val AccentRed = Color(0xFFB85C5C)
private val TextPrimary = Color(0xFF193032)
private val TextMuted = Color(0xFF62716B)
private val DividerCol = Color(0xFFD0D0CA)

private val expenseCategoryOptions = listOf(
    "Makanan", "Transport", "Belanja", "Hiburan",
    "Kesehatan", "Tagihan", "Pendidikan", "Lainnya"
)
private val incomeCategoryOptions = listOf(
    "Gaji", "Bonus", "Investasi", "Lainnya"
)

private fun parseAmount(amount: String): Double? = MoneyInputFormatter.toDoubleOrNull(amount)

@Composable
fun AddScreen(
    transactionRepository: TransactionRepository,
    reminderRepository: ReminderRepository
) {
    val ui = financeUiColors()

    var activeTab by remember { mutableStateOf(AddTab.TRANSACTION) }

    var isLoading by remember { mutableStateOf(false) }
    var isCategoryLoading by remember { mutableStateOf(true) }
    var isSuccess by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var categories by remember {
        mutableStateOf(
            TransactionCategories(
                income = incomeCategoryOptions,
                expense = expenseCategoryOptions
            )
        )
    }

    var title by remember { mutableStateOf("") }
    var amountField by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf("") }
    var typeIncome by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
    var transactionDate by remember { mutableStateOf(today) }

    var reminderSubmitting by remember { mutableStateOf(false) }
    var reminderSuccess by remember { mutableStateOf(false) }
    var reminderError by remember { mutableStateOf<String?>(null) }
    var reminderTitle by remember { mutableStateOf("") }
    var reminderAmount by remember { mutableStateOf(TextFieldValue("")) }
    var reminderType by remember { mutableStateOf("expense") }
    var dueDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var repeatInterval by remember { mutableStateOf("monthly") }
    var typeExpanded by remember { mutableStateOf(false) }
    var repeatExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        isCategoryLoading = true
        transactionRepository.getTransactionCategories(forceRefresh = false)
            .onSuccess { fetched ->
                val income = fetched.income.ifEmpty { incomeCategoryOptions }
                val expense = fetched.expense.ifEmpty { expenseCategoryOptions }
                categories = TransactionCategories(income = income, expense = expense)
            }
            .onFailure {
                errorMsg = it.message ?: "Gagal memuat kategori transaksi."
            }
        isCategoryLoading = false
    }

    val isFormValid =
        title.isNotBlank() &&
            amountField.text.isNotBlank() &&
            parseAmount(amountField.text) != null &&
            category.isNotBlank()

    val isReminderFormValid =
        reminderTitle.isNotBlank() &&
            MoneyInputFormatter.isPositiveAmount(reminderAmount.text) &&
            dueDate.isNotBlank()

    Surface(modifier = Modifier.fillMaxSize(), color = ui.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            AddHeader(activeTab = activeTab)
            AddTabSwitcher(activeTab = activeTab, onTabChange = { activeTab = it })

            Spacer(Modifier.height(20.dp))

            if (activeTab == AddTab.TRANSACTION) {
                TypeToggle(
                    isIncome = typeIncome,
                    onToggle = {
                        typeIncome = it
                        category = ""
                    }
                )

                Spacer(Modifier.height(20.dp))

                FormCard {
                    FormField(
                        icon = Icons.Rounded.Title,
                        label = "Judul Transaksi",
                        value = title,
                        placeholder = "Contoh: Makan siang",
                        onValueChange = { title = it },
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    FieldDivider()

                    MoneyFormField(
                        icon = Icons.Rounded.AttachMoney,
                        label = "Jumlah (Rp)",
                        value = amountField,
                        placeholder = "Rp 0",
                        onValueChange = { input ->
                            val formatted = MoneyInputFormatter.formatInput(input.text)
                            amountField = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        },
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    FieldDivider()

                    ReadOnlyField(
                        modifier = Modifier.testTag("transaction_date_field"),
                        icon = Icons.Rounded.DateRange,
                        label = "Tanggal",
                        value = transactionDate,
                        suffix = "Pilih",
                        onClick = {
                            val current = runCatching { LocalDate.parse(transactionDate.take(10)) }
                                .getOrElse { LocalDate.now() }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    transactionDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
                                },
                                current.year,
                                current.monthValue - 1,
                                current.dayOfMonth
                            ).show()
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                SectionLabel("KATEGORI")
                Spacer(Modifier.height(10.dp))
                CategoryGrid(
                    options = if (typeIncome) categories.income else categories.expense,
                    isLoading = isCategoryLoading,
                    selected = category,
                    onSelected = { category = it }
                )

                Spacer(Modifier.height(28.dp))

                AnimatedContent(
                    targetState = isSuccess to errorMsg,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "tx_status"
                ) { (success, error) ->
                    when {
                        success -> StatusBanner(message = "Transaksi berhasil ditambahkan!", isSuccess = true)
                        error != null -> StatusBanner(message = error, isSuccess = false)
                        else -> Spacer(Modifier.height(0.dp))
                    }
                }

                if (isSuccess || errorMsg != null) Spacer(Modifier.height(16.dp))

                SubmitButton(
                    isLoading = isLoading,
                    isEnabled = isFormValid && !isLoading,
                    isIncome = typeIncome,
                    onClick = {
                        isSuccess = false
                        errorMsg = null
                        isLoading = true
                        focusManager.clearFocus()
                        scope.launch {
                            val amountValue = parseAmount(amountField.text) ?: 0.0
                            val type = if (typeIncome) "income" else "expense"
                            val result = transactionRepository.createTransaction(
                                title = title.trim(),
                                amount = amountValue,
                                type = type,
                                category = category,
                                date = transactionDate,
                                note = ""
                            )
                            isLoading = false
                            result.onSuccess {
                                isSuccess = true
                                title = ""
                                amountField = TextFieldValue("")
                                category = ""
                                transactionDate = today
                            }.onFailure {
                                errorMsg = it.message ?: "Gagal menambahkan transaksi."
                            }
                        }
                    }
                )
            } else {
                FormCard {
                    OutlinedTextField(
                        value = reminderTitle,
                        onValueChange = { reminderTitle = it },
                        label = { Text("Judul reminder") },
                        singleLine = true,
                        colors = financeOutlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reminderAmount,
                        onValueChange = {
                            val formatted = MoneyInputFormatter.formatInput(it.text)
                            reminderAmount = TextFieldValue(formatted, TextRange(formatted.length))
                        },
                        label = { Text("Jumlah") },
                        placeholder = { Text("Rp 0") },
                        singleLine = true,
                        colors = financeOutlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SelectField(
                        label = "Tipe",
                        value = reminderType,
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it },
                        options = listOf("expense", "debt", "installment")
                    ) {
                        reminderType = it
                        typeExpanded = false
                    }

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { },
                        label = { Text("Due date") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val current = runCatching { LocalDate.parse(dueDate.take(10)) }
                                    .getOrElse { LocalDate.now() }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        dueDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
                                    },
                                    current.year,
                                    current.monthValue - 1,
                                    current.dayOfMonth
                                ).show()
                            }) {
                                Icon(Icons.Rounded.DateRange, contentDescription = "Pilih tanggal")
                            }
                        },
                        readOnly = true,
                        singleLine = true,
                        colors = financeOutlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SelectField(
                        label = "Repeat interval",
                        value = repeatInterval,
                        expanded = repeatExpanded,
                        onExpandedChange = { repeatExpanded = it },
                        options = listOf("none", "weekly", "monthly")
                    ) {
                        repeatInterval = it
                        repeatExpanded = false
                    }

                    Button(
                        onClick = {
                            reminderSuccess = false
                            reminderError = null
                            reminderSubmitting = true
                            focusManager.clearFocus()
                            scope.launch {
                                val result = reminderRepository.createReminder(
                                    title = reminderTitle.trim(),
                                    amount = MoneyInputFormatter.toDoubleOrNull(reminderAmount.text) ?: 0.0,
                                    type = reminderType,
                                    dueDate = dueDate,
                                    repeatInterval = repeatInterval
                                )
                                reminderSubmitting = false
                                result.onSuccess {
                                    reminderSuccess = true
                                    reminderTitle = ""
                                    reminderAmount = TextFieldValue("")
                                    reminderType = "expense"
                                    dueDate = LocalDate.now().toString()
                                    repeatInterval = "monthly"
                                }.onFailure {
                                    reminderError = it.message ?: "Gagal membuat reminder."
                                }
                            }
                        },
                        enabled = isReminderFormValid && !reminderSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (reminderSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Buat reminder")
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                AnimatedContent(
                    targetState = reminderSuccess to reminderError,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "reminder_status"
                ) { (success, error) ->
                    when {
                        success -> StatusBanner(message = "Reminder berhasil ditambahkan!", isSuccess = true)
                        error != null -> StatusBanner(message = error, isSuccess = false)
                        else -> Spacer(Modifier.height(0.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AddHeader(activeTab: AddTab) {
    val ui = financeUiColors()
    val subtitle = if (activeTab == AddTab.TRANSACTION) "Transaksi" else "Reminder"
    val trailingIcon = if (activeTab == AddTab.TRANSACTION) Icons.AutoMirrored.Rounded.Notes else Icons.Rounded.Notifications

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Tambah",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ui.primaryText,
                letterSpacing = (-0.5).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = subtitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ui.positiveText,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = ui.positive, modifier = Modifier.size(20.dp))
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ui.surface)
                .border(1.dp, ui.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = trailingIcon, contentDescription = null, tint = ui.positive, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AddTabSwitcher(activeTab: AddTab, onTabChange: (AddTab) -> Unit) {
    val ui = financeUiColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AddTabButton(
            label = "Transaksi",
            isSelected = activeTab == AddTab.TRANSACTION,
            modifier = Modifier.weight(1f),
            onClick = { onTabChange(AddTab.TRANSACTION) }
        )
        AddTabButton(
            label = "Reminder",
            isSelected = activeTab == AddTab.REMINDER,
            modifier = Modifier.weight(1f),
            onClick = { onTabChange(AddTab.REMINDER) }
        )
    }
}

@Composable
private fun AddTabButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val ui = financeUiColors()
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ui.positive.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200),
        label = "add_tab_bg_$label"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ui.positive.copy(alpha = 0.4f) else Color.Transparent,
        animationSpec = tween(200),
        label = "add_tab_border_$label"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) ui.positiveText else ui.mutedTextReadable,
        animationSpec = tween(200),
        label = "add_tab_text_$label"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun TypeToggle(isIncome: Boolean, onToggle: (Boolean) -> Unit) {
    val ui = financeUiColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ui.surface)
            .border(1.dp, ui.outline, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeTab(
            label = "Pengeluaran",
            icon = Icons.Rounded.ArrowUpward,
            isSelected = !isIncome,
            color = AccentRed,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(false) }
        )
        TypeTab(
            label = "Pemasukan",
            icon = Icons.Rounded.ArrowDownward,
            isSelected = isIncome,
            color = AccentGreen,
            modifier = Modifier.weight(1f),
            onClick = { onToggle(true) }
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
        targetValue = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200), label = "type_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.4f) else Color.Transparent,
        animationSpec = tween(200), label = "type_border"
    )
    val ui = financeUiColors()
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (color == AccentGreen) ui.positiveText else color
        } else ui.secondaryTextReadable,
        animationSpec = tween(200), label = "type_content"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
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

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    FinanceCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.screenHorizontal),
        cornerRadius = FinanceCorners.cardMedium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
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
    val ui = financeUiColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Text(text = label, fontSize = 10.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ui.primaryText),
                cursorBrush = SolidColor(ui.accent),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(text = placeholder, fontSize = 14.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Normal)
                        }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun ReadOnlyField(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    suffix: String,
    onClick: () -> Unit
) {
    val ui = financeUiColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
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
            Text(text = label, fontSize = 10.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ui.primaryText)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(ui.surfaceAlt)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(text = suffix, fontSize = 10.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FieldDivider() {
    val ui = financeUiColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp)
            .height(1.dp)
            .background(ui.outline)
    )
}

@Composable
private fun MoneyFormField(
    icon: ImageVector,
    label: String,
    value: TextFieldValue,
    placeholder: String,
    onValueChange: (TextFieldValue) -> Unit,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    val ui = financeUiColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Text(text = label, fontSize = 10.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ui.primaryText),
                cursorBrush = SolidColor(ui.accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                decorationBox = { inner ->
                    Box {
                        if (value.text.isEmpty()) {
                            Text(text = placeholder, fontSize = 14.sp, color = ui.secondaryTextReadable, fontWeight = FontWeight.Normal)
                        }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    options: List<String>,
    isLoading: Boolean,
    selected: String,
    onSelected: (String) -> Unit
) {
    val ui = financeUiColors()

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = ui.positive)
        }
        return
    }

    if (options.isEmpty()) {
        Text(
            text = "Kategori belum tersedia.",
            fontSize = 12.sp,
            color = ui.mutedTextReadable,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        return
    }

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
                    val isSelected = cat == selected
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) ui.positive.copy(alpha = 0.15f) else ui.surface,
                        animationSpec = tween(200), label = "cat_bg_$cat"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) ui.positive.copy(alpha = 0.5f) else ui.outline,
                        animationSpec = tween(200), label = "cat_border_$cat"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) ui.positiveText else ui.secondaryTextReadable,
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
                                indication = null,
                                onClick = { onSelected(if (isSelected) "" else cat) }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val ui = financeUiColors()
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            colors = financeOutlinedFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) { Text("Pilih") }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val ui = financeUiColors()

    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = ui.mutedTextReadable,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun SubmitButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    isIncome: Boolean,
    onClick: () -> Unit
) {
    val ui = financeUiColors()
    val accentColor = if (isIncome) AccentGreen else AccentRed
    val bgColor = if (isEnabled) accentColor.copy(alpha = 0.15f) else BgCard
    val borderColor = if (isEnabled) accentColor.copy(alpha = 0.4f) else DividerCol
    val textColor = if (isEnabled) ui.primaryText else ui.secondaryTextReadable

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
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
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
                CircularProgressIndicator(color = accentColor, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isIncome) "Tambah Pemasukan" else "Tambah Pengeluaran",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun financeOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = financeUiColors().primaryText,
    unfocusedTextColor = financeUiColors().primaryText,
    focusedLabelColor = financeUiColors().secondaryText,
    unfocusedLabelColor = financeUiColors().secondaryText,
    focusedPlaceholderColor = financeUiColors().secondaryText,
    unfocusedPlaceholderColor = financeUiColors().secondaryText,
    cursorColor = financeUiColors().accent
)

@Composable
private fun StatusBanner(message: String, isSuccess: Boolean) {
    val color = if (isSuccess) AccentGreen else AccentRed
    val icon = if (isSuccess) Icons.Rounded.CheckCircle else Icons.AutoMirrored.Rounded.Notes

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

@Preview(showBackground = true, backgroundColor = 0xFFDFDFDF)
@Composable
private fun AddScreenPreview() {
    val fakeTransactionRepository = object : TransactionRepository {
        override suspend fun getTransactions(): Result<List<TransactionItem>> = Result.success(emptyList())

        override suspend fun getTransactionCategories(forceRefresh: Boolean): Result<TransactionCategories> {
            return Result.success(
                TransactionCategories(
                    income = listOf("Gaji", "Bonus"),
                    expense = listOf("Makanan", "Transport")
                )
            )
        }

        override suspend fun createTransaction(
            title: String,
            amount: Double,
            type: String,
            category: String,
            date: String,
            note: String
        ): Result<TransactionItem> =
            Result.success(TransactionItem("preview", title, amount, type, category, date, note))

        override suspend fun getMonthlySummary(month: String): Result<MonthlySummary> =
            Result.success(MonthlySummary(0.0, 0.0, 0.0))
    }

    val fakeReminderRepository = object : ReminderRepository {
        override suspend fun getReminders(): Result<List<ReminderItem>> = Result.success(emptyList())
        override suspend fun getUpcomingReminders(): Result<List<ReminderItem>> = Result.success(emptyList())

        override suspend fun createReminder(
            title: String,
            amount: Double,
            type: String,
            dueDate: String,
            repeatInterval: String?
        ): Result<ReminderItem> =
            Result.success(
                ReminderItem(
                    id = "preview-reminder",
                    title = title,
                    amount = amount,
                    type = type,
                    dueDate = dueDate,
                    isPaid = false,
                    repeatInterval = repeatInterval,
                    userId = null,
                    createdAt = null
                )
            )

        override suspend fun markReminderPaid(reminderId: String): Result<ReminderItem> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteReminder(reminderId: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override suspend fun syncReminderSchedules(): Result<Unit> = Result.success(Unit)
    }

    FinanceFreedomTheme(darkTheme = false) {
        AddScreen(
            transactionRepository = fakeTransactionRepository,
            reminderRepository = fakeReminderRepository
        )
    }
}


