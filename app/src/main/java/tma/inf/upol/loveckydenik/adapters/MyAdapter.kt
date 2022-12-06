package tma.inf.upol.loveckydenik.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.classes.HuntingItem
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton

class MyAdapter(
    private val context: Context,
    private val listener: OnHuntingItemClickListener
) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private var huntingItems = mutableListOf<HuntingItem>()
    private val huntingMethodStrings = context.resources
        .getStringArray(R.array.hunting_methods_strings)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.one_row_in_recycler_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = huntingItems[position]
        holder.dateTv.text = "${item.date.dayOfMonth}. ${item.date.month.value}. ${item.date.year}"

        // Správný formát data
        holder.timeTv.text = WholeAppMethodsSingleton
            .getRightTimeFormatToDisplay(item.time.hour, item.time.minute)

        holder.methodTv.text = huntingMethodStrings[item.huntingMethod.ordinal]
        holder.animalIv.setImageResource(item.animal.icon)
    }

    override fun getItemCount(): Int {
        return  huntingItems.size
    }

    fun setData(items: MutableList<HuntingItem>) {
        huntingItems = items
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val view: View):
        RecyclerView.ViewHolder(view), View.OnClickListener {
        var dateTv: TextView = view.findViewById(R.id.date_tv)
        var timeTv: TextView = view.findViewById(R.id.time_tv)
        var methodTv: TextView = view.findViewById(R.id.method_tv)
        var animalIv: ImageView = view.findViewById(R.id.animal_iv)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedItem = huntingItems[position]
                listener.onItemClick(view, position, clickedItem)
            }
        }
    }

    interface OnHuntingItemClickListener {
        fun onItemClick(view: View, position: Int, clickedItem: HuntingItem)
    }
}