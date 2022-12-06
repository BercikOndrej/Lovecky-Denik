package tma.inf.upol.loveckydenik.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.prolificinteractive.materialcalendarview.CalendarDay
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.AddEventActivity
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.adapters.EventAdapter
import tma.inf.upol.loveckydenik.calendardecorators.*
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import tma.inf.upol.loveckydenik.database.EventViewModel
import tma.inf.upol.loveckydenik.databinding.FragmentCalendarBinding
import tma.inf.upol.loveckydenik.singletons.CalendarMethodsSingleton
import java.time.LocalDate
import java.util.*

class CalendarFragment : Fragment(), EventAdapter.OnEventClickListener, EasyPermissions.PermissionCallbacks {

    // View binding
    private lateinit var binding: FragmentCalendarBinding

    // Přístup do databáze
    private lateinit var eventViewModel: EventViewModel

    // Uchovám si mnou upravený adaptér abych mohl měnit hodnoty za běhu aplikace
    private lateinit var eventAdapter: EventAdapter
    private var currentLiveData: LiveData<MutableList<CalendarEvent>>? = null
    private lateinit var singleDayEventDecorator: SingleDayEventDecorator
    private lateinit var multipleDayEventDecorator: MultipleDayEventDecorator
    private lateinit var endDayEventDecorator: EndDayEventDecorator
    private lateinit var startDayEventDecorator: StartDayEventDecorator
    private lateinit var selectedDayDecorator: SelectedDayDecorator

    // Zaregostrování aktivity sloužící, pro vytvoření eventu
    val addEventActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Zde definuji, co udělám s výsledkem
            // Načtu data z databáze, již upravené a  načtu je do listView
            if (it.resultCode == RESULT_OK) {
                val date = it.data?.getSerializableExtra(MainActivity.DATE_KEY) as LocalDate
                fillEventList(date)

                binding.calendarView.invalidateDecorators()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val view = binding.root

        // Nastavení aby se zobrazovalo menu
        setHasOptionsMenu(true)

        // Inicializace přístupu do databáze
        initializeViewModel()

        // Nastavení adaptéru pro recyclerView
        initializeRecyclerView()

        // Nastavení vzhledu kalendáře
        initializeCalendarView()

        // Obsluha tlačítka pro přidání eventu
        binding.addEventFab.setOnClickListener {
            val date = binding.calendarView.selectedDate ?: CalendarDay.today()
            val addEventIntent = Intent().apply {
                setClass(requireContext(), AddEventActivity::class.java)
                putExtra(MainActivity.EVENT_ACTION_KEY, MainActivity.EVENT_NEW)
                putExtra(MainActivity.DAY_KEY, date.day)
                putExtra(MainActivity.MONTH_KEY, date.month)
                putExtra(MainActivity.YEAR_KEY, date.year)
            }
            addEventActivityLauncher.launch(addEventIntent)
        }
        return view
    }

    // Funkce pro prvotní nastavení kalendáře -> většina nastavena přímo v XML souboru
    private fun initializeCalendarView() {
        // Kalendář pro zisk data
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Upravení kalendáře
        binding.calendarView.apply {
            // Zvýraznění víkendů
            addDecorator(HighlightSundaysDecorator(requireContext()))
            // Zvýraznění aktuálního dne
            addDecorator(CurrentDayDecorator(CalendarDay.today(), requireContext()))
            // Zvýraznění vybraného dne
            selectedDayDecorator = SelectedDayDecorator(requireContext(), null)
            addDecorator(selectedDayDecorator)
            // Tato třída slouží jako odstranění defaultního selectoru
            addDecorator(SelectedDayDefaultDecorator(requireContext()))
            // Zvýraznění eventů u našich dnu
            singleDayEventDecorator = SingleDayEventDecorator(requireContext(), mutableListOf())
            multipleDayEventDecorator = MultipleDayEventDecorator(requireContext(), mutableListOf())
            endDayEventDecorator = EndDayEventDecorator(requireContext(), mutableListOf())
            startDayEventDecorator = StartDayEventDecorator(requireContext(), mutableListOf())

            eventViewModel.getAllEvents.observe(viewLifecycleOwner, Observer { events ->
                singleDayEventDecorator.updateEvents(events)
                multipleDayEventDecorator.updateEvents(events)
                endDayEventDecorator.updateEvents(events)
                startDayEventDecorator.updateEvents(events)

                addDecorators(
                    singleDayEventDecorator,
                    multipleDayEventDecorator,
                    endDayEventDecorator,
                    startDayEventDecorator
                )
            })

            // Listener, který se provede při změně data
            setOnDateChangedListener { calendarView, date, isSelected ->
                if (isSelected) {
                    fillEventList(LocalDate.of(date.year, date.month, date.day))
                    selectedDayDecorator.selectedDay = date
                    calendarView.invalidateDecorators()
                }
            }

            // Nastavíme datum na aktuální den a načteme jeho eventy
            fillEventList(LocalDate.of(year, month, day))
        }
    }

    // Inicializace přístupu k databázi
    private fun initializeViewModel() {
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
    }

    // Inicializace RecyclerView
    private  fun initializeRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.eventRv.layoutManager = layoutManager
        context?.let {
            eventAdapter = EventAdapter(it, this)
            binding.eventRv.adapter = eventAdapter
        }
    }

    private fun fillEventList(selectedDate: LocalDate) {
        currentLiveData?.removeObservers(viewLifecycleOwner)
        currentLiveData = eventViewModel.getEventsByDate(selectedDate.toEpochDay())
        currentLiveData!!.observe(viewLifecycleOwner, Observer { items ->
            eventAdapter.setData(items)
        })
        eventAdapter.notifyDataSetChanged()
    }

    // Metody pro OnEventClickListener
    override fun onEventClick(view: View, position: Int, clickedEvent: CalendarEvent) {
        val intent = Intent().apply {
            setClass(requireContext(),AddEventActivity::class.java)
            putExtra(MainActivity.EVENT_ACTION_KEY, MainActivity.EVENT_EDIT)
            putExtra(MainActivity.EVENT_KEY, clickedEvent)
        }
        addEventActivityLauncher.launch(intent)
    }

    // Vytvoření menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Informujeme uživatele jakým způsobem probíhá synchronizace a jestli chce synchronizaci zapnout
            R.id.google_button -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_google)
                    .setTitle(getString(R.string.google_calendar_synchronization_title_text))
                    .setMessage(getString(R.string.google_calendar_synchronization_message_text))
                    .setPositiveButton(getString(R.string.permission_allow_text)) { dialog, which ->
                        dialog.dismiss()
                        if (CalendarMethodsSingleton.hasAllNecessaryCalendarPermissions(requireContext())) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.permissions_granted_text),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else {
                            CalendarMethodsSingleton.requestAllNecessaryCalendarPermissions(this)
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
                return true
                }
            else -> return false
        }
    }

    // Reakce, pokud uživatel vícekrát odmítne udělit povolení
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setTitle(getString(R.string.permissions_required_title_text))
                .setRationale(R.string.permissions_required_message_text)
                .setPositiveButton(R.string.permission_allow_text)
                .build()
                .show()
        }
        else {
            CalendarMethodsSingleton.requestAllNecessaryCalendarPermissions(this)
        }
    }

    // Reakce, když uživatel udělí povolení
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(
            requireContext(),
            getString(R.string.permissions_granted_text),
            Toast.LENGTH_SHORT
        ).show()
    }

    // Reakce na výsledek žádosti o udělení povolení od uživatele
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}