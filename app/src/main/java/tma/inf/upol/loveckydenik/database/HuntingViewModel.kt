package tma.inf.upol.loveckydenik.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tma.inf.upol.loveckydenik.classes.HuntingItem

class HuntingViewModel(application: Application): AndroidViewModel(application) {

    private val repository: HuntingRepository
    val getAllItems: LiveData<MutableList<HuntingItem>>

    init {
        val dao = MyDatabase.getDatabase(application).huntingDao()
        repository = HuntingRepository(dao)
        getAllItems = repository.getAllItems
    }

    fun getItemsByAnimal(animal: String): LiveData<MutableList<HuntingItem>> {
        return repository.getItemsByAnimal(animal)
    }

    fun getItemByID(id: Long): HuntingItem {
        return repository.getItemByID(id)
    }

    fun getAllItemsForExport(): List<HuntingItem> {
        return repository.getAllItemsForExport()
    }

    fun updateItem(item: HuntingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: HuntingItem) {
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteItem(item)
        }
    }

    fun insertItem(item: HuntingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertItem(item)
        }
    }

    fun deleteAllItems() {
        repository.deleteAllItems()
    }
}