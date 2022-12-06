package tma.inf.upol.loveckydenik.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import tma.inf.upol.loveckydenik.singletons.WholeAppMethodsSingleton

class EventAdapter(
    val ctx: Context,
    private val listener: OnEventClickListener
):
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var eventItems = mutableListOf<CalendarEvent>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater
            .from(ctx)
            .inflate(R.layout.one_row_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventItems[position]
        holder.eventNameTv.text = event.name
        holder.secondaryTextTv.text =
            WholeAppMethodsSingleton.generateRightSecondaryText(ctx, event)
    }

    override fun getItemCount(): Int {
        return eventItems.size
    }


    fun setData(items: MutableList<CalendarEvent>) {
        eventItems = items
        notifyDataSetChanged()
    }

    // Třída pro uchování dat
    inner class EventViewHolder(private val view: View):
    RecyclerView.ViewHolder(view), View.OnClickListener {
        var eventNameTv: TextView = view.findViewById(R.id.event_name_tv)
        var secondaryTextTv: TextView = view.findViewById(R.id.event_secondary_tv)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedEvent = eventItems[position]
                listener.onEventClick(view, position, clickedEvent)
            }
        }
    }

    interface OnEventClickListener {
        fun onEventClick(view: View, position: Int, clickedEvent: CalendarEvent)
    }
}