package tma.inf.upol.loveckydenik.singletons

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.EasyPermissions
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


// Všechny metody o přístupu do kalendáře jsou převzaty ze zdroje:
// https://www.codexpedia.com/android/crud-operations-using-calendar-provider-in-android/
object CalendarMethodsSingleton {



    // Zkontroluje všechna povolení -> true pokud jsou všechna udělena jinak false
    fun hasAllNecessaryCalendarPermissions(ctx: Context): Boolean {
        return hasReadCalendarPermission(ctx) && hasWriteCalendarPermission(ctx)
    }

    // Zjištění zda má aplikace povolení zapisovat do kalendářů
    private fun hasWriteCalendarPermission(ctx: Context): Boolean {
        return EasyPermissions.hasPermissions(ctx, Manifest.permission.WRITE_CALENDAR)
    }

    // Zjištění zda má aplikace povolení číst kalendáře
    private fun hasReadCalendarPermission(ctx: Context): Boolean {
        return EasyPermissions.hasPermissions(ctx, Manifest.permission.READ_CALENDAR)
    }

    fun requestAllNecessaryCalendarPermissions(host: Fragment) {
        EasyPermissions.requestPermissions(
            host,
            // Zpráva, když uživatel odmítne
            host.getString(R.string.calendar_permissions_needed_text),
            MainActivity.MY_CALENDAR_PERMISSIONS_REQUEST,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR
        )
    }

    // Zjištění všech kalendářu z mobilního zařízení
    // Pokud uživatel neudělil oprávnění, tak jednoduše event nesdílím a vracím null
    private fun getCalendars(ctx: Context) :ArrayList<String?>? {
        // Kontrola povolení -> pokud nemá, synchronizace se neprovede
        if (!hasAllNecessaryCalendarPermissions(ctx)) {
            return null
        }

        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        val EVENT_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,  // 0
            CalendarContract.Calendars.ACCOUNT_NAME,  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,  // 2
            CalendarContract.Calendars.OWNER_ACCOUNT // 3
        )

        // The indices for the projection array above.
        val PROJECTION_ID_INDEX = 0
        val PROJECTION_ACCOUNT_NAME_INDEX = 1
        val PROJECTION_DISPLAY_NAME_INDEX = 2
        val PROJECTION_OWNER_ACCOUNT_INDEX = 3
        val contentResolver = ctx.contentResolver
        val cur: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            EVENT_PROJECTION,
            null,
            null,
            null
        )
        val calendarInfos: ArrayList<String?> = ArrayList()
        if (cur != null) {
            while (cur.moveToNext()) {
                var calID: Long = 0
                var displayName: String? = null
                var accountName: String? = null
                var ownerName: String? = null

                // Get the field values
                calID = cur.getLong(PROJECTION_ID_INDEX)
                displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                val calendarInfo = "$calID,$displayName,$accountName,$ownerName"
                calendarInfos.add(calendarInfo)
            }
        }

        return  calendarInfos
    }

    // Zjištění ID kalendáře Google v mobilním zařízení
    // Pokud není účet přihlášen, vrátí -1
    fun getGmailCalendarID(ctx: Context): Int {
        // Kontrola oprávnění -> pokud není, nedělám nic, evnet se nesynchronizuje
        if (!hasAllNecessaryCalendarPermissions(ctx)) {
            return -1
        }
        val allCalendars = getCalendars(ctx)
        allCalendars?.let {
            val gmailSulfixString = "@gmail.com"
            allCalendars.forEach { calendarInfo ->
                calendarInfo?.let {
                    val splitInfos = calendarInfo.split(",")
                    if (splitInfos[1].endsWith(gmailSulfixString)
                        && splitInfos[2].endsWith(gmailSulfixString)
                        && splitInfos[3].endsWith(gmailSulfixString)
                    ) {
                        return splitInfos[0].toInt()
                    }
                }
            }
        }
        return -1
    }

    // Přidání evenetu
    fun addEvent(ctx: Context, event: CalendarEvent): CalendarEvent {
        // Kontrola oprávnění -> pokud není, event nesynchronizuji
        if (!hasAllNecessaryCalendarPermissions(ctx)) {
            return event
        }

        // Zjištění, zda je přihlášen gmail účet
        val calID = getGmailCalendarID(ctx)
        if (calID == -1) {
            return event
        }

        // Zde nastavím časový interval doby trvání eventu -> funkce getCalendarTimeFromEvent
        // vráti instance Calendar s již nastaveným správným časem
        var calendar = if (event.startingTime == null) {
            getCalendarTimeFromDate(event.startingDate)
        }
        else {
            getCalendarTimeFromDate(event.startingDate, event.startingTime!!)
        }
        val startTime = calendar.timeInMillis
        // Log.d("CalendarTest", "${calendar.get(Calendar.YEAR)}, ${calendar.get(Calendar.MONTH)}, ${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.get(Calendar.HOUR)}, ${calendar.get(Calendar.MINUTE)} ${calendar.get(Calendar.SECOND)}")

        calendar = if (event.endingTime == null) {
            getCalendarTimeFromDate(event.endingDate)
        }
        else {
            getCalendarTimeFromDate(event.endingDate, event.endingTime!!)
        }
        val endTime = calendar.timeInMillis
        // Log.d("CalendarTest", "${calendar.get(Calendar.YEAR)}, ${calendar.get(Calendar.MONTH)}, ${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.get(Calendar.HOUR)}, ${calendar.get(Calendar.MINUTE)} ${calendar.get(Calendar.SECOND)}")

        val cr = ctx.contentResolver
        val values = ContentValues()
        values.apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, event.name)
            put(CalendarContract.Events.CALENDAR_ID, calID)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ORGANIZER, "google_calendar@gmail.com")
        }
        if (ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventID = uri?.lastPathSegment!!.toLong()
            // Log.i("CalendarTag", "Event: ${event.name}, event id: ${event.googleEventId}, post event id: $eventID")
            // Přiřadím id eventu abych jej později mohl editovat
            event.googleEventId = eventID
            // Log.i("CalendarTag", "Event: ${event.name}, event id: ${event.googleEventId}, post event id: $eventID")

        } else {
            ActivityCompat.requestPermissions(
                ctx as Activity,
                arrayOf(Manifest.permission.WRITE_CALENDAR),
                MainActivity.MY_CALENDAR_PERMISSIONS_REQUEST
            )
        }
        return event
    }

    // Update eventu ->  správné updatování
    fun updateEvent(ctx: Context, event: CalendarEvent): CalendarEvent {
        if (!hasAllNecessaryCalendarPermissions(ctx)) {
            return event
        }

        // Zjištění, zda je přihlášen gmail účet
        val calID = getGmailCalendarID(ctx)
        if (calID == -1) {
            return event
        }

        // Zisk počátečního data
        var calendar = if (event.startingTime == null) {
            getCalendarTimeFromDate(event.startingDate)
        }
        else {
            getCalendarTimeFromDate(event.startingDate, event.startingTime!!)
        }
        val startTime = calendar.timeInMillis

        // Zisk koncového data
        calendar = if (event.endingTime == null) {
            getCalendarTimeFromDate(event.endingDate)
        }
        else {
            getCalendarTimeFromDate(event.endingDate, event.endingTime!!)
        }
        val endTime = calendar.timeInMillis


        val cr: ContentResolver = ctx.contentResolver
        val values = ContentValues()
        values.apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, event.name)
            put(CalendarContract.Events.CALENDAR_ID, calID)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ORGANIZER, "google_calendar@gmail.com")
        }
        val updateUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.googleEventId)
        cr.update(updateUri, values, null, null)
        return event
    }

    // Smazání eventu
    fun deleteEvent(ctx: Context, event: CalendarEvent) {
        // Kontrola zda jsou poskytnuta všechna oprávnění
        if (!hasAllNecessaryCalendarPermissions(ctx)) {
            return
        }

        val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.googleEventId)
        ctx.contentResolver.delete(deleteUri, null, null)
    }

    // Pomocné funkce
    private fun getCalendarTimeFromDate(
        date: LocalDate,
        time: LocalTime = LocalTime.of(0,0)
    ): Calendar {
        val calendar = Calendar.getInstance()
        // Log.d("DateTest", "${date.year}, ${date.month.value}, ${date.dayOfMonth} ${time.hour}, ${time.minute}")
        calendar.clear()
        calendar.set(date.year, date.month.value - 1 , date.dayOfMonth, time.hour, time.minute)
        return calendar
    }
}