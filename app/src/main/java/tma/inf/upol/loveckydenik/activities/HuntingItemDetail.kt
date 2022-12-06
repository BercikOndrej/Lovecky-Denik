package tma.inf.upol.loveckydenik.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.database.HuntingViewModel
import tma.inf.upol.loveckydenik.database.MarkerViewModel
import tma.inf.upol.loveckydenik.databinding.ActivityHuntingItemDetailBinding
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton


class HuntingItemDetail : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding: ActivityHuntingItemDetailBinding

    // ViewModel
    private lateinit var huntingViewModel: HuntingViewModel
    private lateinit var markerViewModel: MarkerViewModel

    private lateinit var viewedItem: HuntingItem

    // Zaregistrování aktivit pro editaci a smazání
    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            viewedItem = intent?.getParcelableExtra(MainActivity.ITEM_KEY)!!
            // Musím promítnout změny
            fillInformations(viewedItem)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHuntingItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViewModels()

        viewedItem = intent.getParcelableExtra<HuntingItem>(MainActivity.ITEM_KEY)!!
        fillInformations(viewedItem)

        // Obsloužení stisknutím na fotku pro náhled
        binding.animalImageIv.setOnClickListener {
            val uri = viewedItem.imageFileName?.let {
                WholeAppMethodsSingleton.getUriFromExistingFile(this, it)
            }
            val intent = Intent().apply {
                putExtra(MainActivity.IMAGE_URI_KEY, uri.toString())
                setClass(
                    this@HuntingItemDetail,
                    FullScreenImageActivity::class.java
                )
            }
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.activity_hunting_item_detail_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.edit_item -> {
                val intent = Intent().apply {
                    setClass(
                        this@HuntingItemDetail,
                        AddHuntingItemActivity::class.java
                    )
                    putExtra(MainActivity.IS_EDITING_ACTION_KEY, true)
                    putExtra(MainActivity.ITEM_KEY, viewedItem)
                }
                editLauncher.launch(intent)
                return true
            }
            R.id.delete_item -> {
                // Zobrazení dialogu -> pokud vymažu item, musím vymazat i k němu patřící fotku
                var isSuccessful = false
                MaterialAlertDialogBuilder(this)
                    .setMessage(resources.getString(R.string.delete_item_message_dialog))
                    .setNegativeButton(resources.getString(R.string.no_message_button_dialog)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.yes_message_button_dialog)) { dialog, which ->
                        viewedItem.imageFileName?.let {
                            isSuccessful = WholeAppMethodsSingleton
                                .deletePhotoFromExternalStorage(this, it)
                        }
                        val marker = markerViewModel.getMarkerByAssociatedId(viewedItem.id)
                        markerViewModel.deleteMarker(marker)
                        huntingViewModel.deleteItem(viewedItem)
                        dialog.dismiss()
                        if (isSuccessful) {
                            Toast.makeText(
                                this,
                                getString(R.string.delete_was_successfully_text),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                    .show()
            }
        }
        return false
    }

    private fun fillInformations(item: HuntingItem) {
        // Nastavení data a času
        val date = item.date
        val time = item.time

        // Správný formát data
        val timeString = WholeAppMethodsSingleton
            .getRightTimeFormatToDisplay(time.hour, time.minute)
        binding.dateTimeTv.text =
            "${date.dayOfMonth}.${date.month.value}. ${date.year}, $timeString"

        // Nastavení boolean hodnot
        if (item.dogIsUse) binding.dogIsUseTv.isVisible = true
        if (item.nightVisionIsUse) binding.nightVisionIsUseTv.isVisible = true
        if (item.accompanimentAtTheHunt) binding.accompanimentAtTheHuntTv.isVisible = true

        // Foto
        if (item.imageFileName == null) {
            binding.animalImageIv.isVisible = false
        }
        else {
            val bmp = WholeAppMethodsSingleton
                .loadPhotoFromExternalStorage(this, item.imageFileName!!)
            binding.animalImageIv.apply {
                setImageBitmap(bmp)
                isVisible = true
            }
        }

        // Zvíře
        binding.animalIconIv.apply {
            setImageResource(item.animal.icon)
            setColorFilter(ContextCompat.getColor(context, R.color.color_on_primary))
        }
        binding.animalLabelTv.apply {
            text = resources.getStringArray(R.array.animals_strings)[item.animal.ordinal]
            setTextColor(ContextCompat.getColor(context, R.color.color_on_primary))
        }

        binding.huntingMethodTv.text = resources
            .getStringArray(R.array.hunting_methods_strings)[item.huntingMethod.ordinal]

        // Poloha
        val locationPresentString = "${item.locationLat}, ${item.locationLng}"
        binding.locationTv.setText(locationPresentString)

        // Lovec
        if (item.hunterName == null) {
            binding.hunterNameTv.isVisible = false
        }
        else {
            binding.hunterNameTv.apply {
                text = item.hunterName
                isVisible = true
            }
        }

        // Poznámka
        if (item.note == null) {
            binding.noteTv.isVisible = false
        }
        else {
            binding.noteTv.apply {
                text = item.note
                isVisible = true
            }
        }

        // Věk
        if (item.age == null) {
            binding.ageTv.text = getString(R.string.dash)
        }
        else {
            binding.ageTv.text = item.age.toString()
        }

        // Váha
        if (item.weight == null) {
            binding.weightTv.text = getString(R.string.dash)
        }
        else {
            binding.weightTv.text = "${item.weight.toString()} kg"
        }

        // Bodování
        if (item.scoreEvaluation == null) {
            binding.scoreTv.text = getString(R.string.dash)
        }
        else {
            binding.scoreTv.text = item.scoreEvaluation.toString()
        }

        // Pohlaví
        binding.genderTv.text = item.gender.toString()
    }

    // Inicializace ViewModelu (přístupu k databázi)
    private fun initializeViewModels() {
        huntingViewModel = ViewModelProvider(this).get(HuntingViewModel::class.java)
        markerViewModel = ViewModelProvider(this).get(MarkerViewModel::class.java)
    }
}