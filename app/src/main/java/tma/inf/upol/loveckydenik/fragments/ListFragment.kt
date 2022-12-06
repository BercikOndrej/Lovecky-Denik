package tma.inf.upol.loveckydenik.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.activities.AddHuntingItemActivity
import tma.inf.upol.loveckydenik.activities.HuntingItemDetail
import tma.inf.upol.loveckydenik.activities.MainActivity
import tma.inf.upol.loveckydenik.adapters.DialogAdapter
import tma.inf.upol.loveckydenik.adapters.MyAdapter
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.database.HuntingViewModel
import tma.inf.upol.loveckydenik.databinding.FragmentListBinding
import tma.inf.upol.loveckydenik.enums.Animal

//import tma.inf.upol.loveckydenik.ViewModels.HuntingViewModel

class ListFragment : Fragment(), MyAdapter.OnHuntingItemClickListener {
    // View binding
    private lateinit var binding: FragmentListBinding

    // Proměnné sloužící pro snažší správu seznamu s položkami
    private lateinit var adapter: MyAdapter
    private var currentLiveData: LiveData<MutableList<HuntingItem>>? = null

    // Zaregistrování aktivit pro vytvoření
    private val  generalActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            fillList()
        }

    // Přístup k databázi
    private lateinit var huntingViewModel: HuntingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root

        setHasOptionsMenu(true)

        // Inicializace databáze
        initializeViewModel()

        // Obsluha na stisknutí tlačítka pro přidání prvku -> spustíme novou aktivitu
        binding.fbAddButton.setOnClickListener {
            val intent = Intent().apply {
                setClass(requireContext(), AddHuntingItemActivity::class.java)
            }
            generalActivityLauncher.launch(intent)
        }

        // Nastavení adapteru pro recyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        context?.let {
            adapter = MyAdapter(it, this)
            binding.recyclerView.adapter = adapter
        }

        // Naplnění recyclerView z databáze
        fillList()
        return view
    }

    // Nalpnění recyclerView prvky z databáze
    private fun fillList() {
        val currentChosenFilterInInt = getCurrentFilterInInt()
        currentLiveData?.removeObservers(viewLifecycleOwner)
        currentLiveData = getRightItemsToDisplay(currentChosenFilterInInt)
        binding.titleTw.text = getRightAnimalTitleString(currentChosenFilterInInt)

        currentLiveData!!.observe(viewLifecycleOwner, Observer { items ->
            val itemsString = getRightItemsCountString(items.size)
            binding.itemsCountTv.text = "${items.size} $itemsString"
            adapter.setData(items)
        })
    }

    // Inicializace ViewModelu (přístupu k databázi)
    private fun initializeViewModel() {
        huntingViewModel = ViewModelProvider(this).get(HuntingViewModel::class.java)
    }

    // Metody pro OnHuntingItemClickListener -> kliknutí na položku
    override fun onItemClick(view: View, position: Int, clickedItem: HuntingItem) {
        if (context != null) {
            val intent = Intent().apply {
                putExtra(MainActivity.ITEM_KEY, clickedItem)
                setClass(this@ListFragment.requireContext(), HuntingItemDetail::class.java)
            }
            generalActivityLauncher.launch(intent)
        }
    }

    // Metody pro vytvoření menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Metoda pro reakci na kliknutí položky v menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_item -> {
                // Vytvoření objektů potřebné pro náš adaptér
                // Popisy
                val stringLabels = mutableListOf(getString(R.string.all_items_text))
                resources.getStringArray(R.array.animals_strings).forEach {
                    stringLabels.add(it)
                }
                // Icony
                val iconsIds = mutableListOf(R.drawable.ic_all_game)
                for (animal in Animal.values()) {
                    iconsIds.add(animal.icon)
                }
                val dialogAdapter = DialogAdapter(requireContext(), stringLabels, iconsIds)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.filter_text))
                    .setAdapter(dialogAdapter) { dialog, which ->
                        setCurrentFilterInInt(which)
                        fillList()
                    }
                    .show()
            }
        }
        return false
    }

    private fun getRightItemsCountString(num: Int): String {
        return when (num) {
            1 -> getString(R.string.one_item_count_text)
            in 2..4 -> getString(R.string.item_count_between_2_and_4_text)
            else -> getString(R.string.items_count_5_and_more_text)
        }
    }

    private  fun  getRightItemsToDisplay(position: Int): LiveData<MutableList<HuntingItem>> {
        return if (position == 0) {
            huntingViewModel.getAllItems
        }
        else {
            huntingViewModel.getItemsByAnimal(Animal.values()[position - 1].toString())
        }
    }

    private fun getRightAnimalTitleString(position: Int): String {
        return if (position == 0) {
            getString(R.string.all_items_text)
        }
        else {
            resources.getStringArray(R.array.animals_strings)[position - 1]
        }
    }

    private fun getCurrentFilterInInt(): Int {
        return requireContext()
            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getInt(MainActivity.ANIMAL_FILTER, 0)
    }

    private fun setCurrentFilterInInt(num: Int) {
        requireContext()
            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(MainActivity.ANIMAL_FILTER, num)
            .apply()
    }
}