package tma.inf.upol.loveckydenik.calendardecorators

import android.content.Context
import android.util.TypedValue
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.classes.CalendarEvent
import tma.inf.upol.loveckydenik.spans.EndDayLineSpan
import java.time.LocalDate

class EndDayEventDecorator(val ctx: Context, var eventItems: MutableList<CalendarEvent>)
    : DayViewDecorator {

    val color: Int

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val dateInLong = LocalDate.of(day.year, day.month, day.day).toEpochDay()
        eventItems.forEach { event ->
            if (event.startingDate.toEpochDay() != event.endingDate.toEpochDay() &&
                dateInLong == event.endingDate.toEpochDay()) {
                return true
            }
        }
        return false
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(EndDayLineSpan(color))
    }

    fun updateEvents(events: MutableList<CalendarEvent>) {
        eventItems = events
    }

    init {
        val typedValue = TypedValue();
        ctx.theme.resolveAttribute(R.attr.colorPrimaryVariant, typedValue, true);
        color = typedValue.data;
    }
}