package tma.inf.upol.loveckydenik.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.MainActivity

class UserEditDialog(private val listener: UserInformationsDialogListener)
    : AppCompatDialogFragment() {

    // Komponenty dialogu
    private lateinit var cancelButton: Button
    private lateinit var editButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.user_edit_dialog_layout, null)

        // Získání Komponent
        if (view != null) {
            cancelButton = view.findViewById(R.id.user_information_edit_dialog_cancel_button)
            editButton = view.findViewById(R.id.user_information_edit_dialog_edit_button)
            nameEditText = view.findViewById(R.id.user_information_dialog_hunter_name_et)
            emailEditText = view.findViewById(R.id.user_information_dialog_hunter_email_et)
        }

        // Vyplnění informací o uživateli, pokud nějaké jsou
        fillInformations()

        // Navázání akce na stisknutí tlačítek
        cancelButton.setOnClickListener {
            this.dismiss()
        }

        editButton.setOnClickListener {
            listener.processUserInformations(
                nameEditText.text.toString(),
                emailEditText.text.toString()
            )
            this.dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    private fun fillInformations() {
        // Načtení infa o uživateli, pokud nějaké je
        val hunterName = requireContext()
            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(MainActivity.HUNTER_NAME_KEY, null)
        val hunterEmail = requireContext()
            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(MainActivity.HUNTER_EMAIL_KEY, null)

        // Doplnění infa
        hunterName?.let { name ->
            nameEditText.setText(name)
        }

        hunterEmail?.let { email ->
            emailEditText.setText(email)
        }
    }

    // Předám informace z dialogu pomocí prostředí -> v hlavní aktivitě musím toto prostředí implementovat
    // a přepsat metodu proccessMarkerInformations
    interface UserInformationsDialogListener {
        fun processUserInformations(
            name: String,
            email: String
        )
    }
}