package tma.inf.upol.loveckydenik.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan


class SelectedDayBackroundSpan (
    val color: Int = DEFAULT_COLOR
): LineBackgroundSpan {

    companion object {
        const val PADDING = 30f
        const val NUMBER_GAP = 10f
        const val DEFAULT_COLOR = 0
    }

    // Metoda pro vykreslování
    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val oldColor = paint.color
        if (color != 0) {
            paint.color = color
        }

        canvas.drawRoundRect(
            RectF(
                left.toFloat() + PADDING,
                top.toFloat(),
                right.toFloat() - PADDING,
                bottom.toFloat() + (NUMBER_GAP / 2)
            ), 10f, 10f, paint)
        paint.color = oldColor
    }
}