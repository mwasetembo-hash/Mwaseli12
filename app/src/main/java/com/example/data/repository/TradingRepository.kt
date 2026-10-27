package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.ai.AiSwarmManager
import com.example.data.deriv.DerivWebSocketManager
import com.example.data.engine.HybridStrategyEngine
import com.example.data.model.BotSettings
import com.example.data.model.ContractType
import com.example.data.model.Direction
import com.example.data.model.ExecutedOrder
import com.example.data.model.MarketSymbol
import com.example.data.model.SymbolsDatabase
import com.example.data.model.TradeSignal
import com.example.data.model.TradingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class TradingRepository(
    private val context: Context,
    private val scope: CoroutineScope,
    val derivManager: DerivWebSocketManager,
    val aiSwarm: AiSwarmManager,
    val strategyEngine: HybridStrategyEngine
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mwase_deriv_hybrid_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<BotSettings> = _settings.asStateFlow()

    private val _orders = MutableStateFlow<List<ExecutedOrder>>(emptyList())
    val orders: StateFlow<List<ExecutedOrder>> = _orders.asStateFlow()

    private val _currentSignal = MutableStateFlow<TradeSignal?>(null)
    val currentSignal: StateFlow<TradeSignal?> = _currentSignal.asStateFlow()

    private val _scannedSignals = MutableStateFlow<Map<String, TradeSignal>>(emptyMap())
    val scannedSignals: StateFlow<Map<String, TradeSignal>> = _scannedSignals.asStateFlow()

    private var autoBotJob: Job? = null
    private var scannerJob: Job? = null
    private var currentMartingaleStep = 0
    private var currentActiveStake = 5.0

    init {
        // Collect executed order updates from Deriv WS
        scope.launch {
            derivManager.orderExecutedEvents.collect { order ->
                updateOrderHistory(order)
            }
        }

        // Start real-time multi-market scanner loop
        startScannerLoop()

        // Auto-connect Deriv WebSocket with stored App ID & Token
        derivManager.connect(
            _settings.value.derivAppId,
            _settings.value.derivApiToken,
            _settings.value.selectedSymbolId
        )
    }

    private fun loadSettings(): BotSettings {
        val rawAppId = prefs.getString("deriv_app_id", "1089") ?: "1089"
        val cleanAppId = if (rawAppId.all { it.isDigit() } && rawAppId.isNotBlank()) rawAppId else "1089"
        return BotSettings(
            derivAppId = cleanAppId,
            derivApiToken = prefs.getString("deriv_api_token", "pat_e2d6096c3495c0fad8c2e0693e22742a022eb390a7661d5b074137a2847828cd") ?: "pat_e2d6096c3495c0fad8c2e0693e22742a022eb390a7661d5b074137a2847828cd",
            selectedAiModelId = prefs.getString("selected_ai_model", "DeepSeek-V4-Flash") ?: "DeepSeek-V4-Flash",
            contractType = ContractType.valueOf(prefs.getString("contract_type", ContractType.ONLY_UPS.name) ?: ContractType.ONLY_UPS.name),
            contractDirection = prefs.getString("contract_direction", "BOTH_AUTO") ?: "BOTH_AUTO",
            tradingMode = TradingMode.valueOf(prefs.getString("trading_mode", TradingMode.AUTO_DEMO.name) ?: TradingMode.AUTO_DEMO.name),
            selectedSymbolId = prefs.getString("selected_symbol", "1HZ100V") ?: "1HZ100V",
            stakeAmount = prefs.getFloat("stake_amount", 5.0f).toDouble(),
            durationTicks = prefs.getInt("duration_ticks", 5),
            barrierOffset = prefs.getFloat("barrier_offset", 0.35f).toDouble(),
            autoSmartBarrier = prefs.getBoolean("auto_smart_barrier", true),
            maxDailyLoss = prefs.getFloat("max_daily_loss", 50.0f).toDouble(),
            dailyProfitTarget = prefs.getFloat("daily_profit_target", 100.0f).toDouble(),
            riskPerTradePercent = prefs.getFloat("risk_per_trade_percent", 1.0f).toDouble(),
            martingaleMultiplier = prefs.getFloat("martingale_multiplier", 2.0f).toDouble(),
            maxMartingaleSteps = prefs.getInt("max_martingale_steps", 3),
            isBotRunning = false,
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            hapticsEnabled = prefs.getBoolean("haptics_enabled", true)
        )
    }

    fun updateSettings(newSettings: BotSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("deriv_app_id", newSettings.derivAppId)
            putString("deriv_api_token", newSettings.derivApiToken)
            putString("selected_ai_model", newSettings.selectedAiModelId)
            putString("contract_type", newSettings.contractType.name)
            putString("contract_direction", newSettings.contractDirection)
            putString("trading_mode", newSettings.tradingMode.name)
            putString("selected_symbol", newSettings.selectedSymbolId)
            putFloat("stake_amount", newSettings.stakeAmount.toFloat())
            putInt("duration_ticks", newSettings.durationTicks)
            putFloat("barrier_offset", newSettings.barrierOffset.toFloat())
            putBoolean("auto_smart_barrier", newSettings.autoSmartBarrier)
            putFloat("max_daily_loss", newSettings.maxDailyLoss.toFloat())
            putFloat("daily_profit_target", newSettings.dailyProfitTarget.toFloat())
            putFloat("risk_per_trade_percent", newSettings.riskPerTradePercent.toFloat())
            putFloat("martingale_multiplier", newSettings.martingaleMultiplier.toFloat())
            putInt("max_martingale_steps", newSettings.maxMartingaleSteps)
            putBoolean("sound_enabled", newSettings.soundEnabled)
            putBoolean("hapticsEnabled", newSettings.hapticsEnabled)
            apply()
        }

        // If symbol changed, subscribe WS
        derivManager.subscribeTicks(newSettings.selectedSymbolId)
    }

    private fun startScannerLoop() {
        scannerJob?.cancel()
        scannerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val tickMap = derivManager.currentTicks.value
                val candleMap = derivManager.candles.value
                val currentSet = _settings.value

                val newScanned = mutableMapOf<String, TradeSignal>()
                SymbolsDatabase.allSymbols.forEach { sym ->
                    val ticks = tickMap[sym.id] ?: emptyList()
                    val candles = candleMap[sym.id] ?: emptyList()
                    val sig = strategyEngine.analyzeMarket(
                        symbol = sym,
                        ticks = ticks,
                        candles = candles,
                        targetContractType = currentSet.contractType,
                        directionMode = currentSet.contractDirection
                    )
                    newScanned[sym.id] = sig
                }
                _scannedSignals.value = newScanned

                // Update selected symbol active signal
                val activeSig = newScanned[currentSet.selectedSymbolId]
                if (activeSig != null) {
                    _currentSignal.value = activeSig
                }

                delay(1500)
            }
        }
    }

    fun toggleAutoBot(start: Boolean) {
        _settings.value = _settings.value.copy(isBotRunning = start)
        if (start) {
            currentMartingaleStep = 0
            currentActiveStake = _settings.value.stakeAmount
            vibrateDevice(true)
            startAutoBotLoop()
        } else {
            autoBotJob?.cancel()
            autoBotJob = null
            vibrateDevice(false)
        }
    }

    private fun startAutoBotLoop() {
        autoBotJob?.cancel()
        autoBotJob = scope.launch(Dispatchers.Default) {
            while (isActive && _settings.value.isBotRunning) {
                delay(6000) // Bot evaluates market every 6 seconds

                // 1. Capital Protection Risk Gate
                val totalProfit = _orders.value.sumOf { it.profit }
                if (totalProfit <= -_settings.value.maxDailyLoss) {
                    toggleAutoBot(false)
                    break
                }
                if (totalProfit >= _settings.value.dailyProfitTarget) {
                    toggleAutoBot(false)
                    break
                }

                // 2. Evaluate Signal
                val sym = SymbolsDatabase.getSymbol(_settings.value.selectedSymbolId)
                val ticks = derivManager.currentTicks.value[sym.id] ?: emptyList()
                val candles = derivManager.candles.value[sym.id] ?: emptyList()

                val sig = strategyEngine.analyzeMarket(
                    symbol = sym,
                    ticks = ticks,
                    candles = candles,
                    targetContractType = _settings.value.contractType,
                    directionMode = _settings.value.contractDirection
                )

                // 3. AI Swarm confirmation
                val (_, swarmConfidence) = aiSwarm.evaluateMarketDecision(
                    symbol = sym.id,
                    currentQuote = sig.entryPrice,
                    direction = sig.direction,
                    confidence = sig.confidenceScore,
                    regime = sig.marketRegime,
                    activeModelId = _settings.value.selectedAiModelId
                )

                // 4. Execute if high confidence
                if (swarmConfidence >= 84 && sig.direction != Direction.NEUTRAL) {
                    executeTradeNow(
                        contractType = _settings.value.contractType,
                        direction = sig.direction,
                        stake = currentActiveStake
                    )
                }
            }
        }
    }

    fun executeTradeNow(
        contractType: ContractType = _settings.value.contractType,
        direction: Direction? = null,
        stake: Double = _settings.value.stakeAmount
    ) {
        val targetDirection = direction ?: _currentSignal.value?.direction ?: Direction.UP
        vibrateDevice(true)

        derivManager.executeOrder(
            symbol = _settings.value.selectedSymbolId,
            contractType = contractType,
            direction = targetDirection,
            stake = stake,
            durationTicks = _settings.value.durationTicks,
            barrierOffset = _settings.value.barrierOffset
        ) { completedOrder ->
            // Manage Martingale on bot
            if (_settings.value.isBotRunning) {
                if (completedOrder.status == "WON") {
                    currentMartingaleStep = 0
                    currentActiveStake = _settings.value.stakeAmount
                } else if (completedOrder.status == "LOST") {
                    if (currentMartingaleStep < _settings.value.maxMartingaleSteps) {
                        currentMartingaleStep++
                        currentActiveStake *= _settings.value.martingaleMultiplier
                    } else {
                        currentMartingaleStep = 0
                        currentActiveStake = _settings.value.stakeAmount
                    }
                }
            }
        }
    }

    private fun updateOrderHistory(order: ExecutedOrder) {
        val list = _orders.value.toMutableList()
        val index = list.indexOfFirst { it.orderId == order.orderId }
        if (index >= 0) {
            list[index] = order
        } else {
            list.add(0, order)
        }
        if (list.size > 100) {
            list.removeAt(list.lastIndex)
        }
        _orders.value = list
    }

    private fun vibrateDevice(isLong: Boolean) {
        if (!_settings.value.hapticsEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = if (isLong) 80L else 35L
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(50)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration error on some devices
        }
    }
}
