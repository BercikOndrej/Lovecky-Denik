package tma.inf.upol.loveckydenik.singletons

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import tma.inf.upol.loveckydenik.enums.Animal
import tma.inf.upol.loveckydenik.enums.Gender
import tma.inf.upol.loveckydenik.enums.HuntingMethod
import java.time.LocalDate
import java.time.LocalTime

object ConvertorsSingleton {

    fun animalToString(animal: Animal): String {
        return animal.toString()
    }

    fun stringToAnimal(string: String): Animal? {
        for (animal in Animal.values()) {
            if (animal.toString() == string) {
                return animal
            }
        }
        return null
    }

    fun genderToString(gender: Gender): String {
        return gender.toString()
    }

    fun stringToGender(string: String): Gender? {
        for (gender in Gender.values()){
            if(gender.toString() == string) {
                return gender
            }
        }
        return null
    }

    fun methodToString(method: HuntingMethod): String {
        return method.toString()
    }

    fun stringToMethod(string: String): HuntingMethod? {
        for (method in HuntingMethod.values()) {
            if (method.toString() == string) {
                return method
            }
        }
        return null
    }

    // Reprezentace data pomocí formátovaného stringu
    fun fromDate(date: LocalDate): Long {
        return date.toEpochDay()
    }

    fun toDate(date: Long): LocalDate {
        return LocalDate.ofEpochDay(date)
    }

    fun fromTime(time: LocalTime?): String {
        return time?.toString() ?: ""
    }

    fun toTime(string: String): LocalTime? {
        return if (string.isEmpty()) {
            null
        }
        else {
            LocalTime.parse(string)
        }
    }

    fun stringToLatLng(json: String): LatLng {
        val gson = Gson()
        val type = object : TypeToken<LatLng>() {}.type
        return gson.fromJson(json, type)
    }

    fun latLngToString(position: LatLng): String {
        val gson = Gson()
        val type = object : TypeToken<LatLng>() {}.type
        return gson.toJson(position, type)
    }
}