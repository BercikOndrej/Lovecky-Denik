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
import tma.inf.upol.loveckydenik.enums.Animal

class AnimalListAdapter(val context: Context):
    RecyclerView.Adapter<AnimalListAdapter.AnimalViewHolder>() {

    // Potřebná data
    var chosenItemPosition = RecyclerView.NO_POSITION
    private val stringLabels = context.resources.getStringArray(R.array.animals_strings)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.item_in_animals_list, parent, false)
        return AnimalViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        holder.textView.text = stringLabels[position]
        holder.imageView.setImageResource(Animal.values()[position].icon)

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
        return Animal.values().size
    }

    inner class AnimalViewHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        var imageView: ImageView = view.findViewById(R.id.animal_image)
        var textView: TextView = view.findViewById(R.id.animal_label)

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
