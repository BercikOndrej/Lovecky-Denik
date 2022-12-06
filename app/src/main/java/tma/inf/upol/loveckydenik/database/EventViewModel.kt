package tma.inf.upol.loveckydenik.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tma.inf.upol.loveckydenik.classes.CalendarEvent

class EventViewModel(app: Application): AndroidViewModel(app) {
    private val repository: EventRepository
    val getAllEvents: LiveData<MutableList<CalendarEvent>>

    init {
        val eventDao = MyDatabase.getDatabase(app).eventDao()
        repository = EventRepository(eventDao)
        getAllEvents = repository.getAllEvents
    }

    fun getEventsByDate(date: Long): LiveData<MutableList<CalendarEvent>> {
        return repository.getEventsByDate(date)
    }

    fun getComingEvents(date: Long): List<CalendarEvent> {
        return repository.getComingEvents(date)
    }

    fun getAllEventsForExport(): List<CalendarEvent> {
        return  repository.getAllEventsForExport()
    }

    fun updateEvent(event: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event)
        }
    }

    fun insertEvent(event: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertEvent(event)
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
        }
    }

    fun deleteAllEvents() {
        repository.deleteAllEvents()
    }
}