package com.example.data.model

enum class ContractType(val displayName: String, val derivCode: String, val category: String) {
    ONLY_UPS("Only Ups", "RUNHIGH", "Only Ups / Downs"),
    ONLY_DOWNS("Only Downs", "RUNLOW", "Only Ups / Downs"),
    BOTH_AUTO("Both (Auto Detect)", "BOTH_AUTO", "Only Ups / Downs"),
    RISE_FALL("Rise / Fall", "CALL_PUT", "Core"),
    HIGHER_LOWER("Higher / Lower", "HIGH_LOW", "Barriers"),
    OVER_UNDER("Over / Under", "DIGIT_OVER_UNDER", "Digits"),
    MATCHES_DIFFERS("Matches / Differs", "DIGIT_MATCH_DIFF", "Digits"),
    EVEN_ODD("Even / Odd", "DIGIT_EVEN_ODD", "Digits"),
    TOUCH_NO_TOUCH("Touch / No Touch", "TOUCH_NOTOUCH", "Barriers")
}

enum class Direction(val label: String, val colorHex: Long) {
    UP("CALL / ONLY UPS", 0xFF00E676),
    DOWN("PUT / ONLY DOWNS", 0xFFFF1744),
    NEUTRAL("WAITING / NEUTRAL", 0xFF90A4AE)
}

enum class TradingMode(val displayName: String) {
    SIGNALS_ONLY("Signals Only"),
    AUTO_DEMO("Automatic Demo Trading"),
    CONFIRM_EACH("Ask to Confirm Each Trade")
}

enum class MarketRegime(val title: String, val description: String) {
    BULLISH_TREND("Bullish Strong Trend", "Confluence of EMA crossovers, expanding volume and bullish BOS"),
    BEARISH_TREND("Bearish Strong Trend", "Persistent lower lows, CHoCH breakdown and high-velocity selloff"),
    RANGING("Consolidation / Range", "Oscillating between support & resistance with neutral momentum"),
    HIGH_VOLATILITY("High Volatility / Expansion", "ATR spike with liquidity sweeps and fast micro-tick action"),
    SQUEEZE("Volatility Squeeze", "Bollinger Band compression, impending explosive breakout")
}

data class MarketSymbol(
    val id: String,
    val name: String,
    val market: String, // Synthetic, Forex, Commodities
    val pipDecimals: Int = 2,
    val minBarrier: Double = 0.10,
    val maxBarrier: Double = 5.0,
    val defaultBarrierOffset: Double = 0.35,
    val isVolatilityIndex: Boolean = true
)

data class Tick(
    val symbol: String,
    val quote: Double,
    val epoch: Long,
    val pipChange: Double = 0.0,
    val lastDigit: Int = (quote.toString().takeLast(1).toIntOrNull() ?: 0)
)

data class Candle(
    val epoch: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 1.0
)

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val specialty: String,
    val accuracyScore: Double, // e.g. 98.4
    val latencyMs: Int,
    val isRecommended: Boolean = false,
    var apiKey: String = ""
)

data class TradeSignal(
    val id: String,
    val symbol: String,
    val contractType: ContractType,
    val direction: Direction,
    val entryPrice: Double,
    val barrier: String,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskReward: String,
    val confidenceScore: Int, // 0-100%
    val strategyAgreement: String, // "5/5 Strategies Agree"
    val marketRegime: MarketRegime,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExecutedOrder(
    val orderId: String,
    val contractId: String,
    val symbol: String,
    val contractType: ContractType,
    val direction: Direction,
    val stake: Double,
    val payout: Double,
    val profit: Double,
    val status: String, // "WON", "LOST", "RUNNING", "PENDING", "REJECTED"
    val barrier: String,
    val entrySpot: Double,
    val exitSpot: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = ""
)

data class BotSettings(
    val derivAppId: String = "1089",
    val derivApiToken: String = "pat_e2d6096c3495c0fad8c2e0693e22742a022eb390a7661d5b074137a2847828cd",
    val selectedAiModelId: String = "DeepSeek-V4-Flash",
    val contractType: ContractType = ContractType.ONLY_UPS,
    val contractDirection: String = "BOTH_AUTO", // ONLY_UPS, ONLY_DOWNS, BOTH_AUTO
    val tradingMode: TradingMode = TradingMode.AUTO_DEMO,
    val selectedSymbolId: String = "1HZ100V",
    val stakeAmount: Double = 5.0,
    val durationTicks: Int = 5,
    val barrierOffset: Double = 0.35,
    val autoSmartBarrier: Boolean = true,
    val maxDailyLoss: Double = 50.0,
    val dailyProfitTarget: Double = 100.0,
    val riskPerTradePercent: Double = 1.0,
    val martingaleMultiplier: Double = 2.0,
    val maxMartingaleSteps: Int = 3,
    val isBotRunning: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

data class AccountInfo(
    val loginId: String = "VRTC1002345",
    val balance: Double = 10000.0,
    val currency: String = "USD",
    val isDemo: Boolean = true,
    val email: String = "mwaserobison@gmail.com",
    val isConnected: Boolean = true,
    val lastPingMs: Long = 24,
    // Dual Account Balances
    val demoLoginId: String = "VRTC1002345",
    val demoBalance: Double = 10000.0,
    val realLoginId: String = "CR3458921",
    val realBalance: Double = 1452.80,
    val activeAccountType: String = "DEMO" // "DEMO" or "REAL"
)

data class DigitTelemetry(
    val lastDigits: List<Int> = emptyList(),
    val digitFrequency: List<Double> = List(10) { 10.0 },
    val hotDigit: Int = 7,
    val coldDigit: Int = 2,
    val evenPercentage: Double = 50.0,
    val oddPercentage: Double = 50.0,
    val overPercentage: Double = 50.0,
    val underPercentage: Double = 50.0
)
