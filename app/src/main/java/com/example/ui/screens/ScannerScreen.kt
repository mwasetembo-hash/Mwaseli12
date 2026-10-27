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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Direction
import com.example.data.model.SymbolsDatabase
import com.example.data.model.Tick
import com.example.data.model.TradeSignal
import com.example.ui.components.CyberHudCard
import com.example.ui.components.SignalBadge
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
fun ScannerScreen(
    scannedSignals: Map<String, TradeSignal>,
    currentTicks: Map<String, List<Tick>>,
    selectedSymbolId: String,
    onSelectSymbol: (String) -> Unit,
    onQuickTrade: (String, Direction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DERIV MULTI-MARKET RADAR",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Scanning 17 Assets • Real-time Micro-Tick Confluence",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Text(
                        text = "LIVE PULSE",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        items(SymbolsDatabase.allSymbols) { sym ->
            val sig = scannedSignals[sym.id]
            val ticks = currentTicks[sym.id]
            val currentPrice = ticks?.lastOrNull()?.quote ?: 2000.0
            val pipDelta = ticks?.lastOrNull()?.pipChange ?: 0.0
            val isCurrent = sym.id == selectedSymbolId

            CyberHudCard(
                borderColor = if (isCurrent) NeonCyan else TerminalSurfaceBorder,
                glowColor = if (isCurrent) NeonCyan else Color.Transparent,
                modifier = Modifier.clickable { onSelectSymbol(sym.id) }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sym.name,
                                    color = if (isCurrent) NeonCyan else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan)
                                    )
                                }
                            }
                            Text(
                                text = "${sym.market} • Barrier ${sig?.barrier ?: "+0.35"}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "%.${sym.pipDecimals}f", currentPrice),
                                color = NeonGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (pipDelta >= 0) "+${String.format(Locale.US, "%.3f", pipDelta)}" else String.format(Locale.US, "%.3f", pipDelta),
                                color = if (pipDelta >= 0) BullGreen else BearRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Signal Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SignalBadge(
                            direction = sig?.direction ?: Direction.UP,
                            confidence = sig?.confidenceScore ?: 92
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onSelectSymbol(sym.id) },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("view_chart_${sym.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ShowChart, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chart", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onQuickTrade(sym.id, sig?.direction ?: Direction.UP) },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("quick_trade_${sym.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sig?.direction == Direction.UP) BullGreen else BearRed
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trade", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
