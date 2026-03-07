package com.example.financefreedom.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

object Routes {
    // Auth / Onboarding
    const val ENTRY    = "entry"
    const val LOGIN    = "login"
    const val REGISTER = "register"
    // Main tabs
    const val HOME     = "home"
    const val REPORT   = "report"
    const val ADD      = "add"
    const val HISTORY  = "history"
    const val PROFILE  = "profile"
    const val REMINDER = "reminder"
    const val SAVINGS  = "savings"
}

val bottomTabs = listOf(
    BottomTab(route = Routes.HOME,    title = "Home",    icon = Icons.Rounded.Home),
    BottomTab(route = Routes.REPORT,  title = "Report",  icon = Icons.Rounded.PieChart),
    BottomTab(route = Routes.ADD, title = "Add", icon = Icons.Rounded.AddCircle),
    BottomTab(route = Routes.HISTORY, title = "History", icon = Icons.Rounded.History),
    BottomTab(route = Routes.PROFILE, title = "Profile", icon = Icons.Rounded.Person),
)
