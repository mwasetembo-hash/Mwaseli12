package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotSettings
import com.example.data.model.ContractType
import com.example.data.model.TradingMode
import com.example.ui.components.CyberHudCard
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
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
fun AutoBotScreen(
    settings: BotSettings,
    totalProfit: Double,
    onUpdateSettings: (BotSettings) -> Unit,
    onToggleBot: (Boolean) -> Unit
) {
    val directions = listOf(
        Pair("ONLY_UPS", "Only Ups"),
        Pair("ONLY_DOWNS", "Only Downs"),
        Pair("BOTH_AUTO", "Both — Detect Direction Automatically")
    )

    val modes = listOf(
        Pair(TradingMode.SIGNALS_ONLY, "Signals Only"),
        Pair(TradingMode.AUTO_DEMO, "Automatic Demo Trading"),
        Pair(TradingMode.CONFIRM_EACH, "Ask to Confirm Each Trade")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Master Bot Status Banner
        item {
            Spacer(modifier = Modifier.height(6.dp))
            CyberHudCard(
                borderColor = if (settings.isBotRunning) BullGreen else TerminalSurfaceBorder,
                glowColor = if (settings.isBotRunning) BullGreen else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (settings.isBotRunning) BullGreen else TextSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (settings.isBotRunning) "MWASE BOT RUNNING" else "MWASE BOT STANDBY",
                                color = if (settings.isBotRunning) BullGreen else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Hybrid Only Ups/Downs Automated Quant Engine",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { onToggleBot(!settings.isBotRunning) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.isBotRunning) BearRed else BullGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("toggle_bot_button")
                    ) {
                        Icon(
                            imageVector = if (settings.isBotRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (settings.isBotRunning) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isBotRunning) "STOP BOT" else "START BOT",
                            color = if (settings.isBotRunning) Color.White else Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // P&L and Safeguard Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TODAY'S P&L", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = (if (totalProfit >= 0) "+$" else "-$") + String.format(Locale.US, "%.2f", kotlin.math.abs(totalProfit)),
                            color = if (totalProfit >= 0) BullGreen else BearRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("MAX DAILY LOSS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "-$${settings.maxDailyLoss.toInt()}",
                            color = BearRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PROFIT TARGET", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "+$${settings.dailyProfitTarget.toInt()}",
                            color = NeonGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Contract Direction Configuration
        item {
            CyberHudCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CONTRACT DIRECTION",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    directions.forEach { (key, label) ->
                        val isSelected = settings.contractDirection == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else TerminalSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyan else TerminalSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateSettings(settings.copy(contractDirection = key)) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonCyan else Color.Transparent)
                                        .border(2.dp, if (isSelected) NeonCyan else TextSecondary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) NeonCyan else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trading Mode Configuration
        item {
            CyberHudCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TRADING MODE",
                        color = NeonGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    modes.forEach { (mode, label) ->
                        val isSelected = settings.tradingMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonGold.copy(alpha = 0.15f) else TerminalSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonGold else TerminalSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateSettings(settings.copy(tradingMode = mode)) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonGold else Color.Transparent)
                                        .border(2.dp, if (isSelected) NeonGold else TextSecondary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) NeonGold else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Risk Engine & Martingale Guard
        item {
            CyberHudCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CAPITAL PROTECTION SAFEGUARDS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto Smart Barrier Optimizer", color = TextSecondary, fontSize = 12.sp)
                        Switch(
                            checked = settings.autoSmartBarrier,
                            onCheckedChange = { onUpdateSettings(settings.copy(autoSmartBarrier = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Max Martingale Steps", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Caps loss progression to prevent drawdown", color = TextSecondary, fontSize = 10.sp)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = TerminalSurfaceElevated) {
                            Text(
                                "${settings.maxMartingaleSteps} steps",
                                color = NeonGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic & Sound Alerts", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Vibrate on signal & execution", color = TextSecondary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.hapticsEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(hapticsEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = BullGreen)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
