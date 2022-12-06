package tma.inf.upol.loveckydenik.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.enums.MarkerType

class MarkerTypeAdapter(val ctx: Context):
    BaseAdapter(){

    private val markerStrings: Array<String> = ctx.resources.getStringArray(R.array.marker_types)

    override fun getCount(): Int {
        return markerStrings.size
    }

    override fun getItem(position: Int): Any {
        return MarkerType.values()[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(ctx).inflate(
            R.layout.marker_dialog_one_row_layout,
            parent,
            false
        )

        val imageView: ImageView = view.findViewById(R.id.marker_image)
        val textView: TextView = view.findViewById(R.id.marker_label)

        val icon = MarkerType.values()[position].icon
        imageView.setImageResource(icon)
        textView.setText(markerStrings[position])

        return view
    }

}