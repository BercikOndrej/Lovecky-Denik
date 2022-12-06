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

class ComingEventAdapter(
    val ctx: Context,
    var comingEvents: List<CalendarEvent>
): RecyclerView.Adapter<ComingEventAdapter.ComingEventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComingEventViewHolder {
        val view = LayoutInflater
            .from(ctx)
            .inflate(R.layout.coming_events_one_row_layout, parent, false)
        return ComingEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComingEventViewHolder, position: Int) {
        val event = comingEvents[position]
        holder.comingEventName.setText(event.name)
        holder.comingEventTime.setText(
            WholeAppMethodsSingleton.generateRightSecondaryText(ctx, event)
        )
        holder.dateTv.setText(
            WholeAppMethodsSingleton.generateStringForDateNumberAndMonth(ctx, event)
        )
        holder.dayNameTv.setText(
            WholeAppMethodsSingleton.generateDayNameWithTodayAndTomorrow(ctx, event)
        )
    }

    override fun getItemCount(): Int {
        return comingEvents.size
    }

    // Třída pro uchování dat
    inner class ComingEventViewHolder(private val view: View):
        RecyclerView.ViewHolder(view) {
        var dateTv: TextView = view.findViewById(R.id.coming_event_date_tv)
        var dayNameTv: TextView = view.findViewById(R.id.coming_event_day_name_tv)
        var comingEventName: TextView = view.findViewById(R.id.coming_event_name)
        var comingEventTime: TextView = view.findViewById(R.id.coming_event_time)
    }
}