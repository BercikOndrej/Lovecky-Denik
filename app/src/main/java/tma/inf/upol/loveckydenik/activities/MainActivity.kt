package tma.inf.upol.loveckydenik.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import tma.inf.upol.loveckydenik.R

class MainActivity : AppCompatActivity() {

    companion object {
        // Veškeré konstanty potřebné k předávání přes intent atd
        const val DB_NAME = "my_database"
        const val ANIMAL_FILTER = "current_animal_filter_in_int"
        const val ALL_IMAGES_PREFIX = "image"
        const val IMAGE_COUNTER_NAME = "image_counter"
        const val NOTIFICATION_ID_COUNTER = "notification_id_counter"
        const val ITEM_KEY = "item_extra"
        const val IMAGE_URI_KEY = "image_uri_extra"
        const val IS_EDITING_ACTION_KEY = "is_editing"
        const val DATE_KEY = "date"
        const val EVENT_KEY = "event"
        const val DAY_KEY = "day"
        const val MONTH_KEY = "month"
        const val YEAR_KEY = "year"
        const val EVENT_ACTION_KEY = "action"
        const val MY_CALENDAR_PERMISSIONS_REQUEST = 10
        const val MY_STORAGE_PERMISSIONS_REQUEST = 1010
        const val SHARED_PREFERENCES_NAME = "my_preferences"
        const val MARKER_ID_NAME = "marker_id"
        const val HUNTING_ITEM_ID_NAME = "hunting_item_id"
        const val EVENT_EDIT = "EDIT"
        const val EVENT_NEW = "NEW"
        const val REQUEST_PERMISSION_CODE = 345
        const val MARKER_VISUALIZATION_KEY: Float = 0F
        const val HUNTER_MARKERS_COUNT = 22
        const val ANIMATION_DURATION_TIME = 700L
        const val MARKER_ICON_SIZE = 1.5F
        const val ID_MAP_LAYER ="id_layer"
        const val HUNTER_NAME_KEY = "hunter_name"
        const val HUNTER_EMAIL_KEY = "hunter_email"
        const val LOCATION_EXTRA_KEY = "position"
        const val LATITUDE_EXTRA_KEY = "latitude"
        const val LONGITUDE_EXTRA_KEY = "longitude"
        const val IS_SELECTING_LOCATION_EXTRA_KEY = "is_selecting_location"
        var isSelectingLocation = false
        const val MARKERS_CSV_FILE_NAME = "markers.csv"
        const val HUNTING_ITEMS_CSV_FILE_NAME = "huntingItems.csv"
        const val EVENTS_CSV_FILE_NAME = "events.csv"
        const val APP_PICTURES_FOLDER_NAME = "AppPictures"
        const val ALL_CSV_FOLDER = "ExportedDatabase/CsvFiles"
        const val EXPORTED_PICTURES_FOLDER = "ExportedDatabase/Pictures"
        const val IMAGE_START_INDEX_FOR_GETTING_VALUE = 5
    }

    // Proměnná sloužící k signalizaci, že se ukončí splashscreen a spustí real aplikaci
    private var isEverythingLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instalace splashscreenu
        installSplashScreen().apply {
            this.setKeepOnScreenCondition {
                isEverythingLoading
            }
        }
        setContentView(R.layout.activity_main)


        // Najdu si potřebné komponenty k navigaci pomocí bottomMenu
        val bottomMenu = findViewById<BottomNavigationView>(R.id.bottom_nav_menu)
        val navController = findNavController(R.id.fragmentContainerView)

        // Nastavení aby se měnil i title spolu s fragmenty
        val appBarConfig = AppBarConfiguration(setOf(
            R.id.homeFragment,
            R.id.mapFragment,
            R.id.listFragment,
            R.id.calendarFragment
        ))
        setupActionBarWithNavController(navController, appBarConfig)

        // Funkcionalita navigování v aplikaci
        bottomMenu.setupWithNavController(navController)

        // Jestliže je tohle požadavek na vybrání polohy, tak nasměrujeme na map fragment
        if (intent.extras?.getBoolean(IS_SELECTING_LOCATION_EXTRA_KEY) == true) {
            isSelectingLocation = true
            bottomMenu.selectedItemId = R.id.mapFragment
        }

        // Jakmile je vše načteno, tak to signalizujeme
        isEverythingLoading = false
    }
}