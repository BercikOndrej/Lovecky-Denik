package tma.inf.upol.loveckydenik.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapbox.mapboxsdk.geometry.LatLng
import tma.inf.upol.loveckydenik.enums.MarkerType

@Entity(tableName = "markers")
data class Marker(

    @ColumnInfo(name = "marker_type")
    var markerType: MarkerType,

    var location: LatLng,

    var name: String?,

    var note: String?,

    @ColumnInfo(name = "associated_item_id")
    val associatedItemID: Long?,

    @PrimaryKey
    val id: Long,
)