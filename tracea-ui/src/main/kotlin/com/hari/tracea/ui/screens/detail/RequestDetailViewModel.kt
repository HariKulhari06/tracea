package com.hari.tracea.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.CurlGenerator
import com.hari.tracea.ui.TraceaServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class DetailTab(val label: String) {
    OVERVIEW("Overview"),
    REQUEST("Request"),
    RESPONSE("Response"),
    TIMING("Timing")
}

enum class BodyDisplayMode { RAW, PRETTY }

class RequestDetailViewModel : ViewModel() {

    private val store = TraceaServiceLocator.store

    private val _event = MutableStateFlow<NetworkEvent?>(null)
    val event: StateFlow<NetworkEvent?> = _event

    private val _selectedTab = MutableStateFlow(DetailTab.OVERVIEW)
    val selectedTab: StateFlow<DetailTab> = _selectedTab

    private val _responseBodyMode = MutableStateFlow(BodyDisplayMode.PRETTY)
    val responseBodyMode: StateFlow<BodyDisplayMode> = _responseBodyMode

    private val _requestBodyMode = MutableStateFlow(BodyDisplayMode.PRETTY)
    val requestBodyMode: StateFlow<BodyDisplayMode> = _requestBodyMode

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _event.value = store?.get(eventId)
        }
    }

    fun selectTab(tab: DetailTab) {
        _selectedTab.value = tab
    }

    fun setResponseBodyMode(mode: BodyDisplayMode) {
        _responseBodyMode.value = mode
    }

    fun setRequestBodyMode(mode: BodyDisplayMode) {
        _requestBodyMode.value = mode
    }

    fun getCurlCommand(): String {
        val currentEvent = _event.value ?: return ""
        return CurlGenerator.generate(currentEvent)
    }
}
