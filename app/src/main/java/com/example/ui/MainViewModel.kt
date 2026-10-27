package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiSwarmManager
import com.example.data.deriv.DerivWebSocketManager
import com.example.data.engine.HybridStrategyEngine
import com.example.data.model.AccountInfo
import com.example.data.model.AiModelInfo
import com.example.data.model.BotSettings
import com.example.data.model.Candle
import com.example.data.model.ContractType
import com.example.data.model.Direction
import com.example.data.model.ExecutedOrder
import com.example.data.model.Tick
import com.example.data.model.TradeSignal
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val derivManager = DerivWebSocketManager(viewModelScope)
    private val aiSwarm = AiSwarmManager()
    private val strategyEngine = HybridStrategyEngine()

    private val repository = TradingRepository(
        context = application.applicationContext,
        scope = viewModelScope,
        derivManager = derivManager,
        aiSwarm = aiSwarm,
        strategyEngine = strategyEngine
    )

    val account: StateFlow<AccountInfo> = repository.derivManager.account
    val settings: StateFlow<BotSettings> = repository.settings
    val orders: StateFlow<List<ExecutedOrder>> = repository.orders
    val currentSignal: StateFlow<TradeSignal?> = repository.currentSignal
    val scannedSignals: StateFlow<Map<String, TradeSignal>> = repository.scannedSignals
    val currentTicks: StateFlow<Map<String, List<Tick>>> = repository.derivManager.currentTicks
    val candles: StateFlow<Map<String, List<Candle>>> = repository.derivManager.candles
    val connectionStatus: StateFlow<String> = repository.derivManager.connectionStatus
    val digitTelemetry: StateFlow<Map<String, com.example.data.model.DigitTelemetry>> = repository.derivManager.digitTelemetry

    val aiModels: StateFlow<List<AiModelInfo>> = repository.aiSwarm.models
    val selectedAiModelId: StateFlow<String> = repository.aiSwarm.selectedModelId
    val isSwarmConsensusMode: StateFlow<Boolean> = repository.aiSwarm.isSwarmConsensusMode

    // Gemini Protocol Signal Processor
    private val geminiProcessor = com.example.data.ai.GeminiTradingSignalProcessor()
    private val _geminiSignal = kotlinx.coroutines.flow.MutableStateFlow<com.example.data.ai.GeminiProtocolSignal?>(null)
    val geminiSignal: StateFlow<com.example.data.ai.GeminiProtocolSignal?> = _geminiSignal

    private val _isGeminiAnalyzing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isGeminiAnalyzing: StateFlow<Boolean> = _isGeminiAnalyzing

    fun runGeminiAnalysis(symbolId: String? = null) {
        val symId = symbolId ?: repository.settings.value.selectedSymbolId
        val symbol = com.example.data.model.SymbolsDatabase.getSymbol(symId)
        val ticks = repository.derivManager.currentTicks.value[symId] ?: emptyList()
        val candles = repository.derivManager.candles.value[symId] ?: emptyList()
        val regime = repository.currentSignal.value?.marketRegime ?: com.example.data.model.MarketRegime.BULLISH_TREND

        viewModelScope.launch {
            _isGeminiAnalyzing.value = true
            try {
                val signal = geminiProcessor.processSignal(
                    symbol = symbol,
                    ticks = ticks,
                    candles = candles,
                    activeRegime = regime
                )
                _geminiSignal.value = signal
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error running Gemini analysis: ${e.message}")
            } finally {
                _isGeminiAnalyzing.value = false
            }
        }
    }

    fun switchAccount(toReal: Boolean) {
        repository.derivManager.switchAccount(toReal)
    }

    fun updateSettings(newSettings: BotSettings) {
        repository.updateSettings(newSettings)
    }

    fun selectSymbol(symbolId: String) {
        repository.updateSettings(repository.settings.value.copy(selectedSymbolId = symbolId))
    }

    fun selectAiModel(modelId: String) {
        repository.aiSwarm.selectModel(modelId)
        repository.updateSettings(repository.settings.value.copy(selectedAiModelId = modelId))
    }

    fun toggleSwarmConsensus(enabled: Boolean) {
        repository.aiSwarm.toggleSwarmConsensus(enabled)
    }

    fun saveModelApiKey(modelId: String, apiKey: String) {
        repository.aiSwarm.updateApiKey(modelId, apiKey)
    }

    fun toggleAutoBot(start: Boolean) {
        repository.toggleAutoBot(start)
    }

    fun executeTrade(
        contractType: ContractType = repository.settings.value.contractType,
        direction: Direction? = null,
        stake: Double = repository.settings.value.stakeAmount
    ) {
        repository.executeTradeNow(contractType, direction, stake)
    }

    fun reconnectDeriv(appId: String, apiToken: String) {
        repository.derivManager.connect(
            appId = appId,
            apiToken = apiToken,
            selectedSymbol = repository.settings.value.selectedSymbolId
        )
    }

    override fun onCleared() {
        super.onCleared()
        repository.derivManager.disconnect()
    }
}
