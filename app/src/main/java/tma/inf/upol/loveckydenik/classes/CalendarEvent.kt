package tma.inf.upol.loveckydenik.classes

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
data class CalendarEvent(
    var name: String,

    @ColumnInfo(name = "is_all_day_event")
    var isAllDayEvent: Boolean,

    @ColumnInfo(name = "starting_date")
    var startingDate: LocalDate,

    @ColumnInfo(name = "ending_date")
    var endingDate: LocalDate,

    @ColumnInfo(name = "notification_id")
    val notificationId: Int,

    @ColumnInfo(name = "starting_time")
    var startingTime: LocalTime? = null,

    @ColumnInfo(name = "ending_time")
    var endingTime: LocalTime? = null,

    @ColumnInfo(name = "google_event_id")
    var googleEventId: Long = 0

): Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readSerializable() as LocalDate,
        parcel.readSerializable() as LocalDate,
        parcel.readInt(),
        parcel.readSerializable() as LocalTime?,
        parcel.readSerializable() as LocalTime?,
        parcel.readLong()
        ) {
        id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeByte(if (isAllDayEvent) 1 else 0)
        parcel.writeSerializable(startingDate)
        parcel.writeSerializable(endingDate)
        parcel.writeInt(notificationId)
        parcel.writeSerializable(startingTime)
        parcel.writeSerializable(endingTime)
        parcel.writeLong(googleEventId)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalendarEvent> {
        override fun createFromParcel(parcel: Parcel): CalendarEvent {
            return CalendarEvent(parcel)
        }

        override fun newArray(size: Int): Array<CalendarEvent?> {
            return arrayOfNulls(size)
        }
    }
}