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
