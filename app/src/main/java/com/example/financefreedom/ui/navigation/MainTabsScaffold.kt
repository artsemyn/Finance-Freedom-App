package com.example.financefreedom.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0F1117)
private val BgNavBar    = Color(0xFF13161E)
private val AccentGreen = Color(0xFF34D997)
private val TextMuted   = Color(0xFF565C72)
private val DividerCol  = Color(0xFF1E2330)

// ─── Scaffold ─────────────────────────────────────────────────────────────────

@Composable
fun MainTabsScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        // Penting: nonaktifkan insets bawaan Scaffold agar tidak bentrok
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = BgDeep,
        bottomBar = {
            CustomBottomNavBar(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

// ─── Custom Bottom Nav Bar ────────────────────────────────────────────────────

@Composable
private fun CustomBottomNavBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgNavBar)
    ) {
        // Garis pembatas tipis di atas nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerCol)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars) // gesture nav bar
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { tab ->
                if (tab.route == Routes.ADD) {
                    AddTabButton(
                        tab        = tab,
                        isSelected = currentRoute == tab.route,
                        onClick    = { onTabSelected(tab.route) }
                    )
                } else {
                    RegularTabItem(
                        tab        = tab,
                        isSelected = currentRoute == tab.route,
                        onClick    = { onTabSelected(tab.route) }
                    )
                }
            }
        }
    }
}

// ─── Regular Tab ──────────────────────────────────────────────────────────────

@Composable
private fun RegularTabItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue   = if (isSelected) AccentGreen else TextMuted,
        animationSpec = tween(250),
        label         = "color_${tab.route}"
    )
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.9f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "scale_${tab.route}"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentGreen.copy(alpha = 0.12f))
                )
            }
            Icon(
                imageVector        = tab.icon,
                contentDescription = tab.title,
                tint               = iconColor,
                modifier           = Modifier.size(22.dp)
            )
        }

        Text(
            text          = tab.title,
            fontSize      = 10.sp,
            fontWeight    = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color         = iconColor,
            letterSpacing = 0.3.sp
        )
    }
}

// ─── Add (FAB-style) Tab ──────────────────────────────────────────────────────

@Composable
private fun AddTabButton(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) AccentGreen else AccentGreen.copy(alpha = 0.9f),
        animationSpec = tween(200),
        label         = "add_bg"
    )
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            stiffness    = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label         = "add_scale"
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = tab.icon,
            contentDescription = tab.title,
            tint               = BgDeep,
            modifier           = Modifier.size(26.dp)
        )
    }
}