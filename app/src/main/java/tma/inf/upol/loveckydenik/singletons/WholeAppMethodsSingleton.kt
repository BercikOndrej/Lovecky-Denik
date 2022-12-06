package tma.inf.upol.loveckydenik.singletons

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.classes.Marker
import tma.inf.upol.loveckydenik.database.EventViewModel
import tma.inf.upol.loveckydenik.database.HuntingViewModel
import tma.inf.upol.loveckydenik.database.MarkerViewModel
import tma.inf.upol.loveckydenik.enums.Animal
import tma.inf.upol.loveckydenik.enums.Gender
import tma.inf.upol.loveckydenik.enums.HuntingMethod
import tma.inf.upol.loveckydenik.enums.MarkerType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

// Singleton pro celou aplikaci
object WholeAppMethodsSingleton {

    // Funkce pro práci s fotkami -> uložení, načtení, získání fotografie

    // Funkce, která vrátí složku, kam se budou ukládat fotky
    // -> pokud ještě neexistuje, tak ji vytvoří
    private fun getPictureFolder(ctx: Context): File? {
        val externalFilesDir = ctx.getExternalFilesDir(null)
        val pictureFile = File(
            externalFilesDir,
            MainActivity.APP_PICTURES_FOLDER_NAME
        )
        if (!pictureFile.exists()) {
            pictureFile.mkdirs()
        }

        return if (pictureFile.exists()) {
            pictureFile
        } else {
            null
        }
    }

    fun savePhotoIntoExternalStorage(ctx: Context, filename: String, bmp: Bitmap): Boolean {
        val finalDir = getPictureFolder(ctx)
        return if (finalDir == null) {
            false
        }
        else {
            val imageFile = File(finalDir, "$filename.jpg")
            try {
                val outputStream = FileOutputStream(imageFile)
                if (bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                    throw IOException("Couldn't save a photo")
                }
                outputStream.flush()
                outputStream.close()
            }
            catch (e: IOException) {
                e.printStackTrace()
                false
            }
            return true
        }
    }

    fun loadPhotoFromExternalStorage(ctx: Context, filename: String): Bitmap? {
        val pictureFolder = getPictureFolder(ctx)
        return if (pictureFolder != null) {
            val filtredFiles = pictureFolder.listFiles().filter { file ->
                file.canRead()
                        && file.isFile
                        && file.name.endsWith("jpg")
                        && file.name.equals("$filename.jpg", true)
            }
            if (filtredFiles.isEmpty()) {
                null
            }
            else {
                val bytes = filtredFiles[0].readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bmp
            }
        }
        else {
            null
        }
    }

    fun deletePhotoFromExternalStorage(ctx: Context, filename: String): Boolean {
        val pictureFolder = getPictureFolder(ctx)
        if (pictureFolder == null) {
            return false
        }

        val filtredFiles = pictureFolder.listFiles()?.filter { file ->
            file.canRead()
                    && file.isFile
                    && file.name.endsWith("jpg")
                    && file.name.equals("$filename.jpg", true)
        }

        return if (filtredFiles != null) {
            try {
                filtredFiles[0].delete()
                true
            }
            catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        } else {
            false
        }
    }

    // Převod uri na bitmapu
    fun uriToBmp(context: Context, uri: Uri?): Bitmap? {
        return if (uri == null) {
            null
        }
        else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    fun getUriFromExistingFile(ctx: Context, filename: String): Uri? {
        val pictureFolder = getPictureFolder(ctx)
        if (pictureFolder == null) {
            return null
        }

        val filtredFiles = pictureFolder.listFiles()?.filter {
            it.canRead()
                    && it.isFile
                    && it.name.endsWith("jpg")
                    && it.name.equals("$filename.jpg", true)
        }
        return if (filtredFiles == null) {
            null
        }
        else if (filtredFiles.size == 1) {
            Uri.fromFile(filtredFiles.first())
        }
        else {
            null
        }
    }

    // Funkce pro zisk správného stringu pro zobrazení z data
    fun getRightTimeFormatToDisplay(hour: Int, minute: Int): String {
        val minuteString = if (minute > 9) minute.toString() else "0${minute}"
        return "$hour:$minuteString"
    }

    // Funkce pro získ stringu vyjadřující den v týdnu
    // Dny se počítají od neděle (1 = neděle, 2 = pondělí atd)
    fun getStringFromDayNumber(context: Context, number: Int): String {
        val dayStrings = context.resources.getStringArray(R.array.days_of_week_shortcuts)
        return when(number) {
            2 -> dayStrings[number - 2] // Pondělí
            3 -> dayStrings[number - 2] // Úterý
            4 -> dayStrings[number - 2] // Středa
            5 -> dayStrings[number - 2] // Čtvrtek
            6 -> dayStrings[number - 2] // Pátek
            7 -> dayStrings[number - 2] // Sobota
            else -> dayStrings[6]       // Neděle
        }
    }

    // Funkce pro získání správného formátu pro secondary text
    fun generateRightSecondaryText(ctx: Context, event: CalendarEvent): String{
        return if (event.isAllDayEvent) {
            ctx.getString(R.string.all_day_text)
        }
        else if (event.startingDate.dayOfMonth == event.endingDate.dayOfMonth
            && event.startingDate.month == event.endingDate.month
            && event.startingDate.year == event.endingDate.year) {
            val startingTimeString =
                getRightTimeFormatToDisplay(
                    event.startingTime!!.hour,
                    event.startingTime!!.minute
                )
            val endingTimeString =
                getRightTimeFormatToDisplay(
                    event.endingTime!!.hour,
                    event.endingTime!!.minute
                )
            "$startingTimeString - $endingTimeString"
        }
        else {
            val startingTimeString =
                getRightTimeFormatToDisplay(
                    event.startingTime!!.hour,
                    event.startingTime!!.minute
                )
            val endingTimeString =
                getRightTimeFormatToDisplay(
                    event.endingTime!!.hour,
                    event.endingTime!!.minute
                )
            val startingDateString =
                "${event.startingDate.dayOfMonth}. ${event.startingDate.month.value}"
            val endingDateString =
                "${event.endingDate.dayOfMonth}. ${event.endingDate.month.value}"
            "$startingDateString. $startingTimeString - $endingDateString. $endingTimeString"
        }
    }

    // Funkce, která vrátí ke dni jeho datum ve formátu "číslo. zkratka měsíce"
    fun generateStringForDateNumberAndMonth(ctx: Context, event: CalendarEvent): String {
        val monthShortcuts = ctx.resources.getStringArray(R.array.months_shortcuts)
        val dayInMonthNumber = event.startingDate.dayOfMonth
        val monthString = monthShortcuts[event.startingDate.monthValue - 1]
        return "$dayInMonthNumber. $monthString"
    }

    // Funkce, podobná funkci getStringFromDayNumber ale pokud se jedná o dnešek nebo zítřek, tak vrátí tento string
    fun generateDayNameWithTodayAndTomorrow(ctx: Context, event: CalendarEvent): String {
        val calendar = Calendar.getInstance()
        val date = event.startingDate
        calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
        val eventNameDay = getStringFromDayNumber(ctx, calendar.get((Calendar.DAY_OF_WEEK)))

        if (isTodayDate(event.startingDate)) {
            return ctx.getString(R.string.today_day_text)
        }
        else if (isTomorrowDate(event.startingDate)) {
            return ctx.getString(R.string.tomorrow_day_text)
        }
        else {
            return eventNameDay
        }
    }

    // Funkce, který zjistí zda datum je zítřejší od dnešního data
    private fun isTomorrowDate(date: LocalDate): Boolean {
        val currentDate = LocalDate.now()
        return (currentDate.dayOfMonth + 1 == date.dayOfMonth
                && currentDate.monthValue == date.monthValue
                && currentDate.year == date.year)

    }

    // Funkce, která zjistí, zda se jedná o stejné datum
    private fun isTodayDate(date: LocalDate): Boolean {
        val currentDate = LocalDate.now()
        return (currentDate.dayOfMonth == date.dayOfMonth
                && currentDate.monthValue == date.monthValue
                && currentDate.year == date.year)
    }

    // Funkce pro získání správného id pro hunting item
    fun getHuntingItemNewID(ctx: Context): Long {
        // Získání id
        val id = ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .getLong(MainActivity.HUNTING_ITEM_ID_NAME, 1)

        // Inkrementace id pro příště
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putLong(MainActivity.HUNTING_ITEM_ID_NAME, id + 1)
            .apply()
        return id
    }

    // Funkce pro získání správného id pro marker
    fun getMarkerNewID(ctx: Context): Long {
        // Získání id
        val id = ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .getLong(MainActivity.MARKER_ID_NAME, 1)

        // Inkrementace id pro příště
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putLong(MainActivity.MARKER_ID_NAME, id + 1)
            .apply()
        return id
    }

    // Funkce k vygenerování čísla pro obrázek pro uložení
    fun generateImageNumber(ctx: Context): Int {
        var imageCounter = ctx.getSharedPreferences(
            MainActivity.SHARED_PREFERENCES_NAME,
            MODE_PRIVATE
        )
            .getInt(MainActivity.IMAGE_COUNTER_NAME, 1)
        val number = imageCounter
        imageCounter++
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putInt(MainActivity.IMAGE_COUNTER_NAME, imageCounter)
            .apply()
        return number
    }

    // Funkce pro vygenerování unikátního id pro notifikaci
    fun generateNotificationId(ctx: Context): Int {
        var idCounter = ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .getInt(MainActivity.NOTIFICATION_ID_COUNTER, 1)
        val id = idCounter
        idCounter++
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putInt(MainActivity.NOTIFICATION_ID_COUNTER, idCounter)
            .apply()
        return id
    }

    // Funkce, které zajišťují export/import databáze -> permissions v home
    // -> fragmentě, nemusím kontrolovat
    // Musím vytvořit novou složku pro csv soubory a pro vyexportované obrázky
    //--------------------------------------------------------------------------------------------


    // Funkce, která vytvoří složku pro všechna csv

    private fun createDirectoryForCsvFiles(ctx: Context): String? {

        // Starší pokus
        return try {
            val file = File(
                ctx.getExternalFilesDir(null),
                MainActivity.ALL_CSV_FOLDER
            )
            if (!file.exists()) {
                file.mkdirs()
            }
            if (file.exists()) {
                file.absolutePath.toString()
            }
            else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    // Samotný export -> vygeneruje 3 soubory csv (itemy, eventy a markery) + vyexportuje fotky
    fun exportDatabaseToCsvFiles(
        ctx: Context,
        markerViewModel: MarkerViewModel,
        eventViewModel: EventViewModel,
        huntingViewModel: HuntingViewModel
    ) {
        val finalDirectoryPath = createDirectoryForCsvFiles(ctx)
        finalDirectoryPath?.let {
            val markersSuccess = exportMarkers(markerViewModel, finalDirectoryPath)
            val eventSuccess = exportEvents(eventViewModel, finalDirectoryPath)
            val itemSuccess = exportHuntingItems(huntingViewModel, finalDirectoryPath)
            val pictureSuccess = exportPictures(ctx, huntingViewModel)
            if (markersSuccess && eventSuccess && itemSuccess && pictureSuccess) {
                Toast.makeText(ctx, ctx.getString(R.string.export_possitive_text), Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(ctx, ctx.getString(R.string.export_negative_text), Toast.LENGTH_LONG).show()
            }
        }
    }

    // Export jednotlivých tabulek
    private fun exportEvents(
        eventViewModel: EventViewModel,
        parentFilePath: String
    ): Boolean {
        val csvFile = generateFile(parentFilePath, MainActivity.EVENTS_CSV_FILE_NAME)
        return if (csvFile != null) {
            exportEventsToCsvFile(csvFile, eventViewModel)
            true
        }
        else {
            false
        }
    }

    private fun exportHuntingItems(
        huntingViewModel: HuntingViewModel,
        parentFilePath: String
    ): Boolean {
        val csvFile = generateFile(parentFilePath, MainActivity.HUNTING_ITEMS_CSV_FILE_NAME)
        return if (csvFile != null) {
            exportHuntingItemsToCsFile(csvFile, huntingViewModel)
            true
        } else {
            false
        }
    }

    private fun exportMarkers(markerViewModel: MarkerViewModel, parentFileName: String): Boolean {
        val markerCsvFile = generateFile(parentFileName, MainActivity.MARKERS_CSV_FILE_NAME)
        return if (markerCsvFile != null) {
            exportMarkersToCsvFile(markerCsvFile, markerViewModel)
            true
        } else {
            false
        }
    }

    // Funkce, která zajišťuje export markerů
    private fun exportMarkersToCsvFile(file: File, viewModel: MarkerViewModel) {
        if (file.exists()) {
            csvWriter().open(file, append = false) {
                // Header
                writeRow(listOf(
                    "[id]",
                    "[marker_type]",
                    "[name]",
                    "[note]",
                    "[location]",
                    "[associated_item_id]"
                ))
                viewModel.getAllMarkersForExport().forEach { marker ->
                    writeRow(listOf(
                        marker.id.toString(),
                        marker.markerType.toString(),
                        marker.name.toString(),
                        marker.note.toString(),
                        ConvertorsSingleton.latLngToString(marker.location),
                        marker.associatedItemID.toString()
                    ))
                }
            }
        }
    }

    // Funkce zajišťující export eventu
    private fun exportEventsToCsvFile(csvFile: File, viewModel: EventViewModel) {
        csvWriter().open(csvFile, append = false) {
            // Header
            writeRow(listOf(
                "[id]",
                "[name]",
                "[is_all_day_event]",
                "[starting_date]",
                "[starting_time]",
                "[ending_date]",
                "[ending_time]",
                "[google_event_id]",
                "[notification_id]"
            ))
            viewModel.getAllEventsForExport().forEach { event ->
                writeRow(listOf(
                    event.id.toString(),
                    event.name,
                    event.isAllDayEvent.toString(),
                    ConvertorsSingleton.fromDate(event.startingDate),
                    ConvertorsSingleton.fromTime(event.startingTime),
                    ConvertorsSingleton.fromDate(event.endingDate),
                    ConvertorsSingleton.fromTime(event.endingTime),
                    event.googleEventId.toString(),
                    event.notificationId.toString()
                ))
            }
        }
    }

    // Funkce zajišťující export loveckých položek
    private fun exportHuntingItemsToCsFile(csvFile: File, viewModel: HuntingViewModel) {
        csvWriter().open(csvFile, append = false) {
            // Header
            writeRow(listOf(
                "[id]",
                "[date]",
                "[time]",
                "[animal]",
                "[hunting_method]",
                "[location_latitude]",
                "[location_longitude]",
                "[image_file_name]",
                "[note]",
                "[night_vision_is_use]",
                "[dog_is_use]",
                "[accompaniment_at_the_hunt]",
                "[hunter_name]",
                "[age]",
                "[weight]",
                "[score_evaluation]",
                "[Gender]"
            ))
            viewModel.getAllItemsForExport().forEach { item ->
                writeRow(listOf(
                    item.id,
                    ConvertorsSingleton.fromDate(item.date),
                    ConvertorsSingleton.fromTime(item.time),
                    item.animal.toString(),
                    item.huntingMethod.toString(),
                    item.locationLat.toString(),
                    item.locationLng.toString(),
                    item.imageFileName.toString(),
                    item.note.toString(),
                    item.nightVisionIsUse.toString(),
                    item.dogIsUse.toString(),
                    item.accompanimentAtTheHunt.toString(),
                    item.hunterName.toString(),
                    item.age.toString(),
                    item.weight.toString(),
                    item.scoreEvaluation.toString(),
                    item.gender.toString()
                ))
            }
        }
    }

    // Funkce, která vygeneruje soubor
    private fun generateFile(parentFilePath: String, fileName: String): File? {
        val csvFile = File(parentFilePath, fileName)
        if (csvFile.exists()) {
            csvFile.delete()
        }
        csvFile.createNewFile()
        return if (csvFile.exists()) {
            csvFile
        } else {
            null
        }
    }

    // Export fotek -> musím fotky přesunout do složky exportovaných obrázků
    // -> tu prvně musím vytvořit
    private fun exportPictures(ctx: Context, huntingViewModel: HuntingViewModel): Boolean {
        // Vytvořím složku pro export
        val exportedPictureFolder = getExportedPictureFolder(ctx)
        if (exportedPictureFolder != null) {
            // Získám všechny hunting items a projdu je -> jejich obrázky načtu
            // a uložím pod stejným jménem do složky pro export
            val items = huntingViewModel.getAllItemsForExport()
            val itemsPictureNames = mutableListOf<String>()
            items.forEach { item ->
                item.imageFileName?.let {
                    itemsPictureNames.add(it)
                }
            }
            // pokud neexistují fotky neexportuji nic a mám hotovo
            if (itemsPictureNames.size == 0) {
                return true
            }
            // Jinak provedu export všech z nich
            else {
                itemsPictureNames.forEach { pictureName ->
                    val pictureBmp = loadPhotoFromExternalStorage(ctx, pictureName)
                    if (pictureBmp == null) {
                        return false
                    }
                    else if (!exportPicture(exportedPictureFolder, pictureName, pictureBmp)) {
                        return false
                    }
                }
            }
            // Pokud vče dopadlo, tak jak mělo tak vrátím true
            return true
        }
        else {
            return false
        }
    }

    // Funkce, která exportuje obrázek -> uloží je do složky určené pro export
    private fun exportPicture(finalDir: File, filename: String, bmp: Bitmap): Boolean {
        val imageFile = File(finalDir, "$filename.jpg")
        try {
            val outputStream = FileOutputStream(imageFile)
            if (bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                throw IOException("Couldn't save a photo")
            }
            outputStream.flush()
            outputStream.close()
        }
        catch (e: IOException) {
            e.printStackTrace()
            false
        }
        return true
    }

    // Funkce, pro vytvožení složky exportedPictureFolder
    private fun getExportedPictureFolder(ctx: Context): File? {
        val externalFilesDir = ctx.getExternalFilesDir(null)
        val pictureFile = File(
            externalFilesDir,
            MainActivity.EXPORTED_PICTURES_FOLDER
        )
        if (!pictureFile.exists()) {
            pictureFile.mkdirs()
        }

        return if (pictureFile.exists()) {
            pictureFile
        } else {
            null
        }
    }

    // Funkce, pro získání složky exportedPictureFolder, pouze pokud existuje
    private fun getExportedPictureFolderIfExists(ctx: Context): File? {
        val externalFilesDir = ctx.getExternalFilesDir(null)
        val pictureFile = File(
            externalFilesDir,
            MainActivity.EXPORTED_PICTURES_FOLDER
        )

        return if (pictureFile.exists()) {
            pictureFile
        } else {
            null
        }
    }

    // Funkce pro import do databáze
    // Při importu zkontrolovat, zda složky v zařízení vůbec existují
    // musíme zkontrolovat importované data
    // musíme smazat všechny data
    // resetovat generátory pro ID markerů, eventů, itemů, fotek, notificací
    // -> nastavit jim hodnotu na největší číslo
    // naimportovat data

    // Funkce, obsluhující celý import
    fun importData(
        ctx: Context,
        huntingViewModel: HuntingViewModel,
        eventViewModel: EventViewModel,
        markerViewModel: MarkerViewModel
    ) {
        // Prvně zkontrolujeme složky, zda vůbec existují
        val isExistsFiles = isExistsAllRequiredFiles(ctx)
        if (isExistsFiles) {
            // Pokud existují, přečteme a zkontrolujeme data z poskytnutých dat
            val markersList = readMarkers(ctx)
            val eventsList = readEvents(ctx)
            val itemsList = readItems(ctx)

            if (markersList != null && eventsList != null && itemsList != null) {
                // Pokud jsou data v pořádku, musíme smazat ty staré data a vyresetovat generátory
                // Pokud to proběhne úspěšně, tak pokračujeme dále
                if (deleteWholeDatabase(
                        ctx,
                        huntingViewModel, itemsList,
                        markerViewModel, markersList,
                        eventViewModel, eventsList
                    )) {
                    // Teď stačí naimportovat nová data -> pokud neúspěšně, tak to zahlásím
                    if (importAllData(
                            ctx,
                            huntingViewModel, itemsList,
                            markerViewModel, markersList,
                            eventViewModel, eventsList
                        )
                    ) {
                        Toast.makeText(
                            ctx,
                            ctx.getString(R.string.import_possitive_text),
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }
            }
        }
        Toast.makeText(
            ctx,
            ctx.getString(R.string.import_negative_text),
            Toast.LENGTH_LONG
        ).show()
    }

    // Funkce, která najde složku pro export databáze
    private fun getAllCsvFilesFolderIfExists(ctx: Context): File? {
        val csvFolder = File(
            ctx.getExternalFilesDir(null),
            MainActivity.ALL_CSV_FOLDER
        )
        return if (csvFolder.exists()) {
            csvFolder
        }
        else {
            null
        }
    }

    // Funkce, která najde složku pro obrázky
    private fun getPictureFolderIfExists(ctx: Context): File? {
        val pictureFolder = File(
            ctx.getExternalFilesDir(null),
            MainActivity.EXPORTED_PICTURES_FOLDER
        )
        return if (pictureFolder.exists()) {
            pictureFolder
        }
        else {
            null
        }
    }

    // Funkce, která kontroluje, zda všechny Csv složky existují
    private fun isExistsAllRequiredFiles(ctx: Context):  Boolean {
        val csvFolder = getAllCsvFilesFolderIfExists(ctx)
        val pictureFolder = getExportedPictureFolderIfExists(ctx)
        return if (csvFolder != null && pictureFolder != null) {
            val markersFile = File(csvFolder, MainActivity.MARKERS_CSV_FILE_NAME)
            val eventsFile = File(csvFolder, MainActivity.EVENTS_CSV_FILE_NAME)
            val itemsFile = File(csvFolder, MainActivity.HUNTING_ITEMS_CSV_FILE_NAME)
            (itemsFile.exists()
                    && eventsFile.exists()
                    && markersFile.exists()
                    && pictureFolder.exists())
        }
        else {
            false
        }
    }

    // Funkce, které přečtou data -> pokud je vše v pořádku vrátí seznam, pokud ne vrátí null
    private fun readMarkers(ctx: Context): MutableList<Marker>? {
        // Kontrola zda složka existuje proběhne v jiné funkci
        val markersCsvFile = File(
            getAllCsvFilesFolderIfExists(ctx),
            MainActivity.MARKERS_CSV_FILE_NAME
        )
        try {
            val returnList = mutableListOf<Marker>()
            CsvReader().open(markersCsvFile) {
                // Skip header
                var row = readNext()
                row = readNext()
                while (row != null) {
                    val id = row[0]
                    val markerType = row[1]
                    val name = if (row[2] == "null") null else row[2]
                    val note = if (row[3] == "null") null else row[3]
                    val location = ConvertorsSingleton.stringToLatLng(row[4])
                    val itemId = if (row[5] == "null") null else row[5].toLong()


                    // Vytvoření markeru
                    val marker = Marker(
                        MarkerType.valueOf(markerType),
                        location,
                        name,
                        note,
                        itemId,
                        id.toLong()
                    )
                    returnList.add(marker)
                    row = readNext()
                }
            }
            return returnList
        }
        catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun readEvents(ctx: Context): MutableList<CalendarEvent>? {
        // Kontrola zda složka existuje proběhne v jiné funkci
        val eventsCsvFile = File(
            getAllCsvFilesFolderIfExists(ctx),
            MainActivity.EVENTS_CSV_FILE_NAME
        )
        try {
            val returnList = mutableListOf<CalendarEvent>()
            CsvReader().open(eventsCsvFile) {
                // Skip header
                var row = readNext()
                row = readNext()
                while (row != null) {
                    val id = row[0].toInt()
                    val name = row[1]
                    val isAllDayEvent = row[2].toBoolean()
                    val startingDate = ConvertorsSingleton.toDate(row[3].toLong())
                    val startingTime = ConvertorsSingleton.toTime(row[4])
                    val endingDate = ConvertorsSingleton.toDate(row[5].toLong())
                    val endingTime = ConvertorsSingleton.toTime(row[6])
                    val googleEventId = row[7].toLong()
                    val notificationId = row[8].toInt()

                    val event = CalendarEvent(
                        name,
                        isAllDayEvent,
                        startingDate,
                        endingDate,
                        notificationId,
                        startingTime,
                        endingTime,
                        googleEventId
                    )
                    event.id = id

                    returnList.add(event)
                    row = readNext()
                }
            }
            return returnList
        }
        catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Funkce, které přečtou data -> pokud je vše v pořádku vrátí seznam, pokud ne vrátí null
    private fun readItems(ctx: Context): MutableList<HuntingItem>? {
        // Kontrola zda složka existuje proběhne v jiné funkci
        val itemsCsvFile = File(
            getAllCsvFilesFolderIfExists(ctx),
            MainActivity.HUNTING_ITEMS_CSV_FILE_NAME
        )
        try {
            val returnList = mutableListOf<HuntingItem>()
            var picturesIsRight = true
            CsvReader().open(itemsCsvFile) {
                // Skip header
                var row = readNext()
                row = readNext()
                while (row != null) {
                    val id = row[0].toLong()
                    val date = ConvertorsSingleton.toDate(row[1].toLong())
                    val time = ConvertorsSingleton.toTime(row[2])
                    val animal = Animal.valueOf(row[3])
                    val method = HuntingMethod.valueOf(row[4])
                    val latitude = row[5].toDouble()
                    val longitude = row[6].toDouble()
                    val imageFileName = if (row[7] == "null") null else row[7]
                    val note = if (row[8] == "null") null else row[8]
                    val nightVision = row[9].toBoolean()
                    val dogIsUse = row[10].toBoolean()
                    val accompanimentAtTheHunt = row[11].toBoolean()
                    val hunterName = if (row[12] == "null") null else row[12]
                    val age = if (row[13] == "null") null else row[13].toInt()
                    val weight = if (row[14] == "null") null else row[14].toInt()
                    val score = if (row[15] == "null") null else row[15].toInt()
                    val gender = Gender.valueOf(row[16])

                    // Vytvořím item
                    val item = HuntingItem(
                        date,
                        time ?: LocalTime.now(),
                        imageFileName,
                        nightVision,
                        dogIsUse,
                        accompanimentAtTheHunt,
                        animal,
                        method,
                        latitude,
                        longitude,
                        hunterName,
                        age,
                        weight,
                        score,
                        gender,
                        note,
                        id
                    )

                    // Zkontroluji, zda je pro něj i obrázek
                    if (item.imageFileName != null && !existsAssocciatedPicture(ctx, item)) {
                        picturesIsRight = false
                    }

                    returnList.add(item)
                    row = readNext()
                }
            }
            return if (picturesIsRight) returnList else null
        }
        catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun existsAssocciatedPicture(ctx: Context, item: HuntingItem): Boolean {
        val pictureFolder = getPictureFolderIfExists(ctx)
        return if (pictureFolder != null) {
            val filtredPictures = pictureFolder.listFiles().filter { file ->
                file.canRead()
                        && file.isFile
                        && file.name.endsWith("jpg")
                        && file.name.equals("${item.imageFileName}.jpg", true)
            }
            if (filtredPictures.isEmpty()) {
                false
            } else filtredPictures.size == 1
        } else {
            false
        }
    }

    private fun deleteWholeDatabase(
        ctx: Context,
        huntingViewModel: HuntingViewModel,
        items: MutableList<HuntingItem>,
        markerViewModel: MarkerViewModel,
        markers: MutableList<Marker>,
        eventViewModel: EventViewModel,
        events: MutableList<CalendarEvent>
    ): Boolean {
        return if (deleteAllActualPictures(ctx)) {
            huntingViewModel.deleteAllItems()
            markerViewModel.deleteAllMarkers()
            eventViewModel.deleteAllEvents()
            resetsAllGenerators(ctx, items, markers, events)
            true
        }
        else {
            false
        }
    }

    // Funkce, která maže všechny obrázky -> musím je taktéž smazat, jelikož itemy smažu ale k nim
    // asociované obrázky už ne
    private fun deleteAllActualPictures(ctx: Context): Boolean {
        val pictureFolder = getPictureFolder(ctx)
        return if (pictureFolder != null) {
            pictureFolder.listFiles()?.forEach { file ->
                if(!file.delete()) {
                    return false
                }
            }
            true
        }
        else {
            false
        }
    }

    // Funkce, která resetuje všechny generátory, které aplikace používá -> nastaví je na 1
    private fun resetsAllGenerators(
        ctx: Context,
        items: MutableList<HuntingItem>,
        markers: MutableList<Marker>,
        events: MutableList<CalendarEvent>,
    ) {
        resetItemIdGenerator(ctx, items)
        resetMarkerIdGenerator(ctx, markers)
        resetImageNumberGenerator(ctx, items)
        resetNotificationIdGenerator(ctx, events)
    }

    // Reset item id generátoru
    private fun resetItemIdGenerator(ctx: Context, newItems: MutableList<HuntingItem>) {
        val value = getRightItemIdCounterValue(newItems)
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putLong(MainActivity.HUNTING_ITEM_ID_NAME, value)
            .apply()
    }

    // Funkce, která získá hodnotu se kterou má pokračovat v Id pro item
    private fun getRightItemIdCounterValue(newItems: MutableList<HuntingItem>): Long {
        var returnValue = 0L
        newItems.forEach { item ->
            if (item.id > returnValue) {
                returnValue = item.id
            }
        }
        return returnValue + 1
    }

    // Reset marker id generátoru
    private fun resetMarkerIdGenerator(ctx: Context, newMarkers: MutableList<Marker>) {
        val value = getRightMarkerIdCounterValue(newMarkers)
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putLong(MainActivity.MARKER_ID_NAME, value)
            .apply()
    }

    // Funkce, která získá hodnotu se kterou má pokračovat v Id pro marker
    private fun getRightMarkerIdCounterValue(newMarkers: MutableList<Marker>): Long {
        var returnValue = 0L
        newMarkers.forEach { marker ->
            if (marker.id > returnValue) {
                returnValue = marker.id
            }
        }
        return returnValue + 1
    }

    // Reset image number generátoru
    private fun resetImageNumberGenerator(ctx: Context,newItems: MutableList<HuntingItem> ) {
        val value = getRightImageCounterValue(newItems)
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putInt(MainActivity.IMAGE_COUNTER_NAME, value)
            .apply()
    }

    // Funkce, která vrátí správnou hodnotu pro image counter
    private fun getRightImageCounterValue(newItems: MutableList<HuntingItem>): Int {
        var returnValue = 0
        newItems.forEach { item ->
            item.imageFileName?.let {
                val value = it.subSequence(
                    MainActivity.IMAGE_START_INDEX_FOR_GETTING_VALUE,
                    it.length
                ).toString().toInt()
                if (value > returnValue) {
                    returnValue = value
                }
            }
        }
        return returnValue + 1
    }

    // Reset notification id generátoru
    private fun resetNotificationIdGenerator(ctx: Context, newEvents: MutableList<CalendarEvent>) {
        val value = getRightNotificationIdCounterValue(newEvents)
        ctx.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putInt(MainActivity.NOTIFICATION_ID_COUNTER, value)
            .apply()
    }

    // Funkce, která získá hodnotu se kterou má pokračovat v Id pro marker
    private fun getRightNotificationIdCounterValue(newEvents: MutableList<CalendarEvent>): Int {
        var returnValue = 0
        newEvents.forEach { event ->
            if (event.notificationId > returnValue) {
                returnValue = event.notificationId
            }
        }
        return returnValue + 1
    }

    // Následná funkce pro import všech dat
    private fun importAllData(
        ctx: Context,
        huntingViewModel: HuntingViewModel,
        items: MutableList<HuntingItem>,
        markerViewModel: MarkerViewModel,
        markers: MutableList<Marker>,
        eventViewModel: EventViewModel,
        events: MutableList<CalendarEvent>
        ): Boolean {
        return if (importPictures(ctx, items)) {
            importItems(huntingViewModel, items)
            importEvents(eventViewModel, events)
            importMarkers(markerViewModel, markers)
            true
        }
        else {
            false
        }
    }

    // Import obrázků
    private fun importPictures(ctx: Context, items: MutableList<HuntingItem>): Boolean {
        items.forEach { item ->
            // Pokud fotku má, tak ji načtu
            if (item.imageFileName != null) {
                val itemPicture = loadPhotoFromExportedFolder(
                    ctx,
                    item.imageFileName!!
                )
                // Pokud se správně načetla, tak se ji pokusím uložit
                if (itemPicture != null) {
                    // Pokud se uloží správně, pokračuje se na další item
                    if (!savePhotoIntoExternalStorage(ctx, item.imageFileName!!, itemPicture)) {
                        return  false
                    }
                }
            }
        }
        // Pokud proběhlo vše v pořádku, tak se importovali obrázky úspěšně
        return true
    }

    // Funkce, která načte obrázek z exportované složky
    private fun loadPhotoFromExportedFolder(ctx: Context, filename: String): Bitmap? {
        val pictureFolder = getExportedPictureFolderIfExists(ctx)
        return if (pictureFolder != null) {
            val filtredFiles = pictureFolder.listFiles().filter { file ->
                file.canRead()
                        && file.isFile
                        && file.name.endsWith("jpg")
                        && file.name.equals("$filename.jpg", true)
            }
            if (filtredFiles.isEmpty()) {
                null
            }
            else {
                val bytes = filtredFiles[0].readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bmp
            }
        }
        else {
            null
        }
    }
    // Import itemů do databáze
    private fun importItems(huntingViewModel: HuntingViewModel, items: MutableList<HuntingItem>) {
        items.forEach { item ->
            huntingViewModel.insertItem(item)
        }
    }

    private fun importMarkers(markerViewModel: MarkerViewModel, markers: MutableList<Marker>) {
        markers.forEach { marker ->
            markerViewModel.insertMarker(marker)
        }
    }

    private fun importEvents(eventViewModel: EventViewModel, events: MutableList<CalendarEvent>) {
        events.forEach { event ->
            eventViewModel.insertEvent(event)
        }
    }
}