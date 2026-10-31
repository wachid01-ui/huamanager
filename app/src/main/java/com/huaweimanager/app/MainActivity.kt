package com.huaweimanager.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaweimanager.app.ui.screens.BandLockScreen
import com.huaweimanager.app.ui.screens.DashboardScreen
import com.huaweimanager.app.ui.screens.SmsScreen
import com.huaweimanager.app.ui.screens.ToolsScreen
import com.huaweimanager.app.ui.screens.TowerScreen
import com.huaweimanager.app.ui.theme.AccentPrimary
import com.huaweimanager.app.ui.theme.CardBackground
import com.huaweimanager.app.ui.theme.DarkBackground
import com.huaweimanager.app.ui.theme.HuaweiManagerTheme
import com.huaweimanager.app.ui.theme.OnAccentColor
import com.huaweimanager.app.ui.theme.TextMuted
import com.huaweimanager.app.ui.theme.TextPrimary
import com.huaweimanager.app.ui.viewmodel.HuaweiViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    private val viewModel: HuaweiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            HuaweiManagerTheme(themeMode = appTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: HuaweiViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.toastMessage.collectAsState()

    val navItems = listOf(
        NavItem("Sinyal", Icons.Default.SignalCellularAlt),
        NavItem("Tower", Icons.Default.CellTower),
        NavItem("Kunci Band", Icons.Default.Lock),
        NavItem("SMS", Icons.Default.Email),
        NavItem("Alat", Icons.Default.Build)
    )

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnAccentColor,
                            selectedTextColor = AccentPrimary,
                            indicatorColor = AccentPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> TowerScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> BandLockScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SmsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            4 -> ToolsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
