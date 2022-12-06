package tma.inf.upol.loveckydenik.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.adapters.MarkerTypeAdapter
import tma.inf.upol.loveckydenik.classes.Marker
import tma.inf.upol.loveckydenik.enums.MarkerType

class EditMarkerDialog(
    val marker: Marker,
    private val listener: EditDialogListener,
): AppCompatDialogFragment() {
    // Komponenty z layoutu
    private lateinit var cancelBtn: Button
    private lateinit var editBtn: Button
    private lateinit var nameEt: EditText
    private lateinit var noteEt: EditText
    private lateinit var selectMarkerTv: TextView
    private lateinit var deleteMarkerIv: ImageView

    private lateinit var markerTypeStrings: Array<String>
    private var selectedMarkerTypePosition = 0
    private var selectedMarkerType: MarkerType? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.marker_dialog_layout, null)

        // Získání component
        if (view != null) {
            cancelBtn = view.findViewById(R.id.marker_dialog_cancel_btn)
            editBtn = view.findViewById(R.id.marker_create_btn)
            nameEt = view.findViewById(R.id.marker_name_ev )
            noteEt = view.findViewById(R.id.marker_note_ev)
            selectMarkerTv = view.findViewById(R.id.selecting_marker_type_tv)
            deleteMarkerIv = view.findViewById(R.id.marker_dialog_delete_iv)
        }

        // Inicializace názvů typů markerů
        markerTypeStrings = resources.getStringArray(R.array.marker_types)

        // Vyplnění layoutu informacemi z markeru
        fillKnownInformations()

        // Akce po stisknutí tlačítka cancel
        cancelBtn.setOnClickListener {
            this.dismiss()
        }

        // Akce po stisknutí na vybrání markeru
        selectMarkerTv.setOnClickListener {
            // Zde spustím jednoduchý dialog, kde si uživatel vybere typ markeru
            // k tomu budu potřebovat adapter, pro zobrazení dialogu + icony markeru
            val markerTypeAdapter = MarkerTypeAdapter(requireContext())

            // Už vytvoření dialogu
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(getString(R.string.select_marker_type_text))
                setAdapter(markerTypeAdapter) { dialog, which ->
                    selectedMarkerTypePosition = which
                    selectedMarkerType = MarkerType.values()[selectedMarkerTypePosition]
                    selectMarkerTv.setText(markerTypeStrings[selectedMarkerTypePosition])
                    selectMarkerTv.setCompoundDrawablesWithIntrinsicBounds(
                        selectedMarkerType!!.icon,
                        0,
                        0,
                        0
                    )
                    dialog.dismiss()
                }
                isCancelable = true
            }.show()
        }

        // Akce po stisknutí na delete tlačítko
        deleteMarkerIv.setOnClickListener {
            listener.handleMarkerChanges(marker, true)
            this.dismiss()
        }

        // Akce pro stisknutí editovacího tlačítka
        editBtn.setOnClickListener {
            // Nastavíme všechny změnené metody
            marker.name = nameEt.text.toString()
            marker.note = noteEt.text.toString()
            marker.markerType = selectedMarkerType!!

            // Předáme info
            listener.handleMarkerChanges(marker, false)

            // A ukončíme dialog
            this.dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    // Funkce, která vyplní informace do uživatelského prostředí
    private fun fillKnownInformations() {
        // Změníme text tlačítka
        editBtn.setText(getString(R.string.save_text))

        marker.name?.let {
            nameEt.setText(it)
        }

        marker.note?.let {
            noteEt.setText(it)
        }

        selectMarkerTv.apply {
            selectedMarkerType = marker.markerType
            selectedMarkerTypePosition = MarkerType.values().indexOf(marker.markerType)
            setText(markerTypeStrings[MarkerType.values().indexOf(marker.markerType)])
            setCompoundDrawablesWithIntrinsicBounds(
                marker.markerType.icon,
                0,
                0,
                0
            )
        }

        // Zviditelníme delete tlačítko
        deleteMarkerIv.visibility = View.VISIBLE
    }

    // Předám informace z dialogu pomocí prostředí -> v hlavní aktivitě musím toto prostředí implementovat
    // a přepsat metodu proccessMarkerInformations
    interface EditDialogListener {
        fun handleMarkerChanges(marker: Marker, isDeleting: Boolean)
    }
}