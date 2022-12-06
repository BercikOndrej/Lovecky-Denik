package tma.inf.upol.loveckydenik.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tma.inf.upol.loveckydenik.classes.Marker

class MarkerViewModel(application: Application): AndroidViewModel(application) {

    private val repository: MarkerRepository
    val getAllMarkers: LiveData<MutableList<Marker>>

    init {
        val dao = MyDatabase.getDatabase(application).markerDao()
        repository = MarkerRepository(dao)
        getAllMarkers = repository.getAllMarkers
    }

    fun getMarkerByAssociatedId(id: Long): Marker {
        return repository.getMarkerByAssociatedId(id)
    }

    fun getMarkerById(id: Long): Marker {
        return repository.getMarkerById(id)
    }

    fun getAllMarkersForExport(): List<Marker> {
        return  repository.getAllMarkersForExport()
    }

    fun updateMarker(marker: Marker) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMarker(marker)
        }
    }

    fun deleteMarker(marker: Marker) {
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteMarker(marker)
        }
    }

    fun insertMarker(marker: Marker) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMarker(marker)
        }
    }

    fun deleteAllMarkers() {
        repository.deleteAllMarkers()
    }
}