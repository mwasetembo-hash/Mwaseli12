package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.AiSwarmScreen
import com.example.ui.screens.AutoBotScreen
import com.example.ui.screens.DigitTideScreen
import com.example.ui.screens.JournalSettingsScreen
import com.example.ui.screens.PatternsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    TERMINAL("Terminal", Icons.Default.ShowChart, "nav_terminal"),
    DIGIT_TIDE("Digit Tide", Icons.Default.Waves, "nav_digit_tide"),
    SCANNER("Radar", Icons.Default.Radar, "nav_scanner"),
    PATTERNS("Patterns", Icons.Default.MenuBook, "nav_patterns"),
    AI_SWARM("AI Swarm", Icons.Default.Psychology, "nav_ai_swarm"),
    AUTO_BOT("Bot", Icons.Default.Bolt, "nav_auto_bot"),
    JOURNAL("Accounts", Icons.Default.Assessment, "nav_journal")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MwaseHybridApp()
            }
        }
    }
}

@Composable
fun MwaseHybridApp(viewModel: MainViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(NavTab.TERMINAL) }

    val account by viewModel.account.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val currentSignal by viewModel.currentSignal.collectAsStateWithLifecycle()
    val scannedSignals by viewModel.scannedSignals.collectAsStateWithLifecycle()
    val currentTicks by viewModel.currentTicks.collectAsStateWithLifecycle()
    val candles by viewModel.candles.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val digitTelemetry by viewModel.digitTelemetry.collectAsStateWithLifecycle()
    val aiModels by viewModel.aiModels.collectAsStateWithLifecycle()
    val selectedAiModelId by viewModel.selectedAiModelId.collectAsStateWithLifecycle()
    val isSwarmConsensusMode by viewModel.isSwarmConsensusMode.collectAsStateWithLifecycle()
    val geminiSignal by viewModel.geminiSignal.collectAsStateWithLifecycle()
    val isGeminiAnalyzing by viewModel.isGeminiAnalyzing.collectAsStateWithLifecycle()

    val totalProfit = orders.sumOf { it.profit }

    // Android BackHandler requirement: Return to TERMINAL if on any other tab
    if (currentTab != NavTab.TERMINAL) {
        BackHandler {
            currentTab = NavTab.TERMINAL
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = TerminalCard,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, TerminalSurfaceBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) NeonCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TerminalBg)
        ) {
            when (currentTab) {
                NavTab.TERMINAL -> {
                    TerminalScreen(
                        account = account,
                        settings = settings,
                        ticks = currentTicks[settings.selectedSymbolId] ?: emptyList(),
                        candles = candles[settings.selectedSymbolId] ?: emptyList(),
                        signal = currentSignal,
                        geminiSignal = geminiSignal,
                        isGeminiAnalyzing = isGeminiAnalyzing,
                        onRunGeminiAnalysis = { viewModel.runGeminiAnalysis() },
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onExecuteTrade = { type, dir, stake ->
                            viewModel.executeTrade(type, dir, stake)
                        },
                        onSwitchAccount = { toReal ->
                            viewModel.switchAccount(toReal)
                        }
                    )
                }
                NavTab.DIGIT_TIDE -> {
                    DigitTideScreen(
                        symbolId = settings.selectedSymbolId,
                        ticks = currentTicks[settings.selectedSymbolId] ?: emptyList(),
                        telemetry = digitTelemetry[settings.selectedSymbolId],
                        currentStake = settings.stakeAmount,
                        onSelectSymbol = { viewModel.selectSymbol(it) },
                        onExecuteDigitTrade = { type, dir, stake ->
                            viewModel.executeTrade(type, dir, stake)
                        }
                    )
                }
                NavTab.SCANNER -> {
                    ScannerScreen(
                        scannedSignals = scannedSignals,
                        currentTicks = currentTicks,
                        selectedSymbolId = settings.selectedSymbolId,
                        onSelectSymbol = {
                            viewModel.selectSymbol(it)
                            currentTab = NavTab.TERMINAL
                        },
                        onQuickTrade = { sym, dir ->
                            viewModel.selectSymbol(sym)
                            viewModel.executeTrade(settings.contractType, dir, settings.stakeAmount)
                        }
                    )
                }
                NavTab.PATTERNS -> {
                    PatternsScreen(
                        onApplyPatternToBot = { pattern ->
                            val targetDir = if (pattern.bias == "BEARISH") "ONLY_DOWNS" else "ONLY_UPS"
                            viewModel.updateSettings(settings.copy(contractDirection = targetDir))
                            currentTab = NavTab.TERMINAL
                        }
                    )
                }
                NavTab.AI_SWARM -> {
                    AiSwarmScreen(
                        models = aiModels,
                        selectedModelId = selectedAiModelId,
                        isSwarmConsensusMode = isSwarmConsensusMode,
                        geminiSignal = geminiSignal,
                        isGeminiAnalyzing = isGeminiAnalyzing,
                        onRunGeminiAnalysis = { viewModel.runGeminiAnalysis() },
                        onExecuteTrade = { dir, stake ->
                            viewModel.executeTrade(settings.contractType, dir, stake)
                        },
                        onSelectModel = { viewModel.selectAiModel(it) },
                        onToggleSwarmConsensus = { viewModel.toggleSwarmConsensus(it) },
                        onSaveApiKey = { modelId, key -> viewModel.saveModelApiKey(modelId, key) }
                    )
                }
                NavTab.AUTO_BOT -> {
                    AutoBotScreen(
                        settings = settings,
                        totalProfit = totalProfit,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onToggleBot = { viewModel.toggleAutoBot(it) }
                    )
                }
                NavTab.JOURNAL -> {
                    JournalSettingsScreen(
                        account = account,
                        settings = settings,
                        orders = orders,
                        connectionStatus = connectionStatus,
                        onUpdateSettings = { viewModel.updateSettings(it) },
                        onReconnectDeriv = { id, token -> viewModel.reconnectDeriv(id, token) },
                        onSwitchAccount = { toReal -> viewModel.switchAccount(toReal) }
                    )
                }
            }
        }
    }
}
