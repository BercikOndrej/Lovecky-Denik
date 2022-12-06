package tma.inf.upol.loveckydenik.calendardecorators

import android.content.Context
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import tma.inf.upol.loveckydenik.R
import java.util.*

class HighlightSundaysDecorator(val ctx: Context) : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    private val color = ctx.getColor(R.color.red)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        calendar.set(day.year, day.month - 1, day.day)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(color))
    }
}