package tma.inf.upol.loveckydenik.database

import androidx.lifecycle.LiveData
import androidx.room.*
import tma.inf.upol.loveckydenik.classes.Marker

@Dao
interface MarkerDao {
    @Query("SELECT * FROM markers")
    fun getAllMarkers(): LiveData<MutableList<Marker>>

    @Query("SELECT * FROM markers WHERE associated_item_id = :id")
    fun getMarkerByAssociatedId(id: Long): Marker

    @Query("SELECT * FROM markers WHERE id = :id")
    fun getMarkerById(id: Long): Marker

    @Query("SELECT * FROM markers")
    fun getAllMarkersForExport(): List<Marker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: Marker)

    @Update
    suspend fun updateMarker(marker: Marker)

    @Delete
    suspend fun deleteMarker(marker: Marker)

    @Query("DELETE FROM markers")
    fun deleteAllMarkers()
}