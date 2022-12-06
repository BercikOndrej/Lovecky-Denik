package tma.inf.upol.loveckydenik.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import tma.inf.upol.loveckydenik.R

class DialogAdapter(
    val context: Context,
    private val stringLabels: MutableList<String>,
    private val iconsIds: MutableList<Int>
): BaseAdapter() {

    override fun getCount(): Int {
        return stringLabels.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder = ViewHolder()
        lateinit var resultView: View

        val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (convertView == null) {
            resultView = inflater.inflate(R.layout.item_in_filter_dialog, null)
            holder.animalIv = resultView.findViewById(R.id.animal_iv)
            holder.animalIv.setColorFilter(context.getColor(R.color.text_color))
            holder.animalTv = resultView.findViewById(R.id.animal_tv)
            resultView.tag = holder
        }
        else {
            holder = convertView.tag as ViewHolder
            resultView = convertView
        }
        holder.animalTv.text = stringLabels[position]
        holder.animalIv.setImageResource(iconsIds[position])
        return resultView
    }

    inner class ViewHolder {
        lateinit var animalIv: ImageView
        lateinit var animalTv: TextView
    }
}