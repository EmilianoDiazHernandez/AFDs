package ui

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import data.State
import data.TypeState
import org.jetbrains.skia.*

data class StateCoords(val offset: MutableState<Offset>, val id: State)

fun DrawScope.drawState(state: StateCoords, num: Int) {
    when (state.id.type.value) {
        TypeState.NORMAL -> drawCircle(color = Color.LightGray, radius = 25f, state.offset.value)
        TypeState.FINAL -> {
            drawCircle(color = Color.Gray, radius = 25f, state.offset.value)
            drawCircle(color = Color.LightGray, radius = 20f, state.offset.value)
        }

        TypeState.INITIAL -> {
            drawCircle(color = Color.LightGray, radius = 25f, state.offset.value)
            drawPath(
                path = Path().apply {
                    moveTo(state.offset.value.x - 25f, state.offset.value.y)
                    lineTo(state.offset.value.x - 25f - 20f, state.offset.value.y + 20f)
                    lineTo(state.offset.value.x - 25f - 20f, state.offset.value.y - 20f)
                },
                color = Color.LightGray,
            )
        }
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawTextLine(
            TextLine.make(num.toString(), font = Font(Typeface.makeFromName("Arial", FontStyle.BOLD), 25f)),
            state.offset.value.x - 7f,
            state.offset.value.y + 8f,
            paint = Paint().apply { color = Color.White.toArgb() },
        )
    }
}