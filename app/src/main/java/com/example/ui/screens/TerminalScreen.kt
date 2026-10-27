package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountInfo
import com.example.data.model.BotSettings
import com.example.data.model.Candle
import com.example.data.model.ContractType
import com.example.data.model.Direction
import com.example.data.model.SymbolsDatabase
import com.example.data.model.Tick
import com.example.data.model.TradeSignal
import com.example.ui.components.CyberHudCard
import com.example.ui.components.LiveTradingChart
import com.example.ui.components.OneButtonExecute
import com.example.ui.components.SignalBadge
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedDim
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenDim
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurfaceBorder
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TerminalScreen(
    account: AccountInfo,
    settings: BotSettings,
    ticks: List<Tick>,
    candles: List<Candle>,
    signal: TradeSignal?,
    geminiSignal: com.example.data.ai.GeminiProtocolSignal? = null,
    isGeminiAnalyzing: Boolean = false,
    onRunGeminiAnalysis: () -> Unit = {},
    onUpdateSettings: (BotSettings) -> Unit,
    onExecuteTrade: (ContractType, Direction, Double) -> Unit,
    onSwitchAccount: (Boolean) -> Unit = {}
) {
    val currentSymbol = SymbolsDatabase.getSymbol(settings.selectedSymbolId)
    val currentQuote = ticks.lastOrNull()?.quote ?: 2450.0
    var symbolMenuExpanded by remember { mutableStateOf(false) }

    val isReal = account.activeAccountType == "REAL"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Account HUD Header with Demo / Real Switcher
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Balance Badge with Quick Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isReal) NeonGold else NeonCyan),
                    modifier = Modifier.clickable { onSwitchAccount(!isReal) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (isReal) NeonGold else NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isReal) "REAL (${account.realLoginId})" else "DEMO (${account.demoLoginId})",
                                    color = if (isReal) NeonGold else NeonCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "⇄",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", account.balance)}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Connection & Ping Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (account.isConnected) BullGreen else NeonGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (account.isConnected) "${account.loginId} • ${account.lastPingMs}ms" else "DERIV DEMO ACTIVE",
                            color = if (account.isConnected) BullGreen else NeonGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 2. Symbol Selector & Price Bar
        item {
            CyberHudCard(
                glowColor = NeonCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalSurfaceElevated)
                                .clickable { symbolMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentSymbol.name,
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = symbolMenuExpanded,
                            onDismissRequest = { symbolMenuExpanded = false },
                            modifier = Modifier.background(TerminalCard)
                        ) {
                            SymbolsDatabase.allSymbols.forEach { sym ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${sym.name} (${sym.id})",
                                            color = if (sym.id == settings.selectedSymbolId) NeonCyan else TextPrimary,
                                            fontWeight = if (sym.id == settings.selectedSymbolId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onUpdateSettings(settings.copy(selectedSymbolId = sym.id))
                                        symbolMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Spot Price with Pip Flashing
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "%.${currentSymbol.pipDecimals}f", currentQuote),
                            color = NeonGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        val lastPip = ticks.lastOrNull()?.pipChange ?: 0.0
                        Text(
                            text = if (lastPip >= 0) "+${String.format(Locale.US, "%.4f", lastPip)}" else String.format(Locale.US, "%.4f", lastPip),
                            color = if (lastPip >= 0) BullGreen else BearRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 3. Live 3D Candlestick & Tick Chart
        item {
            LiveTradingChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                candles = candles,
                ticks = ticks,
                currentPrice = currentQuote,
                barrierStr = signal?.barrier ?: "+0.35"
            )
        }

        // 4. Contract Type Selector Chips
        item {
            Text(
                text = "DERIV CONTRACT SPECS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ContractType.values()) { type ->
                    val isSelected = settings.contractType == type
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else TerminalCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else TerminalSurfaceBorder
                        ),
                        modifier = Modifier.clickable {
                            onUpdateSettings(settings.copy(contractType = type))
                        }
                    ) {
                        Text(
                            text = type.displayName,
                            color = if (isSelected) NeonCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 5. AI Signal & Reasoning Card
        item {
            CyberHudCard(
                borderColor = if (signal?.direction == Direction.UP) BullGreen.copy(alpha = 0.6f) else BearRed.copy(alpha = 0.6f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SignalBadge(
                            direction = signal?.direction ?: Direction.UP,
                            confidence = signal?.confidenceScore ?: 94
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerminalSurfaceElevated
                        ) {
                            Text(
                                text = signal?.marketRegime?.title ?: "Strong Confluence",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = signal?.reason ?: "Micro-tick momentum aligned with Smart Money Concepts order flow.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Agreed: ${signal?.strategyAgreement?.take(22) ?: "5/5 Strategies"}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Smart Barrier: ${signal?.barrier ?: "+0.35"}",
                            color = NeonGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 5b. Gemini AI Standard Trading Protocol Signal Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRunGeminiAnalysis,
                    enabled = !isGeminiAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("run_gemini_analysis_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isGeminiAnalyzing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI QUANTUM ANALYSIS IN PROGRESS...",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ RUN GEMINI QUANT PROTOCOL ANALYSIS",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                if (geminiSignal != null) {
                    com.example.ui.components.GeminiProtocolSignalCard(
                        signal = geminiSignal,
                        onExecute = {
                            val dir = if (geminiSignal.direction.equals("BUY", ignoreCase = true)) Direction.UP else Direction.DOWN
                            onExecuteTrade(settings.contractType, dir, settings.stakeAmount)
                        }
                    )
                }
            }
        }

        // 6. Stake & Duration Adjustment Deck
        item {
            CyberHudCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stake
                    Column {
                        Text(
                            text = "STAKE AMOUNT",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val newStake = maxOf(1.0, settings.stakeAmount - 1.0)
                                    onUpdateSettings(settings.copy(stakeAmount = newStake))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = NeonCyan)
                            }
                            Text(
                                text = "$${settings.stakeAmount.toInt()}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = {
                                    val newStake = settings.stakeAmount + 1.0
                                    onUpdateSettings(settings.copy(stakeAmount = newStake))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }

                    // Quick Stakes
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(2, 5, 10, 25).forEach { amount ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (settings.stakeAmount == amount.toDouble()) NeonCyan else TerminalSurfaceElevated,
                                modifier = Modifier.clickable {
                                    onUpdateSettings(settings.copy(stakeAmount = amount.toDouble()))
                                }
                            ) {
                                Text(
                                    text = "$$amount",
                                    color = if (settings.stakeAmount == amount.toDouble()) Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Duration
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DURATION",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            listOf(5, 10).forEach { ticksCount ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (settings.durationTicks == ticksCount) NeonGold else TerminalSurfaceElevated,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .clickable {
                                            onUpdateSettings(settings.copy(durationTicks = ticksCount))
                                        }
                                ) {
                                    Text(
                                        text = "${ticksCount}t",
                                        color = if (settings.durationTicks == ticksCount) Color.Black else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Execution Buttons Deck (1-Button Execute + Double Execute)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 1-Button High-Confidence Execute
                OneButtonExecute(
                    modifier = Modifier.fillMaxWidth(),
                    title = "ONE-BUTTON EXECUTE SIGNAL",
                    subtitle = "Automated Smart Barrier • ${signal?.direction?.label ?: "CALL"}",
                    isBullish = signal?.direction == Direction.UP,
                    onClick = {
                        onExecuteTrade(
                            settings.contractType,
                            signal?.direction ?: Direction.UP,
                            settings.stakeAmount
                        )
                    }
                )

                // Double Execution Deck: Higher / Lower with calibrated barriers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onExecuteTrade(ContractType.HIGHER_LOWER, Direction.UP, settings.stakeAmount)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("execute_higher_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BullGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("HIGHER", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text("Barrier +${settings.barrierOffset}", color = Color.Black.copy(alpha = 0.8f), fontSize = 9.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onExecuteTrade(ContractType.HIGHER_LOWER, Direction.DOWN, settings.stakeAmount)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("execute_lower_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BearRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("LOWER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text("Barrier -${settings.barrierOffset}", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
