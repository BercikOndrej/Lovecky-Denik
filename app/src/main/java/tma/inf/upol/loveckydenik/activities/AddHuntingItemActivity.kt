package tma.inf.upol.loveckydenik.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.mapboxsdk.geometry.LatLng
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.adapters.AnimalListAdapter
import tma.inf.upol.loveckydenik.adapters.MethodListAdapter
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.classes.Marker
import tma.inf.upol.loveckydenik.databinding.ActivityAddHuntingItemBinding
import tma.inf.upol.loveckydenik.enums.Animal
import tma.inf.upol.loveckydenik.enums.Gender
import tma.inf.upol.loveckydenik.enums.HuntingMethod
import tma.inf.upol.loveckydenik.database.HuntingViewModel
import tma.inf.upol.loveckydenik.database.MarkerViewModel
import tma.inf.upol.loveckydenik.enums.MarkerType
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class AddHuntingItemActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var  binding: ActivityAddHuntingItemBinding

    // Přístup k databázi
    private lateinit var huntingViewModel: HuntingViewModel
    private lateinit var markerViewModel: MarkerViewModel

    // Potřebná inicializace kalendáře
    private var year = 0
    private var month = 0
    private var day= 0
    private var hour = 0
    private var minute = 0

    // Proměnné potřebné k pořízení fotky
    private var uriToDetailView: Uri? = null
    private var currentPhotoBmp: Bitmap? = null

    // Proměnné pro zjištění, zda se bude editovat či vytvářet
    private var isEditing = false

    // Proměnná, která říká, zda se předala lokace nebo ne
    private var hasPosition = false

    // Adaptéry
    private lateinit var animalAdapter: AnimalListAdapter
    private lateinit var methodAdapter: MethodListAdapter

    // Akce
    private val selectLocationActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val latString = result.data?.extras?.getString(MainActivity.LATITUDE_EXTRA_KEY)
                val lngString = result.data?.extras?.getString(MainActivity.LONGITUDE_EXTRA_KEY)
                binding.locationEt.setText("$latString, $lngString")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHuntingItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.title_add_activity)

        // Inicializace přístupu k databázi
        initializeViewModels()

        // Zjišťujeme jestli se bude editovat, či ne
        isEditing = intent.getBooleanExtra(MainActivity.IS_EDITING_ACTION_KEY, false)

        // Také zjišťujeme, zda se předala lokace či nikoli
        hasPosition = intent.getBooleanExtra(MainActivity.LOCATION_EXTRA_KEY, false)
        val lng = intent.getDoubleExtra(MainActivity.LONGITUDE_EXTRA_KEY, 0.0)
        val lat = intent.getDoubleExtra(MainActivity.LATITUDE_EXTRA_KEY, 0.0)
        val sentItem = intent.getParcelableExtra<HuntingItem>(MainActivity.ITEM_KEY)

        // Pokud editujeme, musíme přenastavit componenty aby se zobrazili detaily lovecké položky
        if (isEditing) {
            // Datum a čas
            day = sentItem!!.date.dayOfMonth
            month = sentItem.date.month.value
            year = sentItem.date.year
            hour = sentItem.time.hour
            minute = sentItem.time.minute

            // Foto
            sentItem.imageFileName?.let { fileNameString ->
                currentPhotoBmp = WholeAppMethodsSingleton
                    .loadPhotoFromExternalStorage(this, fileNameString)
                binding.infoPhoto.setImageBitmap(currentPhotoBmp)
                binding.infoPhoto.isVisible = true
                binding.dropPhotoBt.isVisible = true
            }


            // Checkboxy
            binding.nightVisionIsUseChb.isChecked = sentItem.nightVisionIsUse
            binding.dogIsUseChb.isChecked = sentItem.dogIsUse
            binding.accompanimentAtTheHuntChb.isChecked = sentItem.accompanimentAtTheHunt

            // Výběr zvěře a metody lovu je proveden až po úspěšném vytvoření daných adaptérů

            // Poloha
            val locationRepresentString = "${sentItem.locationLat}, ${sentItem.locationLng}"
            binding.locationEt.setText(locationRepresentString)

            // Jméno lovce
            sentItem.hunterName?.let { name ->
                binding.hunterNameEt.setText(name)
            }

            // Věk
            sentItem.age?.let { age ->
                binding.ageEt.setText(age.toString())
            }

            // Bodování
            sentItem.scoreEvaluation?.let { score ->
                binding.scoreEt.setText(score.toString())
            }

            // Hmotnost
            sentItem.weight?.let { weight ->
                binding.weightEt.setText(weight.toString())
            }

            // Gender
            binding.genderActv.setText(sentItem.gender.toString())

            // Poznámka
            sentItem.note?.let {
                binding.noteEt.setText(it)
            }
        }
        else {
            // Kalendář pro zisk data
            val calendar = Calendar.getInstance()

            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH) + 1
            day = calendar.get(Calendar.DAY_OF_MONTH)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)

            // Předvyplnění jména lovce, pokud je k dizpozici
            val name = this
                .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                .getString(MainActivity.HUNTER_NAME_KEY, null)
            name?.let {
                binding.hunterNameEt.setText(it)
            }
            // Pokud se předala pozice, tak ji vyplním
            if (hasPosition) {
                binding.locationEt.setText("$lat, $lng")
            }
        }

        binding.dateTv.text = ("$day. $month. $year").toString()

        // Správný formát času a data -> použito na více místech, proto se používá singleton pro metody
        binding.timeTv.text = WholeAppMethodsSingleton.getRightTimeFormatToDisplay(hour,minute)

        // Obsluha pro výběr data
        binding.dateCard.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener {view, year, month, dayOfMonth ->
                    val rightMonth = month + 1
                    binding.dateTv.text = ("$dayOfMonth. $rightMonth. $year").toString()
                },
                year, month - 1, day
            )
            datePickerDialog.show()
        }

        // Obsluha pro výběr času
        binding.timeCard.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener {view, hourOfDay, minute ->
                    // Správný formát -> pomocí singletonu
                    binding.timeTv.text = WholeAppMethodsSingleton
                        .getRightTimeFormatToDisplay(hourOfDay, minute)
                },
                hour, minute, true
            )
            timePickerDialog.show()
        }

        // Vytvoření contractu pro pořízení fotky
        val cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                currentPhotoBmp = WholeAppMethodsSingleton.uriToBmp(this, result.uriContent)
                binding.infoPhoto.isVisible = true
                binding.infoPhoto.setImageBitmap(currentPhotoBmp)
                uriToDetailView = result.uriContent
                binding.dropPhotoBt.isVisible = true
            }
        }

        // Obsluha stisknutí na tlačítko pro pořízení fotky
        binding.galeryImageBt.setOnClickListener {
            cropImage.launch(
                options {
                    setGuidelines(CropImageView.Guidelines.ON)
                    setAspectRatio(3, 4)
                }
            )
        }

        // Obsluha tlačítka pro vybrání lokace
        binding.selectLocationButton.setOnClickListener {
            val intent = Intent().apply {
                setClass(this@AddHuntingItemActivity, MainActivity::class.java)
                putExtra(MainActivity.IS_SELECTING_LOCATION_EXTRA_KEY, true)
            }
            selectLocationActivityLauncher.launch(intent)
        }

        // Obsloužení stisknutí na náhed fotky
        binding.infoPhoto.setOnClickListener {
            val uri = if (uriToDetailView != null) {
                uriToDetailView
            }
            else {
                sentItem?.imageFileName?.let { fileName ->
                    WholeAppMethodsSingleton.getUriFromExistingFile(this, fileName)
                }
            }
            val intent = Intent().apply {
                putExtra(MainActivity.IMAGE_URI_KEY, uri.toString())
                setClass(
                    this@AddHuntingItemActivity,
                    FullScreenImageActivity::class.java
                )
            }
            startActivity(intent)
        }

        // Obsloužení tlačítka pro zahození fotky
        binding.dropPhotoBt.setOnClickListener {
            binding.infoPhoto.setImageBitmap(null)
            currentPhotoBmp = null
            uriToDetailView = null
            binding.infoPhoto.isVisible = false
            binding.dropPhotoBt.isVisible = false
        }

        // Inicializace recyclerView pro výběr zvěře
        val animalLayout = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.animalRecyclerView.layoutManager = animalLayout
        animalAdapter = AnimalListAdapter(this)
        binding.animalRecyclerView.adapter = animalAdapter

        // Inicializace recyclerView pro výběr způsobu lovu
        val methodLayout = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.methodRecyclerView.layoutManager = methodLayout
        methodAdapter = MethodListAdapter(this)
        binding.methodRecyclerView.adapter = methodAdapter

        // Pokud se edituje položka, tak musíme nastavit již známé informace
        if (isEditing) {
            val selectedAnimalPosition = Animal.values().indexOf(sentItem!!.animal)
            animalAdapter.chosenItemPosition = selectedAnimalPosition
            animalAdapter.notifyDataSetChanged()

            val selectedMethodPosition = HuntingMethod.values().indexOf(sentItem.huntingMethod)
            methodAdapter.chosenItemPosition = selectedMethodPosition
            animalAdapter.notifyDataSetChanged()
        }

        // Inicializace dropdown menu pro pohlaví -> musím být uveda i v metodě onResume()

        // Obsloužení cancel tlačítka
        binding.cancelButton.setOnClickListener{
            setResult(RESULT_CANCELED)
            finish()
        }

        // Obsloužení create tlačítka
        binding.createButton.setOnClickListener {
            // Pokud editujeme položku, tak nemusíme nic kontrolovat
            if (isEditing) {
                // Zkontroluji, zda jsou dobře zadanné informace o poloze
                if (!isRightLocationInput(binding.locationEt.text.toString())) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.item_not_saved))
                        .setMessage(getString(R.string.error_location_text))
                        .setPositiveButton(
                            getString(R.string.ok_text),
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            }
                        )
                        .show()
                }
                else {
                    editItem(sentItem!!)
                    // Editaci promítnu i do databáze i do markeru, pokud se něco změnilo
                    huntingViewModel.updateItem(sentItem)
                    editHuntingMarker(sentItem)
                    // Musím smazat uri sloužící pouze pro náhled fotky
                    uriToDetailView = null
                    // Informuji uživatele
                    Toast.makeText(
                        this,
                        getString(R.string.item_edited),
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent().apply {
                        putExtra(MainActivity.ITEM_KEY, sentItem)
                    }
                    setResult(RESULT_OK, intent)
                    // Pokud edituji, musím vrátit item jako parcelable aby se promítly změny
                    isEditing = false
                    finish()
                }
            }
            else {
                lateinit var potentialErrorMessage: String
                var shouldDisplayDialog = true
                if (checkAllRequiredInfo()) {
                    // Vytvoření objektu
                    val item = createDefinedItem()
                    val markerForItem = createHuntingMarkerForHuntingItem(item)
                    huntingViewModel.insertItem(item)
                    markerViewModel.insertMarker(markerForItem)
                    // Musím smazat uri sloužící pouze pro náhled fotky
                    uriToDetailView = null
                    shouldDisplayDialog = false
                    Toast.makeText(
                        this,
                        getString(R.string.item_saved),
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                // Pokud vše neproběhlo v pořádku, tak to jen oznámím uživateli
                else if (animalAdapter.chosenItemPosition == RecyclerView.NO_POSITION) {
                    potentialErrorMessage = getString(R.string.required_animal_missing)
                }
                else if (methodAdapter.chosenItemPosition == RecyclerView.NO_POSITION) {
                    potentialErrorMessage = getString(R.string.required_hunting_method_missing)
                }
                else {
                    potentialErrorMessage = this.getString(R.string.error_location_text)
                }
                if (shouldDisplayDialog) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.item_not_saved))
                        .setMessage(potentialErrorMessage)
                        .setPositiveButton(
                            getString(R.string.ok_text),
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            }
                        )
                        .show()
                }
            }

        }

        // Upozornění pro uživatele -> ovšem pokud udělá chybu i přes upozornění, tak informace
        // nebude uložena

        // Upozornění po věk
        binding.ageEt.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                binding.ageTil.error = null
            }
            else if (text.toString().toInt() <= 0) {
                binding.ageTil.error = getString(R.string.wrong_input_error_msg)
            }
            else {
                binding.ageTil.error = null
            }
        }

        // Upozornění pro bodování
        binding.scoreEt.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                binding.scoreTil.error = null
            }
            else if (text.toString().toInt() < 0) {
                binding.scoreTil.error = getString(R.string.wrong_input_error_msg)
            }
            else {
                binding.scoreTil.error = null
            }
        }

        // Upozornění pro hmotnost
        binding.weightEt.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                binding.weightTil.error = null
            }
            else if (text.toString().toInt() <= 0) {
                binding.weightTil.error = getString(R.string.wrong_input_error_msg)
            }
            else {
                binding.weightTil.error = null
            }
        }

        // Upozornění po poznámku
        binding.noteEt.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                binding.noteTil.error = null
            }
            else if (text.toString().length > R.integer.max_note_length) {
                binding.noteTil.error = getString(R.string.wrong_input_error_msg)
            }
            else {
                binding.noteTil.error = null
            }
        }
    }

    // Inicializace dropdown menu pro pohlaví -> musím být uveda i v metodě onResume()
    override fun onResume() {
        super.onResume()
        if (!isEditing) {
            binding.genderActv.setText(Gender.UNKNOWN.toString())
        }
        val genderStrings = arrayOf(
            Gender.MALE.toString(),
            Gender.FEMALE.toString(),
            Gender.UNKNOWN.toString()
        )
        val adapter = ArrayAdapter(this, R.layout.gender_dropdown_item, genderStrings)
        binding.genderActv.setAdapter(adapter)
    }

    // Metody pro extrakci veškerého infa ->
    // pokud uživatel nezadá dané info, tak bereme hodnoty null
    // Jen zvěř,způsob lovu a lokace musí být vybrány -> to budu při vytváření kontrolovat

    // Získání údajů a vytvoření definovaného objektu
    private fun createDefinedItem(): HuntingItem {
        val date: LocalDate = dateExtraction()
        val time: LocalTime = timeExtraction()
        val photoBitmap: Bitmap? = photoExtraction()
        var imageFileName: String? = null
        val nightVisionIsUse = binding.nightVisionIsUseChb.isChecked
        val dogIsUse = binding.dogIsUseChb.isChecked
        val moreHuntersAreHunting = binding.accompanimentAtTheHuntChb.isChecked
        val animal = Animal.values()[animalAdapter.chosenItemPosition]
        val huntingMethod = HuntingMethod.values()[methodAdapter.chosenItemPosition]
        val location = locationExtraction()
        val hunterName = hunterNameExtraction()
        val age = ageExtraction()
        val weight = weightExtraction()
        val score = scoreExtraction()
        val gender = genderExtraction()
        val note = noteExtraction()
        val id = WholeAppMethodsSingleton.getHuntingItemNewID(this)

        photoBitmap?.let { bmp ->
            // První vygeneruji název fotky a uložím ji do zařízení
            val imageNumber = WholeAppMethodsSingleton.generateImageNumber(this)
            imageFileName = MainActivity.ALL_IMAGES_PREFIX + imageNumber.toString()
            WholeAppMethodsSingleton.savePhotoIntoExternalStorage(this, imageFileName!!, bmp)
        }

        return HuntingItem(
            date,
            time,
            imageFileName,
            nightVisionIsUse,
            dogIsUse,
            moreHuntersAreHunting,
            animal,
            huntingMethod,
            location.latitude,
            location.longitude,
            hunterName,
            age,
            weight,
            score,
            gender,
            note,
            id
        )
    }

    // Funkce, která se stará o správné přiřazení typu markeru podle zvířete
    private fun getCorectMarkerType(animal: Animal): MarkerType {
        val huntingMarkers = MarkerType.values().takeLast(MainActivity.HUNTER_MARKERS_COUNT)
        return  huntingMarkers.get(Animal.values().indexOf(animal))
    }

    // Funkce, která vytvoří nový marker k dané položce -> zatím s defaultní lokací
    private fun createHuntingMarkerForHuntingItem(item: HuntingItem): Marker {
        val correctMarkerType = getCorectMarkerType(item.animal)
        return Marker(
            correctMarkerType,
            LatLng(item.locationLat, item.locationLng),
            null,
            null,
            item.id,
            WholeAppMethodsSingleton.getMarkerNewID(this)
        )
    }

    // Funkce, která upraví marker, který patří k dané položce
    private fun editHuntingMarker(item: HuntingItem) {
        val marker = markerViewModel.getMarkerByAssociatedId(item.id)
        marker.location = LatLng(item.locationLat, item.locationLng)
        marker.markerType = getCorectMarkerType(item.animal)
        markerViewModel.updateMarker(marker)
    }

    private fun editItem(item: HuntingItem) {
        item.date = dateExtraction()
        item.time = timeExtraction()

        // Editace fotky -> mohou nastat 4 případy
        val photo = photoExtraction()
        // Buď item obsahoval fotku a ta byla teď odebrána
        if (item.imageFileName != null && photo == null) {
            WholeAppMethodsSingleton
                .deletePhotoFromExternalStorage(this, item.imageFileName!!)
            item.imageFileName = null
        }
        // Nebo item měl svoji fotku a ta byla změněna
        else if(item.imageFileName != null && photo != null) {
            val bmp: Bitmap? = WholeAppMethodsSingleton
                .loadPhotoFromExternalStorage(this, item.imageFileName!!)

            if (bmp != photo) {
                WholeAppMethodsSingleton
                    .deletePhotoFromExternalStorage(this, item.imageFileName!!)
                WholeAppMethodsSingleton
                    .savePhotoIntoExternalStorage(this, item.imageFileName!!, photo)
            }
            // A pokud se jedná o tu samou fotku neudělá se nic
        }
        // Nebo item neměl žádnou fotku a ta byla přidána
        else if (item.imageFileName == null && photo != null) {
            val imageNumber = WholeAppMethodsSingleton.generateImageNumber(this)
            val imageFileName = MainActivity.ALL_IMAGES_PREFIX + imageNumber.toString()
            WholeAppMethodsSingleton.savePhotoIntoExternalStorage(this, imageFileName, photo)
            item.imageFileName = imageFileName
        }
        // Nebo neměl item fotku a ta nebyla ani změněna -> neuděláme nic

        // Musíme ověřit, zda je poloh v pořádku
        if (isRightLocationInput(binding.locationEt.text.toString())) {
            val newLocation = locationExtraction()
            item.locationLat = newLocation.latitude
            item.locationLng = newLocation.longitude
        }

        item.note = noteExtraction()
        item.nightVisionIsUse = binding.nightVisionIsUseChb.isChecked
        item.dogIsUse = binding.dogIsUseChb.isChecked
        item.accompanimentAtTheHunt = binding.accompanimentAtTheHuntChb.isChecked
        item.animal = Animal.values()[animalAdapter.chosenItemPosition]
        item.huntingMethod = HuntingMethod.values()[methodAdapter.chosenItemPosition]
        val newLocation = locationExtraction()
        item.locationLat = newLocation.latitude
        item.locationLng = newLocation.longitude
        item.hunterName = hunterNameExtraction()
        item.age = ageExtraction()
        item.scoreEvaluation = scoreExtraction()
        item.weight = weightExtraction()
        item.gender = genderExtraction()
    }

    private fun dateExtraction(): LocalDate {
        val dateSplitStrings = binding.dateTv.text.split(". ")
        val day: Int = dateSplitStrings.first().toInt()
        val month: Int = dateSplitStrings[1].toInt()
        val year: Int = dateSplitStrings[2].toInt()
        return LocalDate.of(year, month, day)
    }

    private fun timeExtraction(): LocalTime {

        val timeSplitStrings = binding.timeTv.text.split(":").toMutableList()
        // Správný formát minut
        if (timeSplitStrings[1].first() == '0') {
            timeSplitStrings[1] = timeSplitStrings[1].drop(1)
        }
        val hours: Int = timeSplitStrings.first().toInt()
        val minutes: Int = timeSplitStrings[1].toInt()
        return LocalTime.of(hours, minutes)
    }

    private fun photoExtraction(): Bitmap? {
        return currentPhotoBmp
    }

    private fun hunterNameExtraction(): String? {
        val str = binding.hunterNameEt.text.toString()

        return if (
            str.isEmpty()
            || str.length > resources.getInteger(R.integer.max_hunter_name_length)
        ) {
            null
        }
        else {
            str
        }
    }

    private fun locationExtraction(): LatLng {
        val splitStrings = binding.locationEt.text.toString().split(", ")
        val lat = splitStrings[0].toDouble()
        val lng = splitStrings[1].toDouble()
        return LatLng(lat, lng)
    }

    private fun isRightLocationInput(str: String): Boolean {
        if (str.isEmpty()) {
            return false
        }
        val splitStrings = str.split(", ")
        val lat = splitStrings[0].toDouble()
        val lng = splitStrings[1].toDouble()
        return !(splitStrings.size != 2
                || lat <= -90
                || lat >= 90
                || lng <= -180
                || lng >= 180)
    }

    private fun ageExtraction(): Int? {
        val str = binding.ageEt.text.toString()
        return if (
            str.isEmpty()
            || str.toInt() <= 0
            || str.length > resources.getInteger(R.integer.max_age_length)
        ) {
            null
        }
        else {
            str.toInt()
        }
    }

    private fun weightExtraction(): Int? {
        val str = binding.weightEt.text.toString()

        return if (
            str.isEmpty()
            || str.toInt() <= 0
            || str.length > resources.getInteger(R.integer.max_weight_length)
        ) {
            null
        }
        else {
            str.toInt()
        }
    }

    private fun scoreExtraction(): Int? {
        val str = binding.scoreEt.text.toString()

        return if (
            str.isEmpty()
            || str.toInt() < 0
            || str.length > resources.getInteger(R.integer.max_score_length)
        ) {
            null
        }
        else {
            str.toInt()
        }
    }

    private fun genderExtraction(): Gender {
        val str = binding.genderActv.text.toString()

        return if (str == Gender.FEMALE.toString()) {
            Gender.FEMALE
        }
        else if (str == Gender.MALE.toString()) {
            Gender.MALE
        }
        else {
            Gender.UNKNOWN
        }
    }

    private fun noteExtraction(): String? {
        val note = binding.noteEt.text.toString()

        return if (
            note.isEmpty()
            || note.length > resources.getInteger(R.integer.max_note_length)
        ) {
            null
        }
        else {
            note
        }
    }

    // Kontrola veškerého potřebného infa -> metody a zvěře
    private fun checkAllRequiredInfo(): Boolean {
        return animalAdapter.chosenItemPosition != RecyclerView.NO_POSITION
                && methodAdapter.chosenItemPosition != RecyclerView.NO_POSITION
                && isRightLocationInput(binding.locationEt.text.toString())
    }

    // Inicializace ViewModelu (přístupu k databázi)
    private fun initializeViewModels() {
        huntingViewModel = ViewModelProvider(this).get(HuntingViewModel::class.java)
        markerViewModel = ViewModelProvider(this).get(MarkerViewModel::class.java)
    }
}