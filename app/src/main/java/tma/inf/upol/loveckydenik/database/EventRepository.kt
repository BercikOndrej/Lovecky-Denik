package tma.inf.upol.loveckydenik.database

import androidx.lifecycle.LiveData
import tma.inf.upol.loveckydenik.classes.CalendarEvent

class EventRepository(private val eventDao: EventDao) {

    val getAllEvents: LiveData<MutableList<CalendarEvent>> = eventDao.getAllEvents()


    fun getEventsByDate(date: Long): LiveData<MutableList<CalendarEvent>> {
        return eventDao.getEventsByDate(date)
    }

    fun getComingEvents(date: Long): List<CalendarEvent> {
        return eventDao.getComingEvents(date)
    }

    fun getAllEventsForExport(): List<CalendarEvent> {
        return eventDao.getAllEventsForExport()
    }

    suspend fun insertEvent(event: CalendarEvent) {
        eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: CalendarEvent) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        eventDao.deleteEvent(event)
    }

    fun deleteAllEvents() {
        eventDao.deleteAllEvents()
    }
}