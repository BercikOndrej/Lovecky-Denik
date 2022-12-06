package tma.inf.upol.loveckydenik.calendardecorators

import android.content.Context
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import tma.inf.upol.loveckydenik.R

class SelectedDayDefaultDecorator(val ctx: Context) : DayViewDecorator {
    val selector = ctx.getDrawable(R.drawable.selected_day_background_drawable)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true
    }

    override fun decorate(view: DayViewFacade) {
        view.setSelectionDrawable(selector!!)
    }
}