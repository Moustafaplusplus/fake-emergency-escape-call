package com.fakeemergencyescape.call.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.local.DatabaseSeeder
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.FakeCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val scheduledCalls: List<FakeCall> = emptyList(),
    val pastCalls: List<FakeCall> = emptyList(),
    val isEmpty: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FakeCallRepository,
    databaseSeeder: DatabaseSeeder,
) : ViewModel() {

    init {
        viewModelScope.launch { databaseSeeder.seedTemplatesIfNeeded() }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeScheduled(),
        repository.observePast(),
    ) { scheduled, past ->
        HomeUiState(
            scheduledCalls = scheduled,
            pastCalls = past,
            isEmpty = scheduled.isEmpty() && past.isEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun cancelCall(id: String) {
        viewModelScope.launch { repository.cancelCall(id) }
    }

    fun deleteCall(id: String) {
        viewModelScope.launch { repository.deleteCall(id) }
    }

    fun statusLabel(status: CallStatus): String = when (status) {
        CallStatus.SCHEDULED -> "Scheduled"
        CallStatus.COMPLETED -> "Completed"
        CallStatus.DECLINED -> "Declined"
        CallStatus.MISSED -> "Missed"
        CallStatus.CANCELLED -> "Cancelled"
        else -> status.name
    }
}
