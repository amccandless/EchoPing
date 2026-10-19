package com.booknerd.echopin.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booknerd.echopin.data.repository.ReminderRepo
import com.booknerd.echopin.domain.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepo
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addReminder(reminder: Reminder) = viewModelScope.launch { repository.add(reminder) }
    fun deleteReminder(reminder: Reminder) = viewModelScope.launch { repository.delete(reminder) }
    fun setActive(reminder: Reminder, active: Boolean) =
        viewModelScope.launch { repository.setActive(reminder, active) }
}