package ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import data.Transition
import org.jetbrains.skia.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class TransitionCoords (val coordState1: StateCoords, val coordState2: StateCoords, val id: Transition)

fun DrawScope.drawArrow(from: Offset, to: Offset, chars: List<Char>, color: Color) {
    if (from == to) {
        val radius = 30f
        val center = Offset(from.x, from.y - radius)

        drawArc(
            color = color,
            startAngle = -30f,
            sweepAngle = 320f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 1f)
        )

        // Posición de las letras en el arco
        val textPosition = Offset(from.x + 13f, from.y - 48f)

        drawIntoCanvas { canvas ->
            val textLine =
                TextLine.make(chars.joinToString(","), Font(Typeface.makeFromName("Arial", FontStyle.ITALIC), 15f))
            canvas.nativeCanvas.drawTextLine(textLine, textPosition.x, textPosition.y, Paint())
        }

        drawPath(
            path = Path().apply {
                moveTo(from.x + 30f, from.y - 15f)
                lineTo(from.x + 22f, from.y - 10f)
                lineTo(from.x + 23f, from.y - 20f)
            },
            color = Color.Black,
        )

    } else {
        // Caso normal de transición entre diferentes estados
        val direction = Offset(to.x - from.x, to.y - from.y)
        val length = direction.getDistance()

        val unitDirection = if (length > 0) Offset(direction.x / length, direction.y / length) else Offset(0f, 0f)
        val adjustedTo = Offset(to.x - unitDirection.x * 25f, to.y - unitDirection.y * 25f)

        drawLine(color = color, start = from, end = adjustedTo)

        val angle = atan2(adjustedTo.y - from.y, adjustedTo.x - from.x)
        val arrowSize = 10f
        val arrowPoint1 = Offset(
            x = adjustedTo.x - (arrowSize * cos(angle - Math.PI / 6)).toFloat(),
            y = adjustedTo.y - (arrowSize * sin(angle - Math.PI / 6)).toFloat()
        )
        val arrowPoint2 = Offset(
            x = adjustedTo.x - (arrowSize * cos(angle + Math.PI / 6)).toFloat(),
            y = adjustedTo.y - (arrowSize * sin(angle + Math.PI / 6)).toFloat()
        )

        val path = Path().apply {
            moveTo(adjustedTo.x, adjustedTo.y)
            lineTo(arrowPoint1.x, arrowPoint1.y)
            lineTo(arrowPoint2.x, arrowPoint2.y)
            close()
        }

        // Posición de las letras
        val midPoint = Offset((from.x + adjustedTo.x) / 2, (from.y + adjustedTo.y) / 2)
        val perpendicularOffset = Offset(unitDirection.y, -unitDirection.x) * 15f
        val textPosition = midPoint + perpendicularOffset

        drawIntoCanvas { canvas ->
            val textLine =
                TextLine.make(chars.joinToString(","), Font(Typeface.makeFromName("Arial", FontStyle.ITALIC), 15f))
            canvas.nativeCanvas.drawTextLine(textLine, textPosition.x, textPosition.y, Paint())
        }

        drawPath(path = path, color = color)
    }
}