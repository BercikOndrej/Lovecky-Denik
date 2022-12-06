package tma.inf.upol.loveckydenik.database

import androidx.lifecycle.LiveData
import tma.inf.upol.loveckydenik.classes.Marker

class MarkerRepository(private  val dao: MarkerDao) {

    val getAllMarkers: LiveData<MutableList<Marker>> = dao.getAllMarkers()

    fun getMarkerById(id: Long): Marker {
        return dao.getMarkerById(id)
    }

    fun getMarkerByAssociatedId(id: Long): Marker {
        return dao.getMarkerByAssociatedId(id)
    }

    fun getAllMarkersForExport(): List<Marker> {
        return  dao.getAllMarkersForExport()
    }

    suspend fun insertMarker(marker: Marker) {
        dao.insertMarker(marker)
    }

    suspend fun updateMarker(marker: Marker) {
        dao.updateMarker(marker)
    }

    suspend fun deleteMarker(marker: Marker) {
        dao.deleteMarker(marker)
    }

    fun deleteAllMarkers() {
        dao.deleteAllMarkers()
    }
}