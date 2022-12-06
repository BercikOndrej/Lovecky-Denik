package tma.inf.upol.loveckydenik.calendardecorators

import android.content.Context
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import tma.inf.upol.loveckydenik.R
import tma.inf.upol.loveckydenik.spans.SelectedDayBackroundSpan

class SelectedDayDecorator(val ctx: Context, var selectedDay: CalendarDay?)
    : DayViewDecorator {
    private val backgroundColor: Int
    private val foregroundColor: Int

    override fun shouldDecorate(day: CalendarDay): Boolean {
       return day == selectedDay
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(SelectedDayBackroundSpan(backgroundColor))
        view.addSpan(ForegroundColorSpan(foregroundColor))
        view.addSpan(StyleSpan(Typeface.BOLD))
    }

    init {
        var typedValue = TypedValue();
        ctx.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        backgroundColor = typedValue.data;

        typedValue = TypedValue()
        ctx.theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        foregroundColor = typedValue.data
    }
}