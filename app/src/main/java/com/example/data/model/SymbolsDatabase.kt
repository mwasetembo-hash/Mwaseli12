package com.example.data.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

object SymbolsDatabase {
    val allSymbols = listOf(
        MarketSymbol("1HZ100V", "Volatility 100 (1s)", "Synthetic Volatility", 2, 0.15, 2.0, 0.35, true),
        MarketSymbol("1HZ75V", "Volatility 75 (1s)", "Synthetic Volatility", 2, 0.12, 1.8, 0.30, true),
        MarketSymbol("1HZ50V", "Volatility 50 (1s)", "Synthetic Volatility", 2, 0.10, 1.5, 0.25, true),
        MarketSymbol("1HZ25V", "Volatility 25 (1s)", "Synthetic Volatility", 2, 0.08, 1.2, 0.20, true),
        MarketSymbol("1HZ10V", "Volatility 10 (1s)", "Synthetic Volatility", 3, 0.05, 0.8, 0.15, true),
        MarketSymbol("R_100", "Volatility 100", "Continuous Volatility", 2, 0.20, 2.5, 0.40, true),
        MarketSymbol("R_75", "Volatility 75", "Continuous Volatility", 2, 0.15, 2.0, 0.35, true),
        MarketSymbol("R_50", "Volatility 50", "Continuous Volatility", 2, 0.12, 1.5, 0.25, true),
        MarketSymbol("R_25", "Volatility 25", "Continuous Volatility", 2, 0.10, 1.2, 0.20, true),
        MarketSymbol("R_10", "Volatility 10", "Continuous Volatility", 3, 0.05, 0.8, 0.15, true),
        MarketSymbol("CRASH500", "Crash 500 Index", "Crash / Boom", 2, 0.25, 3.0, 0.50, true),
        MarketSymbol("BOOM500", "Boom 500 Index", "Crash / Boom", 2, 0.25, 3.0, 0.50, true),
        MarketSymbol("STEPINDEX", "Step Index", "Step Indices", 1, 0.10, 1.0, 0.20, true),
        // Major Forex Pairs
        MarketSymbol("frxEURUSD", "EUR/USD", "Forex Major", 5, 0.0002, 0.0020, 0.0005, false),
        MarketSymbol("frxGBPUSD", "GBP/USD", "Forex Major", 5, 0.0003, 0.0025, 0.0006, false),
        MarketSymbol("frxUSDJPY", "USD/JPY", "Forex Major", 3, 0.02, 0.20, 0.05, false),
        MarketSymbol("frxAUDUSD", "AUD/USD", "Forex Major", 5, 0.0002, 0.0020, 0.0005, false),
        MarketSymbol("frxUSDCAD", "USD/CAD", "Forex Major", 5, 0.0002, 0.0020, 0.0005, false),
        MarketSymbol("frxUSDCHF", "USD/CHF", "Forex Major", 5, 0.0002, 0.0020, 0.0005, false),
        // Minor & Cross Forex Pairs
        MarketSymbol("frxEURGBP", "EUR/GBP", "Forex Minor", 5, 0.0002, 0.0020, 0.0005, false),
        MarketSymbol("frxEURJPY", "EUR/JPY", "Forex Minor", 3, 0.02, 0.20, 0.05, false),
        MarketSymbol("frxGBPJPY", "GBP/JPY", "Forex Minor", 3, 0.03, 0.25, 0.06, false),
        MarketSymbol("frxNZDUSD", "NZD/USD", "Forex Minor", 5, 0.0002, 0.0020, 0.0005, false),
        // Commodities
        MarketSymbol("frxXAUUSD", "Gold / USD", "Commodities", 2, 0.30, 3.0, 0.60, false)
    )

    fun getSymbol(symbolId: String): MarketSymbol {
        return allSymbols.find { it.id.equals(symbolId, ignoreCase = true) }
            ?: allSymbols.first()
    }

    /**
     * Resolves the exact barrier string for Deriv API.
     * Prevents:
     * 1) "Invalid barrier" (by strictly clamping to symbol-specific valid micro ranges)
     * 2) "This contract offers no return" on LOWER orders (by using negative offset for LOWER contracts)
     */
    fun computeSmartBarrier(
        symbolId: String,
        isHigher: Boolean,
        customOffset: Double? = null,
        recentVolatility: Double = 0.0
    ): String {
        val sym = getSymbol(symbolId)
        val baseOffset = customOffset ?: sym.defaultBarrierOffset
        
        // Dynamically adjust slightly with recent volatility (max 1.5x)
        val adjustedOffset = if (recentVolatility > 0.0) {
            val ratio = (recentVolatility / 0.5).coerceIn(0.8, 1.4)
            (baseOffset * ratio).coerceIn(sym.minBarrier, sym.maxBarrier)
        } else {
            baseOffset.coerceIn(sym.minBarrier, sym.maxBarrier)
        }

        val formattedOffset = String.format(Locale.US, "%.${sym.pipDecimals}f", adjustedOffset)

        return if (isHigher) {
            "+$formattedOffset"
        } else {
            // For LOWER contract, Deriv expects a negative offset to provide a realistic payout return
            "-$formattedOffset"
        }
    }
}
