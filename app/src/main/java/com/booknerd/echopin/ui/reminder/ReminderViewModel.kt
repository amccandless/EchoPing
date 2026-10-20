package com.booknerd.echopin.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booknerd.echopin.data.geocoding.GeoResult
import com.booknerd.echopin.data.geocoding.GeocodingService
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
    private val reminderRepo: ReminderRepo,
    private val geocodingService: GeocodingService
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = reminderRepo.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addReminder(reminder: Reminder) = viewModelScope.launch { reminderRepo.add(reminder) }
    fun deleteReminder(reminder: Reminder) = viewModelScope.launch { reminderRepo.delete(reminder) }
    fun setActive(reminder: Reminder, active: Boolean) =
        viewModelScope.launch { reminderRepo.setActive(reminder, active) }

    suspend fun searchAddress(query: String): List<GeoResult> = geocodingService.search(query)

    fun updateReminder(reminder: Reminder) = viewModelScope.launch { reminderRepo.update(reminder) }
}