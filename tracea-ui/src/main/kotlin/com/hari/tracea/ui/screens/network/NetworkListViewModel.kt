package com.hari.tracea.ui.screens.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hari.tracea.core.model.NetworkEvent
import com.hari.tracea.ui.TraceaServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NetworkListViewModel : ViewModel() {

    private val store = TraceaServiceLocator.store

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeFilter = MutableStateFlow(StatusFilter.ALL)
    val activeFilter: StateFlow<StatusFilter> = _activeFilter

    private val _activeMethodFilter = MutableStateFlow(MethodFilter.ALL)
    val activeMethodFilter: StateFlow<MethodFilter> = _activeMethodFilter

    private val _isSearchVisible = MutableStateFlow(true)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible

    // Persistent domain expansion: keys are "$sessionId/$domain". Empty by default => all collapsed.
    private val _expandedDomains = MutableStateFlow<Set<String>>(emptySet())
    val expandedDomains: StateFlow<Set<String>> = _expandedDomains

    // Persistent session collapse: set of collapsed session IDs. Empty by default => all sessions open.
    private val _collapsedSessions = MutableStateFlow<Set<String>>(emptySet())
    val collapsedSessions: StateFlow<Set<String>> = _collapsedSessions

    // Scroll state preservation across detail navigation
    var scrollIndex: Int = 0
    var scrollOffset: Int = 0

    fun toggleDomainExpansion(sessionId: String, domain: String) {
        val key = "$sessionId/$domain"
        val current = _expandedDomains.value
        _expandedDomains.value = if (current.contains(key)) {
            current - key
        } else {
            current + key
        }
    }

    fun toggleSessionCollapse(sessionId: String) {
        val current = _collapsedSessions.value
        _collapsedSessions.value = if (current.contains(sessionId)) {
            current - sessionId
        } else {
            current + sessionId
        }
    }

    val events: StateFlow<List<NetworkEvent>> = combine(
        store?.getAll() ?: MutableStateFlow(emptyList()),
        _searchQuery,
        _activeFilter,
        _activeMethodFilter
    ) { allEvents, query, filter, methodFilter ->
        allEvents.filter { event ->
            // Path/URL/host contains search
            val matchesQuery = query.isBlank() ||
                event.url.contains(query, ignoreCase = true) ||
                (event.path?.contains(query, ignoreCase = true) == true) ||
                (event.host?.contains(query, ignoreCase = true) == true)

            // Filter by status category
            val matchesFilter = when (filter) {
                StatusFilter.ALL -> true
                StatusFilter.SUCCESS, StatusFilter.SUCCESS_2XX -> (event.statusCode ?: 0) in 200..299
                StatusFilter.REDIRECT_3XX -> (event.statusCode ?: 0) in 300..399
                StatusFilter.CLIENT_ERROR_4XX -> (event.statusCode ?: 0) in 400..499
                StatusFilter.SERVER_ERROR_5XX -> (event.statusCode ?: 0) in 500..599
                StatusFilter.ERRORS -> (event.statusCode ?: 0) >= 400 || event.error != null
            }

            // Filter by HTTP method
            val matchesMethod = when (methodFilter) {
                MethodFilter.ALL -> true
                else -> event.method == methodFilter.method
            }

            matchesQuery && matchesFilter && matchesMethod
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalCount: StateFlow<Int> = (store?.getAll() ?: MutableStateFlow(emptyList()))
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: StatusFilter) {
        _activeFilter.value = filter
    }

    fun setMethodFilter(filter: MethodFilter) {
        _activeMethodFilter.value = filter
    }

    fun resetFilters() {
        _activeFilter.value = StatusFilter.ALL
        _activeMethodFilter.value = MethodFilter.ALL
    }

    fun toggleSearch() {
        _isSearchVisible.value = !_isSearchVisible.value
        if (!_isSearchVisible.value) {
            _searchQuery.value = ""
        }
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

data class DomainGroup(
    val domain: String,
    val events: List<NetworkEvent>
) {
    val requestCount: Int get() = events.size
    val errorCount: Int get() = events.count { (it.statusCode ?: 0) >= 400 || it.error != null }
}

data class SessionGroup(
    val sessionId: String,
    val sessionName: String,
    val events: List<NetworkEvent>,
    val domainGroups: List<DomainGroup>
)

fun groupEventsByDomain(events: List<NetworkEvent>): List<DomainGroup> {
    val domainDict = events.groupBy { event ->
        val h = event.host?.trim()
        if (!h.isNullOrEmpty()) {
            h
        } else {
            try {
                java.net.URI(event.url).host?.takeIf { it.isNotBlank() } ?: "Unknown Host"
            } catch (_: Exception) {
                "Unknown Host"
            }
        }
    }
    return domainDict.map { (domain, evts) ->
        DomainGroup(domain = domain, events = evts)
    }.sortedBy { it.domain.lowercase() }
}

