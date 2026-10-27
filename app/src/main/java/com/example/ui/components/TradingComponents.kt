package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.Direction
import com.example.data.model.Tick
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedDim
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenDim
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurfaceBorder
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CyberHudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = TerminalSurfaceBorder,
    glowColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalCard)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .drawBehind {
                if (glowColor != Color.Transparent) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(glowColor.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
                }
            }
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun LiveTradingChart(
    modifier: Modifier = Modifier,
    candles: List<Candle>,
    ticks: List<Tick>,
    currentPrice: Double,
    barrierStr: String = "+0.35"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var touchedPrice by remember { mutableStateOf<Double?>(null) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF060A13))
            .border(1.dp, TerminalSurfaceBorder, RoundedCornerShape(14.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .pointerInput(candles, ticks) {
                    detectTapGestures(
                        onPress = { offset ->
                            // Touch to inspect
                            touchedPrice = currentPrice
                            tryAwaitRelease()
                            touchedPrice = null
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height

            // Calculate min/max range
            val prices = if (candles.isNotEmpty()) {
                candles.flatMap { listOf(it.high, it.low) }
            } else if (ticks.isNotEmpty()) {
                ticks.map { it.quote }
            } else {
                listOf(currentPrice - 1.0, currentPrice + 1.0)
            }

            val rawMin = prices.minOrNull() ?: (currentPrice - 1.0)
            val rawMax = prices.maxOrNull() ?: (currentPrice + 1.0)
            val paddingVal = max(0.2, (rawMax - rawMin) * 0.15)
            val minY = rawMin - paddingVal
            val maxY = rawMax + paddingVal
            val rangeY = max(0.0001, maxY - minY)

            fun getY(price: Double): Float {
                return (h - ((price - minY) / rangeY * h)).toFloat()
            }

            // Draw Subtle Grid lines
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val gridY = (h / gridSteps) * i
                val gridPrice = maxY - (rangeY / gridSteps) * i
                drawLine(
                    color = Color(0x1A00E5FF),
                    start = Offset(0f, gridY),
                    end = Offset(w, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            // Draw Candlesticks if available
            val displayCandles = candles.takeLast(30)
            if (displayCandles.size >= 2) {
                val candleWidth = (w / displayCandles.size) * 0.65f
                val step = w / displayCandles.size

                val maPath = Path()
                displayCandles.forEachIndexed { idx, c ->
                    val cx = idx * step + (step / 2f)
                    val openY = getY(c.open)
                    val closeY = getY(c.close)
                    val highY = getY(c.high)
                    val lowY = getY(c.low)

                    val isBullish = c.close >= c.open
                    val candleColor = if (isBullish) BullGreen else BearRed

                    // Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(cx, highY),
                        end = Offset(cx, lowY),
                        strokeWidth = 1.8f
                    )

                    // Body
                    val top = min(openY, closeY)
                    val bodyH = max(2.5f, kotlin.math.abs(closeY - openY))
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(cx - (candleWidth / 2f), top),
                        size = Size(candleWidth, bodyH)
                    )

                    // Moving Average Line
                    val avg = (c.open + c.close + c.high + c.low) / 4.0
                    val maY = getY(avg)
                    if (idx == 0) maPath.moveTo(cx, maY) else maPath.lineTo(cx, maY)
                }

                // Render neon moving average line
                drawPath(
                    path = maPath,
                    color = NeonCyan.copy(alpha = 0.85f),
                    style = Stroke(width = 2.5f)
                )
            } else {
                // Tick line fallback
                val displayTicks = ticks.takeLast(40)
                if (displayTicks.size >= 2) {
                    val tickStep = w / (displayTicks.size - 1)
                    val tickPath = Path()
                    displayTicks.forEachIndexed { i, t ->
                        val tx = i * tickStep
                        val ty = getY(t.quote)
                        if (i == 0) tickPath.moveTo(tx, ty) else tickPath.lineTo(tx, ty)
                    }
                    drawPath(
                        path = tickPath,
                        color = NeonCyan,
                        style = Stroke(width = 3f)
                    )
                }
            }

            // Current Price horizontal laser line
            val currentY = getY(currentPrice)
            drawLine(
                color = NeonCyan.copy(alpha = pulseAlpha),
                start = Offset(0f, currentY),
                end = Offset(w - 70f, currentY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
            )

            // Live glowing dot at current price
            drawCircle(
                color = NeonCyan.copy(alpha = pulseAlpha),
                radius = 7.dp.toPx(),
                center = Offset(w - 72f, currentY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(w - 72f, currentY)
            )
        }

        // Price Badge overlay on right edge
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            shape = RoundedCornerShape(6.dp),
            color = NeonCyan.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
        ) {
            Text(
                text = String.format(Locale.US, "%.2f", currentPrice),
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Barrier Indicator badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xCC0E1626)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BullGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SMART BARRIER: $barrierStr",
                    color = NeonGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SignalBadge(
    direction: Direction,
    confidence: Int,
    modifier: Modifier = Modifier
) {
    val isBull = direction == Direction.UP
    val badgeBg = if (isBull) BullGreenDim else BearRedDim
    val borderCol = if (isBull) BullGreen else BearRed
    val textColor = if (isBull) BullGreen else BearRed

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isBull) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = direction.label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$confidence%",
                    color = NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun OneButtonExecute(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isPrimary: Boolean = true,
    isBullish: Boolean = true,
    onClick: () -> Unit
) {
    val bgBrush = if (isPrimary) {
        if (isBullish) {
            Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF00E676)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xFFD50000), Color(0xFFFF1744)))
        }
    } else {
        Brush.horizontalGradient(listOf(TerminalSurfaceElevated, TerminalCard))
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .testTag("one_button_execute_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .border(
                    1.dp,
                    if (isBullish) BullGreen.copy(alpha = 0.6f) else BearRed.copy(alpha = 0.6f),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (isPrimary) Color.Black else NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        color = if (isPrimary) Color.Black else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = subtitle,
                        color = if (isPrimary) Color.Black.copy(alpha = 0.8f) else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun GeminiProtocolSignalCard(
    signal: com.example.data.ai.GeminiProtocolSignal,
    modifier: Modifier = Modifier,
    onExecute: () -> Unit
) {
    val isBuy = signal.direction.equals("BUY", ignoreCase = true)
    val accentColor = if (isBuy) BullGreen else BearRed

    CyberHudCard(
        modifier = modifier,
        borderColor = accentColor.copy(alpha = 0.8f),
        glowColor = accentColor
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GEMINI AI PROTOCOL SIGNAL",
                        color = NeonGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                ) {
                    Text(
                        text = "${signal.confidenceScore}% CONFIDENCE",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Pair & Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = signal.currencyPair,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = signal.marketRegime,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBuy) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isBuy) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = signal.direction.uppercase(),
                            color = if (isBuy) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Entry / SL / TP / R:R Grid
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TerminalSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ENTRY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(java.util.Locale.US, "%.4f", signal.entryPrice),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column {
                        Text("STOP LOSS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(java.util.Locale.US, "%.4f", signal.stopLoss),
                            color = BearRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column {
                        Text("TAKE PROFIT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(java.util.Locale.US, "%.4f", signal.takeProfit),
                            color = BullGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("RISK/REWARD", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = signal.riskReward,
                            color = NeonGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Agreement & Reason
            Text(
                text = signal.strategyAgreement,
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = signal.reason,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // One-click Execute Button
            Button(
                onClick = onExecute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("gemini_execute_signal_button"),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isBuy) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXECUTE PROTOCOL ORDER (${signal.direction.uppercase()})",
                        color = if (isBuy) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

