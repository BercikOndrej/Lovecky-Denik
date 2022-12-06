package tma.inf.upol.loveckydenik.activities

import android.app.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import tma.inf.upol.loveckydenik.classes.Notification
import tma.inf.upol.loveckydenik.database.EventViewModel
import tma.inf.upol.loveckydenik.databinding.ActivityAddEventBinding
import tma.inf.upol.loveckydenik.singletons.CalendarMethodsSingleton
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


class AddEventActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivityAddEventBinding

    // Přístup k databázi
    private lateinit var eventViewModel: EventViewModel

    // Pomocná proměnná značící, zda se bude editovat
    private var  isEditing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Inicializace přístupu  databázi
        initializeViewModel()

        // Vytvoření channelu pro posílání a vytvoření notifikací
        createNotificationChannel()

        // Vyplnění předaných informací
        fillInformation(intent)

        // Obsluha switche
        binding.allDaySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.startingTimeTv.visibility = View.GONE
                binding.endingTimeTv.visibility = View.GONE
            }
            else {
                binding.startingTimeTv.text = WholeAppMethodsSingleton
                    .getRightTimeFormatToDisplay(8,0)
                binding.endingTimeTv.text = WholeAppMethodsSingleton
                    .getRightTimeFormatToDisplay(9,0)

                binding.startingTimeTv.visibility = View.VISIBLE
                binding.endingTimeTv.visibility = View.VISIBLE
            }
        }

        // Výběr počátečního data po stisknutí
        binding.startingDateTv.setOnClickListener {
            val array = getDateFromText(binding.startingDateTv.text.toString())
            val startingdDay = array[0]
            val startingMonth = array[1]
            val startingYear = array[2]
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val dayOfWeekString = getDayOfTheWeekString(dayOfMonth, month, year)
                    binding.startingDateTv.text = ("$dayOfWeekString. $dayOfMonth. ${month + 1}. $year").toString()

                    // Kontrola vstupu
                    if (!isDatesInputCorrect()) {
                        editDateToCorrectState(true)
                    }
                },
                startingYear, startingMonth, startingdDay
            )
            datePickerDialog.show()
        }

        // Výběr koncového data po stisknutí -> musím zajistit aby datum nebylo dřív než počáteční
        binding.endingDateTv.setOnClickListener {
            val array = getDateFromText(binding.endingDateTv.text.toString())
            val endingDay = array[0]
            val endingMonth = array[1]
            val endingYear = array[2]
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val dayOfWeekString = getDayOfTheWeekString(dayOfMonth, month, year)
                    binding.endingDateTv.text = ("$dayOfWeekString. $dayOfMonth. ${month + 1}. $year").toString()

                    // Kontrola vstupu
                    if (!isDatesInputCorrect()) {
                        editDateToCorrectState(false)
                    }
                },
                endingYear, endingMonth, endingDay
            )
            datePickerDialog.show()
        }

        // To samé pro čas
        binding.startingTimeTv.setOnClickListener {
            // Pro iniciální nastavení času dialogu musím přepočítat z textu pomocí funkce
            val array = getTimeFromText(binding.startingTimeTv.text.toString())
            val hours = array[0]
            val minutes = array[1]
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    // Správný formát -> pomocí singletonu
                    binding.startingTimeTv.text = WholeAppMethodsSingleton
                        .getRightTimeFormatToDisplay(hourOfDay, minute)

                    // Kontrola vstupu
                    if (!isDatesInputCorrect()) {
                        editDateToCorrectState(true)
                    }
                },
                // Nastavení iniciálního času
                hours,
                minutes,
                true
            )
            timePickerDialog.show()
        }

        binding.endingTimeTv.setOnClickListener {
            // Pro iniciální nastavení času dialogu musím přepočítat z textu pomocí funkce
            val array = getTimeFromText(binding.endingTimeTv.text.toString())
            val hours = array[0]
            val minutes = array[1]
            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    // Správný formát -> pomocí singletonu
                    binding.endingTimeTv.text = WholeAppMethodsSingleton
                        .getRightTimeFormatToDisplay(hourOfDay, minute)

                    // Kontrola vstupu
                    if (!isDatesInputCorrect()) {
                        editDateToCorrectState(false)
                    }
                },
                // Nastavení iniciálního času
                hours,
                minutes,
                true
            )
            timePickerDialog.show()
        }

        // Upozornění pro uživatele na nesprávný vstup
        binding.eventNameEt.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()) {
                binding.eventNameTil.error = null
            }
            else if (text.toString().length > R.integer.max_note_length) {
                binding.eventNameTil.error = getString(R.string.wrong_input_error_msg)
            }
            else {
                binding.eventNameTil.error = null
            }
        }

        // Obsluha tlačítek
        // Cancel button
        binding.cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Create button
        binding.createButton.setOnClickListener {
            if (isEditing) {
                var event = intent.getParcelableExtra<CalendarEvent>(MainActivity.EVENT_KEY)!!
                editEvent(event)
                // Vytvoření notifikace
                pushNotification(event)
                // Synchronizování editace
                event = CalendarMethodsSingleton.updateEvent(this, event)
                // Editace i v databázi
                eventViewModel.updateEvent(event)
                Toast.makeText(
                    this,
                    getString(R.string.successful_edit_event),
                    Toast.LENGTH_LONG
                ).show()
                isEditing = false
                val resultData = Intent().putExtra(MainActivity.DATE_KEY, event.startingDate)
                setResult(RESULT_OK, resultData)
                finish()
            }
            else {
                var event = createEvent()
                // Provedení synchronizace
                event = CalendarMethodsSingleton.addEvent(this, event)
                // Přidání eventu do databáze
                eventViewModel.insertEvent(event)
                // Vytvoření notifikace
                pushNotification(event)
                Toast.makeText(
                    this,
                    getString(R.string.successful_add_event),
                    Toast.LENGTH_LONG
                ).show()
                val resultData = Intent().putExtra(MainActivity.DATE_KEY, event.startingDate)
                setResult(RESULT_OK, resultData)
                finish()
            }
        }

        // Delete button
        binding.deleteButton.setOnClickListener {
            val event = intent.getParcelableExtra<CalendarEvent>(MainActivity.EVENT_KEY)!!
            // Odstranění jeho notifikace
            cancelNotification(event)
            // Odstranění eventu z google kalendáře
            CalendarMethodsSingleton.deleteEvent(this, event)
            // Odstranění eventu z databáze
            eventViewModel.deleteEvent(event)
            Toast.makeText(
                this,
                getString(R.string.successful_delete_event),
                Toast.LENGTH_LONG
            ).show()
            isEditing = false
            val resultData = Intent().putExtra(MainActivity.DATE_KEY, event.startingDate)
            setResult(RESULT_OK, resultData)
            finish()
        }
    }

    // Funkce pro vytvoření notifikačního kanálu
    private fun createNotificationChannel() {
        val name = Notification.NOTIFICATION_CHANNEL_NAME
        val description = Notification.CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(Notification.CHANEL_ID, name, importance)
        channel.description = description
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Funkce pro vytvoření/editaci notifikací
    private fun pushNotification(event: CalendarEvent) {
        if (isInPastTime(event)) {
            return
        }
        val secondaryText = WholeAppMethodsSingleton.generateRightSecondaryText(this, event)

        val intent = Intent().apply {
            putExtra(Notification.TITLE_EXTRA, event.name)
            putExtra(Notification.CONTENT_EXTRA, secondaryText)
            putExtra(Notification.NOTIFICATION_ID_EXTRA, event.notificationId)
            setClass(applicationContext, Notification::class.java)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            event.notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val hour = event.startingTime?.hour
        val minute = event.startingTime?.minute
        val time = event.startingDate.run {
            val calendar = Calendar.getInstance()
            calendar.set(
                year,
                month.value - 1,
                dayOfMonth,
                hour ?: 9,
                minute ?: 0,
                0
            )
            calendar.timeInMillis
        }

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent  
        )
    }

    private fun isInPastTime(event: CalendarEvent): Boolean {
        val nowDate = LocalDate.now()
        return when (event.startingDate.compareTo(nowDate)) {
            -1 -> true
            1 -> false
            // Pokud jsou stejná, tak se rozhoduji podle času
            else -> {
                val value = event.startingTime?.compareTo(LocalTime.now()) ?: 1
                return value == -1
            }
        }
    }

    // Funkce na zrušení notifikace
    private fun cancelNotification(event: CalendarEvent) {
        val secondaryText = WholeAppMethodsSingleton.generateRightSecondaryText(this, event)

        val intent = Intent().apply {
            putExtra(Notification.TITLE_EXTRA, event.name)
            putExtra(Notification.CONTENT_EXTRA, secondaryText)
            putExtra(Notification.NOTIFICATION_ID_EXTRA, event.notificationId)
            setClass(applicationContext, Notification::class.java)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            event.notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    // Funkce pro vyplnění předaných informací
    private fun fillInformation(intent: Intent) {
        if (intent.getStringExtra(MainActivity.EVENT_ACTION_KEY) == MainActivity.EVENT_NEW) {
            val day = intent.getIntExtra(MainActivity.DAY_KEY, 0)
            // Zde musím odečíst jedničku protože CalendarDay čísluje od 1 a Calendar od 0
            val month = intent.getIntExtra(MainActivity.MONTH_KEY, 0) - 1
            val year = intent.getIntExtra(MainActivity.YEAR_KEY, 0)

            setStartingDate(day, month, year)
            setEndingDate(day, month, year)
            setStartingTime()
            setEndingTime()
        }
        else {
            isEditing = true
            // Zpřístupnění i tlačítka pro smazání eventu
            binding.deleteButton.visibility = View.VISIBLE

            // Práce při editování -> musím vyplnit všechny vlastnosti eventu
            val event = intent.getParcelableExtra<CalendarEvent>(MainActivity.EVENT_KEY)!!

            binding.eventNameEt.setText(event.name)

            setStartingDate(
                event.startingDate.dayOfMonth,
                event.startingDate.month.value - 1,
                event.startingDate.year
            )

            setEndingDate(
                event.endingDate.dayOfMonth,
                event.endingDate.month.value - 1,
                event.endingDate.year
            )

            if (event.isAllDayEvent) {
                binding.allDaySwitch.isChecked = true
                binding.startingTimeTv.visibility = View.GONE
                binding.endingTimeTv.visibility = View.GONE
            }
            else {
                setStartingTime(
                    event.startingTime!!.hour,
                    event.startingTime!!.minute
                )

                setEndingTime(
                    event.endingTime!!.hour,
                    event.endingTime!!.minute
                )
            }
            // Také musíme změnit label tlačítka
            binding.createButton.text = getString(R.string.save_text)
        }
    }

    // Inicializace přístupu k databázi
    private fun initializeViewModel() {
        eventViewModel = ViewModelProvider(this@AddEventActivity).get(EventViewModel::class.java)
    }

    // Funkce pro nastavení počátečního data
    private fun setStartingDate(day: Int, month: Int, year: Int) {
        val dayOfWeek = getDayOfTheWeekString(day, month, year)
        binding.startingDateTv.text = "$dayOfWeek. $day. ${month + 1}. $year"
    }

    // Funkce pro nastavení konečného data
    private fun setEndingDate(day: Int, month: Int, year: Int) {
        val dayOfWeek = getDayOfTheWeekString(day, month, year)
        binding.endingDateTv.text = "$dayOfWeek. $day. ${month + 1}. $year"
    }

    // Funkce pro nastavení počátečního času
    private fun setStartingTime(hours: Int = 8, minutes: Int = 0) {
        binding.startingTimeTv.text = WholeAppMethodsSingleton
            .getRightTimeFormatToDisplay(hours, minutes)
    }

    // Funkce pro nastavení konečného času
    private fun setEndingTime(hour: Int = 9, minutes: Int = 0) {
        binding.endingTimeTv.text = WholeAppMethodsSingleton
            .getRightTimeFormatToDisplay(hour, minutes)
    }

    // Funkce pro zjištění typu dne
    private fun getDayOfTheWeekString(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return WholeAppMethodsSingleton
            .getStringFromDayNumber(
                this,
                calendar.get(Calendar.DAY_OF_WEEK)
            )
    }

    // Funkce pro zjištění hodin a minut z textView
    private fun getTimeFromText(string: String): Array<Int> {
        val splitString = string.split(":").toMutableList()
        if (splitString[1].first() == '0') {
            splitString[1] = splitString[1].drop(1)
        }
        val hours: Int = splitString.first().toInt()
        val minutes: Int = splitString[1].toInt()
        return arrayOf(hours, minutes)
    }

    // Funkce pro zjštění dnu, měsíce a roku z textu + pozor od měsíce musím odečíst jedničku
    private fun getDateFromText(string: String): Array<Int> {
        val splitString = string.split(". ")
        val day = splitString[1].toInt()
        val month = splitString[2].toInt() - 1
        val year = splitString[3].toInt()
        return arrayOf(day, month, year)
    }

    // Funkce pro ověření správného zadání datumů
    private fun isDatesInputCorrect(): Boolean {
        val startingDateValues = getDateFromText(binding.startingDateTv.text.toString())
        val endingDateValues = getDateFromText(binding.endingDateTv.text.toString())
        val startingTimeValues = getTimeFromText(binding.startingTimeTv.text.toString())
        val endingTimeValues = getTimeFromText(binding.endingTimeTv.text.toString())

        // Vytvoření prvního data vrací true, pokud je vše v pořádku
        val calendar1 = Calendar.getInstance()
        calendar1.set(
            startingDateValues[2],
            startingDateValues[1],
            startingDateValues[0],
            startingTimeValues[0],
            startingTimeValues[1]
        )
        val startingDateAndTime = calendar1.time

        val calendar2 = Calendar.getInstance()
        calendar2.set(
            endingDateValues[2],
            endingDateValues[1],
            endingDateValues[0],
            endingTimeValues[0],
            endingTimeValues[1]
        )
        val endingDateAndTime = calendar2.time
        return startingDateAndTime.compareTo(endingDateAndTime) <= 0
    }

    // Funkce pro uvedení končícího data do správného tvaru
    private fun editDateToCorrectState(isEditingStaringDate: Boolean) {
        // Pokud uživatel mění začínající datum a zadá špatný vstup, tak musím ragovat
        // a to tak, že změníme končící datum -> na to samé datum jako začínající + 1 hodina
        if (isEditingStaringDate) {
            val startingTimeValues = getTimeFromText(binding.startingTimeTv.text.toString())
            startingTimeValues[0]++
            val newTimeString = WholeAppMethodsSingleton
                .getRightTimeFormatToDisplay(startingTimeValues[0], startingTimeValues[1])
            binding.endingDateTv.text = binding.startingDateTv.text.toString()
            binding.endingTimeTv.text = newTimeString
        }
        // Pokud upravoval končící datum a zadal špatný vstup musíme reagovat začínajícím datem
        else {
            val endingTimeValues = getTimeFromText(binding.endingTimeTv.text.toString())
            endingTimeValues[0]--
            val newTimeString = WholeAppMethodsSingleton
                .getRightTimeFormatToDisplay(endingTimeValues[0], endingTimeValues[1])
            binding.startingDateTv.text = binding.endingDateTv.text.toString()
            binding.startingTimeTv.text = newTimeString
        }
    }

    // Funkce pro získání všech údajů
    private fun createEvent(): CalendarEvent {
        val notificationId = WholeAppMethodsSingleton.generateNotificationId(this)

        return if (binding.allDaySwitch.isChecked) {
            CalendarEvent(
                eventNameExtraction(),
                binding.allDaySwitch.isChecked,
                startingDateExtraction(),
                endingDateExtraction(),
                notificationId
            )
        }
        else {
            CalendarEvent(
                eventNameExtraction(),
                binding.allDaySwitch.isChecked,
                startingDateExtraction(),
                endingDateExtraction(),
                notificationId,
                startingTimeExtraction(),
                endingTimeExtraction(),
            )
        }
    }

    // Funkce pro editaci eventu
    private fun editEvent(event: CalendarEvent) {
        event.name = eventNameExtraction()
        event.startingDate = startingDateExtraction()
        event.endingDate = endingDateExtraction()
        event.isAllDayEvent = binding.allDaySwitch.isChecked
        if (!event.isAllDayEvent) {
            event.startingTime = startingTimeExtraction()
            event.endingTime = endingTimeExtraction()
        }
    }

    // Funkce pro zisk názvu -> pokud žádný nebude, vrátí defaultní název
    private fun eventNameExtraction(): String {
        val name = binding.eventNameEt.text.toString()

        return if (
            name.isEmpty()
            || name.length > resources.getInteger(R.integer.max_note_length)
        ) {
            getString(R.string.default_event_name)
        }
        else {
            name
        }
    }

    // Funkce pro zisk datumu -> opět musíme zvýšit měsíc, protože i LocalDate čísluje od 1
    private fun startingDateExtraction(): LocalDate {
        val values = getDateFromText(binding.startingDateTv.text.toString())
        return LocalDate.of(values[2], values[1] + 1, values[0])
    }

    private fun endingDateExtraction(): LocalDate {
        val values = getDateFromText(binding.endingDateTv.text.toString())
        return LocalDate.of(values[2], values[1] + 1, values[0])
    }

    // Funkce pro zisk času
    private fun startingTimeExtraction(): LocalTime {
        val values = getTimeFromText(binding.startingTimeTv.text.toString())
        return LocalTime.of(values[0], values[1])
    }

    private fun endingTimeExtraction(): LocalTime {
        val values = getTimeFromText(binding.endingTimeTv.text.toString())
        return LocalTime.of(values[0], values[1])
    }
}