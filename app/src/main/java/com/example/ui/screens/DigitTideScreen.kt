package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.ContractType
import com.example.data.model.DigitTelemetry
import com.example.data.model.Direction
import com.example.data.model.SymbolsDatabase
import com.example.data.model.Tick
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
import java.util.Locale

@Composable
fun DigitTideScreen(
    symbolId: String,
    ticks: List<Tick>,
    telemetry: DigitTelemetry?,
    currentStake: Double,
    onSelectSymbol: (String) -> Unit,
    onExecuteDigitTrade: (ContractType, Direction, Double) -> Unit
) {
    val sym = SymbolsDatabase.getSymbol(symbolId)
    val quote = ticks.lastOrNull()?.quote ?: 2450.0
    var symbolMenuExpanded by remember { mutableStateOf(false) }

    val tele = telemetry ?: DigitTelemetry()
    val last10 = tele.lastDigits.takeLast(10)
    val freq = tele.digitFrequency
    val maxFreq = freq.maxOrNull() ?: 15.0

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
                        imageVector = Icons.Default.Waves,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DIGIT TIDE SIGNAL STUDIO",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Rolling 100 Ticks • Quant Frequency & Markov Prediction",
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
                        text = "LIVE RADAR",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Symbol Selector Bar
        item {
            CyberHudCard {
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
                                text = sym.name,
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
                            SymbolsDatabase.allSymbols.filter { it.isVolatilityIndex }.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${s.name} (${s.id})",
                                            color = if (s.id == symbolId) NeonCyan else TextPrimary,
                                            fontWeight = if (s.id == symbolId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectSymbol(s.id)
                                        symbolMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "%.${sym.pipDecimals}f", quote),
                            color = NeonGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Last Digit: ${quote.toString().takeLast(1)}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Tick Digit Vector (Last 10 Digits)
        item {
            CyberHudCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE TICK DIGIT VECTOR",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Rolling 10 Ticks",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        last10.forEachIndexed { idx, digit ->
                            val isLatest = idx == last10.size - 1
                            val isEven = digit % 2 == 0
                            val borderCol = if (isLatest) NeonCyan else if (isEven) BullGreen.copy(alpha = 0.5f) else BearRed.copy(alpha = 0.5f)
                            val textCol = if (isLatest) NeonCyan else if (isEven) BullGreen else BearRed
                            val bgCol = if (isLatest) NeonCyan.copy(alpha = 0.2f) else if (isEven) BullGreenDim else BearRedDim

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgCol)
                                    .border(if (isLatest) 2.dp else 1.dp, borderCol, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$digit",
                                    color = textCol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Digit Frequency Histogram (0–9)
        item {
            CyberHudCard(glowColor = NeonCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIGIT FREQUENCY HISTOGRAM (0–9)",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = BearRedDim, border = androidx.compose.foundation.BorderStroke(1.dp, BearRed)) {
                                Text("HOT: ${tele.hotDigit}", color = BearRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = NeonCyan.copy(alpha = 0.15f), border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)) {
                                Text("COLD: ${tele.coldDigit}", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // 10 Frequency Bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        freq.forEachIndexed { d, f ->
                            val isHot = d == tele.hotDigit
                            val isCold = d == tele.coldDigit
                            val barColor = if (isHot) BearRed else if (isCold) NeonCyan else Color(0xFF455A64)

                            val heightRatio = (f / maxOf(1.0, maxFreq)).toFloat().coerceIn(0.1f, 1f)
                            val animatedHeight by animateFloatAsState(targetValue = heightRatio, animationSpec = tween(400))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = "${f.toInt()}%",
                                    color = if (isHot) BearRed else if (isCold) NeonCyan else TextSecondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(barColor)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$d",
                                    color = if (isHot) BearRed else if (isCold) NeonCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Even vs Odd and Over vs Under Gauges
        item {
            CyberHudCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Even vs Odd
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("EVEN DIGITS: ${tele.evenPercentage.toInt()}%", color = BullGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("ODD DIGITS: ${tele.oddPercentage.toInt()}%", color = BearRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (tele.evenPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = BullGreen,
                            trackColor = BearRed
                        )
                    }

                    // Over vs Under (5 barrier)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("UNDER (0–4): ${tele.underPercentage.toInt()}%", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("OVER (5–9): ${tele.overPercentage.toInt()}%", color = NeonGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (tele.underPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = NeonGold
                        )
                    }
                }
            }
        }

        // Instant 1-Click Digit Execution Deck
        item {
            Text("1-CLICK DIGIT EXECUTION ($currentStake USD)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1: Even / Odd
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onExecuteDigitTrade(ContractType.EVEN_ODD, Direction.UP, currentStake) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_digit_even"),
                        colors = ButtonDefaults.buttonColors(containerColor = BullGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EVEN (${tele.evenPercentage.toInt()}%)", color = Color.Black, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { onExecuteDigitTrade(ContractType.EVEN_ODD, Direction.DOWN, currentStake) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_digit_odd"),
                        colors = ButtonDefaults.buttonColors(containerColor = BearRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ODD (${tele.oddPercentage.toInt()}%)", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }

                // Row 2: Over / Under
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onExecuteDigitTrade(ContractType.OVER_UNDER, Direction.DOWN, currentStake) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_digit_under"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("UNDER 5 (${tele.underPercentage.toInt()}%)", color = Color.Black, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { onExecuteDigitTrade(ContractType.OVER_UNDER, Direction.UP, currentStake) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_digit_over"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("OVER 5 (${tele.overPercentage.toInt()}%)", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }

                // Row 3: Matches / Differs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onExecuteDigitTrade(ContractType.MATCHES_DIFFERS, Direction.UP, currentStake) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_digit_matches"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("MATCH HOT (${tele.hotDigit}) • 8.5x", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onExecuteDigitTrade(ContractType.MATCHES_DIFFERS, Direction.DOWN, currentStake) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("quick_digit_differs"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("DIFF COLD (${tele.coldDigit}) • 1.1x", color = NeonGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
