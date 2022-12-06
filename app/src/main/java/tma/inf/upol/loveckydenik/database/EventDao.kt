package tma.inf.upol.loveckydenik.database

import androidx.lifecycle.LiveData
import androidx.room.*
import tma.inf.upol.loveckydenik.classes.CalendarEvent

@Dao
interface EventDao {

    @Query("SELECT * FROM events")
    fun getAllEvents(): LiveData<MutableList<CalendarEvent>>

    @Query("SELECT * FROM events WHERE starting_date <= :date AND :date <= ending_date")
    fun getEventsByDate(date: Long): LiveData<MutableList<CalendarEvent>>

    @Query("SELECT * FROM events WHERE starting_date >= :date ORDER BY starting_date")
    fun getComingEvents(date: Long): List<CalendarEvent>

    @Query("SELECT * FROM events")
    fun getAllEventsForExport(): List<CalendarEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM events")
    fun deleteAllEvents()
}