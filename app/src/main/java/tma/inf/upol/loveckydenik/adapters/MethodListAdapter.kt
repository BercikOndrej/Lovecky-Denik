package tma.inf.upol.loveckydenik.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.enums.HuntingMethod

class MethodListAdapter(val context: Context):
    RecyclerView.Adapter<MethodListAdapter.MethodViewHolder>() {

    // Inicializace potřebných dat
    private val stringLabels: Array<String> = context.resources.getStringArray(R.array.hunting_methods_strings)
    var chosenItemPosition = RecyclerView.NO_POSITION
    // Opět obrázky jako u animalAdapteru

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MethodViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.item_in_method_list, parent, false)
        return MethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MethodViewHolder, position: Int) {
        holder.textView.text = stringLabels[position]
        holder.imageView.setImageResource(HuntingMethod.values()[position].icon)

        if (chosenItemPosition == position) {
            holder.imageView.setColorFilter(ContextCompat.getColor(context, R.color.adapter_selector_color))
            holder.textView.setTextColor(ContextCompat.getColor(context,R.color.adapter_selector_color))
        }
        else {
            holder.imageView.setColorFilter(ContextCompat.getColor(context, R.color.text_color))
            holder.textView.setTextColor(ContextCompat.getColor(context,R.color.text_color))
        }

        holder.itemView.setOnClickListener {
            chosenItemPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return HuntingMethod.values().size
    }

    inner class MethodViewHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        var imageView: ImageView = view.findViewById(R.id.method_image)
        var textView: TextView = view.findViewById(R.id.method_label)

        // Zde definuji itemClickListener -> aby byl nastaven pouze při vytvoření itemu(video z netu)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                chosenItemPosition = position
            }
        }
    }
}