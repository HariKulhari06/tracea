package com.hari.tracea.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.core.util.DurationFormatter
import com.hari.tracea.ui.TraceaServiceLocator
import com.hari.tracea.ui.screens.network.StatusFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.hari.tracea.core.util.SizeFormatter
import kotlinx.coroutines.launch

data class TimelineStats(
    val totalRequests: Int = 0,
    val formattedDuration: String = "00:00:00",
    val slowestFormatted: String = "0 ms",
    val errorCount: Int = 0,
    val totalDataTransfer: String = "0 B"
)

data class SessionStats(
    val totalRequests: Int = 0,
    val formattedDuration: String = "00:00:00",
    val slowestFormatted: String = "N/A"
)

class TimelineViewModel : ViewModel() {

    private val store = TraceaServiceLocator.store

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeFilter = MutableStateFlow(StatusFilter.ALL)
    val activeFilter: StateFlow<StatusFilter> = _activeFilter

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible

    val events: StateFlow<List<NetworkEvent>> = combine(
        store?.getAll() ?: MutableStateFlow(emptyList()),
        _searchQuery,
        _activeFilter
    ) { allEvents, query, filter ->
        allEvents.filter { event ->
            val matchesQuery = query.isBlank() ||
                    event.url.contains(query, ignoreCase = true) ||
                    event.host.contains(query, ignoreCase = true) ||
                    event.method.name.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                StatusFilter.ALL -> true
                StatusFilter.SUCCESS, StatusFilter.SUCCESS_2XX -> (event.statusCode ?: 0) in 200..299
                StatusFilter.REDIRECT_3XX -> (event.statusCode ?: 0) in 300..399
                StatusFilter.CLIENT_ERROR_4XX -> (event.statusCode ?: 0) in 400..499
                StatusFilter.SERVER_ERROR_5XX -> (event.statusCode ?: 0) in 500..599
                StatusFilter.ERRORS -> (event.statusCode ?: 0) >= 400 || event.error != null
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sessionStats: StateFlow<SessionStats> = (store?.getAll() ?: MutableStateFlow(emptyList<NetworkEvent>())).map { allEvents ->
        if (allEvents.isEmpty()) {
            SessionStats()
        } else {
            val total = allEvents.size
            val minTime = allEvents.minOf { it.timestamp }
            val maxTime = System.currentTimeMillis()
            val diffSec = (maxTime - minTime) / 1000

            val hours = diffSec / 3600
            val minutes = (diffSec % 3600) / 60
            val seconds = diffSec % 60
            val durationStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            val slowestMs = allEvents.mapNotNull { it.timing.totalMs }.maxOrNull()
            val slowestStr = slowestMs?.let { DurationFormatter.format(it) } ?: "N/A"

            SessionStats(totalRequests = total, formattedDuration = durationStr, slowestFormatted = slowestStr)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, SessionStats())

    val timelineStats: StateFlow<TimelineStats> = (store?.getAll() ?: MutableStateFlow(emptyList())).map { events ->
        if (events.isEmpty()) {
            TimelineStats()
        } else {
            val total = events.size
            val first = events.minOf { it.timestamp }
            val last = events.maxOf { it.timing.endTimestamp ?: (it.timestamp + (it.timing.totalMs ?: 0L)) }
            val diff = maxOf(0L, last - first)
            val hours = diff / 3600000
            val minutes = (diff % 3600000) / 60000
            val seconds = (diff % 60000) / 1000
            val durationStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            val slowestMs = events.mapNotNull { it.timing.totalMs }.maxOrNull()
            val slowestStr = slowestMs?.let { DurationFormatter.format(it) } ?: "0 ms"

            val errors = events.count { event ->
                val code = event.statusCode ?: 0
                code in 400..599 || event.error != null
            }

            val totalBytes = events.sumOf { it.requestSize + it.responseSize }
            val dataStr = SizeFormatter.format(totalBytes)

            TimelineStats(
                totalRequests = total,
                formattedDuration = durationStr,
                slowestFormatted = slowestStr,
                errorCount = errors,
                totalDataTransfer = dataStr
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, TimelineStats())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: StatusFilter) {
        _activeFilter.value = filter
    }

    fun toggleSearch() {
        _isSearchVisible.value = !_isSearchVisible.value
        if (!_isSearchVisible.value) _searchQuery.value = ""
    }

    fun clearAll() {
        viewModelScope.launch {
            store?.clear()
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            store?.deleteSession(sessionId)
        }
    }

    fun exportSessionHar(context: android.content.Context, sessionId: String, sessionName: String) {
        viewModelScope.launch {
            val sessionEvents = store?.getSessionEvents(sessionId) ?: emptyList()
            com.hari.tracea.ui.util.HarSharer.shareSessionHar(context, sessionName, sessionEvents)
        }
    }
}
