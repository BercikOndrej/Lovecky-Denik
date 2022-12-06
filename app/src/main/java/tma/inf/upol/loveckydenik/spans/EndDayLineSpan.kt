package tma.inf.upol.loveckydenik.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan


class EndDayLineSpan (
    val color: Int = DEFAULT_COLOR
): LineBackgroundSpan {

    companion object {
        const val HEIGHT = 25f
        const val PADDING = 12f
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
                left.toFloat() + ((right - left) / 2),
                bottom.toFloat() + NUMBER_GAP,
                right.toFloat() - PADDING,
                bottom.toFloat() + HEIGHT
            ), 10f, 10f, paint
        )

        // Musíme ještě přebarvit zaoblené rohy
        canvas.drawRect(
            RectF(
                left.toFloat(),
                bottom.toFloat() + NUMBER_GAP,
                right.toFloat()  - ((right - left) / 2) + PADDING,
                bottom.toFloat() + HEIGHT
            ), paint
        )
        paint.color = oldColor
    }
}