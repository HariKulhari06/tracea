package com.hari.tracea.ui.screens.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hari.tracea.ui.TraceaServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("tracea_settings", Context.MODE_PRIVATE)
    private val store = TraceaServiceLocator.store

    private val _enableDebugger = MutableStateFlow(prefs.getBoolean("enable_debugger", true))
    val enableDebugger: StateFlow<Boolean> = _enableDebugger

    private val _floatingButton = MutableStateFlow(prefs.getBoolean("floating_button", true))
    val floatingButton: StateFlow<Boolean> = _floatingButton

    private val _captureRequests = MutableStateFlow(prefs.getBoolean("capture_requests", true))
    val captureRequests: StateFlow<Boolean> = _captureRequests

    private val _captureWebSocket = MutableStateFlow(prefs.getBoolean("capture_websocket", false))
    val captureWebSocket: StateFlow<Boolean> = _captureWebSocket

    private val _showRedactedPlaceholder = MutableStateFlow(prefs.getBoolean("show_redacted_placeholder", true))
    val showRedactedPlaceholder: StateFlow<Boolean> = _showRedactedPlaceholder

    private val _logCurl = MutableStateFlow(prefs.getBoolean("log_curl", true))
    val logCurl: StateFlow<Boolean> = _logCurl

    private val _showGetRequestBody = MutableStateFlow(prefs.getBoolean("show_get_body", false))
    val showGetRequestBody: StateFlow<Boolean> = _showGetRequestBody

    fun setEnableDebugger(enabled: Boolean) {
        _enableDebugger.value = enabled
        prefs.edit().putBoolean("enable_debugger", enabled).apply()
    }

    fun setFloatingButton(enabled: Boolean) {
        _floatingButton.value = enabled
        prefs.edit().putBoolean("floating_button", enabled).apply()
        com.hari.tracea.ui.overlay.FloatingButtonManager.setEnabled(enabled)
    }

    fun setCaptureRequests(enabled: Boolean) {
        _captureRequests.value = enabled
        prefs.edit().putBoolean("capture_requests", enabled).apply()
    }

    fun setCaptureWebSocket(enabled: Boolean) {
        _captureWebSocket.value = enabled
        prefs.edit().putBoolean("capture_websocket", enabled).apply()
    }

    fun setShowRedactedPlaceholder(enabled: Boolean) {
        _showRedactedPlaceholder.value = enabled
        prefs.edit().putBoolean("show_redacted_placeholder", enabled).apply()
    }

    fun setLogCurl(enabled: Boolean) {
        _logCurl.value = enabled
        prefs.edit().putBoolean("log_curl", enabled).apply()
    }

    fun setShowGetRequestBody(enabled: Boolean) {
        _showGetRequestBody.value = enabled
        prefs.edit().putBoolean("show_get_body", enabled).apply()
    }

    val eventCount: Int get() = 0  // TODO: bind to actual store
    val storageUsage: String get() = "0 KB"  // TODO: bind to actual store

    fun clearAllData() {
        viewModelScope.launch {
            store?.clear()
        }
    }
}
