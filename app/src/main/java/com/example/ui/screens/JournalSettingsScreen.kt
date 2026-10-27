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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountInfo
import com.example.data.model.BotSettings
import com.example.data.model.Direction
import com.example.data.model.ExecutedOrder
import com.example.ui.components.CyberHudCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalSettingsScreen(
    account: AccountInfo,
    settings: BotSettings,
    orders: List<ExecutedOrder>,
    connectionStatus: String,
    onUpdateSettings: (BotSettings) -> Unit,
    onReconnectDeriv: (String, String) -> Unit,
    onSwitchAccount: (Boolean) -> Unit
) {
    var appIdInput by remember(settings.derivAppId) { mutableStateOf(settings.derivAppId) }
    var tokenInput by remember(settings.derivApiToken) { mutableStateOf(settings.derivApiToken) }
    var credentialsSaved by remember { mutableStateOf(false) }

    val wonOrders = orders.filter { it.status == "WON" }
    val lostOrders = orders.filter { it.status == "LOST" }
    val totalTrades = wonOrders.size + lostOrders.size
    val winRate = if (totalTrades > 0) (wonOrders.size * 100) / totalTrades else 0
    val netProfit = orders.sumOf { it.profit }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "JOURNAL & DUAL ACCOUNTS",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Demo & Real Accounts • Execution Mining Console • Deriv API",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Dual Account Balances (Demo vs Real)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Demo Account Card
                val isDemoActive = account.activeAccountType == "DEMO"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSwitchAccount(false) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDemoActive) NeonCyan.copy(alpha = 0.12f) else TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isDemoActive) 2.dp else 1.dp,
                        if (isDemoActive) NeonCyan else TerminalSurfaceBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DEMO ACCOUNT", color = if (isDemoActive) NeonCyan else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            if (isDemoActive) {
                                Surface(shape = RoundedCornerShape(4.dp), color = NeonCyan) {
                                    Text("ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", account.demoBalance)}",
                            color = if (isDemoActive) TextPrimary else TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = account.demoLoginId,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Real Account Card
                val isRealActive = account.activeAccountType == "REAL"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSwitchAccount(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isRealActive) NeonGold.copy(alpha = 0.12f) else TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isRealActive) 2.dp else 1.dp,
                        if (isRealActive) NeonGold else TerminalSurfaceBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("REAL ACCOUNT", color = if (isRealActive) NeonGold else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            if (isRealActive) {
                                Surface(shape = RoundedCornerShape(4.dp), color = NeonGold) {
                                    Text("ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", account.realBalance)}",
                            color = if (isRealActive) TextPrimary else TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = account.realLoginId,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Statistics KPI Deck
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
                        Text("TOTAL TRADES", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("$totalTrades", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("WIN RATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("$winRate%", color = if (winRate >= 50) BullGreen else BearRed, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("NET P&L", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            (if (netProfit >= 0) "+$" else "-$") + String.format(Locale.US, "%.2f", kotlin.math.abs(netProfit)),
                            color = if (netProfit >= 0) BullGreen else BearRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Deriv Configuration Card
        item {
            CyberHudCard(
                glowColor = NeonCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DERIV.COM API BRIDGE", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (account.isConnected) BullGreenDim else Color(0x22FFD166),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (account.isConnected) BullGreen else NeonGold)
                        ) {
                            Text(
                                text = connectionStatus.take(28),
                                color = if (account.isConnected) BullGreen else NeonGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // App ID Field
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Deriv App ID (Numeric integer)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonCyan.copy(alpha = 0.15f),
                                modifier = Modifier.clickable {
                                    appIdInput = "1089"
                                    credentialsSaved = false
                                }
                            ) {
                                Text(
                                    text = "Reset to 1089 (Official Public ID)",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = appIdInput,
                            onValueChange = {
                                appIdInput = it
                                credentialsSaved = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deriv_app_id_input"),
                            placeholder = { Text("1089", color = TextSecondary) },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TerminalSurfaceBorder,
                                focusedContainerColor = TerminalSurfaceElevated,
                                unfocusedContainerColor = TerminalSurfaceElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // API Token Field
                    Column {
                        Text("Deriv API Token (Read / Trade / Admin)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = {
                                tokenInput = it
                                credentialsSaved = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deriv_api_token_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = TerminalSurfaceBorder,
                                focusedContainerColor = TerminalSurfaceElevated,
                                unfocusedContainerColor = TerminalSurfaceElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Save & Reconnect Button
                    Button(
                        onClick = {
                            val newSet = settings.copy(
                                derivAppId = appIdInput.trim(),
                                derivApiToken = tokenInput.trim()
                            )
                            onUpdateSettings(newSet)
                            onReconnectDeriv(appIdInput.trim(), tokenInput.trim())
                            credentialsSaved = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_and_reconnect_deriv_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (credentialsSaved) BullGreen else NeonCyan
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (credentialsSaved) Icons.Default.Check else Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (credentialsSaved) "Credentials Saved & Connected" else "Save & Connect Deriv API",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Executed Orders History Table
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXECUTION JOURNAL (${orders.size})",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Real-time Fill Audit",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        if (orders.isEmpty()) {
            item {
                CyberHudCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No orders placed yet in this session.", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Use 1-Button Execute or start MWASE Bot to begin.", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(orders) { order ->
                val isWin = order.status == "WON"
                val isLost = order.status == "LOST"
                val statusColor = if (isWin) BullGreen else if (isLost) BearRed else NeonGold
                val statusBg = if (isWin) BullGreenDim else if (isLost) BearRedDim else Color(0x22FFD166)

                CyberHudCard(
                    borderColor = statusColor.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = order.symbol,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = order.contractType.displayName,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                            ) {
                                Text(
                                    text = order.status,
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stake: $${order.stake.toInt()} • Direction: ${order.direction.name}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Text(
                                text = (if (order.profit >= 0) "+$" else "-$") + String.format(Locale.US, "%.2f", kotlin.math.abs(order.profit)),
                                color = statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Spot: ${String.format(Locale.US, "%.2f", order.entrySpot)} ➔ ${String.format(Locale.US, "%.2f", order.exitSpot)} • Barrier: ${order.barrier}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "${dateFormat.format(Date(order.timestamp))} • ${order.reason}",
                            color = TextTertiary,
                            fontSize = 10.sp
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

val TextTertiary = Color(0xFF607D8B)
