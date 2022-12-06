package tma.inf.upol.loveckydenik.classes

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tma.inf.upol.loveckydenik.enums.Animal
import tma.inf.upol.loveckydenik.enums.Gender
import tma.inf.upol.loveckydenik.enums.HuntingMethod
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "hunting_items")
data class HuntingItem  (
    var date: LocalDate,

    var time: LocalTime,

    @ColumnInfo(name = "image_file_name")
    var imageFileName: String?,

    @ColumnInfo(name = "night_vision_is_use")
    var nightVisionIsUse: Boolean,

    @ColumnInfo(name = "dog_is_use")
    var dogIsUse: Boolean,

    @ColumnInfo(name = "accompaniment_at_the_hunt")
    var accompanimentAtTheHunt: Boolean,

    var animal: Animal,

    @ColumnInfo(name = "hunting_method")
    var huntingMethod: HuntingMethod,

    @ColumnInfo(name = "location_latitude")
    var locationLat: Double,

    @ColumnInfo(name = "location_longitude")
    var locationLng: Double,

    var hunterName: String?,

    var age: Int?,

    var weight: Int?,

    @ColumnInfo(name = "score_evaluation")
    var scoreEvaluation: Int?,

    var gender: Gender,

    var note: String?,

    @PrimaryKey
    var id: Long

    ) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readSerializable() as LocalDate,
        parcel.readSerializable() as LocalTime,
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        Animal.valueOf(parcel.readString().toString()),
        HuntingMethod.valueOf(parcel.readString().toString()),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        Gender.valueOf(parcel.readString().toString()),
        parcel.readString(),
        parcel.readLong()
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(this.date)
        parcel.writeSerializable(this.time)
        parcel.writeString(imageFileName)
        parcel.writeByte(if (nightVisionIsUse) 1 else 0)
        parcel.writeByte(if (dogIsUse) 1 else 0)
        parcel.writeByte(if (accompanimentAtTheHunt) 1 else 0)
        parcel.writeString(this.animal.toString())
        parcel.writeString(this.huntingMethod.toString())
        parcel.writeDouble(locationLat)
        parcel.writeDouble(locationLng)
        parcel.writeString(hunterName)
        parcel.writeValue(age)
        parcel.writeValue(weight)
        parcel.writeValue(scoreEvaluation)
        parcel.writeString(this.gender.toString())
        parcel.writeString(note)
        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HuntingItem> {



        override fun createFromParcel(parcel: Parcel): HuntingItem {
            return HuntingItem(parcel)
        }

        override fun newArray(size: Int): Array<HuntingItem?> {
            return arrayOfNulls(size)
        }
    }
}