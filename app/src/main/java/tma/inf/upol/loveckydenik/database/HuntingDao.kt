package tma.inf.upol.loveckydenik.database

import androidx.lifecycle.LiveData
import androidx.room.*
import tma.inf.upol.loveckydenik.classes.HuntingItem

@Dao
interface HuntingDao {

    @Query("SELECT * FROM hunting_items")
    fun getAllItems(): LiveData<MutableList<HuntingItem>>

    @Query("SELECT * FROM hunting_items WHERE animal = :animal")
    fun getItemsByAnimal(animal: String): LiveData<MutableList<HuntingItem>>

    @Query("SELECT * FROM hunting_items WHERE id = :id")
    fun getItemByID(id: Long): HuntingItem

    @Query("SELECT * FROM hunting_items")
    fun getAllItemsForExport(): List<HuntingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HuntingItem)

    @Update
    suspend fun updateItem(item: HuntingItem)

    @Delete
    suspend fun deleteItem(item: HuntingItem)

    @Query("DELETE FROM hunting_items")
    fun deleteAllItems()
}