package tma.inf.upol.loveckydenik.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.classes.Marker

@Database(entities = [HuntingItem::class, CalendarEvent::class, Marker::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MyDatabase: RoomDatabase() {
    abstract fun huntingDao(): HuntingDao
    abstract fun eventDao(): EventDao
    abstract fun markerDao():MarkerDao

    // Vytvoření Singletonu -> pouze jedna instance v celé aplikaci
    // Jelikož s databází budou pracovat převážně jiná vlákna než hlavní, tak tomu musíme uzpůsobit
    // naši databázi -> synchtonized a Volatile
    companion object {
        // Volatile nás upozorňuje na to, že proměnná může být neočekávaně změněna dalšímy vlákny
        @Volatile
        private var INSTANCE: MyDatabase? = null

        // Synchronized block nám zaručuje, že do tohoto kusu kódu nevstoupí jiné vlákno
        fun getDatabase(context: Context): MyDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    MainActivity.DB_NAME
                )
                    .allowMainThreadQueries()
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}