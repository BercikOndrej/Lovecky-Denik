package tma.inf.upol.loveckydenik.database

import androidx.lifecycle.LiveData
import tma.inf.upol.loveckydenik.classes.HuntingItem

class HuntingRepository(private val dao: HuntingDao) {

    val getAllItems: LiveData<MutableList<HuntingItem>> = dao.getAllItems()

    fun getItemsByAnimal(animal: String): LiveData<MutableList<HuntingItem>> {
        return dao.getItemsByAnimal(animal)
    }

    fun getItemByID(id: Long): HuntingItem {
        return dao.getItemByID(id)
    }

    fun getAllItemsForExport(): List<HuntingItem> {
        return dao.getAllItemsForExport()
    }

    suspend fun insertItem(item: HuntingItem) {
        dao.insertItem(item)
    }

    suspend fun deleteItem(item: HuntingItem) {
        dao.deleteItem(item)
    }

    suspend fun updateItem(item: HuntingItem) {
        dao.updateItem(item)
    }

    fun deleteAllItems() {
        dao.deleteAllItems()
    }
}