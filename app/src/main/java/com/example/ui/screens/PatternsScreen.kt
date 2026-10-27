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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class ForexPattern(
    val id: Int,
    val name: String,
    val category: String,
    val bias: String, // "BULLISH", "BEARISH", "BILATERAL"
    val winProbability: Int, // e.g. 84
    val confirmationRules: String,
    val botUsage: String
)

@Composable
fun PatternsScreen(
    onApplyPatternToBot: (ForexPattern) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All",
        "Chart Patterns",
        "Candlesticks",
        "Market Structure",
        "Smart Money (SMC)",
        "Harmonics",
        "Indicators"
    )

    val masterPatterns = remember {
        listOf(
            // Major Chart Patterns
            ForexPattern(1, "Head and Shoulders", "Chart Patterns", "BEARISH", 83, "Break below neckline with expanding volume", "Triggers PUT / ONLY DOWNS after neckline breach"),
            ForexPattern(2, "Inverse Head and Shoulders", "Chart Patterns", "BULLISH", 85, "Neckline breakout retest with bullish rejection", "Triggers CALL / ONLY UPS on breakout confirmation"),
            ForexPattern(3, "Double Top", "Chart Patterns", "BEARISH", 79, "Rejection at key resistance zone twice", "Executes LOWER / PUT with resistance barrier"),
            ForexPattern(4, "Double Bottom", "Chart Patterns", "BULLISH", 81, "Double bounce off support floor with momentum", "Executes HIGHER / CALL with support barrier"),
            ForexPattern(5, "Triple Top / Bottom", "Chart Patterns", "BILATERAL", 84, "Three touches of key boundary before explosion", "Monitors range extremes for breakout momentum"),
            ForexPattern(7, "Ascending Triangle", "Chart Patterns", "BULLISH", 82, "Higher lows pushing against flat resistance", "High-probability Only Ups breakout signal"),
            ForexPattern(8, "Descending Triangle", "Chart Patterns", "BEARISH", 80, "Lower highs pushing against flat support", "High-probability Only Downs breakdown signal"),
            ForexPattern(9, "Symmetrical Triangle", "Chart Patterns", "BILATERAL", 76, "Compression coil before explosive expansion", "Waits for volatility expansion direction"),
            ForexPattern(10, "Bull Flag & Pennant", "Chart Patterns", "BULLISH", 87, "Impulse pole followed by narrow downward consolidation", "Fast tick velocity scalping on pole continuation"),
            ForexPattern(11, "Bear Flag & Pennant", "Chart Patterns", "BEARISH", 86, "Sharp decline followed by slight upward consolidation", "High-velocity down momentum contract execution"),
            ForexPattern(13, "Rising / Falling Wedge", "Chart Patterns", "BILATERAL", 78, "Converging trendlines signaling momentum exhaustion", "Executes mean reversion order near apex"),
            ForexPattern(15, "Rectangle Range", "Chart Patterns", "BILATERAL", 77, "Clean oscillations between support & resistance", "Ideal for Touch/No Touch or Range Higher/Lower"),
            ForexPattern(16, "Cup and Handle", "Chart Patterns", "BULLISH", 84, "Rounded base with shallow handle pullback", "Strong multi-timeframe bull continuation"),
            ForexPattern(27, "Gartley Pattern", "Harmonics", "BILATERAL", 83, "0.618 B-point retracement and 0.786 D-point PRZ", "Harmonic reversal at exact Fibonacci confluence"),
            ForexPattern(28, "Bat Pattern", "Harmonics", "BILATERAL", 86, "0.382-0.50 B-point and 0.886 D-point zone", "High-accuracy reversal pivot with tight stop loss"),
            ForexPattern(29, "Butterfly Pattern", "Harmonics", "BILATERAL", 82, "0.786 B-point and 1.27 / 1.618 extension", "Exhaustion counter-trend execution"),
            ForexPattern(30, "Crab & Deep Crab", "Harmonics", "BILATERAL", 85, "1.618 extreme Fibonacci impulse extension", "Extreme overextension snap-back entry"),
            ForexPattern(34, "Hammer / Pin Bar", "Candlesticks", "BULLISH", 88, "Long lower rejection wick >= 2x body size", "Immediate micro-tick upward surge trigger"),
            ForexPattern(36, "Bullish Engulfing", "Candlesticks", "BULLISH", 89, "Green body completely consumes prior red candle", "Only Ups high-probability surge momentum"),
            ForexPattern(37, "Morning Star", "Candlesticks", "BULLISH", 86, "Three-candle reversal pattern with star bottom", "Trend reversal confirmation trigger"),
            ForexPattern(38, "Three White Soldiers", "Candlesticks", "BULLISH", 91, "Three strong consecutive green candles closing near highs", "Ultra-high confidence Only Ups continuation"),
            ForexPattern(42, "Shooting Star", "Candlesticks", "BEARISH", 87, "Long upper wick rejecting highs with small red body", "Immediate downward impulse trigger"),
            ForexPattern(44, "Bearish Engulfing", "Candlesticks", "BEARISH", 89, "Red body completely engulfs prior green candle", "Only Downs high-probability breakdown"),
            ForexPattern(45, "Evening Star", "Candlesticks", "BEARISH", 86, "Three-candle peak reversal confirmation", "Trend reversal sell execution"),
            ForexPattern(46, "Three Black Crows", "Candlesticks", "BEARISH", 92, "Three strong consecutive red candles with minimal wicks", "Ultra-high confidence Only Downs continuation"),
            ForexPattern(50, "Doji & Long-Legged Doji", "Candlesticks", "BILATERAL", 75, "Open and close virtually identical; balance point", "Risk gate pauses bot until directional breakout"),
            ForexPattern(55, "Marubozu", "Candlesticks", "BILATERAL", 90, "Solid candle with no wicks, pure institutional drive", "Follows direction of Marubozu with high stake"),
            ForexPattern(56, "Higher Highs & Lows", "Market Structure", "BULLISH", 93, "Consecutive higher structure pivots", "Locks auto-bot into Bullish Only Ups mode"),
            ForexPattern(57, "Lower Highs & Lows", "Market Structure", "BEARISH", 93, "Consecutive lower structure pivots", "Locks auto-bot into Bearish Only Downs mode"),
            ForexPattern(58, "Break of Structure (BOS)", "Market Structure", "BILATERAL", 94, "Decisive close beyond previous swing high/low", "Enters in trend direction after BOS validation"),
            ForexPattern(59, "Change of Character (CHoCH)", "Market Structure", "BILATERAL", 91, "First break of counter-trend swing structure", "Early alert for macro trend reversal"),
            ForexPattern(60, "Market Structure Shift (MSS)", "Market Structure", "BILATERAL", 92, "Aggressive displacement breaking previous range", "High-conviction entry with Smart Money alignment"),
            ForexPattern(71, "Liquidity Sweep", "Smart Money (SMC)", "BILATERAL", 95, "Fakeout above/below key level followed by sharp rejection", "Peak institutional trap execution"),
            ForexPattern(72, "Fair Value Gap (FVG)", "Smart Money (SMC)", "BILATERAL", 94, "Three-candle imbalance waiting to be filled", "Precision limit order at 50% FVG retest"),
            ForexPattern(73, "Order Block (OB)", "Smart Money (SMC)", "BILATERAL", 93, "Last opposing candle before massive expansion", "Entry zone with highest risk-to-reward ratio"),
            ForexPattern(76, "Premium & Discount Zones", "Smart Money (SMC)", "BILATERAL", 90, "Buys in discount (<50%), sells in premium (>50%)", "Filters out bad entries and maximizes edge"),
            ForexPattern(88, "EMA 9 / 21 Crossover", "Indicators", "BILATERAL", 84, "Fast moving average crosses slow moving average", "Core trend alignment trigger"),
            ForexPattern(90, "RSI Divergence", "Indicators", "BILATERAL", 88, "Price makes new extreme while RSI fails to confirm", "High-probability exhaustion reversal entry"),
            ForexPattern(93, "Bollinger Band Squeeze", "Indicators", "BILATERAL", 89, "Bands contract to lowest width in 20 periods", "Prepares for massive impending volatility drive"),
            ForexPattern(95, "ATR Volatility Dynamic Filter", "Indicators", "BILATERAL", 96, "Calculates exact barrier width from current market ATR", "Eliminates Deriv 'Invalid barrier' errors!")
        )
    }

    val filteredPatterns = masterPatterns.filter { p ->
        val matchesCategory = (selectedCategory == "All") || p.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true) ||
                p.confirmationRules.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PATTERN ENCYCLOPEDIA",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "102 Institutional & SMC Trading Patterns",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold)
                ) {
                    Text(
                        text = "${filteredPatterns.size} ACTIVE",
                        color = NeonGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pattern_search_input"),
                placeholder = { Text("Search pattern, SMC, BOS, FVG, Candlesticks...", color = TextSecondary, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = TerminalSurfaceBorder,
                    focusedContainerColor = TerminalCard,
                    unfocusedContainerColor = TerminalCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan else TerminalSurfaceElevated,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Patterns List
        items(filteredPatterns) { pat ->
            val biasColor = when (pat.bias) {
                "BULLISH" -> BullGreen
                "BEARISH" -> BearRed
                else -> NeonGold
            }
            val biasBg = when (pat.bias) {
                "BULLISH" -> BullGreenDim
                "BEARISH" -> BearRedDim
                else -> Color(0x22FFD166)
            }

            CyberHudCard(
                borderColor = TerminalSurfaceBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TerminalSurfaceElevated
                            ) {
                                Text(
                                    text = "#${pat.id}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pat.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = biasBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, biasColor)
                        ) {
                            Text(
                                text = pat.bias,
                                color = biasColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "• Rules: ${pat.confirmationRules}",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "• Bot Action: ${pat.botUsage}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EST. WIN PROBABILITY: ",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${pat.winProbability}%",
                                color = NeonGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            modifier = Modifier.clickable { onApplyPatternToBot(pat) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Feed Strategy", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
