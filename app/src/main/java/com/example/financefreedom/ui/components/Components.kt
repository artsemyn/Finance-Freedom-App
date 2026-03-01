package com.example.financefreedom.ui

package com.example.financefreedom.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.financefreedom.ui.theme.AppColors
import com.example.financefreedom.ui.theme.AppType

// ── Primary CTA Button ────────────────────────────────────────────────────────
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled)
                    Brush.horizontalGradient(
                        listOf(AppColors.Accent, Color(0xFF3A6FD8))
                    )
                else
                    Brush.horizontalGradient(
                        listOf(AppColors.Tertiary, AppColors.Tertiary)
                    )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.2f)),
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text  = text,
                style = AppType.BodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}

// ── Ghost / Outline Button ────────────────────────────────────────────────────
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AppColors.Outline, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = AppColors.Accent.copy(alpha = 0.1f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = text,
            style = AppType.BodyMedium.copy(color = AppColors.Secondary)
        )
    }
}

// ── Input Field ───────────────────────────────────────────────────────────────
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isFocused = remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> AppColors.Negative
            isFocused.value -> AppColors.Accent
            else -> AppColors.Outline
        },
        animationSpec = tween(200),
        label = "border"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Text(
            text  = label.uppercase(),
            style = AppType.LabelSmall.copy(color = AppColors.Secondary),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AppType.BodyMedium.copy(color = AppColors.Primary),
            cursorBrush = SolidColor(AppColors.Accent),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction    = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.SurfaceVariant)
                        .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text  = placeholder,
                                    style = AppType.BodyMedium.copy(color = AppColors.Tertiary)
                                )
                            }
                            innerTextField()
                        }
                        if (isPassword) {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Text(
                                    text  = if (passwordVisible) "👁" else "👁‍🗨",
                                    style = AppType.BodySmall.copy(color = AppColors.Secondary)
                                )
                            }
                        } else {
                            trailingIcon?.invoke()
                        }
                    }
                }
            }
        )

        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text     = errorMessage,
                style    = AppType.BodySmall.copy(color = AppColors.Negative),
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}

// ── Divider with label ────────────────────────────────────────────────────────
@Composable
fun DividerWithLabel(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier            = modifier.fillMaxWidth(),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = AppColors.Outline,
            thickness = 1.dp
        )
        Text(
            text  = label,
            style = AppType.BodySmall.copy(color = AppColors.Tertiary)
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = AppColors.Outline,
            thickness = 1.dp
        )
    }
}

// ── Subtle animated dot indicator ────────────────────────────────────────────
@Composable
fun PulsingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue   = 0.7f,
        targetValue    = 1.3f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Box(
        modifier = modifier
            .size((8 * scale).dp)
            .clip(CircleShape)
            .background(AppColors.Positive)
    )
}