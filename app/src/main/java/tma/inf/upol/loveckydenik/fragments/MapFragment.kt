package tma.inf.upol.loveckydenik.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.AddHuntingItemActivity
import tma.inf.upol.loveckydenik.activities.HuntingItemDetail
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.classes.Marker
import tma.inf.upol.loveckydenik.database.HuntingViewModel
import tma.inf.upol.loveckydenik.database.MarkerViewModel
import tma.inf.upol.loveckydenik.dialogs.CreateMarkerDialog
import tma.inf.upol.loveckydenik.dialogs.EditMarkerDialog
import tma.inf.upol.loveckydenik.enums.Animal
import tma.inf.upol.loveckydenik.enums.HuntingMethod
import tma.inf.upol.loveckydenik.enums.MarkerType
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton


class MapFragment
    : Fragment(),
    EasyPermissions.PermissionCallbacks,
    OnMapReadyCallback,
    CreateMarkerDialog.MarkerDialogListener,
    EditMarkerDialog.EditDialogListener {

    // Proměnné pro obsluhu mapy
    private var map: MapboxMap? = null
    private lateinit var currentMapStyle: Style
    private lateinit var currentMapStyleName: String
    private lateinit var symbolsForMarkers: MutableList<Symbol>

    private var isAddingMarker = false
    private var isAddingHarvest = false

    private var currentMarkersLiveData: LiveData<MutableList<Marker>>? = null

    private var currentMarkerVisualization: Symbol? = null
    private var currentViewedHuntingItem: HuntingItem? = null
    private var clickedHuntingMarkerId: Long? = null
    private var symbolManager: SymbolManager? = null


    // Databáze
    private lateinit var markerViewModel: MarkerViewModel
    private lateinit var huntingViewModel: HuntingViewModel

    // Komponenty z layoutu
    private lateinit var mapView: MapView
    private lateinit var locationBtn: FloatingActionButton
    private lateinit var changeMapBtn: FloatingActionButton
    private lateinit var infoText: TextView
    private lateinit var addMarkerBtn: FloatingActionButton
    private lateinit var addHarvestBtn: FloatingActionButton
    private lateinit var aimCrossImage: ImageView
    private lateinit var addingMarkerWindow: ConstraintLayout
    private lateinit var dropPinBtn: FloatingActionButton
    private lateinit var cancelAddingMarkerBtn: ImageView
    private lateinit var logLatInfoTv: TextView
    private lateinit var addMarkerDoneBtn: Button
    private lateinit var huntingMarkerWindowCancelBtn: ImageView
    private lateinit var huntingMarkerWindow: ConstraintLayout
    private lateinit var huntingMarkerWindowAnimalTv: TextView
    private lateinit var huntingMarkerWindowMethodTv: TextView
    private lateinit var huntingMarkerWindowMarkerTypeIv: ImageView
    private lateinit var huntingMarkerWindowMoreBtn: Button
    private lateinit var addingHarvestMarkerDoneBtn: Button
    private lateinit var selectOnlyLocationButton: Button


    // Zaregistrování aktivit pro smazání/ editování lovecké položky přes mapu
    private val detailViewItemActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val oldMarker = getMarkerByKey(clickedHuntingMarkerId!!.toFloat())
            oldMarker?.let {
                val marker = markerViewModel.getMarkerById(it.id)
                if (marker == null) {
                    setCameraToLocation(oldMarker.location)
                }
                else {
                    setCameraToLocation(marker.location)
                }
            }
            exitHuntingMarkerWindow()
        }

    private val addingHarvestMarkerActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Zde nemusím dělat nic -> při vytváření položky se vytvoří marker i lovecká položka
            // a při opětovném načtení mapy se o vizualizaci postará objekt LiveData
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializace mapbox mapy -> musí být před nastavením View
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_public_token))
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.map_view)
        mapView.getMapAsync(this)

        // Inicializace všech komponent
        locationBtn = view.findViewById(R.id.location_button)
        infoText = view.findViewById(R.id.map_fragment_info_text)
        changeMapBtn = view.findViewById(R.id.change_map_button)
        addMarkerBtn = view.findViewById(R.id.add_marker_button)
        addHarvestBtn = view.findViewById(R.id.add_harvest_button)
        aimCrossImage = view.findViewById(R.id.aim_cross_image)
        addingMarkerWindow = view.findViewById(R.id.adding_marker_window)
        dropPinBtn = view.findViewById(R.id.drop_pin_button)
        cancelAddingMarkerBtn = view.findViewById(R.id.adding_marker_cancel_button)
        logLatInfoTv = view.findViewById(R.id.log_lat_info_tv)
        addMarkerDoneBtn = view.findViewById(R.id.add_marker_done_button)
        huntingMarkerWindow = view.findViewById(R.id.hunting_marker_window)
        huntingMarkerWindowAnimalTv = view.findViewById(R.id.hunting_marker_window_animal_tv)
        huntingMarkerWindowMethodTv = view.findViewById(R.id.hunting_marker_window_method_tv)
        huntingMarkerWindowCancelBtn = view.findViewById(R.id.hunting_marker_window_cancel_button)
        huntingMarkerWindowMoreBtn = view.findViewById(R.id.hunting_marker_window_more_button)
        huntingMarkerWindowMarkerTypeIv = view.findViewById(R.id.hunting_marker_window_marker_type_iv)
        addingHarvestMarkerDoneBtn = view.findViewById(R.id.add_harvest_done_button)
        selectOnlyLocationButton = view.findViewById(R.id.select_only_location_button)

        // Vyžádání povolení
        if (!hasAllNecessaryMapPermissions()) {
            requestAllNecessaryMapPermissions()
        }

        // Nastavení správného pohledu vzhledem k udělení oprávnění
        setCorrectView()

        // Inicializace přístupu k databázi
        initializeViewModels()

        // Navázání akci na tlačítko lokace / neboli vycentrování
        locationBtn.setOnClickListener {
            val location = map!!.locationComponent.lastKnownLocation
            location?.let {
                setCameraToLocation(LatLng(location.latitude, location.longitude))
            }
        }

        // Akce po stisknutí na změnění stylu mapy
        changeMapBtn.setOnClickListener {
            if (currentMapStyleName == Style.SATELLITE_STREETS) {
                changeMapStyle(Style.OUTDOORS)
                YoYo.with(Techniques.SlideInUp)
                    .duration(MainActivity.ANIMATION_DURATION_TIME)
                    .playOn(mapView)
            }
            else {
                changeMapStyle(Style.SATELLITE_STREETS)
                YoYo.with(Techniques.SlideInUp)
                    .duration(MainActivity.ANIMATION_DURATION_TIME)
                    .playOn(mapView)
            }
        }

        // Akce stisknutí na přídání markeru
        addMarkerBtn.setOnClickListener {
            if (isAddingMarker) {
                exitAddingMarkerAction()
            }
            else {
                enterAddingMarkerAction()
            }
        }

        // Akce na stisknutí přidání polygonu
        addHarvestBtn.setOnClickListener {
            if (isAddingHarvest) {
                exitAddingHarvestMarkerAction()
            }
            else {
                enterAddingHarvestMarkerAction()
            }
        }

        // Akce stisknutí tlačítka cancel v okně pro přidání markeru
        cancelAddingMarkerBtn.setOnClickListener {
            if (isAddingMarker) {
                exitAddingMarkerAction()
            }
            if (isAddingHarvest) {
                exitAddingHarvestMarkerAction()
            }
        }

        // Akce po stisknutí na drop pin tlačítko
        dropPinBtn.setOnClickListener {
            currentMarkerVisualization?.let {
                symbolManager!!.delete(currentMarkerVisualization)

            }
            val position = map!!.cameraPosition.target
            val latitude = position.latitude
            val longitude = position.longitude
            val symbolOptions = SymbolOptions()
                .withLatLng(position)
                .withIconImage(MarkerType.CUSTOM.toString())
                .withIconSize(MainActivity.MARKER_ICON_SIZE)
                .withSymbolSortKey(MainActivity.MARKER_VISUALIZATION_KEY)

            currentMarkerVisualization = symbolManager!!.create(symbolOptions)
            logLatInfoTv.setText("$latitude, $longitude")
        }

        // Akce po stisknutí určení pozice markeru
        addMarkerDoneBtn.setOnClickListener {
            if (currentMarkerVisualization != null) {
                val markerPosition = currentMarkerVisualization!!.latLng
                symbolManager!!.delete(currentMarkerVisualization)
                exitAddingMarkerAction()
                openMarkerDialog(markerPosition)
            }
            else {
                YoYo.with(Techniques.Tada)
                    .duration(MainActivity.ANIMATION_DURATION_TIME)
                    .repeat(1)
                    .playOn(logLatInfoTv)
            }
        }

        // Akce po stisknutí tlačítka done pro přidání hunting markeru
        // -> předá se lokace a nasměruje se do aktivity, která přidává item
        addingHarvestMarkerDoneBtn.setOnClickListener {
            if (currentMarkerVisualization != null) {
                val markerPosition = currentMarkerVisualization!!.latLng
                symbolManager!!.delete(currentMarkerVisualization)
                exitAddingMarkerAction()
                val intent = Intent().apply {
                    setClass(this@MapFragment.requireContext(), AddHuntingItemActivity::class.java)
                    putExtra(MainActivity.LOCATION_EXTRA_KEY, true)
                    putExtra(MainActivity.LONGITUDE_EXTRA_KEY, markerPosition.longitude)
                    putExtra(MainActivity.LATITUDE_EXTRA_KEY, markerPosition.latitude)
                }
                addingHarvestMarkerActivityLauncher.launch(intent)
            }
            else {
                YoYo.with(Techniques.Tada)
                    .duration(MainActivity.ANIMATION_DURATION_TIME)
                    .repeat(1)
                    .playOn(logLatInfoTv)
            }
        }

        // Akce po stisknutí tlačítka cancel v okně detailu hunting markeru
        huntingMarkerWindowCancelBtn.setOnClickListener {
            exitHuntingMarkerWindow()
        }

        // Akce po stisknutí tlačítka more v hunting marker window
        huntingMarkerWindowMoreBtn.setOnClickListener {
            currentViewedHuntingItem?.let { item ->
                val intent = Intent().apply {
                    setClass(this@MapFragment.requireContext(), HuntingItemDetail::class.java)
                    putExtra(MainActivity.ITEM_KEY, item)
                }
                detailViewItemActivityLauncher.launch(intent)
            }
        }

        // Akce po stisknutí tlačítka pro potvrzení vybrání polohy
        selectOnlyLocationButton.setOnClickListener {
            if (currentMarkerVisualization != null) {
                val locationString = logLatInfoTv.text.toString().split(", ")

                val intent = Intent().apply {
                    putExtra(MainActivity.LATITUDE_EXTRA_KEY, locationString[0])
                    putExtra(MainActivity.LONGITUDE_EXTRA_KEY, locationString[1])
                }
                exitSelectingLocationForHuntingItem()
                MainActivity.isSelectingLocation = false
                activity?.setResult(RESULT_OK, intent)
                activity?.finish()
            }
            else {
                YoYo.with(Techniques.Tada)
                    .duration(MainActivity.ANIMATION_DURATION_TIME)
                    .repeat(1)
                    .playOn(logLatInfoTv)
            }
        }

        // Pokud se odstatrovalala aktivita pro vybrání polohy musíme připtavit uživatelské rozhraní
        if (MainActivity.isSelectingLocation) {
            enterSelectingLocationForHuntingItem()
        }

        // Vrácení výsledného vzhledu
        return view
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Funkce týkající se mapy

    // Metoda pro prvotní nastavení mapy -> až bude nachystaná
    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        map!!.uiSettings.setCompassFadeFacingNorth(false)
        map!!.setStyle(Style.OUTDOORS) { style ->
            currentMapStyle = style
            currentMapStyleName = Style.OUTDOORS
            enableLocationComponent(style)
            inititializeManagers(style)
        }
        setCorrectView()
    }

    // Povolení lokace -> pomocí location component
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        if (hasAllNecessaryMapPermissions()) {
            // Vytvoříme nastavení pro locationComponentu
            val locationComponentOptions = LocationComponentOptions.builder(requireContext())
                .trackingGesturesManagement(true)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                requireContext(),
                style
            )
                .locationComponentOptions(locationComponentOptions)
                .build()

            // Získáme instanci componenty
            map!!.locationComponent.apply {
                // Aktivace local komponenty s mým nastavením
                activateLocationComponent(locationComponentActivationOptions)

                // Zde nemusíme kontrolovat ověření díky if statmentu
                isLocationComponentEnabled = true

                // Nastavení kamery
                cameraMode = CameraMode.TRACKING

                // Nastavení rendrování
                renderMode = RenderMode.COMPASS
            }

            // Nastavíme pozici kamery
            val location = map!!.locationComponent.lastKnownLocation
            location?.let {
                setCameraToLocation(LatLng(location.latitude, location.longitude))
            }

        }
    }

    // Změna stylu mapy -> musí se načíst všechny ikony a inicializovat všichni manažeři
    private fun changeMapStyle(styleName: String) {
        map!!.setStyle(styleName) { style ->
            currentMapStyle = style
            currentMapStyleName = styleName
            changeColorOfAimCross(styleName)
            enableLocationComponent(style)
            inititializeManagers(style)
        }
    }

    // Funkce pro animaci kamery na uživatelovu pozici
    private fun setCameraToLocation(location: LatLng) {
        val position = CameraPosition.Builder()
            .target(location)
            .zoom(13.0)
            .bearing(0.0)
            .tilt(0.0)
            .build()
        map!!.animateCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    // Inicializace potřebných manageru
    private fun inititializeManagers(style: Style) {
        // Vymažu všechny předešlé markery
        symbolManager?.deleteAll()

        symbolManager = SymbolManager(mapView, map!!, style, MainActivity.ID_MAP_LAYER).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
            iconIgnorePlacement = true
        }

        // Nastaveni akce po kliknutí na nějaký
        symbolManager!!.addClickListener { symbol ->
            if (symbol.symbolSortKey == MainActivity.MARKER_VISUALIZATION_KEY) {
                // Nedělám nic
            }
            else {
                val clickedMarker = getMarkerByKey(symbol.symbolSortKey)

                clickedMarker?.let {
                    if(isAddingHarvest) {
                        exitAddingHarvestMarkerAction()
                    }
                    if (isAddingMarker) {
                        exitAddingMarkerAction()
                    }
                    // Rozdílné akce podle toho, který marker se zobrazuje
                    if (isHuntingItemMarker(it)) {
                        clickedHuntingMarkerId = clickedMarker.id
                        prepareDetailViewOfHuntingItem(it)
                    }
                    else {
                        EditMarkerDialog(clickedMarker, this).show(parentFragmentManager, "")
                    }
                }
            }
            false
        }

        // Načtení ikon pro markery abychom mohli umisťovat symboli na mapu
        loadAllIcons()

        // Inicializuji list symbolů, který budu používat pro update ikon
        symbolsForMarkers = mutableListOf()

        // Načtení všech markerů z databáze + zobrazení na mapě
        currentMarkersLiveData?.removeObservers(viewLifecycleOwner)
        currentMarkersLiveData =  markerViewModel.getAllMarkers
        currentMarkersLiveData!!.observe(viewLifecycleOwner, Observer { markers ->
            symbolManager!!.deleteAll()
            for (marker in markers) {
                createSymbol(marker)
            }
        })
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------
    // Funkce pracující se symboly

    // Funkce, která vytvoří symbol a umístí jej do mapy -> funkce vrací id vytvořenéjo symbolu
    private fun createSymbol(marker: Marker){
        val symbolOptions = SymbolOptions()
            .withLatLng(marker.location)
            .withIconImage(marker.markerType.toString())
            .withIconSize(MainActivity.MARKER_ICON_SIZE)
            .withSymbolSortKey(marker.id.toFloat())
        val symbol = symbolManager!!.create(symbolOptions)
        symbolsForMarkers.add(symbol)
    }

    // Funkce, která smaže symbol z mapy -> funkce nic nevrací, jelikož se smaže i celý marker
    private fun deleteSymbol(marker: Marker) {
        val symbolToDelete = findRightSymbolForMarker(marker)
        symbolManager!!.delete(symbolToDelete)
        symbolsForMarkers.remove(symbolToDelete)
    }

    // Funkce, která obstará update symbolu -> opět vrací id nového symbolu, který bude spjat s markerem
    private fun updateSymbol(marker: Marker) {
        val symbol = findRightSymbolForMarker(marker)
        symbolsForMarkers.remove(symbol)

        symbol?.iconImage = marker.markerType.toString()
        symbol?.let {
            symbolsForMarkers.add(it)
            symbolManager!!.update(it)
        }
    }

    // Funkce, která najde správný symbol k markeru
    private fun findRightSymbolForMarker(marker: Marker): Symbol? {
        for (index in 0..symbolsForMarkers.size) {
            val symbol = symbolsForMarkers[index]
            if (symbol.symbolSortKey == marker.id.toFloat()) {
                return symbol
            }
        }
        return null
    }

    // Funkce, která rozezná o jaký typ markeru se jedná
    private fun isHuntingItemMarker(marker: Marker): Boolean {
        for (markerType in MarkerType.values().takeLast(MainActivity.HUNTER_MARKERS_COUNT)) {
            if (marker.markerType == markerType) {
                return true
            }
        }
        return false
    }

    // Funkce, která najde marker pomocí symbol klíče
    private fun getMarkerByKey(key: Float): Marker? {
        val markers = markerViewModel.getAllMarkers.value
        if (markers != null) {
            for (marker in markers) {
                if (key.toLong() == marker.id) {
                    return marker
                }
            }
        }
        return null
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------
    // Funkce, které zpracovávají data z dialogů + vytváření dialogů
    // Funkce, pomocí které předávám všechy potřebné informace k vytvoření markeru
    override fun processMarkerInformations(
        name: String,
        note: String,
        markerType: MarkerType,
        position: LatLng,
    ) {
        // Vytvoření markeru
        val marker = Marker (
            markerType,
            position,
            if (name.isEmpty()) null else name,
            if (note.isEmpty()) null else note,
            null,
            WholeAppMethodsSingleton.getMarkerNewID(requireContext())
        )

        // Uložení do DB
        markerViewModel.insertMarker(marker)
    }

    // Funkce, která se stará o zpracování změn u markeru
    override fun handleMarkerChanges(marker: Marker, isDeleting: Boolean) {
        if (isDeleting) {
            deleteSymbol(marker)
            markerViewModel.deleteMarker(marker)
        }
        else {
            // Prvně změny promítneme do databáze
            markerViewModel.updateMarker(marker)
            updateSymbol(marker)
        }
    }

    // Funkce, která inicializuje dialog pro vytvoření markeru
    private fun openMarkerDialog(position: LatLng) {
        // Počáteční inicializace
        CreateMarkerDialog(position, this)
            .show(parentFragmentManager, "")
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    // Funkce, které se starají o správné nastavení layoutu -> animace, viditelnost component
    // Funkce, která upraví layout pro přidání markeru
    private fun editLayoutForAddingMarker() {
        addingMarkerWindow.visibility = View.VISIBLE
        dropPinBtn.visibility = View.VISIBLE
        aimCrossImage.visibility = View.VISIBLE

        YoYo.with(Techniques.SlideInUp)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(addingMarkerWindow)

        YoYo.with(Techniques.SlideInRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(dropPinBtn)

        YoYo.with(Techniques.SlideInUp)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(aimCrossImage)

        YoYo.with(Techniques.SlideOutRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(locationBtn)
    }

    // Funkce, která se postará o připravení prostředí pro přidání markeru do mapy
    private fun enterAddingMarkerAction() {
        editLayoutForAddingMarker()
        isAddingMarker = true
        addMarkerBtn.isSelected = true
    }

    // Funkce, která vrátí vše do původního stavu po ukončení přidávání markeru
    private fun exitAddingMarkerAction() {
        isAddingMarker = false
        addMarkerBtn.isSelected = false
        currentMarkerVisualization?.let {
            symbolManager!!.delete(it)
        }
        currentMarkerVisualization = null
        editLayoutForExitAddingMarker()
    }

    // Funkce, která opět nastaví layout do původního stavu
    private fun editLayoutForExitAddingMarker() {

        logLatInfoTv.setText(getString(R.string.default_location_text))

        YoYo.with(Techniques.SlideOutDown)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(addingMarkerWindow)

        YoYo.with(Techniques.SlideOutRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(dropPinBtn)

        YoYo.with(Techniques.SlideOutDown)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(aimCrossImage)

        YoYo.with(Techniques.SlideInRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(locationBtn)
    }

    // Funkce, která se postará o zobrazení UI pro přidání loveckého markeru
    private fun editLayoutForAddingHarvestMarker() {
        addMarkerDoneBtn.visibility = View.GONE
        addingHarvestMarkerDoneBtn.visibility = View.VISIBLE
        editLayoutForAddingMarker()
    }

    // Funkce, která se postará o vrácení všech změn po přidání loveckého markeru
    private fun exitLayoutForAddingHarvestMarker() {
        addingHarvestMarkerDoneBtn.visibility= View.GONE
        addMarkerDoneBtn.visibility= View.VISIBLE
        exitAddingMarkerAction()
    }

    // Funkce, která nastaví proměnné pro přidání loveckého markeru
    private fun enterAddingHarvestMarkerAction() {
        isAddingHarvest = true
        addHarvestBtn.isSelected = true
        editLayoutForAddingHarvestMarker()
    }

    // Funkce, která nastaví layout pouze pro vybrání polohy
    private fun enterSelectingLocationForHuntingItem() {
        editLayoutForAddingMarker()
        // Také tlačítka v dialogu
        cancelAddingMarkerBtn.visibility = View.INVISIBLE
        addMarkerDoneBtn.visibility = View.INVISIBLE
        selectOnlyLocationButton.visibility = View.VISIBLE
    }

    // Funkce, která opět vrázi vše dopořádku po předání polohy
    private fun exitSelectingLocationForHuntingItem() {
        exitHuntingMarkerWindow()
        addMarkerBtn.visibility = View.VISIBLE
        locationBtn.visibility = View.VISIBLE
        addHarvestBtn.visibility = View.VISIBLE
        changeMapBtn.visibility = View.VISIBLE
        // Také tlačítka v dialogu
        addMarkerDoneBtn.visibility = View.VISIBLE
        selectOnlyLocationButton.visibility = View.GONE
        cancelAddingMarkerBtn.visibility = View.VISIBLE
    }

    // Funkce, která změní proměnné do původního stavu po přidání hunting markeru
    private fun exitAddingHarvestMarkerAction() {
        isAddingHarvest = false
        addHarvestBtn.isSelected = false
        exitLayoutForAddingHarvestMarker()
    }

    // Funkce, která zajistí zobrazení okna pro detail loveckého markeru
    private fun prepareDetailViewOfHuntingItem(marker: Marker) {
        addingMarkerWindow.visibility = View.INVISIBLE
        if (huntingMarkerWindow.visibility == View.VISIBLE) {
            YoYo.with(Techniques.SlideOutDown)
                .duration(MainActivity.ANIMATION_DURATION_TIME)
                .playOn(huntingMarkerWindow)
        }
        currentViewedHuntingItem = huntingViewModel.getItemByID(marker.associatedItemID!!)
        val rightMethodString = resources.getStringArray(R.array.hunting_methods_strings)
            .get(HuntingMethod.values().indexOf(currentViewedHuntingItem!!.huntingMethod))
        val rightAnimalString = resources.getStringArray(R.array.animals_strings)
            .get(Animal.values().indexOf(currentViewedHuntingItem!!.animal))
        huntingMarkerWindowMethodTv.setText(rightMethodString)
        huntingMarkerWindowAnimalTv.setText(rightAnimalString)
        huntingMarkerWindowMarkerTypeIv.setImageResource(marker.markerType.icon)

        YoYo.with(Techniques.SlideInDown)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(huntingMarkerWindow)

        // Tlačítka na kraji mapy odděláme
        YoYo.with(Techniques.SlideOutRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(changeMapBtn)

        YoYo.with(Techniques.SlideOutRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(addHarvestBtn)

        YoYo.with(Techniques.SlideOutRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(addMarkerBtn)

        huntingMarkerWindow.visibility = View.VISIBLE
    }

    // Funkce, která se postará o ukončení všeho ohledně hunting marker window
    private fun exitHuntingMarkerWindow() {
        // Skryjeme huntingMarkerWindow
        currentViewedHuntingItem = null
        clickedHuntingMarkerId = null
        YoYo.with(Techniques.SlideOutUp)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(huntingMarkerWindow)

        // Obnovíme opět tlačítka na boku mapy
        YoYo.with(Techniques.SlideInRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(changeMapBtn)

        YoYo.with(Techniques.SlideInRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(addMarkerBtn)

        YoYo.with(Techniques.SlideInRight)
            .duration(MainActivity.ANIMATION_DURATION_TIME)
            .playOn(addHarvestBtn)
    }

    // Funkce pro změnu barvy křížku na zadávání bodu
    private fun changeColorOfAimCross(styleName: String) {

        if (styleName == Style.SATELLITE_STREETS) {
            aimCrossImage.setColorFilter(Color.WHITE)
        }
        else {
            aimCrossImage.setColorFilter(Color.BLACK)
        }
    }

    // Metoda pro správné zobrazení, pokud nejsou udělena oprávnění
    private fun setCorrectView() {
        if (!hasAllNecessaryMapPermissions()) {
            infoText.visibility = View.VISIBLE
            mapView.visibility = View.INVISIBLE
            locationBtn.visibility = View.INVISIBLE
            changeMapBtn.visibility = View.INVISIBLE
            addMarkerBtn.visibility = View.INVISIBLE
            addHarvestBtn.visibility = View.INVISIBLE
        }
        else {
            infoText.visibility = View.INVISIBLE
            mapView.visibility = View.VISIBLE
            locationBtn.visibility = View.VISIBLE
            changeMapBtn.visibility = View.VISIBLE
            addMarkerBtn.visibility = View.VISIBLE
            addHarvestBtn.visibility = View.VISIBLE
        }
        if (MainActivity.isSelectingLocation) {
            addHarvestBtn.visibility = View.INVISIBLE
            addMarkerBtn.visibility = View.INVISIBLE
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    // Funkce zajišťující povolení

    // Ověření povolení
    private fun hasAllNecessaryMapPermissions(): Boolean {
        return hasInternetPermission() &&
                hasAccessNetworkPermission() &&
                hasFineLocationPermission() &&
                hasCoarseLocationPermission()
    }

    private fun hasCoarseLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun hasFineLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun hasInternetPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.INTERNET
        )
    }

    private fun hasAccessNetworkPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }

    private fun requestAllNecessaryMapPermissions() {
        EasyPermissions.requestPermissions(
            this,
            // Zpráva, když uživatel odmítne
            requireContext().resources.getString(R.string.map_permissions_needed_text),
            MainActivity.REQUEST_PERMISSION_CODE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // Metody pro reakci na ne/udělení oprávnění
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        setCorrectView()
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
        else {
            requestAllNecessaryMapPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        map?.let {
            onMapReady(map!!)
        }
        setCorrectView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    //Ostatní funkce

    // Přepsání metod životního cyklu mapy
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    // Funkce pro načtení všech potřebných ikon
    private fun loadAllIcons() {
        for(markerType in MarkerType.values()) {
            currentMapStyle.addImage(markerType.toString(), resources.getDrawable(markerType.icon))
        }
    }

    // Inicializace přístupu k databázi
    private fun initializeViewModels() {
        markerViewModel = ViewModelProvider(this).get(MarkerViewModel::class.java)
        huntingViewModel = ViewModelProvider(this).get(HuntingViewModel::class.java)
    }
}