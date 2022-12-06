package tma.inf.upol.loveckydenik.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.mapboxsdk.geometry.LatLng
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.adapters.MarkerTypeAdapter
import tma.inf.upol.loveckydenik.enums.MarkerType

class CreateMarkerDialog(
    val position: LatLng,
    private val listener: MarkerDialogListener,
): AppCompatDialogFragment() {

    private lateinit var cancelBtn: Button
    private lateinit var createBtn: Button
    private lateinit var nameEt: EditText
    private lateinit var noteEt: EditText
    private lateinit var selectMarkerTv: TextView

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
            createBtn = view.findViewById(R.id.marker_create_btn)
            nameEt = view.findViewById(R.id.marker_name_ev)
            noteEt = view.findViewById(R.id.marker_note_ev)
            selectMarkerTv = view.findViewById(R.id.selecting_marker_type_tv)
        }

        // Inicializace názvů typů markerů
        markerTypeStrings = resources.getStringArray(R.array.marker_types)

        // Akce po stisknutí tlačítka cancel
        cancelBtn.setOnClickListener {
            this.dismiss()
        }

        // Akce po stisknutí tlačítka create
        createBtn.setOnClickListener {
            if (selectedMarkerType != null) {
                val name = nameEt.text.toString()
                val note = noteEt.text.toString()
                val markerType = MarkerType.values()[selectedMarkerTypePosition]
                listener.processMarkerInformations(name, note, markerType, position)
                this.dismiss()
            }
            else {
                YoYo.with(Techniques.RubberBand)
                    .duration(700)
                    .repeat(1)
                    .playOn(selectMarkerTv)
            }
        }

        // Akce po stisknutí výběru typu markeru
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

        builder.setView(view)
        return builder.create()
    }


    // Předám informace z dialogu pomocí prostředí -> v hlavní aktivitě musím toto prostředí implementovat
    // a přepsat metodu proccessMarkerInformations
    interface MarkerDialogListener {
        fun processMarkerInformations(
            name: String,
            note: String,
            markerType: MarkerType,
            position:LatLng,
        )
    }
}