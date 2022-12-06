package tma.inf.upol.loveckydenik.fragments

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.adapters.ComingEventAdapter
import tma.inf.upol.loveckydenik.database.EventViewModel
import tma.inf.upol.loveckydenik.database.HuntingViewModel
import tma.inf.upol.loveckydenik.database.MarkerViewModel
import tma.inf.upol.loveckydenik.databinding.FragmentHomeBinding
import tma.inf.upol.loveckydenik.dialogs.UserEditDialog
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton
import java.time.LocalDate

class HomeFragment : Fragment(),
    UserEditDialog.UserInformationsDialogListener, EasyPermissions.PermissionCallbacks {

    // View binding
    private lateinit var binding: FragmentHomeBinding

    // Přístup k databázi
    private lateinit var eventViewModel: EventViewModel
    private lateinit var huntingViewModel: HuntingViewModel
    private lateinit var markerViewModel: MarkerViewModel

    private lateinit var adapter: ComingEventAdapter
    private var isExporting = false
    private var isImporting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        setHasOptionsMenu(true)

        // Přístup k databázi
        initializeViewModels()

        // Vyplnění informací
        fillInformations()

        return view
    }

    // Funkce, která se postará o doplnění důležitého infa
    private fun fillInformations() {
        // Načtení infa o uživateli, pokud nějaké je
        val hunterName = requireContext()
            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .getString(MainActivity.HUNTER_NAME_KEY, null)
        val hunterEmail = requireContext()
            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
            .getString(MainActivity.HUNTER_EMAIL_KEY, null)

        // Doplnění infa
        if (hunterName == null || hunterName == "") {
            binding.hunterNameTv.text = getString(R.string.hunter_name)
        }
        else {
            binding.hunterNameTv.text = hunterName
        }

        if (hunterEmail == null || hunterEmail == "") {
            binding.hunterEmailTv.text = getString(R.string.hunter_email)
        }
        else {
            binding.hunterEmailTv.text = hunterEmail
        }

        // Načtení eventů do recycler View
        val todayDate = LocalDate.now()
        val events = eventViewModel.getComingEvents(todayDate.toEpochDay())
        if (events.isEmpty()) {
            binding.comingEventsRecyclerView.visibility = View.GONE
            binding.noComingEventsInfoText.visibility = View.VISIBLE
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.comingEventsRecyclerView.layoutManager = layoutManager
        context?.let {
            adapter = ComingEventAdapter(requireContext(), events)
            binding.comingEventsRecyclerView.adapter = adapter
        }

        val adapter = ComingEventAdapter(requireContext(), events)
        binding.comingEventsRecyclerView.adapter = adapter
    }

    // Inicializace ViewModelu (přístupu k databázi)
    private fun initializeViewModels() {
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        markerViewModel = ViewModelProvider(this).get(MarkerViewModel::class.java)
        huntingViewModel = ViewModelProvider(this).get(HuntingViewModel::class.java)
    }

    // Funkce, která zpracovává informace předané z dialogu pro úpravu uživatelského infa
    override fun processUserInformations(name: String, email: String) {
        if (name.isEmpty()) {
            requireContext()
                .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putString(MainActivity.HUNTER_NAME_KEY, "")
                .apply()
            binding.hunterNameTv.setText(getString(R.string.hunter_name))
        }
        else {
            requireContext()
                .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putString(MainActivity.HUNTER_NAME_KEY, name)
                .apply()
            binding.hunterNameTv.setText(name)
        }

        if (email.isEmpty()) {
            requireContext()
                .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putString(MainActivity.HUNTER_EMAIL_KEY, "")
                .apply()
            binding.hunterEmailTv.setText(getString(R.string.hunter_email))
        }
        else {
            requireContext()
                .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putString(MainActivity.HUNTER_EMAIL_KEY, email)
                .apply()
            binding.hunterEmailTv.setText(email)
        }
    }

    // Metody pro vytvoření menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Metoda pro reakci na kliknutí položky v menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.subMenu
        when (item.itemId) {
            R.id.edit_item -> UserEditDialog(this).show(parentFragmentManager, "")
            R.id.db_export_item ->  {
                // Zde vytvořím dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.export_db_menu_item_text))
                    .setMessage(getString(R.string.export_db_info_text))
                    .setPositiveButton(
                        getString(R.string.yes_message_button_dialog),
                        DialogInterface.OnClickListener { dialog, which ->
                            isExporting = true
                            isImporting = false
                            if (hasReadExternalStoragePermission()
                                && hasWriteExternalStoragePermission()) {
                                // Do export
                                WholeAppMethodsSingleton
                                    .exportDatabaseToCsvFiles(
                                        requireContext(),
                                        markerViewModel,
                                        eventViewModel,
                                        huntingViewModel
                                    )
                            }
                            else {
                                requestWriteAndReadPermissions()
                            }
                            dialog.dismiss()
                        }
                    )
                    .setNegativeButton(
                        getString(R.string.no_text),
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        }
                    )
                    .show()
            }
            R.id.db_import_item -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.import_db_menu_item_text))
                    .setMessage(getString(R.string.import_db_info_text))
                    .setPositiveButton(
                        getString(R.string.yes_message_button_dialog),
                        DialogInterface.OnClickListener { dialog, which ->
                            isExporting = false
                            isImporting = true
                            if (hasReadExternalStoragePermission()
                                && hasWriteExternalStoragePermission()) {
                                // Do import
                                WholeAppMethodsSingleton.importData(
                                    requireContext(),
                                    huntingViewModel,
                                    eventViewModel,
                                    markerViewModel
                                )
                            }
                            else {
                                requestWriteAndReadPermissions()
                            }
                            dialog.dismiss()
                        }
                    )
                    .setNegativeButton(
                        getString(R.string.no_text),
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        }
                    )
                    .show()
            }
        }
        return false
    }

    // Funkce, která zajišťuje povolení
    private fun requestWriteAndReadPermissions() {
        EasyPermissions.requestPermissions(
            this,
            // Zpráva, když uživatel odmítne
            getString(R.string.read_and_write_permissions_needed_text),
            MainActivity.MY_STORAGE_PERMISSIONS_REQUEST,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    // Zjištění zda má aplikace povolení číst
    private fun hasReadExternalStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Zjištění zda má aplikace povolení zapisovat
    private fun hasWriteExternalStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Zkontroluji, zda všechna povolení byla udělena
        var allIsGood = true
        for(result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allIsGood = false
            }
        }

        if(allIsGood) {
            if (isExporting) {
                WholeAppMethodsSingleton
                    .exportDatabaseToCsvFiles(
                        requireContext(),
                        markerViewModel,
                        eventViewModel,
                        huntingViewModel
                    )
            }
            else {
                WholeAppMethodsSingleton.importData(
                    requireContext(),
                    huntingViewModel,
                    eventViewModel,
                    markerViewModel
                )
                // Postarám se aby se aktualizoval seznam eventů
                fillInformations()
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
        else {
            requestWriteAndReadPermissions()
        }
    }
}
